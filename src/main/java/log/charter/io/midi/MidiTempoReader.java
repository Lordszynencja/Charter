package log.charter.io.midi;

import static log.charter.io.Logger.error;

import java.util.ArrayList;
import java.util.List;

import log.charter.io.midi.MidTrack.MidEvent;

public class MidiTempoReader {
	private static final int BPM_CHANGE_ID = 81;
	private static final int TS_CHANGE_ID = 88;

	private static int kiloBeatsPerMinute(final MidEvent e) {
		final int minutesPerQuarterNote = ((e.msg[3] & 0xFF) << 16) | ((e.msg[4] & 0xFF) << 8) | (e.msg[5] & 0xFF);
		return (int) Math.floor(6.0E10D / minutesPerQuarterNote);
	}

	private static int denominator(final int power) {
		int denominator = 1;

		for (int i = 0; i < power; i++) {
			denominator *= 2;
		}

		return denominator;
	}

	public static List<Tempo> read(final MidTrack t) {
		final List<Tempo> tempos = new ArrayList<>(t.events.size());

		for (final MidEvent e : t.events) {
			final int type = e.msg[1];
			switch (type) {
				case BPM_CHANGE_ID:
					tempos.add(Tempo.forTempoChange((int) e.t, kiloBeatsPerMinute(e)));
					break;
				case TS_CHANGE_ID:
					tempos.add(Tempo.forTSChange((int) e.t, e.msg[3], denominator(e.msg[4])));
					break;
				default:
					error("Unknown Tempo: " + e);
					break;
			}
		}

		return tempos;
	}
}
