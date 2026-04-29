package log.charter.gui.components.tabs;

import java.awt.Desktop;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JEditorPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import log.charter.data.config.ChartPanelColors.ColorLabel;
import log.charter.data.config.GraphicalConfig;
import log.charter.gui.menuHandlers.InfoMenuHandler;
import log.charter.io.Logger;
import log.charter.util.collections.ExpiringValuesList;

public class HelpTab extends JTextPane {
	private static final String charterWikiLink = "<a href=\"" + InfoMenuHandler.charterWikiLink
			+ "\">Charter Wiki</a>";

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
		setEditable(false);
		setFocusable(false);
		setContentType("text/html");
		putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

		addHyperlinkListener(e -> {
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED && e.getURL() != null) {
				try {
					Desktop.getDesktop().browse(e.getURL().toURI());
				} catch (final Exception ex) {
					Logger.error("Couldn't open donations page", ex);
				}
			}
		});

		final HTMLDocument doc = (HTMLDocument) getDocument();
		final HTMLEditorKit editorKit = (HTMLEditorKit) getEditorKit();
		try {
			editorKit.insertHTML(doc, doc.getLength(),
					"<a href=\"https://github.com/Lordszynencja/Charter/wiki\">Charter Wiki</a>", 0, 0, null);
		} catch (BadLocationException | IOException e) {
			Logger.error("Error in help tab", e);
		}
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

	private String getStatisticsString() {
		final StringBuilder b = new StringBuilder();
		for (final Entry<String, ExpiringValuesList<Long>> entry : frameTimes.entrySet()) {
			final int fps = entry.getValue().getValues().size();
			if (fps == 0) {
				continue;
			}

			b.append(entry.getKey()).append(" FPS: ").append(entry.getValue().getValues().size()).append("<br/>");
		}

		for (final Entry<String, ExpiringValuesList<Long>> entry : timings.entrySet()) {
			final List<Long> values = entry.getValue().getValues();
			if (values.isEmpty()) {
				continue;
			}

			final Statistics statistics = new Statistics(entry.getValue().getValues());
			b.append(entry.getKey()).append(":<br/>")//
					.append("  minimum: ").append(statistics.min).append("<br/>")//
					.append("  average: ").append(statistics.average).append("<br/>")//
					.append("  maximum: ").append(statistics.max).append("<br/>");
		}

		return b.toString();
	}

	public void updateValues() {
		if (!isVisible()) {
			return;
		}
		if (System.currentTimeMillis() < lastUpdate + 100) {
			return;
		}

		final StringBuilder b = new StringBuilder("<html>")//
				.append(charterWikiLink).append("<br/>")//
				.append("<br/>")//
				.append(getStatisticsString())//
				.append("</html>");

		setText(b.toString());
		lastUpdate = System.currentTimeMillis();
	}

	public void recalculateSizes() {
		setFont(getFont().deriveFont(GraphicalConfig.inputSize * 0.7f));
	}
}
