package log.charter.gui.chartPanelDrawers.instruments;

import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.anchorTextY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.anchorY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.getAsOdd;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.getLaneSize;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.getLaneY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.centeredTextWithBackground;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledRectangle;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.lineVertical;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.strokedRectangle;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.text;
import static log.charter.util.ScalingUtils.timeToX;
import static log.charter.util.ScalingUtils.timeToXLength;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import log.charter.data.ChartData;
import log.charter.data.PositionWithIdAndType.PositionType;
import log.charter.data.managers.selection.ChordOrNote;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.gui.ChartPanel;
import log.charter.gui.ChartPanelColors;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.chartPanelDrawers.common.AudioDrawer;
import log.charter.gui.chartPanelDrawers.common.BeatsDrawer;
import log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShapeList;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePositionWithSize;
import log.charter.io.rs.xml.song.ChordTemplate;
import log.charter.song.Anchor;
import log.charter.song.ArrangementChart;
import log.charter.song.Chord;
import log.charter.song.HandShape;
import log.charter.song.Level;
import log.charter.song.Note;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashSet2;
import log.charter.util.IntRange;
import log.charter.util.Position2D;

public class GuitarDrawer {
	public static final int noteWidth = 23;
	public static final int noteTailOffset = noteWidth / 2;

	private static class DrawingData {
		private final static Color selectColor = ChartPanelColors.get(ColorLabel.SELECT);
		private static final Color[] noteColors = new Color[6];
		private static final Color[] noteTailColors = new Color[6];
		private static final Color anchorColor = ChartPanelColors.get(ColorLabel.ANCHOR);
		private static final Color repeatedChordColor = ChartPanelColors.get(ColorLabel.REPEATED_CHORD);
		private static final Color handShapeColor = ChartPanelColors.get(ColorLabel.HAND_SHAPE);

		static {
			for (int i = 0; i < 6; i++) {
				noteColors[i] = ChartPanelColors.get(ColorLabel.valueOf("NOTE_" + i));
				noteTailColors[i] = ChartPanelColors.get(ColorLabel.valueOf("NOTE_TAIL_" + i));
			}
		}

		private final int[] stringPositions;
		private final int noteHeight;
		private final int noteYOffset;
		private final int tailHeight;

		private final DrawableShapeList anchors;
		private final DrawableShapeList noteTails;
		private final DrawableShapeList chordNotes;
		private final DrawableShapeList notes;
		private final DrawableShapeList noteFrets;
		private final DrawableShapeList handShapes;
		// TODO add note tails that are different shapes

		public DrawingData(final int strings) {
			stringPositions = new int[strings];
			for (int i = 0; i < strings; i++) {
				stringPositions[i] = getLaneY(i, strings);
			}
			noteHeight = getLaneSize(strings);
			noteYOffset = noteHeight / 2;
			tailHeight = getAsOdd(noteHeight * 3 / 4);

			anchors = new DrawableShapeList();
			noteTails = new DrawableShapeList();
			chordNotes = new DrawableShapeList();
			notes = new DrawableShapeList();
			noteFrets = new DrawableShapeList();
			handShapes = new DrawableShapeList();
		}

		public void addNote(final Note note, final int x, final int length, final boolean selected) {
			final int y = stringPositions[note.string];
			final ShapePositionWithSize position = new ShapePositionWithSize(x, y, noteWidth, noteHeight)//
					.centered();
			notes.add(filledRectangle(position, noteColors[note.string]));
			notes.add(centeredTextWithBackground(new Position2D(x, y), "" + note.fret, Color.WHITE, Color.BLACK));

			if (selected) {
				notes.add(strokedRectangle(position.resized(-1, -1, 1, 1), selectColor));
			}
		}

		public void addNoteTail(final Note note, final int x, final int length, final boolean selected) {
			final ShapePositionWithSize position = new ShapePositionWithSize(x + noteTailOffset,
					stringPositions[note.string], length - noteTailOffset, tailHeight).centeredY();
			noteTails.add(filledRectangle(position, noteTailColors[note.string]));

			if (selected) {
				chordNotes.add(strokedRectangle(position.resized(0, -1, 0, 1), selectColor));
			}
		}

		public void addRepeatedChord(final Chord chord, final ChordTemplate chordTemplate, final int x,
				final boolean selected) {
			final IntRange stringRange = chordTemplate.getStringRange();
			final IntRange chordTopBottom = new IntRange(stringPositions[stringRange.min],
					stringPositions[stringRange.max]);
			final int yTop = chordTopBottom.min - noteYOffset;
			final int yBottom = chordTopBottom.max + noteYOffset;

			final ShapePositionWithSize position = new ShapePositionWithSize(x, yTop, noteWidth, yBottom - yTop - 1)//
					.centeredX();
			chordNotes.add(filledRectangle(position, repeatedChordColor));

			if (selected) {
				chordNotes.add(strokedRectangle(position.resized(-1, -1, 1, 1), selectColor));
			}
		}

		public void addAnchor(final Anchor anchor, final int x, final boolean selected) {
			anchors.add(lineVertical(x, anchorY, lanesBottom, anchorColor));
			anchors.add(text(new Position2D(x + 4, anchorTextY), "" + anchor.fret, anchorColor));

			if (selected) {
				final int top = anchorY - 1;
				final int bottom = lanesBottom + 1;
				final ShapePositionWithSize anchorPosition = new ShapePositionWithSize(x - 1, top, 2, bottom - top);
				anchors.add(strokedRectangle(anchorPosition, selectColor));
			}
		}

		public void addHandShape(final int x, final int length, final boolean selected) {
			final ShapePositionWithSize position = new ShapePositionWithSize(x, lanesBottom, length, 10);
			handShapes.add(filledRectangle(position, handShapeColor));

			if (selected) {
				handShapes.add(strokedRectangle(position, selectColor));
			}
		}

		public void draw(final Graphics g) {
			g.setFont(new Font(Font.DIALOG, Font.BOLD, 13));
			anchors.draw(g);
			noteTails.draw(g);
			chordNotes.draw(g);
			g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
			notes.draw(g);
			noteFrets.draw(g);
			handShapes.draw(g);
		}
	}

	private static boolean isPastRightEdge(final int x, final int width) {
		return x > (width + noteWidth / 2);
	}

	private static boolean isOnScreen(final int x, final int length) {
		return x + length >= 0;
	}

	private static boolean isTailVisible(final int length) {
		return length > noteTailOffset;
	}

	private AudioDrawer audioDrawer;
	private BeatsDrawer beatsDrawer;
	private ChartData data;
	private ChartPanel chartPanel;
	private SelectionManager selectionManager;

	public void init(final AudioDrawer audioDrawer, final BeatsDrawer beatsDrawer, final ChartData data,
			final ChartPanel chartPanel, final SelectionManager selectionManager) {
		this.audioDrawer = audioDrawer;
		this.beatsDrawer = beatsDrawer;
		this.data = data;
		this.chartPanel = chartPanel;
		this.selectionManager = selectionManager;
	}

	private void drawGuitarLanes(final Graphics g) {
		final int lanes = data.getCurrentArrangement().tuning.strings;
		final int width = chartPanel.getWidth();

		final int x = timeToX(0, data.time);

		for (int i = 0; i < lanes; i++) {
			g.setColor(ChartPanelColors.get(ColorLabel.valueOf("LANE_" + i)));
			final int y = getLaneY(i, lanes);
			g.drawLine(x, y, width, y);
		}
	}

	private void addAnchors(final DrawingData drawingData, final Level level, final int panelWidth) {
		final HashSet2<Integer> selectedAnchorIds = selectionManager.getSelectedAccessor(PositionType.ANCHOR)//
				.getSelectedSet().map(selection -> selection.id);

		for (int i = 0; i < level.anchors.size(); i++) {
			final Anchor anchor = level.anchors.get(i);
			final int x = timeToX(anchor.position, data.time);
			if (isPastRightEdge(x, panelWidth)) {
				break;
			}

			if (!isOnScreen(x, 20)) {
				continue;
			}

			final boolean selected = selectedAnchorIds.contains(i);
			drawingData.addAnchor(anchor, x, selected);
		}
	}

	private boolean addChord(final DrawingData drawingData, final ArrangementChart arrangement, final int panelWidth,
			final Chord chord, final boolean selected) {
		final int x = timeToX(chord.position, data.time);
		if (isPastRightEdge(x, panelWidth)) {
			return false;
		}

		if (chord.chordNotes.isEmpty()) {
			if (!isOnScreen(x, 0)) {
				return true;
			}

			final ChordTemplate chordTemplate = arrangement.chordTemplates.get(chord.chordId);
			drawingData.addRepeatedChord(chord, chordTemplate, x, selected);

			return true;
		}

		for (final Note note : chord.chordNotes) {
			final int length = timeToXLength(note.sustain);
			if (!isOnScreen(x, length)) {
				continue;
			}

			drawingData.addNote(note, x, length, selected);
			if (isTailVisible(length)) {
				drawingData.addNoteTail(note, x, length, selected);
			}
		}

		return true;
	}

	private boolean addNote(final DrawingData drawingData, final int panelWidth, final Note note,
			final boolean selected) {
		final int x = timeToX(note.position, data.time);
		final int length = timeToXLength(note.sustain);
		if (isPastRightEdge(x, panelWidth)) {
			return false;
		}

		if (!isOnScreen(x, length)) {
			return true;
		}

		drawingData.addNote(note, x, length, selected);
		if (isTailVisible(length)) {
			drawingData.addNoteTail(note, x, length, selected);
		}

		return true;
	}

	private boolean addChordOrNote(final DrawingData drawingData, final ArrangementChart arrangement,
			final int panelWidth, final ChordOrNote chordOrNote, final boolean selected) {
		if (chordOrNote.chord != null) {
			return addChord(drawingData, arrangement, panelWidth, chordOrNote.chord, selected);
		}
		if (chordOrNote.note != null) {
			return addNote(drawingData, panelWidth, chordOrNote.note, selected);
		}

		return true;
	}

	private void addGuitarNotes(final DrawingData drawingData, final ArrangementChart arrangement,
			final int panelWidth) {
		final HashSet2<Integer> selectedNoteIds = selectionManager.getSelectedAccessor(PositionType.GUITAR_NOTE)
				.getSelectedSet()//
				.map(selection -> selection.id);

		final ArrayList2<ChordOrNote> chordsAndNotes = PositionType.GUITAR_NOTE.getPositions(data);

		for (int i = 0; i < chordsAndNotes.size(); i++) {
			final ChordOrNote chordOrNote = chordsAndNotes.get(i);
			final boolean selected = selectedNoteIds.contains(i);
			addChordOrNote(drawingData, arrangement, panelWidth, chordOrNote, selected);
		}
	}

	private void addHandShapes(final DrawingData drawingData, final Level level, final int panelWidth) {
		final HashSet2<Integer> selectedHandShapeIds = selectionManager.getSelectedAccessor(PositionType.HAND_SHAPE)//
				.getSelectedSet().map(selection -> selection.id);

		for (int i = 0; i < level.handShapes.size(); i++) {
			final HandShape handShape = level.handShapes.get(i);
			final int x = timeToX(handShape.position, data.time);
			if (isPastRightEdge(x, panelWidth)) {
				break;
			}

			final int length = timeToXLength(handShape.length);
			if (!isOnScreen(x, length)) {
				continue;
			}

			final boolean selected = selectedHandShapeIds.contains(i);
			drawingData.addHandShape(x, length, selected);
		}
	}

	private void drawGuitarNotes(final Graphics g) {
		final Level level = data.getCurrentArrangementLevel();
		final ArrangementChart arrangement = data.getCurrentArrangement();
		final int strings = data.getCurrentArrangement().tuning.strings;
		final DrawingData drawingData = new DrawingData(strings);

		final int panelWidth = chartPanel.getWidth();

		addGuitarNotes(drawingData, arrangement, panelWidth);
		addHandShapes(drawingData, level, panelWidth);
		addAnchors(drawingData, level, panelWidth);

		drawingData.draw(g);
	}

	public void draw(final Graphics g) {
		beatsDrawer.draw(g);
		drawGuitarLanes(g);
		audioDrawer.draw(g);
		drawGuitarNotes(g);
	}
}
