package log.charter.gui.chartPanelDrawers.common;

import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.anchorY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.beatTextY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.getAsOdd;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.getLaneSize;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.getLaneY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.yToLane;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.line;
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
import log.charter.data.PositionWithIdAndType;
import log.charter.data.PositionWithIdAndType.PositionType;
import log.charter.data.managers.HighlightManager;
import log.charter.data.managers.HighlightManager.PositionWithStringOrNoteId;
import log.charter.data.managers.ModeManager;
import log.charter.gui.ChartPanelColors;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePositionWithSize;
import log.charter.gui.chartPanelDrawers.instruments.GuitarDrawer;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler.MouseButton;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler.MouseButtonPressData;
import log.charter.gui.handlers.MouseHandler;
import log.charter.io.rs.xml.song.ChordTemplate;
import log.charter.song.Chord;
import log.charter.song.HandShape;
import log.charter.song.Note;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.IntRange;
import log.charter.util.Position2D;

public class HighlightDrawer {
	private static final Color highlightColor = ChartPanelColors.get(ColorLabel.HIGHLIGHT);
	private static final Color noteAdditionLineColor = ChartPanelColors.get(ColorLabel.NOTE_ADD_LINE);

	private static final int noteWidth = GuitarDrawer.noteWidth + 1;

	private static interface HighlightTypeDrawer {
		void drawHighlight(Graphics g, PositionWithIdAndType highlight, int x, int y);
	}

	private ChartData data;
	private HighlightManager highlightManager;
	private ModeManager modeManager;
	private MouseHandler mouseHandler;
	private MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler;

	public void init(final ChartData data, final HighlightManager highlightManager, final ModeManager modeManager,
			final MouseHandler mouseHandler, final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler) {
		this.data = data;
		this.highlightManager = highlightManager;
		this.modeManager = modeManager;
		this.mouseHandler = mouseHandler;
		this.mouseButtonPressReleaseHandler = mouseButtonPressReleaseHandler;
	}

	private void drawAnchorHighlight(final Graphics g, final PositionWithIdAndType highlight, final int x,
			final int y) {
		if (highlight.anchor == null) {
			return;
		}

		final int beatX = timeToX(highlight.anchor.position, data.time);
		final int top = anchorY - 1;
		final int bottom = lanesBottom + 1;
		final ShapePositionWithSize beatPosition = new ShapePositionWithSize(beatX - 1, top, 2, bottom - top);
		strokedRectangle(beatPosition, highlightColor).draw(g);
	}

	private void drawBeatHighlight(final Graphics g, final PositionWithIdAndType highlight, final int x, final int y) {
		if (highlight.beat == null) {
			return;
		}

		final int beatX = timeToX(highlight.beat.position, data.time);
		final int top = beatTextY - 1;
		final int bottom = lanesBottom + 1;
		final ShapePositionWithSize beatPosition = new ShapePositionWithSize(beatX - 1, top, 2, bottom - top);
		strokedRectangle(beatPosition, highlightColor).draw(g);
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

	private void drawGuitarNoteHighlight(final Graphics g, final PositionWithIdAndType highlight, final int x,
			final int y) {
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

		final int lane = yToLane(y, strings);
		drawNoteHighlight(g, lane, highlight.position, 0, strings);
	}

	private ShapePositionWithSize getHandShapeHighlightPosition(final PositionWithIdAndType highlight) {
		if (highlight.handShape == null) {
			final int x = timeToX(highlight.position, data.time);
			return new ShapePositionWithSize(x, lanesBottom, 50, 10);
		}

		final HandShape handShape = highlight.handShape;
		final int x = timeToX(handShape.position, data.time);
		final int length = timeToXLength(handShape.length);
		return new ShapePositionWithSize(x, lanesBottom, length, 10);
	}

	private void drawHandShapeHighlight(final Graphics g, final PositionWithIdAndType highlight, final int x,
			final int y) {
		strokedRectangle(getHandShapeHighlightPosition(highlight), highlightColor).draw(g);
	}

	private void drawNoneHighlight(final Graphics g, final PositionWithIdAndType highlight, final int x, final int y) {
	}

	private void drawVocalHighlight(final Graphics g, final PositionWithIdAndType highlight, final int x, final int y) {
		final int position = highlight.position;
		final int length = highlight.vocal == null ? 50 : highlight.vocal.length;
		final ShapePositionWithSize vocalNotePosition = getVocalNotePosition(position, length, data.time);
		strokedRectangle(vocalNotePosition.resized(-1, -1, 1, 1), highlightColor).draw(g);
	}

	private final Map<PositionType, HighlightTypeDrawer> highlightDrawers = new HashMap<>();

	{
		highlightDrawers.put(PositionType.ANCHOR, this::drawAnchorHighlight);
		highlightDrawers.put(PositionType.BEAT, this::drawBeatHighlight);
		highlightDrawers.put(PositionType.GUITAR_NOTE, this::drawGuitarNoteHighlight);
		highlightDrawers.put(PositionType.HAND_SHAPE, this::drawHandShapeHighlight);
		highlightDrawers.put(PositionType.NONE, this::drawNoneHighlight);
		highlightDrawers.put(PositionType.VOCAL, this::drawVocalHighlight);
	}

	private void drawNoteAdditionHighlight(final Graphics g, final int x, final int y) {
		if (modeManager.editMode == EditMode.VOCALS) {
			return;
		}

		final MouseButtonPressData pressPosition = mouseButtonPressReleaseHandler
				.getPressPosition(MouseButton.RIGHT_BUTTON);
		if (pressPosition == null) {
			return;
		}

		final int pressXTime = pressPosition.highlight.position;
		final int pressX = timeToX(pressXTime, data.time);
		final int pressY = pressPosition.position.y;

		final PositionWithIdAndType highlight = highlightManager.getHighlight(x, y);
		final int currentX = timeToX(highlight.position, data.time);
		line(new Position2D(pressX, pressY), new Position2D(currentX, y), noteAdditionLineColor).draw(g);

		final ArrayList2<PositionWithStringOrNoteId> positions = highlightManager.getPositionsWithStrings(pressXTime,
				highlight.position, pressY, y);
		final ArrayList2<Chord> chords = data.getCurrentArrangementLevel().chords;
		final ArrayList2<Note> notes = data.getCurrentArrangementLevel().notes;
		final int strings = data.getCurrentArrangement().tuning.strings;

		for (final PositionWithStringOrNoteId highlightPosition : positions) {
			if (highlightPosition.chordId != null) {
				final Chord chord = chords.get(highlightPosition.chordId);
				final PositionWithIdAndType positionHighlight = PositionWithIdAndType.create(highlightPosition.chordId,
						chord);
				drawGuitarNoteHighlight(g, positionHighlight, currentX, y);
				continue;
			}
			if (highlightPosition.noteId != null) {
				final Note note = notes.get(highlightPosition.noteId);
				final PositionWithIdAndType positionHighlight = PositionWithIdAndType.create(highlightPosition.noteId,
						note);
				drawGuitarNoteHighlight(g, positionHighlight, currentX, y);
				continue;
			}

			final PositionWithIdAndType positionHighlight = PositionWithIdAndType.create(highlightPosition.position,
					PositionType.GUITAR_NOTE);
			final int laneY = getLaneY(highlightPosition.string, strings);
			drawGuitarNoteHighlight(g, positionHighlight, currentX, laneY);
		}
	}

	public void draw(final Graphics g) {
		final int x = mouseHandler.getMouseX();
		final int y = mouseHandler.getMouseY();
		final PositionWithIdAndType highlight = highlightManager.getHighlight(x, y);
		highlightDrawers.get(highlight.type).drawHighlight(g, highlight, x, y);

		drawNoteAdditionHighlight(g, x, y);
	}
}
