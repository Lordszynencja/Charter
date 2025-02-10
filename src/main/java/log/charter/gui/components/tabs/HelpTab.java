package log.charter.gui.components.tabs;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;

import log.charter.gui.ChartPanelColors.ColorLabel;

public class HelpTab extends JLabel {
	private static final long serialVersionUID = -272982208492712044L;

	public HelpTab() {
		setVerticalAlignment(JLabel.TOP);
		setBackground(ColorLabel.BASE_BG_2.color());
		setForeground(ColorLabel.BASE_DARK_TEXT.color());
		setOpaque(true);
		setFocusable(false);
	}

	private final List<Long> frameTimes = new ArrayList<>();

	public void addFrameTime() {
		final long t = (System.nanoTime() / 1_000_000);
		frameTimes.add(t);

		frameTimes.removeIf(t0 -> t - t0 > 1000);
		setText("FPS: " + frameTimes.size());
	}
}
