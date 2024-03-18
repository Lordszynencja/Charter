package log.charter.gui.chartPanelDrawers.instruments;

import static java.lang.Math.max;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesTop;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledRectangle;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.strokedRectangle;
import static log.charter.util.ScalingUtils.positionToX;
import static log.charter.util.ScalingUtils.xToPosition;

import java.awt.Font;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Set;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.vocals.Vocal;
import log.charter.data.song.vocals.Vocal.VocalFlag;
import log.charter.data.types.PositionType;
import log.charter.gui.ChartPanel;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.chartPanelDrawers.common.BeatsDrawer;
import log.charter.gui.chartPanelDrawers.common.LyricLinesDrawer;
import log.charter.gui.chartPanelDrawers.common.waveform.WaveFormDrawer;
import log.charter.gui.chartPanelDrawers.data.FrameData;
import log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShapeList;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePositionWithSize;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapeSize;
import log.charter.gui.chartPanelDrawers.drawableShapes.Text;
import log.charter.util.data.Position2D;

public class VocalsDrawer {
	private static final Font vocalFont = new Font(Font.DIALOG, Font.PLAIN, 12);
	private static int vocalNoteY = (lanesTop + lanesBottom) / 2;

	public static void reloadGraphics() {
		vocalNoteY = (lanesTop + lanesBottom) / 2;
	}

	private static ShapePositionWithSize getVocalNotePosition(final int x, final int length) {
		return new ShapePositionWithSize(x, vocalNoteY - 4, length, 8);
	}

	private class VocalNotesDrawingData {
		private final DrawableShapeList texts = new DrawableShapeList();
		private final DrawableShapeList notes = new DrawableShapeList();
		private final DrawableShapeList wordConnections = new DrawableShapeList();

		private final ImmutableBeatsMap beats;
		private final Graphics2D g;
		private final int time;

		public VocalNotesDrawingData(final ImmutableBeatsMap beats, final Graphics2D g, final int time) {
			this.beats = beats;
			this.g = g;
			this.time = time;
		}

		private void addVocal(final Vocal vocal, final int x, final int length, final boolean selected) {
			final ShapePositionWithSize positionAndSize = getVocalNotePosition(x, length);
			notes.add(filledRectangle(positionAndSize, ColorLabel.VOCAL_NOTE, true));
			if (selected) {
				notes.add(strokedRectangle(positionAndSize.resized(-1, -1, 1, 1), ColorLabel.VOCAL_SELECT));
			}

			final String text = vocal.text() + (vocal.flag() == VocalFlag.WORD_PART ? "-" : "");
			final ShapeSize expectedTextSize = Text.getExpectedSize(g, vocalFont, text);
			if (x + expectedTextSize.width <= 0) {
				return;
			}

			final int textY = vocalNoteY - expectedTextSize.height - positionAndSize.height;
			texts.add(new Text(new Position2D(x + 2, textY), vocalFont, text, ColorLabel.VOCAL_TEXT));
		}

		private void addConnection(final int x, final Vocal next) {
			final int nextStart = positionToX(next.position(beats), time);
			final ShapePositionWithSize position = new ShapePositionWithSize(x, vocalNoteY, nextStart - x, 4)//
					.centeredY();
			wordConnections.add(filledRectangle(position, ColorLabel.VOCAL_NOTE.colorWithAlpha(192)));
		}

		public void addVocal(final Vocal vocal, final Vocal next, final int x, final int length,
				final boolean selected) {
			final int x1 = x + length;
			if (x1 > 0) {
				addVocal(vocal, x, length, selected);
			}

			if (vocal.flag() == VocalFlag.WORD_PART && next != null) {
				addConnection(x1, next);
			}
		}

		public void addHighlight(final int x, final int length) {
			final ShapePositionWithSize positionAndSize = getVocalNotePosition(x, time);
			notes.add(strokedRectangle(positionAndSize.resized(-1, -1, 1, 1), ColorLabel.HIGHLIGHT));
		}

		public void draw(final Graphics2D g) {
			wordConnections.draw(g);
			notes.draw(g);
			texts.draw(g);
		}
	}

	private BeatsDrawer beatsDrawer;
	private ChartPanel chartPanel;
	private LyricLinesDrawer lyricLinesDrawer;
	private WaveFormDrawer waveFormDrawer;

	public void lyricLinesDrawer(final LyricLinesDrawer lyricLinesDrawer) {
		this.lyricLinesDrawer = lyricLinesDrawer;
	}

	private void drawVocals(final FrameData frameData) {
		final VocalNotesDrawingData drawingData = new VocalNotesDrawingData(frameData.beats, frameData.g,
				frameData.time);

		final List<Vocal> vocals = frameData.vocals.vocals;
		final int width = chartPanel.getWidth();
		final Set<Integer> selectedVocalIds = frameData.selection.getSelectedIdsSet(PositionType.VOCAL);

		final int timeFrom = xToPosition(-1, frameData.time);
		final int timeTo = xToPosition(width + 1, frameData.time);

		for (int i = 0; i < vocals.size(); i++) {
			final Vocal vocal = vocals.get(i);
			if (vocal.position(frameData.beats) > timeTo) {
				break;
			}

			Vocal next = null;
			if (vocal.flag() == VocalFlag.WORD_PART) {
				next = vocals.size() > i + 1 ? vocals.get(i + 1) : null;
			}
			final int endPosition = next == null ? vocal.endPosition(frameData.beats) : next.position(frameData.beats);
			if (endPosition < timeFrom) {
				continue;
			}

			final int x = positionToX(vocal.position(frameData.beats), frameData.time);
			final int length = max(2, positionToX(vocal.endPosition(frameData.beats), frameData.time) - x);
			final boolean selected = selectedVocalIds.contains(i);
			drawingData.addVocal(vocal, next, x, length, selected);
		}

		if (frameData.highlightData.type == PositionType.VOCAL) {
			if (frameData.highlightData.id.isPresent()) {
				final Vocal vocal = vocals.get(frameData.highlightData.id.get().id);
				final int x = positionToX(vocal.position(frameData.beats), frameData.time);
				final int length = max(2, positionToX(vocal.endPosition(frameData.beats), frameData.time) - x);
				drawingData.addHighlight(x, length);
			} else {
				frameData.highlightData.highlightedNonIdPositions.forEach(highlightPosition -> {
					final int x = positionToX(highlightPosition.position, frameData.time);
					final int length = max(2,
							positionToX(highlightPosition.endPosition().position(frameData.beats), frameData.time) - x);
					drawingData.addHighlight(x, length);
				});
			}
		}

		drawingData.draw(frameData.g);
	}

	public void draw(final FrameData frameData) {
		waveFormDrawer.draw(frameData);
		beatsDrawer.draw(frameData);
		drawVocals(frameData);
		lyricLinesDrawer.draw(frameData);
	}

}
