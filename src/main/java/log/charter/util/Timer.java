package log.charter.util;

import java.util.ArrayList;
import java.util.List;

import log.charter.util.CollectionUtils.Pair;

public class Timer {
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
		System.out.println(label);
		timings.forEach(pair -> {
			System.out.println(String.format(format, pair.a, pair.b));
		});
	}
}
