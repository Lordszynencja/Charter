package log.charter.io.midi.reader;

import static log.charter.io.Logger.debug;

import java.util.Arrays;

import log.charter.io.midi.MidTrack;
import log.charter.io.midi.MidTrack.MidEvent;
import log.charter.song.Event;
import log.charter.song.Lyric;
import log.charter.song.Vocals;

public class VocalsReader {
	private static enum EventType {
		LYRIC, TEXT, LYRICS_LINE;

		public static EventType from(final MidEvent e) {
			if (e.msg[0] == -1) {
				return TEXT;
			}
			return e.msg[1] == 105 ? LYRICS_LINE : LYRIC;
		}
	}

	public static Vocals read(final MidTrack t) {
		return new VocalsReader().readVocals(t);
	}

	private final Vocals vocals;

	private Event lyricsLine = null;
	private Lyric lyric = null;
	private boolean noteStarted = false;
	private boolean noteEnded = false;

	private VocalsReader() {
		vocals = new Vocals();
	}

	private void handleLyric(final MidEvent e) {
		if (lyric == null) {
			lyric = new Lyric(e.t, e.msg[1]);
			noteStarted = true;
		} else if (noteStarted) {
			lyric.setLength(e.t - lyric.pos);
			noteEnded = true;
			if (lyric.lyric != null) {
				vocals.lyrics.add(lyric);
				lyric = null;
				noteStarted = false;
				noteEnded = false;
			}
		} else {
			lyric.tone = e.msg[1];
			noteStarted = true;
		}
	}

	private void handleLyricsLine(final MidEvent e) {
		if (lyricsLine == null) {
			lyricsLine = new Event(e.t);
		} else {
			lyricsLine.setLength(e.t - lyricsLine.pos);
			vocals.lyricLines.add(lyricsLine);
			lyricsLine = null;
		}
	}

	private void handleText(final MidEvent e) {
		String text = new String(Arrays.copyOfRange(e.msg, 3, e.msg.length));
		final boolean connected = text.startsWith("+");
		if (connected) {
			text = text.substring(1);
		}

		final boolean toneless = text.contains("#");
		if (toneless) {
			text = text.replaceFirst("#", "");
		}

		final boolean wordPart = text.contains("-");
		if (wordPart) {
			text = text.replaceFirst("-", "");
		}

		if (lyric == null) {
			lyric = new Lyric(e.t, text);
			lyric.connected = connected;
			lyric.toneless = toneless;
			lyric.wordPart = wordPart;
		} else {
			lyric.lyric = text;
			lyric.connected = connected;
			lyric.toneless = toneless;
			lyric.wordPart = wordPart;
			if (noteEnded) {
				vocals.lyrics.add(lyric);
				lyric = null;
				noteStarted = false;
				noteEnded = false;
			}
		}
	}

	private Vocals readVocals(final MidTrack t) {
		debug("Reading notes");

		for (final MidEvent e : t.events) {
			switch (EventType.from(e)) {
			case LYRICS_LINE:
				handleLyricsLine(e);
				break;
			case TEXT:
				handleText(e);
				break;
			case LYRIC:
				handleLyric(e);
				break;
			default:
				break;
			}
		}

		vocals.fixNotes();
		debug("Reading notes finished");
		return vocals;
	}
}
