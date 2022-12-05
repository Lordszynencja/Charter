package log.charter.io.midi.reader;

import static log.charter.io.Logger.debug;
import static log.charter.io.Logger.error;

import java.util.ArrayList;
import java.util.List;

import log.charter.io.TickMsConverter;
import log.charter.io.midi.MidTrack;
import log.charter.io.midi.MidTrack.MidEvent;
import log.charter.song.Tempo;

public class TempoReader {
	public static List<Tempo> read(final MidTrack t) {
		debug("Reading tempo");
		final List<Tempo> tempos = new ArrayList<>(t.events.size());
		for (final MidEvent e : t.events) {
			final int type = e.msg[1];
			if (type == 81) {// bpm change
				final int mpq = ((e.msg[3] & 0xFF) << 16) | ((e.msg[4] & 0xFF) << 8) | (e.msg[5] & 0xFF);

				final int kbpm = (int) Math.floor(6.0E10D / mpq);
				tempos.add(new Tempo((int) (e.t / TickMsConverter.ticksPerBeat), e.t, kbpm, true));
			} else if (type == 88) {// sync
				final int num = e.msg[3];
				tempos.add(new Tempo((int) (e.t / TickMsConverter.ticksPerBeat), e.t, num, false));
			} else {
				error("Unknown Tempo: " + e);
			}
		}

		debug("Reading tempo finished");
		return tempos;
	}
}
