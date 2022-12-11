package log.charter.gui.chartPanelDrawers.common;

import static log.charter.gui.ChartPanel.beatTextY;
import static log.charter.gui.ChartPanel.lanesBottom;
import static log.charter.util.ScalingUtils.timeToX;

import java.awt.Font;
import java.awt.Graphics;
import java.util.List;

import log.charter.data.ChartData;
import log.charter.gui.ChartPanel;
import log.charter.gui.ChartPanelColors;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.song.Beat;

public class BeatsDrawer {
	private ChartData data;
	private ChartPanel chartPanel;

	public void init(final ChartData data, final ChartPanel chartPanel) {
		this.data = data;
		this.chartPanel = chartPanel;
	}

	public void draw(final Graphics g) {
		if (data.isEmpty) {
			return;
		}

		int id = 0;
		final List<Beat> beats = data.songChart.beatsMap.beats;
		g.setFont(new Font(Font.DIALOG, Font.PLAIN, 15));

		for (final Beat beat : beats) {
			id++;
			final int x = timeToX(beat.position, data.time);
			if (x < -100) {
				continue;
			}
			if (x > chartPanel.getWidth()) {
				break;
			}

			if (beat.firstInMeasure) {
				g.setColor(ChartPanelColors.get(ColorLabel.MAIN_BEAT));
				g.drawLine(x, beatTextY, x, lanesBottom);
				g.drawString("" + id, x + 3, ChartPanel.beatTextY + 11);
				if (id == 1 || beat.beatsInMeasure != beats.get(id - 2).beatsInMeasure) {
					g.drawString("" + beat.beatsInMeasure, x + 3, ChartPanel.beatSizeTextY + 11);
				}
			} else {
				g.setColor(ChartPanelColors.get(ColorLabel.SECONDARY_BEAT));
				g.drawLine(x, beatTextY, x, lanesBottom);
				g.drawString("" + id, x + 3, ChartPanel.beatTextY + 11);
			}
		}
	}

}
