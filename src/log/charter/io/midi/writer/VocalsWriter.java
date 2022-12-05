package log.charter.io.midi.writer;

import static log.charter.io.Logger.debug;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import log.charter.song.Event;
import log.charter.song.Lyric;
import log.charter.song.Vocals;

public class VocalsWriter {
	private static void addNote(final int note, final double pos, final double end, final Track track)
			throws InvalidMidiDataException {
		final ShortMessage msgStart = new ShortMessage();
		msgStart.setMessage(-112, note, 100);
		track.add(new MidiEvent(msgStart, Math.round(pos)));
		final ShortMessage msgEnd = new ShortMessage();
		msgEnd.setMessage(-112, note, 0);
		track.add(new MidiEvent(msgEnd, Math.round(end)));
	}

	private static void addText(final double pos, final String text, final Track track)
			throws InvalidMidiDataException {
		final byte[] bytes = text.getBytes();
		final MetaMessage msg = new MetaMessage();
		msg.setMessage(5, bytes, bytes.length);
		track.add(new MidiEvent(msg, Math.round(pos)));
	}

	public static void write(final Vocals vocals, final Track track)
			throws InvalidMidiDataException {
		debug("Writing vocals");

		final byte[] bytes = "PART VOCALS".getBytes();
		final MetaMessage msg = new MetaMessage();
		msg.setMessage(3, bytes, bytes.length);
		track.add(new MidiEvent(msg, 0));

		for (final Event e : vocals.lyricLines) {
			addNote(105, e.pos, e.pos + e.getLength(), track);
		}

		for (final Lyric l : vocals.lyrics) {
			addText(l.pos, (l.connected ? "+" : "") + l.lyric + (l.wordPart ? "-" : "") + (l.toneless ? "#" : ""), track);
			addNote(l.toneless ? 50 : l.tone, l.pos, l.pos + l.getLength(), track);
		}

		debug("Writing vocals finished");
	}
}
