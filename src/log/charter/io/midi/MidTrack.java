package log.charter.io.midi;

import static java.lang.Math.round;
import static java.util.Arrays.copyOfRange;
import static log.charter.io.Logger.debug;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.Track;

import log.charter.song.Instrument.InstrumentType;

public class MidTrack {
	public static class MidEvent {
		public final long t;
		public final byte[] msg;

		public MidEvent(final long t, final byte[] msg) {
			this.t = t;
			this.msg = msg;
		}

		@Override
		public String toString() {
			return "MidEvent{t: " + t + ", msg: " + Arrays.toString(msg) + "}";
		}
	}

	public static enum TrackType {
		GUITAR("PART GUITAR"), //
		GUITAR_COOP("PART GUITAR COOP"), //
		GUITAR_RHYTHM("PART RHYTHM"), //
		BASS("PART BASS"), //
		KEYS("PART KEYS"), //
		VOCALS("PART VOCALS"), //
		DRUMS("PART DRUMS"), //
		EVENTS("EVENTS"), //
		TEMPO("TEMPO"), //
		REAL_DRUMS("PART REAL_DRUMS_PS"), //
		UNKNOWN("");

		public static TrackType from(final String s) {
			if (s == null) {
				return UNKNOWN;
			}

			for (final TrackType type : values()) {
				if (type.partName.equals(s)) {
					return type;
				}
			}

			return UNKNOWN;
		}

		public static TrackType fromInstrumentType(final InstrumentType instrumentType) {
			switch (instrumentType) {
			case GUITAR:
				return GUITAR;
			case GUITAR_COOP:
				return GUITAR_COOP;
			case GUITAR_RHYTHM:
				return GUITAR_RHYTHM;
			case BASS:
				return BASS;
			case DRUMS:
				return DRUMS;
			case KEYS:
				return KEYS;
			default:
				return null;
			}
		}

		public final String partName;

		private TrackType(final String partName) {
			this.partName = partName;
		}
	}

	public final TrackType type;
	public final List<MidEvent> events;

	public MidTrack(final Track t, final boolean isTempo, final double scaler) {
		TrackType trackType = null;
		events = new ArrayList<>(t.size());

		for (int i = 0; i < t.size(); i++) {
			final MidiEvent e = t.get(i);
			final byte[] msg = e.getMessage().getMessage();

			debug(e.getTick() + ", " + Arrays.toString(msg));
			if ((msg[0] == -1) && (msg[1] == 3)) {
				trackType = TrackType.from(new String(copyOfRange(msg, 3, msg.length)));
			} else if ((msg[0] != -1) || (msg[1] != 47) || (msg[2] != 0)) {
				events.add(new MidEvent(ms(e, scaler), msg));
			}
		}
		type = isTempo ? TrackType.TEMPO : trackType;
		debug("Track " + type + " ended");
	}

	public MidTrack(final TrackType type, final List<MidEvent> events) {
		this.type = type;
		this.events = events;
	}

	private long ms(final MidiEvent e, final double scaler) {
		return round(e.getTick() * scaler);
	}
}
