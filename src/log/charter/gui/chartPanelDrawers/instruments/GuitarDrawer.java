package log.charter.gui.chartPanelDrawers.instruments;

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
import log.charter.gui.chartPanelDrawers.AudioDrawer;
import log.charter.gui.chartPanelDrawers.Drawer;
import log.charter.gui.chartPanelDrawers.drawableShapes.CenteredText;
import log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShapeList;
import log.charter.gui.chartPanelDrawers.drawableShapes.FilledRectangle;
import log.charter.io.rs.xml.song.Chord;
import log.charter.io.rs.xml.song.ChordTemplate;
import log.charter.song.ArrangementChart;
import log.charter.song.Level;
import log.charter.song.Note;

public class GuitarDrawer implements Drawer {
	private static int getAsOdd(final int x) {
		return x % 2 == 0 ? x + 1 : x;
	}

	private static int getLaneSize(final int lanes) {
		return getAsOdd((int) (ChartPanel.lanesHeight * 0.8 / lanes));
	}

	private static final int noteWidth = getAsOdd(23);
	private static final int noteXOffset = noteWidth / 2;

	private static class DrawingData {
		private final int strings;
		private final int noteHeight;
		private final int noteYOffset;
		private final int tailHeight;
		private final int tailYOffset;

		private final DrawableShapeList[] notes;
		private final DrawableShapeList[] noteTails;
		private final DrawableShapeList[] chordNotes;
		private final DrawableShapeList noteFrets;
		// TODO add note tails that are different shapes

		public DrawingData(final int strings) {
			this.strings = strings;
			noteHeight = getLaneSize(strings);
			noteYOffset = noteHeight / 2;
			tailHeight = getAsOdd(noteHeight * 3 / 4);
			tailYOffset = tailHeight / 2;

			notes = new DrawableShapeList[strings];
			noteTails = new DrawableShapeList[strings];
			chordNotes = new DrawableShapeList[strings];
			noteFrets = new DrawableShapeList();

			for (int i = 0; i < strings; i++) {
				notes[i] = new DrawableShapeList();
				noteTails[i] = new DrawableShapeList();
				chordNotes[i] = new DrawableShapeList();
			}
		}

		public void addNote(final Note note, final int x, final int length) {
			final int y = ChartPanel.getLaneY(note.string, strings);
			notes[note.string].add(new FilledRectangle(x - noteXOffset, y - noteYOffset, noteWidth, noteHeight));
			noteFrets.add(new CenteredText(x, y, "" + note.fret, Color.WHITE, Color.BLACK));
			if (length > noteXOffset) {
				noteTails[note.string]
						.add(new FilledRectangle(x + noteXOffset, y - tailYOffset, length - noteXOffset, tailHeight));
			}
		}

		public void addChordNote(final Note note, final int x) {
			final int y = ChartPanel.getLaneY(note.string, strings);
			chordNotes[note.string].add(new FilledRectangle(x - noteXOffset, y - noteYOffset, noteWidth, noteHeight));
			noteFrets.add(new CenteredText(x, y, "" + note.fret, Color.WHITE, Color.BLACK));
		}

		public void draw(final Graphics g) {
			for (int i = 0; i < strings; i++) {
				g.setColor(ChartPanelColors.get(ColorLabel.valueOf("NOTE_TAIL_" + i)));
				noteTails[i].draw(g);
				g.setColor(ChartPanelColors.get(ColorLabel.valueOf("NOTE_" + i)));
				notes[i].draw(g);
				g.setColor(new Color(50, 250, 250, 128));
				chordNotes[i].draw(g);
			}

			noteFrets.draw(g);
		}
	}

	private static void drawGuitarLanes(final Graphics g, final int lanes, final int width) {
		for (int i = 0; i < lanes; i++) {
			g.setColor(ChartPanelColors.get(ColorLabel.valueOf("LANE_" + i)));
			final int y = getLaneY(i, lanes);
			g.drawLine(0, y, width, y);
		}
	}

	private static boolean isPastRightEdge(final int x, final int width) {
		return x > (width + noteWidth / 2);
	}

	private static boolean isOnScreen(final int x, final int length) {
		return x + length >= 0;
	}

	private void addSingleNotes(final DrawingData drawingData, final Level level, final int panelWidth,
			final int time) {
		for (final Note note : level.notes) {
			final int x = timeToX(note.position, time);
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
			final int panelWidth, final int time) {
		for (final Chord chord : level.chords) {
			final int x = timeToX(chord.position, time);
			if (isPastRightEdge(x, panelWidth)) {
				break;
			}

			final ChordTemplate chordTemplate = arrangement.chordTemplates.get(chord.chordId);
			if (chord.chordNotes.isEmpty()) {
				if (!isOnScreen(x, 0)) {
					continue;
				}

				for (final Entry<Integer, Integer> chordFret : chordTemplate.frets.entrySet()) {
					final int string = chordFret.getKey();
					final int fret = chordFret.getValue();
					final Note note = new Note(0, string, fret);
					note.mute = chord.fretHandMute;
					note.palmMute = chord.palmMute;
					note.accent = chord.accent;
					drawingData.addChordNote(note, x);
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

	private void drawGuitarNotes(final Graphics g, final ChartPanel panel, final ChartData data) {
		final Level level = data.getCurrentArrangementLevel();
		final ArrangementChart arrangement = data.getCurrentArrangement();
		final int strings = data.getCurrentArrangement().tuning.strings;
		final DrawingData drawingData = new DrawingData(strings);

		final int panelWidth = panel.getWidth();

		addSingleNotes(drawingData, level, panelWidth, data.time);
		addChords(drawingData, arrangement, level, panelWidth, data.time);

		drawingData.draw(g);
	}

	private void drawDebugNoteId(final Graphics g, final ChartPanel panel, final ChartData data) {
//		for (int i = 0; i < data.currentNotes.size(); i++) {
//			final Note n = data.currentNotes.get(i);
//			final int x = data.timeToX(n.pos);
//			if (x >= 0 && x < panel.getWidth()) {
//				g.setFont(new Font(Font.DIALOG, Font.PLAIN, 15));
//				g.drawString("" + i, x - 5, ChartPanel.beatTextY - 10);
//			}
//		}
	}

	AudioDrawer audioDrawer = new AudioDrawer();

	@Override
	public void draw(final Graphics g, final ChartPanel panel, final ChartData data) {
		if (data.isEmpty) {
			return;
		}

		drawGuitarLanes(g, data.getCurrentArrangement().tuning.strings, panel.getWidth());
		audioDrawer.draw(g, panel, data);
		drawGuitarNotes(g, panel, data);

		if (data.drawDebug) {
			drawDebugNoteId(g, panel, data);
		}
	}
}
