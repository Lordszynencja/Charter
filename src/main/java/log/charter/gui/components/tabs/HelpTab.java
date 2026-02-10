package log.charter.gui.components.tabs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JTextPane;

import log.charter.data.config.ChartPanelColors.ColorLabel;
import log.charter.util.collections.ExpiringValuesList;

public class HelpTab extends JTextPane {
	private static class Statistics {
		public final long min;
		public final long average;
		public final long max;

		public Statistics(final List<Long> values) {
			if (values.isEmpty()) {
				min = 0;
				average = 0;
				max = 0;
				return;
			}

			long currentMin = values.get(0);
			long currentMax = values.get(0);
			long sum = 0;

			for (final long value : values) {
				if (value < currentMin) {
					currentMin = value;
				}
				if (value > currentMax) {
					currentMax = value;
				}
				sum += value;
			}

			min = currentMin;
			average = sum / values.size();
			max = currentMax;
		}

	}

	private static final long serialVersionUID = -272982208492712044L;

	private long lastUpdate;
	private final Map<String, ExpiringValuesList<Long>> frameTimes = new HashMap<>();
	private final Map<String, ExpiringValuesList<Long>> timings = new HashMap<>();

	public HelpTab() {
		setBackground(ColorLabel.BASE_BG_2.color());
		setForeground(ColorLabel.BASE_DARK_TEXT.color());
		setOpaque(true);
		setFocusable(false);
	}

	public void addFrameTime(final String label) {
		if (!frameTimes.containsKey(label)) {
			frameTimes.put(label, new ExpiringValuesList<>(ExpiringValuesList.second));
		}

		frameTimes.get(label).addValue(1L);
	}

	public void addTiming(final String label, final Long value) {
		if (!timings.containsKey(label)) {
			timings.put(label, new ExpiringValuesList<>(ExpiringValuesList.second));
		}

		timings.get(label).addValue(value);
	}

	public void updateValues() {
		if (!isVisible()) {
			return;
		}
		if (System.currentTimeMillis() < lastUpdate + 100) {
			return;
		}

		final StringBuilder b = new StringBuilder();
		for (final Entry<String, ExpiringValuesList<Long>> entry : frameTimes.entrySet()) {
			b.append(entry.getKey()).append(" FPS: ").append(entry.getValue().getValues().size()).append('\n');
		}

		for (final Entry<String, ExpiringValuesList<Long>> entry : timings.entrySet()) {
			final List<Long> values = entry.getValue().getValues();
			if (values.isEmpty()) {
				continue;
			}

			final Statistics statistics = new Statistics(entry.getValue().getValues());
			b.append(entry.getKey()).append(":\n")//
					.append("  minimum: ").append(statistics.min).append('\n')//
					.append("  average: ").append(statistics.average).append('\n')//
					.append("  maximum: ").append(statistics.max).append('\n');
		}

		setText(b.toString());
		lastUpdate = System.currentTimeMillis();
	}
}
