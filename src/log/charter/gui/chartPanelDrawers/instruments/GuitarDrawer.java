package log.charter.gui.chartPanelDrawers.instruments;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.gui.ChartPanel.getLaneY;
import static log.charter.util.ScalingUtils.timeToX;
import static log.charter.util.ScalingUtils.timeToXLength;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Map.Entry;

import log.charter.data.ChartData;
import log.charter.gui.ChartPanel;
import log.charter.gui.ChartPanelColors;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.chartPanelDrawers.common.AudioDrawer;
import log.charter.gui.chartPanelDrawers.common.BeatsDrawer;
import log.charter.gui.chartPanelDrawers.drawableShapes.CenteredTextWithBackground;
import log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShapeList;
import log.charter.gui.chartPanelDrawers.drawableShapes.FilledRectangle;
import log.charter.io.rs.xml.song.ChordTemplate;
import log.charter.song.ArrangementChart;
import log.charter.song.Chord;
import log.charter.song.HandShape;
import log.charter.song.Level;
import log.charter.song.Note;

public class GuitarDrawer {
	private static int getAsOdd(final int x) {
		return x % 2 == 0 ? x + 1 : x;
	}

	private static int getLaneSize(final int lanes) {
		return getAsOdd((int) (ChartPanel.lanesHeight * 0.8 / lanes));
	}

	private static final int noteWidth = getAsOdd(23);
	private static final int noteXOffset = noteWidth / 2;

	private static class DrawingData {
		private static final Color[] noteColors = new Color[6];
		private static final Color[] noteTailColors = new Color[6];
		private static final Color repeatedChordColor = ChartPanelColors.get(ColorLabel.REPEATED_CHORD);

		static {
			for (int i = 0; i < 6; i++) {
				noteColors[i] = ChartPanelColors.get(ColorLabel.valueOf("NOTE_" + i));
				noteTailColors[i] = ChartPanelColors.get(ColorLabel.valueOf("NOTE_TAIL_" + i));
			}
		}

		private final int strings;
		private final int noteHeight;
		private final int noteYOffset;
		private final int tailHeight;
		private final int tailYOffset;

		private final DrawableShapeList notes;
		private final DrawableShapeList noteTails;
		private final DrawableShapeList chordNotes;
		private final DrawableShapeList noteFrets;
		private final DrawableShapeList anchors;
		private final DrawableShapeList handShapes;
		// TODO add note tails that are different shapes

		public DrawingData(final int strings) {
			this.strings = strings;
			noteHeight = getLaneSize(strings);
			noteYOffset = noteHeight / 2;
			tailHeight = getAsOdd(noteHeight * 3 / 4);
			tailYOffset = tailHeight / 2;

			notes = new DrawableShapeList();
			noteTails = new DrawableShapeList();
			chordNotes = new DrawableShapeList();
			noteFrets = new DrawableShapeList();
			anchors = new DrawableShapeList();
			handShapes = new DrawableShapeList();
		}

		public void addNote(final Note note, final int x, final int length) {
			final int y = getLaneY(note.string, strings);
			notes.add(new FilledRectangle(x, y, noteWidth, noteHeight, noteColors[note.string]).centered());
			noteFrets.add(new CenteredTextWithBackground(x, y, "" + note.fret, Color.WHITE, Color.BLACK));
			if (length > noteXOffset) {
				noteTails.add(new FilledRectangle(x + noteXOffset, y - tailYOffset, length - noteXOffset, tailHeight,
						noteTailColors[note.string]));
			}
		}

		public void addRepeatedChord(final Chord chord, final ChordTemplate chordTemplate, final int x) {
			int minString = 6;
			int maxString = 0;
			for (final int string : chordTemplate.frets.keySet()) {
				minString = min(minString, string);
				maxString = max(maxString, string);
			}
			int yTop = getLaneY(maxString, strings);
			int yBottom = getLaneY(minString, strings);
			if (yTop < yBottom) {
				final int tmp = yTop;
				yTop = yBottom;
				yBottom = tmp;
			}
			yTop += noteYOffset;
			yBottom -= noteYOffset;

			chordNotes.add(
					new FilledRectangle(x - noteXOffset, yBottom, noteWidth, yTop - yBottom + 1, repeatedChordColor));
		}

		public void addAnchor() {

		}

		public void addHandShape(final int x, final int length) {
			handShapes.add(new FilledRectangle(x, ChartPanel.lanesBottom, length, 10, new Color(0, 128, 255, 255)));
		}

		public void draw(final Graphics g) {
			noteTails.draw(g);
			chordNotes.draw(g);
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

	private boolean initiated = false;

	private ChartData data;
	private ChartPanel chartPanel;

	private final AudioDrawer audioDrawer = new AudioDrawer();
	private final BeatsDrawer beatsDrawer = new BeatsDrawer();

	public void init(final ChartData data, final ChartPanel chartPanel) {
		this.data = data;
		this.chartPanel = chartPanel;

		audioDrawer.init(data, chartPanel);
		beatsDrawer.init(data, chartPanel);

		initiated = true;
	}

	private void drawGuitarLanes(final Graphics g) {
		final int lanes = data.getCurrentArrangement().tuning.strings;
		final int width = chartPanel.getWidth();

		for (int i = 0; i < lanes; i++) {
			g.setColor(ChartPanelColors.get(ColorLabel.valueOf("LANE_" + i)));
			final int y = getLaneY(i, lanes);
			g.drawLine(0, y, width, y);
		}
	}

	private void addSingleNotes(final DrawingData drawingData, final Level level, final int panelWidth) {
		for (final Note note : level.notes) {
			final int x = timeToX(note.position, data.time);
			final int length = timeToXLength(note.sustain);
			if (isPastRightEdge(x, panelWidth)) {
				break;
			}

			if (!isOnScreen(x, length)) {
				continue;
			}

			drawingData.addNote(note, x, length);
		}
	}

	private void addChords(final DrawingData drawingData, final ArrangementChart arrangement, final Level level,
			final int panelWidth) {
		for (final Chord chord : level.chords) {
			final int x = timeToX(chord.position, data.time);
			if (isPastRightEdge(x, panelWidth)) {
				break;
			}

			final ChordTemplate chordTemplate = arrangement.chordTemplates.get(chord.chordId);
			if (chord.chordNotes.isEmpty()) {
				if (!isOnScreen(x, 0)) {
					continue;
				}

				drawingData.addRepeatedChord(chord, chordTemplate, x);
				for (final Entry<Integer, Integer> chordFret : chordTemplate.frets.entrySet()) {
					final int string = chordFret.getKey();
					final int fret = chordFret.getValue();
					final Note note = new Note(0, string, fret);
					note.mute = chord.fretHandMute;
					note.palmMute = chord.palmMute;
					note.accent = chord.accent;
				}
			} else {
				for (final Note note : chord.chordNotes) {
					final int length = timeToXLength(note.sustain);
					if (!isOnScreen(x, length)) {
						continue;
					}
					drawingData.addNote(note, x, length);
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
		for (final HandShape handShape : level.handShapes) {
			final int x = timeToX(handShape.position, data.time);
			if (isPastRightEdge(x, panelWidth)) {
				break;
			}

			final int length = timeToXLength(handShape.length);
			if (!isOnScreen(x, length)) {
				continue;
			}

			drawingData.addHandShape(x, length);
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
