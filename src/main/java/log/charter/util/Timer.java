package log.charter.util;

import java.util.ArrayList;
import java.util.List;

import log.charter.io.Logger;
import log.charter.util.collections.Pair;

public class Timer {
	public static final String defaultFormat(final int labelWidth) {
		return "%" + labelWidth + "s: %s";
	}

	private long t = System.nanoTime();
	private List<Pair<String, Long>> timings = new ArrayList<>();

	public void start() {
		t = System.nanoTime();
		timings = new ArrayList<>();
	}

	public void addTimestamp(final String name) {
		final long t1 = System.nanoTime();
		timings.add(new Pair<>(name, t1 - t));
		t = t1;
	}

	private String formatTime(final long t) {
		if (t < 1000) {
			return "" + t;
		}

		return formatTime(t / 1000L) + String.format(" %03d", t % 1000L);
	}

	public void print(final String label, final String format) {
		final StringBuilder s = new StringBuilder(label + " times:\n");
		long totalTime = 0;
		for (final Pair<String, Long> timing : timings) {
			s.append(" " + String.format(format, timing.a, formatTime(timing.b))).append('\n');
			totalTime += timing.b;
		}

		final String totalString = totalTime < 1000 ? totalTime + "ns"//
				: totalTime < 1_000_000 ? totalTime / 1000 + "us" //
						: totalTime / 1_000_000 + "ms";
		if (totalTime > 20_000) {
			System.err.println("long frame");
		}
		s.append("total: " + totalString);
		Logger.debug(s.toString());
	}
}
