package log.charter.util;

import static log.charter.util.Utils.formatTime;

import java.util.ArrayList;
import java.util.List;

import log.charter.io.Logger;
import log.charter.util.Utils.TimeUnit;
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

	public void print(final String label, final String format) {
		Logger.debug(label + " times:");
		timings.forEach(pair -> Logger.debug(String.format(format, pair.a,
				formatTime(pair.b, TimeUnit.NANOSECONDS, TimeUnit.NANOSECONDS, TimeUnit.SECONDS))));
	}
}
