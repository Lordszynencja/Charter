package log.charter.gui.chartPanelDrawers.instruments;

import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.getAsOdd;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.getLaneSize;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.getLaneY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.centeredTextWithBackground;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledRectangle;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.strokedRectangle;
import static log.charter.util.ScalingUtils.timeToX;
import static log.charter.util.ScalingUtils.timeToXLength;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import log.charter.data.ChartData;
import log.charter.gui.ChartPanel;
import log.charter.gui.ChartPanelColors;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.SelectionManager;
import log.charter.gui.chartPanelDrawers.common.AudioDrawer;
import log.charter.gui.chartPanelDrawers.common.BeatsDrawer;
import log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShapeList;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePosition;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePositionWithSize;
import log.charter.io.rs.xml.song.ChordTemplate;
import log.charter.song.ArrangementChart;
import log.charter.song.Chord;
import log.charter.song.HandShape;
import log.charter.song.Level;
import log.charter.song.Note;
import log.charter.util.CollectionUtils.HashSet2;
import log.charter.util.IntRange;

public class GuitarDrawer {
	public static final int noteWidth = 23;
	public static final int noteTailOffset = noteWidth / 2;

	private static class DrawingData {
		private final static Color selectColor = ChartPanelColors.get(ColorLabel.SELECT);
		private static final Color[] noteColors = new Color[6];
		private static final Color[] noteTailColors = new Color[6];
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

		private final DrawableShapeList notes;
		private final DrawableShapeList noteTails;
		private final DrawableShapeList chordNotes;
		private final DrawableShapeList noteFrets;
		private final DrawableShapeList anchors;
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

			notes = new DrawableShapeList();
			noteTails = new DrawableShapeList();
			chordNotes = new DrawableShapeList();
			noteFrets = new DrawableShapeList();
			anchors = new DrawableShapeList();
			handShapes = new DrawableShapeList();
		}

		public void addNote(final Note note, final int x, final int length, final boolean selected) {
			final int y = stringPositions[note.string];
			final ShapePositionWithSize position = new ShapePositionWithSize(x, y, noteWidth, noteHeight)//
					.centered();
			notes.add(filledRectangle(position, noteColors[note.string]));
			notes.add(centeredTextWithBackground(new ShapePosition(x, y), "" + note.fret, Color.WHITE, Color.BLACK));

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

		public void addAnchor() {// TODO
//draw a number somewhere
		}

		public void addHandShape(final int x, final int length, final boolean selected) {
			final ShapePositionWithSize position = new ShapePositionWithSize(x, lanesBottom, length, 10);
			handShapes.add(filledRectangle(position, handShapeColor));

			if (selected) {
				handShapes.add(strokedRectangle(position, selectColor));
			}
		}

		public void draw(final Graphics g) {
			noteTails.draw(g);
			chordNotes.draw(g);

			g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
			notes.draw(g);
			noteFrets.draw(g);
			anchors.draw(g);
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

	private boolean initiated = false;

	private ChartData data;
	private ChartPanel chartPanel;
	private SelectionManager selectionManager;

	private final AudioDrawer audioDrawer = new AudioDrawer();
	private final BeatsDrawer beatsDrawer = new BeatsDrawer();

	public void init(final ChartData data, final ChartPanel chartPanel, final SelectionManager selectionManager) {
		this.data = data;
		this.chartPanel = chartPanel;
		this.selectionManager = selectionManager;

		audioDrawer.init(data, chartPanel);
		beatsDrawer.init(data, chartPanel);

		initiated = true;
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

	private void addSingleNotes(final DrawingData drawingData, final Level level, final int panelWidth) {
		final HashSet2<Integer> selectedNoteIds = selectionManager.getSelectedNotesSet()//
				.map(selection -> selection.id);

		for (int i = 0; i < level.notes.size(); i++) {
			final Note note = level.notes.get(i);
			final int x = timeToX(note.position, data.time);
			final int length = timeToXLength(note.sustain);
			if (isPastRightEdge(x, panelWidth)) {
				break;
			}

			if (!isOnScreen(x, length)) {
				continue;
			}

			final boolean selected = selectedNoteIds.contains(i);
			drawingData.addNote(note, x, length, selected);
			if (isTailVisible(length)) {
				drawingData.addNoteTail(note, x, length, selected);
			}
		}
	}

	private void addChords(final DrawingData drawingData, final ArrangementChart arrangement, final Level level,
			final int panelWidth) {
		final HashSet2<Integer> selectedChordIds = selectionManager.getSelectedChordsSet()//
				.map(selection -> selection.id);

		for (int i = 0; i < level.chords.size(); i++) {
			final Chord chord = level.chords.get(i);
			final int x = timeToX(chord.position, data.time);
			if (isPastRightEdge(x, panelWidth)) {
				break;
			}

			final boolean selected = selectedChordIds.contains(i);

			if (chord.chordNotes.isEmpty()) {
				if (!isOnScreen(x, 0)) {
					continue;
				}

				final ChordTemplate chordTemplate = arrangement.chordTemplates.get(chord.chordId);
				drawingData.addRepeatedChord(chord, chordTemplate, x, selected);
			} else {
				for (final Note note : chord.chordNotes) {
					final int length = timeToXLength(note.sustain);
					if (!isOnScreen(x, length)) {
						continue;
					}

					drawingData.addNote(note, x, length, selected);// TODO
					if (isTailVisible(length)) {
						drawingData.addNoteTail(note, x, length, selected);
					}
				}
			}
		}
	}

	private void addAnchors(final DrawingData drawingData, final ArrangementChart arrangement, final Level level,
			final int panelWidth) {
//TODO
	}

	private void addHandShapes(final DrawingData drawingData, final ArrangementChart arrangement, final Level level,
			final int panelWidth) {
		final HashSet2<Integer> selectedHandShapeIds = selectionManager.getSelectedHandShapesSet()//
				.map(selection -> selection.id);

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

		addSingleNotes(drawingData, level, panelWidth);
		addChords(drawingData, arrangement, level, panelWidth);
		addHandShapes(drawingData, arrangement, level, panelWidth);

		drawingData.draw(g);
	}

	private void drawDebugNoteId(final Graphics g) {
//		for (int i = 0; i < data.currentNotes.size(); i++) {
//			final Note n = data.currentNotes.get(i);
//			final int x = data.timeToX(n.pos);
//			if (x >= 0 && x < panel.getWidth()) {
//				g.setFont(new Font(Font.DIALOG, Font.PLAIN, 15));
//				g.drawString("" + i, x - 5, ChartPanel.beatTextY - 10);
//			}
//		}
	}

	public void draw(final Graphics g) {
		if (!initiated || data.isEmpty) {
			return;
		}

		beatsDrawer.draw(g);
		drawGuitarLanes(g);
		audioDrawer.draw(g);
		drawGuitarNotes(g);

		if (data.drawDebug) {
			drawDebugNoteId(g);
		}
	}
}
