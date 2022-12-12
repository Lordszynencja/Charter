package log.charter.gui.chartPanelDrawers.common;

import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.getAsOdd;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.getLaneSize;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.getLaneY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.yToLane;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.strokedRectangle;
import static log.charter.gui.chartPanelDrawers.instruments.GuitarDrawer.noteTailOffset;
import static log.charter.gui.chartPanelDrawers.instruments.VocalsDrawer.getVocalNotePosition;
import static log.charter.util.ScalingUtils.timeToX;
import static log.charter.util.ScalingUtils.timeToXLength;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.Map;

import log.charter.data.ChartData;
import log.charter.data.EditMode;
import log.charter.gui.ChartPanelColors;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.HighlightManager;
import log.charter.gui.PositionWithIdAndType;
import log.charter.gui.PositionWithIdAndType.PositionType;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePositionWithSize;
import log.charter.gui.chartPanelDrawers.instruments.GuitarDrawer;
import log.charter.gui.handlers.ChartPanelMouseListener;
import log.charter.io.rs.xml.song.ChordTemplate;
import log.charter.song.Chord;
import log.charter.song.HandShape;
import log.charter.song.Note;
import log.charter.util.IntRange;

public class HighlightDrawer {
	private static final Color highlightColor = ChartPanelColors.get(ColorLabel.HIGHLIGHT);

	private static final int noteWidth = GuitarDrawer.noteWidth + 1;

	private static interface HighlightTypeDrawer {
		void drawHighlight(Graphics g, PositionWithIdAndType highlight);
	}

	private ChartPanelMouseListener chartPanelMouseListener;
	private ChartData data;
	private HighlightManager highlightManager;

	public void init(final ChartPanelMouseListener chartPanelMouseListener, final ChartData data,
			final HighlightManager highlightManager) {
		this.chartPanelMouseListener = chartPanelMouseListener;
		this.data = data;
		this.highlightManager = highlightManager;
	}

	private void drawBeatHighlight(final Graphics g, final PositionWithIdAndType highlight) {
	}

	private void drawNoteHighlight(final Graphics g, final int string, final int position, final int length,
			final int strings) {
		final int x = timeToX(position, data.time);
		final int y = getLaneY(string, strings);
		final int noteHeight = getLaneSize(strings) + 1;
		final ShapePositionWithSize notePosition = new ShapePositionWithSize(x, y, noteWidth, noteHeight)//
				.centered();
		strokedRectangle(notePosition, highlightColor).draw(g);

		final int tailHeight = getAsOdd((noteHeight - 1) * 3 / 4) + 1;
		final int tailLength = timeToXLength(length) - noteTailOffset - 2;
		if (tailLength > 0) {
			final int tailX = x + noteTailOffset + 1;
			final ShapePositionWithSize tailPosition = new ShapePositionWithSize(tailX, y, tailLength + 1, tailHeight)//
					.centeredY();
			strokedRectangle(tailPosition, highlightColor).draw(g);
		}
	}

	private void drawRepeatedChordHighlight(final Graphics g, final ChordTemplate chordTemplate, final int position,
			final int strings) {
		final int x = timeToX(position, data.time);
		final IntRange stringRange = chordTemplate.getStringRange();
		final IntRange chordTopBottom = new IntRange(getLaneY(stringRange.min, strings),
				getLaneY(stringRange.max, strings));
		final int offset = getLaneSize(strings) / 2;
		final int yTop = chordTopBottom.min - offset - 1;
		final int yBottom = chordTopBottom.max + offset;

		final ShapePositionWithSize chordPosition = new ShapePositionWithSize(x, yTop, noteWidth, yBottom - yTop - 1)//
				.centeredX();
		strokedRectangle(chordPosition, highlightColor).draw(g);
	}

	private void drawGuitarNoteHighlight(final Graphics g, final PositionWithIdAndType highlight) {
		final int strings = data.getCurrentArrangement().tuning.strings;

		if (highlight.note != null) {
			final Note note = highlight.note;
			drawNoteHighlight(g, note.string, note.position, note.sustain, strings);
			return;
		}

		if (highlight.chord != null) {
			final Chord chord = highlight.chord;
			if (!chord.chordNotes.isEmpty()) {
				for (final Note note : chord.chordNotes) {
					drawNoteHighlight(g, note.string, note.position, note.sustain, strings);
				}

				return;
			}

			final ChordTemplate chordTemplate = data.getCurrentArrangement().chordTemplates.get(chord.chordId);
			drawRepeatedChordHighlight(g, chordTemplate, chord.position, strings);

			return;
		}

		final int lane = yToLane(chartPanelMouseListener.getMouseY(), strings);
		drawNoteHighlight(g, lane, highlight.position, 0, strings);
	}

	private void drawHandShapeHighlight(final Graphics g, final PositionWithIdAndType highlight) {
		final HandShape handShape = highlight.handShape;
		final int x = timeToX(handShape.position, data.time);
		final int length = timeToXLength(handShape.length);
		final ShapePositionWithSize position = new ShapePositionWithSize(x, lanesBottom, length, 10);
		strokedRectangle(position, highlightColor).draw(g);
	}

	private void drawNoneHighlight(final Graphics g, final PositionWithIdAndType highlight) {
	}

	private void drawVocalHighlight(final Graphics g, final PositionWithIdAndType highlight) {
		final int position = highlight.position;
		final int length = highlight.vocal == null ? 50 : highlight.vocal.length;
		strokedRectangle(getVocalNotePosition(position, length, data.time), highlightColor).draw(g);
	}

	private final Map<PositionType, HighlightTypeDrawer> highlightDrawers = new HashMap<>();

	{
		highlightDrawers.put(PositionType.BEAT, this::drawBeatHighlight);
		highlightDrawers.put(PositionType.GUITAR_NOTE, this::drawGuitarNoteHighlight);
		highlightDrawers.put(PositionType.HAND_SHAPE, this::drawHandShapeHighlight);
		highlightDrawers.put(PositionType.NONE, this::drawNoneHighlight);
		highlightDrawers.put(PositionType.VOCAL, this::drawVocalHighlight);
	}

	public void draw(final Graphics g) {
		final PositionWithIdAndType highlight = highlightManager.getHighlight();
		highlightDrawers.get(highlight.type).drawHighlight(g, highlight);

		if (data.editMode == EditMode.GUITAR && data.isNoteAdd) {
			g.setColor(ChartPanelColors.get(ColorLabel.NOTE_ADD_LINE));
			g.drawLine(data.mousePressX, data.mousePressY, data.mx, data.my);
		}
	}
}
