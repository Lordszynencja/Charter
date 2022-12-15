package log.charter.gui.chartPanelDrawers.common;

import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.beatSizeTextY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.beatTextY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.lineVertical;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.strokedRectangle;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.text;
import static log.charter.util.ScalingUtils.timeToX;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.gui.ChartPanel;
import log.charter.gui.ChartPanelColors;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShapeList;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePositionWithSize;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler.MouseButton;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler.MouseButtonPressData;
import log.charter.song.Beat;
import log.charter.util.CollectionUtils.HashSet2;
import log.charter.util.Position2D;

public class BeatsDrawer {
	private static class BeatsDrawingData {
		private static final Color selectColor = ChartPanelColors.get(ColorLabel.SELECT);
		private static final Color mainBeatColor = ChartPanelColors.get(ColorLabel.MAIN_BEAT);
		private static final Color secondaryBeatColor = ChartPanelColors.get(ColorLabel.SECONDARY_BEAT);

		private final DrawableShapeList beats = new DrawableShapeList();

		public void addBeat(final Beat beat, final int x, final int id, final Beat previousBeat,
				final boolean selected) {
			final Color color = beat.firstInMeasure ? mainBeatColor : secondaryBeatColor;

			beats.add(lineVertical(x, beatTextY, lanesBottom, color));

			if (beat.firstInMeasure) {
				final String text = "" + (id + 1);
				beats.add(text(new Position2D(x + 3, beatTextY + 11), text, color));
			}

			if (previousBeat == null || beat.beatsInMeasure != previousBeat.beatsInMeasure) {
				beats.add(text(new Position2D(x + 3, beatSizeTextY + 11), beat.beatsInMeasure + "/4", mainBeatColor));
			}

			if (selected) {
				final int top = beatTextY - 1;
				final int bottom = lanesBottom + 1;
				final ShapePositionWithSize beatPosition = new ShapePositionWithSize(x - 1, top, 2, bottom - top);
				beats.add(strokedRectangle(beatPosition, selectColor));
			}
		}

		public void draw(final Graphics g) {
			g.setFont(new Font(Font.DIALOG, Font.PLAIN, 15));
			beats.draw(g);
		}
	}

	private ChartData data;
	private ChartPanel chartPanel;
	private MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler;
	private SelectionManager selectionManager;

	public void init(final ChartData data, final ChartPanel chartPanel,
			final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler,
			final SelectionManager selectionManager) {
		this.data = data;
		this.chartPanel = chartPanel;
		this.mouseButtonPressReleaseHandler = mouseButtonPressReleaseHandler;
		this.selectionManager = selectionManager;
	}

	public void draw(final Graphics g) {
		final BeatsDrawingData drawingData = new BeatsDrawingData();
		final List<Beat> beats = data.songChart.beatsMap.beats;
		final HashSet2<Integer> selectedBeatIds = selectionManager.getSelectedAccessor(PositionType.BEAT)//
				.getSelectedSet().map(selection -> selection.id);

		if (selectedBeatIds.isEmpty()) {
			final MouseButtonPressData pressData = mouseButtonPressReleaseHandler
					.getPressPosition(MouseButton.LEFT_BUTTON);
			if (pressData != null && pressData.highlight.beat != null) {
				selectedBeatIds.add(pressData.highlight.id);
			}
		}

		for (int i = 0; i < beats.size(); i++) {
			final Beat beat = beats.get(i);
			final int x = timeToX(beat.position, data.time);
			if (x < -1000) {
				continue;
			}
			if (x > chartPanel.getWidth()) {
				break;
			}

			final boolean selected = selectedBeatIds.contains(i);
			drawingData.addBeat(beat, x, i, i > 0 ? beats.get(i - 1) : null, selected);
		}

		drawingData.draw(g);
	}

}
