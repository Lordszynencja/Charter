package log.charter.io;

import java.util.List;

import log.charter.song.Event;
import log.charter.song.Instrument;
import log.charter.song.Song;
import log.charter.song.Tempo;

public class TickMsConverter {
	private static interface Converter {
		double convert(final Tempo tmp, Tempo oldTmp, final double t);
	}

	public static final int ticksPerBeat = 768;

	private static final Converter toMsConverter = (tmp, oldTmp, t) -> {
		return tmp.pos + (((t - oldTmp.pos) * 60000000.0) / tmp.kbpm / ticksPerBeat);
	};

	private static final Converter toTickConverter = (tmp, oldTmp, t) -> {
		return tmp.pos + (((t - oldTmp.pos) * tmp.kbpm * ticksPerBeat) / 60000000.0);
	};

	private static double[][] convert(final double[][] data, final List<Tempo> tempos, final List<Tempo> oldTempos,
			final Converter c) {
		final int n = data.length;
		final double[][] conv = new double[n][];
		final int[] ids = new int[n];
		for (int i = 0; i < n; i++) {
			conv[i] = new double[data[i].length];
			ids[i] = 0;
		}

		int tmpId = 0;
		int nextTmpId = findNextSyncId(oldTempos, 1);
		while (nextTmpId < tempos.size()) {
			final Tempo tmp = tempos.get(tmpId);
			final Tempo oldTmp = oldTempos.get(tmpId);
			final Tempo oldNextTmp = oldTempos.get(nextTmpId);

			for (int i = 0; i < n; i++) {
				for (int j = 0; j < data[i].length; j++) {
					final double t = data[i][j];
					if ((t >= oldTmp.pos) && (t < oldNextTmp.pos)) {
						conv[i][j] = c.convert(tmp, oldTmp, t);
					}
				}
			}

			tmpId = nextTmpId;
			nextTmpId = findNextSyncId(oldTempos, nextTmpId + 1);
		}

		final Tempo tmp = tempos.get(tmpId);
		final Tempo oldTmp = oldTempos.get(tmpId);
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < data[i].length; j++) {
				final double t = data[i][j];
				if (t >= oldTmp.pos) {
					conv[i][j] = c.convert(tmp, oldTmp, t);
				}
			}
		}

		return conv;
	}

	private static Song convert(final Song copy, final List<Tempo> oldTempos, final Converter c) {
		final Instrument[] instruments = copy.instruments();
		final double[][] data = new double[(instruments.length * 7) + 2][];

		for (int i = 0; i < instruments.length; i++) {
			data[(i * 7)] = eventListToArray(instruments[i].notes.get(0));
			data[(i * 7) + 1] = eventListToArray(instruments[i].notes.get(1));
			data[(i * 7) + 2] = eventListToArray(instruments[i].notes.get(2));
			data[(i * 7) + 3] = eventListToArray(instruments[i].notes.get(3));
			data[(i * 7) + 4] = eventListToArray(instruments[i].sp);
			data[(i * 7) + 5] = eventListToArray(instruments[i].tap);
			data[(i * 7) + 6] = eventListToArray(instruments[i].solo);
		}
		data[instruments.length * 7] = eventListToArray(copy.v.lyrics);
		data[(instruments.length * 7) + 1] = eventListToArray(copy.v.lyricLines);

		final double[][] convData = convert(data, copy.tempoMap.tempos, oldTempos, c);
		for (int i = 0; i < instruments.length; i++) {
			replaceEventsTime(instruments[i].notes.get(0), convData[(i * 7)]);
			replaceEventsTime(instruments[i].notes.get(1), convData[(i * 7) + 1]);
			replaceEventsTime(instruments[i].notes.get(2), convData[(i * 7) + 2]);
			replaceEventsTime(instruments[i].notes.get(3), convData[(i * 7) + 3]);
			replaceEventsTime(instruments[i].sp, convData[4 + (i * 7)]);
			replaceEventsTime(instruments[i].tap, convData[5 + (i * 7)]);
			replaceEventsTime(instruments[i].solo, convData[6 + (i * 7)]);
		}
		replaceEventsTime(copy.v.lyrics, convData[instruments.length * 7]);
		replaceEventsTime(copy.v.lyricLines, convData[(instruments.length * 7) + 1]);

		return copy;
	}

	public static Song convertToMs(final Song s) {
		final Song copy = new Song(s);
		copy.tempoMap.convertToMs();
		return convert(copy, s.tempoMap.tempos, toMsConverter);
	}

	public static Song convertToTick(final Song s) {
		final Song copy = new Song(s);
		copy.tempoMap.convertToTick();
		return convert(copy, s.tempoMap.tempos, toTickConverter);
	}

	private static double[] eventListToArray(final List<? extends Event> events) {
		final int n = events.size();
		final double[] data = new double[n * 2];

		for (int i = 0; i < n; i++) {
			final Event e = events.get(i);
			data[2 * i] = e.pos;
			data[(2 * i) + 1] = e.pos + e.getLength();
		}

		return data;
	}

	private static int findNextSyncId(final List<Tempo> tempos, final int start) {
		return start < tempos.size() ? start : tempos.size();
	}

	private static void replaceEventsTime(final List<? extends Event> events, final double[] data) {
		for (int i = 0; i < (data.length / 2); i++) {
			final Event e = events.get(i);
			e.pos = data[2 * i];
			e.setLength(data[(2 * i) + 1] - e.pos);
		}
	}
}
