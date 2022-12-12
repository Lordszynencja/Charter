package log.charter.gui.chartPanelDrawers.common;

import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.beatSizeTextY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.beatTextY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.lineVertical;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.text;
import static log.charter.util.ScalingUtils.timeToX;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.List;

import log.charter.data.ChartData;
import log.charter.gui.ChartPanel;
import log.charter.gui.ChartPanelColors;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShapeList;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePosition;
import log.charter.song.Beat;

public class BeatsDrawer {
	private static class BeatsDrawingData {
		private static final Color mainBeatColor = ChartPanelColors.get(ColorLabel.MAIN_BEAT);
		private static final Color secondaryBeatColor = ChartPanelColors.get(ColorLabel.SECONDARY_BEAT);

		private final DrawableShapeList beats = new DrawableShapeList();

		public void addBeat(final Beat beat, final int x, final int id, final Beat previousBeat) {
			final Color color = beat.firstInMeasure ? mainBeatColor : secondaryBeatColor;

			beats.add(lineVertical(x, beatTextY, lanesBottom, color));

			if (beat.firstInMeasure) {
				beats.add(text(new ShapePosition(x + 3, beatTextY + 11), "" + (id + 1), color));
			}

			if (previousBeat == null || beat.beatsInMeasure != previousBeat.beatsInMeasure) {
				beats.add(
						text(new ShapePosition(x + 3, beatSizeTextY + 11), beat.beatsInMeasure + "/4", mainBeatColor));
			}
		}

		public void draw(final Graphics g) {
			g.setFont(new Font(Font.DIALOG, Font.PLAIN, 15));
			beats.draw(g);
		}
	}

	private ChartData data;
	private ChartPanel chartPanel;

	public void init(final ChartData data, final ChartPanel chartPanel) {
		this.data = data;
		this.chartPanel = chartPanel;
	}

	public void draw(final Graphics g) {
		final BeatsDrawingData drawingData = new BeatsDrawingData();
		final List<Beat> beats = data.songChart.beatsMap.beats;

		for (int i = 0; i < beats.size(); i++) {
			final Beat beat = beats.get(i);
			final int x = timeToX(beat.position, data.time);
			if (x < -1000) {
				continue;
			}
			if (x > chartPanel.getWidth()) {
				break;
			}

			drawingData.addBeat(beat, x, i, i > 0 ? beats.get(i - 1) : null);
		}

		drawingData.draw(g);
	}

}
