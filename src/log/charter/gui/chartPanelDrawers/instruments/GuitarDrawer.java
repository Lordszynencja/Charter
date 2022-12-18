package log.charter.gui.chartPanelDrawers.instruments;

import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.anchorTextY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.anchorY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.getAsOdd;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.getLaneSize;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.getLaneY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.centeredImage;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.centeredTextWithBackground;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledRectangle;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledTriangle;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.lineVertical;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.strokedRectangle;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.strokedTriangle;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.text;
import static log.charter.util.ScalingUtils.timeToX;
import static log.charter.util.ScalingUtils.timeToXLength;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import log.charter.data.ChartData;
import log.charter.data.managers.selection.ChordOrNote;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.gui.ChartPanel;
import log.charter.gui.ChartPanelColors;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.chartPanelDrawers.common.AudioDrawer;
import log.charter.gui.chartPanelDrawers.common.BeatsDrawer;
import log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShapeList;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePositionWithSize;
import log.charter.gui.handlers.KeyboardHandler;
import log.charter.io.rs.xml.song.ChordTemplate;
import log.charter.song.Anchor;
import log.charter.song.ArrangementChart;
import log.charter.song.Chord;
import log.charter.song.HandShape;
import log.charter.song.Level;
import log.charter.song.Note;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashSet2;
import log.charter.util.Position2D;

public class GuitarDrawer {
	public static final int noteWidth = 23;
	public static final int noteTailOffset = noteWidth / 2 + 1;

	private static class DrawingData {
		private static BufferedImage loadImage(final String path) {
			try {
				return ImageIO.read(new File(path));
			} catch (final IOException e) {
				e.printStackTrace();
				return new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
			}
		}

		private static final BufferedImage palmMuteMarker = loadImage("images/palmMute.png");

		private final static Color selectColor = ChartPanelColors.get(ColorLabel.SELECT);
		private static final Color[] noteColors = new Color[6];
		private static final Color[] noteTailColors = new Color[6];
		private static final Color anchorColor = ChartPanelColors.get(ColorLabel.ANCHOR);
		private static final Color handShapeColor = ChartPanelColors.get(ColorLabel.HAND_SHAPE);

		static {
			for (int i = 0; i < 6; i++) {
				noteColors[i] = ChartPanelColors.get(ColorLabel.valueOf("NOTE_" + i));
				noteTailColors[i] = ChartPanelColors.get(ColorLabel.valueOf("NOTE_TAIL_" + i));
			}
		}

		private static class NoteData {
			public static ArrayList2<NoteData> fromChord(final Chord chord, final ChordTemplate chordTemplate,
					final int x, final int length, final boolean selected, final boolean ctrl) {
				final ArrayList2<NoteData> notes = new ArrayList2<>();
				for (final Entry<Integer, Integer> chordFret : chordTemplate.frets.entrySet()) {
					final int string = chordFret.getKey();
					final int fret = chordFret.getValue();
					final String fretDescription = fret + (ctrl ? "(" + chordTemplate.fingers.get(string) + ")" : "");

					notes.add(new NoteData(x, length, string, fretDescription, chord.fretHandMute, chord.palmMute,
							false, false, selected));
				}

				return notes;
			}

			public final int x;
			public final int length;

			public final int string;
			public final String fret;
			public final boolean fretHandMute;
			public final boolean palmMute;
			public final boolean hammerOn;
			public final boolean pullOff;

			public final boolean selected;

			public NoteData(final int x, final int length, final Note note, final boolean selected) {
				this(x, length, note.string, note.fret + "", note.mute, note.palmMute, note.hammerOn, note.pullOff,
						selected);
			}

			private NoteData(final int x, final int length, final int string, final String fret,
					final boolean fretHandMute, final boolean palmMute, final boolean hammerOn, final boolean pullOff,
					final boolean selected) {
				this.x = x;
				this.length = length;

				this.string = string;
				this.fret = fret + (fretHandMute ? "X" : "");
				this.fretHandMute = fretHandMute;
				this.palmMute = palmMute;
				this.hammerOn = hammerOn;
				this.pullOff = pullOff;

				this.selected = selected;
			}
		}

		private final int[] stringPositions;
		private final int noteHeight;
		private final int tailHeight;

		private final DrawableShapeList anchors;
		private final DrawableShapeList noteTails;
		private final DrawableShapeList noteTailSelects;
		private final DrawableShapeList notes;
		private final DrawableShapeList noteFrets;
		private final DrawableShapeList handShapes;
		private final DrawableShapeList selects;
		// TODO add note tails that are different shapes

		public DrawingData(final int strings) {
			stringPositions = new int[strings];
			for (int i = 0; i < strings; i++) {
				stringPositions[i] = getLaneY(i, strings);
			}
			noteHeight = getLaneSize(6);
			tailHeight = getAsOdd(noteHeight * 3 / 4);

			anchors = new DrawableShapeList();
			noteTails = new DrawableShapeList();
			noteTailSelects = new DrawableShapeList();
			notes = new DrawableShapeList();
			noteFrets = new DrawableShapeList();
			selects = new DrawableShapeList();
			handShapes = new DrawableShapeList();
		}

		private void addNormalNoteShape(final int y, final NoteData note) {
			final ShapePositionWithSize position = new ShapePositionWithSize(note.x, y, noteWidth, noteHeight)//
					.centered();
			notes.add(filledRectangle(position, noteColors[note.string]));

			if (note.selected) {
				selects.add(strokedRectangle(position.resized(-1, -1, 1, 1), selectColor));
			}
		}

		private void addHammerOnShape(final int y, final NoteData note) {
			final Position2D a = new Position2D(note.x, y - noteHeight / 2);
			final Position2D b = new Position2D(note.x - noteWidth / 2, y + noteHeight / 2);
			final Position2D c = new Position2D(note.x + noteWidth / 2, y + noteHeight / 2);
			notes.add(filledTriangle(a, b, c, noteColors[note.string]));

			if (note.selected) {
				selects.add(strokedTriangle(a, b, c, selectColor));
			}
		}

		private void addPullOffShape(final int y, final NoteData note) {
			final Position2D a = new Position2D(note.x, y + noteHeight / 2);
			final Position2D b = new Position2D(note.x - noteWidth / 2, y - noteHeight / 2);
			final Position2D c = new Position2D(note.x + noteWidth / 2, y - noteHeight / 2);
			notes.add(filledTriangle(a, b, c, noteColors[note.string]));

			if (note.selected) {
				selects.add(strokedTriangle(a, b, c, selectColor));
			}
		}

		private void addSimpleNote(final NoteData note) {
			final int y = stringPositions[note.string];
			if (note.hammerOn) {
				addHammerOnShape(y, note);
			} else if (note.pullOff) {
				addPullOffShape(y, note);
			} else {
				addNormalNoteShape(y, note);
			}

			if (note.palmMute) {
				notes.add(centeredImage(new Position2D(note.x, y), palmMuteMarker));
			}

			noteFrets.add(centeredTextWithBackground(new Position2D(note.x, y), note.fret, Color.WHITE,
					note.fretHandMute ? Color.GRAY : Color.BLACK));

			if (isTailVisible(note.length)) {
				addNoteTail(note.string, note.x, note.length, note.selected);
			}
		}

		private void addNoteTail(final int string, final int x, final int length, final boolean selected) {
			final ShapePositionWithSize position = new ShapePositionWithSize(x, stringPositions[string], length,
					tailHeight).centeredY();
			noteTails.add(filledRectangle(position, noteTailColors[string]));

			if (selected) {
				noteTailSelects.add(strokedRectangle(position.resized(0, -1, 0, 1), selectColor));
			}
		}

		public void addNote(final Note note, final int x, final int length, final boolean selected) {
			addSimpleNote(new NoteData(x, length, note, selected));
		}

		public void addChord(final Chord chord, final ChordTemplate chordTemplate, final int x, final int length,
				final boolean selected, final boolean ctrl) {
			for (final NoteData noteData : NoteData.fromChord(chord, chordTemplate, x, length, selected, ctrl)) {
				addSimpleNote(noteData);
			}
		}

		public void addAnchor(final Anchor anchor, final int x, final boolean selected) {
			anchors.add(lineVertical(x, anchorY, lanesBottom, anchorColor));
			anchors.add(text(new Position2D(x + 4, anchorTextY), "" + anchor.fret, anchorColor));

			if (selected) {
				final int top = anchorY - 1;
				final int bottom = lanesBottom + 1;
				final ShapePositionWithSize anchorPosition = new ShapePositionWithSize(x - 1, top, 2, bottom - top);
				selects.add(strokedRectangle(anchorPosition, selectColor));
			}
		}

		public void addHandShape(final int x, final int length, final boolean selected) {
			final ShapePositionWithSize position = new ShapePositionWithSize(x, lanesBottom, length, 10);
			handShapes.add(filledRectangle(position, handShapeColor));

			if (selected) {
				selects.add(strokedRectangle(position, selectColor));
			}
		}

		public void draw(final Graphics g) {
			g.setFont(new Font(Font.DIALOG, Font.BOLD, 13));
			anchors.draw(g);
			noteTails.draw(g);
			noteTailSelects.draw(g);
			notes.draw(g);

			g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
			noteFrets.draw(g);
			handShapes.draw(g);
			selects.draw(g);
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
	private KeyboardHandler keyboardHandler;
	private SelectionManager selectionManager;

	public void init(final AudioDrawer audioDrawer, final BeatsDrawer beatsDrawer, final ChartData data,
			final ChartPanel chartPanel, final KeyboardHandler keyboardHandler,
			final SelectionManager selectionManager) {
		this.audioDrawer = audioDrawer;
		this.beatsDrawer = beatsDrawer;
		this.data = data;
		this.chartPanel = chartPanel;
		this.keyboardHandler = keyboardHandler;
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

		final int length = timeToXLength(chord.length);
		if (!isOnScreen(x, length)) {
			return true;
		}

		final ChordTemplate chordTemplate = arrangement.chordTemplates.get(chord.chordId);
		drawingData.addChord(chord, chordTemplate, x, length, selected, keyboardHandler.ctrl());
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
