package log.charter.io.midi;

import static java.util.Arrays.copyOfRange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

import org.jcodec.common.logging.Logger;

import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.vocals.Vocal;
import log.charter.data.song.vocals.Vocal.VocalFlag;
import log.charter.util.data.Fraction;

public class MidiVocalsReader {
	private static enum EventType {
		LYRIC, TEXT, LYRICS_LINE;

		public static EventType from(final MidiEvent midiEvent) {
			final byte[] msg = midiEvent.getMessage().getMessage();

			if (msg[0] == -1) {
				return TEXT;
			}

			return msg[1] == 105 ? LYRICS_LINE : LYRIC;
		}
	}

	private static Track findVocalsTrack(final Sequence sequence) {
		for (final Track track : sequence.getTracks()) {
			final MidiEvent event = track.get(0);
			final byte[] msg = event.getMessage().getMessage();

			if ((msg[0] == -1) && (msg[1] == 3)) {
				final String trackName = new String(copyOfRange(msg, 3, msg.length));
				if ("PART VOCALS".equals(trackName)) {
					return track;
				}
			}
		}

		return null;
	}

	public static List<Vocal> read(final Sequence sequence) {
		final Track track = findVocalsTrack(sequence);
		if (track == null) {
			return null;
		}

		return new MidiVocalsReader(sequence.getResolution()).read(track);
	}

	private final int midiResolution;
	private final List<Vocal> vocals = new ArrayList<>();

	private Vocal current = null;
	private boolean textAdded = false;
	private boolean startAdded = false;
	private boolean endAdded = false;

	private boolean lineStarted = false;

	private MidiVocalsReader(final int midiResolution) {
		this.midiResolution = midiResolution;
	}

	private String printMidiValues(final MidiEvent midiEvent) {
		final FractionalPosition position = getPosition(midiEvent.getTick());
		final EventType type = EventType.from(midiEvent);
		final MidiMessage midiMessage = midiEvent.getMessage();

		final StringBuilder s = new StringBuilder(type + " position: " + position + ", status:"
				+ midiMessage.getStatus() + ", msg: " + Arrays.toString(midiMessage.getMessage()));

		if (type == EventType.TEXT) {
			s.append(" (" + getTextAsString(midiMessage.getMessage()) + ")");
		}

		return s.toString();
	}

	private FractionalPosition getPosition(final long t) {
		return new FractionalPosition(new Fraction(t, midiResolution));
	}

	private void createCurrent(final long t) {
		current = new Vocal(getPosition(t));
		textAdded = false;
		startAdded = false;
		endAdded = false;
	}

	private void addNoteIfFinished() {
		if (!(textAdded && startAdded && endAdded)) {
			return;
		}

		vocals.add(current);
		current = null;
		textAdded = false;
		startAdded = false;
		endAdded = false;
	}

	private void handleLyric(final MidiEvent midiEvent) {
		if (midiEvent.getMessage().getMessage()[0] == -112) {
			if (current == null) {
				createCurrent(midiEvent.getTick());
			} else {
				current.position(getPosition(midiEvent.getTick()));
			}

			current.note = midiEvent.getMessage().getMessage()[1];
			startAdded = true;
		} else if (midiEvent.getMessage().getMessage()[0] == -128) {
			if (current == null) {
				return;
			}
			current.endPosition(getPosition(midiEvent.getTick()));
			endAdded = true;
		}

		addNoteIfFinished();
	}

	private void handleLyricsLine(final MidiEvent e) {
		if (e.getMessage().getMessage()[0] == -112) {
			lineStarted = true;
		} else if (e.getMessage().getMessage()[0] == -128) {
			if (!vocals.isEmpty()) {
				vocals.get(vocals.size() - 1).flag(VocalFlag.PHRASE_END);
			}
			lineStarted = false;
		}
	}

	private String getTextAsString(final byte[] midiEventMessage) {
		return new String(Arrays.copyOfRange(midiEventMessage, 3, midiEventMessage.length));
	}

	private void handleText(final MidiEvent midiEvent) {
		String text = getTextAsString(midiEvent.getMessage().getMessage());
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

		if (current == null || textAdded) {
			createCurrent(midiEvent.getTick());
		}
		current.textWithoutFlags(text);
		if (wordPart) {
			current.flag(VocalFlag.WORD_PART);
		} else if (!lineStarted) {
			current.flag(VocalFlag.PHRASE_END);
		}
		textAdded = true;

		addNoteIfFinished();
	}

	private List<Vocal> read(final Track track) {
		for (int eventId = 0; eventId < track.size(); eventId++) {
			final MidiEvent midiEvent = track.get(eventId);

			try {
				switch (EventType.from(midiEvent)) {
					case LYRIC:
						handleLyric(midiEvent);
						break;
					case LYRICS_LINE:
						handleLyricsLine(midiEvent);
						break;
					case TEXT:
						handleText(midiEvent);
						break;
					default:
						throw new IllegalArgumentException(
								"Unknown vocal midi event type " + EventType.from(midiEvent));
				}
			} catch (final Exception e) {
				Logger.error("Exception when handling vocal midi event " + printMidiValues(midiEvent));
				throw e;
			}
		}

		return vocals;
	}
}
