package log.charter.gui.components.preview3D;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.sin;
import static log.charter.gui.ChartPanelColors.getStringBasedColor;
import static log.charter.gui.components.preview3D.Matrix4.moveMatrix;
import static log.charter.gui.components.preview3D.Matrix4.scaleMatrix;
import static log.charter.gui.components.preview3D.Preview3DUtils.getChartboardYPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getFretMiddlePosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getFretPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getStringPositionWithBend;
import static log.charter.gui.components.preview3D.Preview3DUtils.getTimePosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getTopStringYPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.visibility;
import static log.charter.song.notes.IPosition.findFirstAfterEqual;
import static log.charter.song.notes.IPosition.findFirstIdAfterEqual;
import static log.charter.song.notes.IPosition.findLastBeforeEqual;
import static log.charter.song.notes.IPosition.findLastIdBeforeEqual;
import static log.charter.song.notes.IPositionWithLength.findLastIdBefore;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.lwjgl.opengl.GL30;

import log.charter.data.ChartData;
import log.charter.gui.ChartPanelColors.StringColorLabelType;
import log.charter.gui.chartPanelDrawers.instruments.guitar.NoteData;
import log.charter.gui.components.preview3D.BaseShader.BaseShaderDrawData;
import log.charter.gui.components.preview3D.shapes.CompositeModel;
import log.charter.gui.components.preview3D.shapes.FrettedNoteModel;
import log.charter.gui.components.preview3D.shapes.Model;
import log.charter.gui.components.preview3D.shapes.NoteStatusModels;
import log.charter.gui.components.preview3D.shapes.OpenNoteModel;
import log.charter.song.Anchor;
import log.charter.song.BendValue;
import log.charter.song.ChordTemplate;
import log.charter.song.HandShape;
import log.charter.song.enums.HOPO;
import log.charter.song.enums.Harmonic;
import log.charter.song.notes.Chord;
import log.charter.song.notes.ChordOrNote;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.Pair;

public class Preview3DGuitarSoundsDrawer {
	private static final int anticipationWindow = 1_000;
	private static final int highlightWindow = 50;

	private ChartData data;

	private final static Map<Integer, Map<Integer, CompositeModel>> openNoteModels = new HashMap<>();

	private static CompositeModel getOpenNoteModel(final int fret0, final int fret1) {
		if (openNoteModels.get(fret0) == null) {
			openNoteModels.put(fret0, new HashMap<>());
		}
		if (openNoteModels.get(fret0).get(fret1) == null) {
			final double width = getFretPosition(fret1) - getFretPosition(fret0);
			openNoteModels.get(fret0).put(fret1, new OpenNoteModel(width));
		}

		return openNoteModels.get(fret0).get(fret1);
	}

	public void init(final ChartData data) {
		this.data = data;
	}

	private Anchor findAnchorForPosition(final int position) {
		final Anchor anchor = findLastBeforeEqual(data.getCurrentArrangementLevel().anchors, position);
		return anchor == null ? new Anchor(0, 0) : anchor;
	}

//
//	private void drawNoteAnticipation(final Drawable3DShapesListForScene shapesList, final NoteData note) {
//		final Color color = ColorLabel.valueOf("LANE_" + note.string).color();
//		final int dt = data.time + anticipationWindow - note.position;
//		final double y = getStringPositionWithBend(note.string, note.prebend);
//		double scale = 1.0 * dt / anticipationWindow;
//		scale *= scale;
//
//		if (note.fretNumber == 0) {
//			final Anchor anchor = findAnchorForPosition(note.position);
//			final double x0 = getFretPosition(anchor.fret - 1);
//			final double x1 = getFretPosition(anchor.fret + anchor.width - 1);
//			final double y0 = y - 0.05 * scale;
//			final double y1 = y + 0.05 * scale;
//			shapesList.addRectangleZ(color, -1.1, x0, x1, y0, y1, fretboardZ);
//		} else {
//			final double x = getFretMiddlePosition(note.fretNumber);
//			final double x0 = x - 0.3 * scale;
//			final double x1 = x + 0.3 * scale;
//			final double y0 = y - 0.15 * scale;
//			final double y1 = y + 0.15 * scale;
//			final double z0 = fretboardZ - 0.15 * scale;
//			final double z1 = fretboardZ + 0.15 * scale;
//			shapesList.addRectangleX(color, -1.1, x0, y0, y1, z0, z1);
//			shapesList.addRectangleX(color, -1.1, x1, y0, y1, z0, z1);
//			shapesList.addRectangleY(color, -1.1, x0, x1, y0, z0, z1);
//			shapesList.addRectangleY(color, -1.1, x0, x1, y1, z0, z1);
//		}
//	}
//
	private double getNoteHeightAtTime(final NoteData note, final int t) {
		final int dt = t - note.position;

		double bendValue = 0;
		if (!note.bendValues.isEmpty()) {
			final int lastBendId = findLastIdBeforeEqual(note.bendValues, dt);
			double bendAValue = 0;
			int bendAPosition = 0;
			if (lastBendId != -1) {
				final BendValue bend = note.bendValues.get(lastBendId);
				bendAValue = bend.bendValue.doubleValue();
				bendAPosition = bend.position();
			}
			if (lastBendId < note.bendValues.size() - 1) {
				final BendValue nextBend = note.bendValues.get(lastBendId + 1);
				final double bendBValue = nextBend.bendValue.doubleValue();
				final int bendBPosition = nextBend.position();
				final double scale = 1.0 * (dt - bendAPosition) / (bendBPosition - bendAPosition);
				bendValue = bendAValue * (1 - scale) + (bendBValue) * scale;
			} else {
				bendValue = bendAValue;
			}
		}

		if (note.vibrato != null) {
			bendValue += sin(dt * Math.PI * note.vibrato / 10000) * 0.2;
		}
		final double value = getStringPositionWithBend(note.string, data.currentStrings(), bendValue);

		return value;
	}

//
//	private void drawNoteHold(final Drawable3DShapesListForScene shapesList, final NoteData note) {
//		final Color color = ColorLabel.valueOf("LANE_" + note.string).color();
//
//		final double y = getNotePositionAtTime(note, data.time);
//
//		if (note.fretNumber == 0) {
//			final Anchor anchor = findAnchorForPosition(note.position);
//			final double x0 = getFretPosition(anchor.fret - 1);
//			final double x1 = getFretPosition(anchor.fret + anchor.width - 1);
//			final double y0 = y - 0.05;
//			final double y1 = y + 0.05;
//			shapesList.addRectangleZ(color, -1.1, x0, x1, y0, y1, fretboardZ);
//		} else {
//			final double x = getFretMiddlePosition(note.fretNumber);
//			final double x0 = x - 0.3;
//			final double x1 = x + 0.3;
//			final double y0 = y - 0.15;
//			final double y1 = y + 0.15;
//			final double z0 = fretboardZ - 0.15;
//			final double z1 = fretboardZ + 0.15;
//			shapesList.addRectangleX(color, -1.1, x0, y0, y1, z0, z1);
//			shapesList.addRectangleX(color, -1.1, x1, y0, y1, z0, z1);
//			shapesList.addRectangleY(color, -1.1, x0, x1, y0, z0, z1);
//			shapesList.addRectangleY(color, -1.1, x0, x1, y1, z0, z1);
//		}
//	}
//

	private void drawChordShadow(final BaseShader baseShader, final int position) {
		final Anchor anchor = findAnchorForPosition(position);
		final double x0 = getFretPosition(anchor.fret - 1);
		final double x2 = getFretPosition(anchor.topFret());
		final double y0 = getChartboardYPosition(data.currentStrings());
		final double y1 = getTopStringYPosition();
		final double z = max(0, getTimePosition(position - data.time));

		final Point3D p00 = new Point3D(x0, y0, z);
		final Point3D p01 = new Point3D((x2 + x0) / 2, y0, z);
		final Point3D p02 = new Point3D(x2, y0, z);
		final Point3D p10 = new Point3D(x0, y1, z);
		final Point3D p12 = new Point3D(x2, y1, z);
		final Color color = new Color(0, 192, 255);
		final Color shadowInvisibleColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);

		baseShader.clearModelMatrix();
		final BaseShaderDrawData shadowDrawData = baseShader.new BaseShaderDrawData();
		shadowDrawData.addVertex(p00, color)//
				.addVertex(p01, shadowInvisibleColor)//
				.addVertex(p10, shadowInvisibleColor)//
				.addVertex(p02, color)//
				.addVertex(p01, shadowInvisibleColor)//
				.addVertex(p12, shadowInvisibleColor)//
				.draw(GL30.GL_TRIANGLES);
	}

	private void drawNoteShadow(final BaseShader baseShader, final double x, final double y, final double z,
			final Color color) {
		final double shadowBaseY = getChartboardYPosition(data.currentStrings());
		final Point3D shadowBaseP0 = new Point3D(x - 0.02, shadowBaseY, z);
		final Point3D shadowBaseP1 = new Point3D(x, shadowBaseY, z);
		final Point3D shadowBaseP2 = new Point3D(x + 0.02, shadowBaseY, z);
		final Point3D shadowP3 = new Point3D(x, y - 0.3, z);
		final Color shadowInvisibleColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);

		baseShader.clearModelMatrix();
		final BaseShaderDrawData shadowDrawData = baseShader.new BaseShaderDrawData();
		shadowDrawData.addVertex(shadowBaseP1, color)//
				.addVertex(shadowBaseP0, shadowInvisibleColor)//
				.addVertex(shadowP3, shadowInvisibleColor)//
				.addVertex(shadowBaseP2, shadowInvisibleColor)//
				.draw(GL30.GL_TRIANGLE_FAN);
	}

	private void drawHarmonic(final BaseShader baseShader, final double x, final double y, final double z,
			final Harmonic harmonic) {
		baseShader.setModelMatrix(moveMatrix(x, y, z));
		final Model model = NoteStatusModels.harmonicsModels.get(harmonic);
		final Color color = NoteStatusModels.harmonicsColors.get(harmonic).color();
		baseShader.drawModel(model, color);
	}

	private void drawHOPO(final BaseShader baseShader, final double x, final double y, final double z,
			final HOPO hopo) {
		baseShader.setModelMatrix(moveMatrix(x, y, z));
		final Model model = NoteStatusModels.hoposModels.get(hopo);
		final Color color = NoteStatusModels.hoposColors.get(hopo).color();
		baseShader.drawModel(model, color);
	}

	private void drawNoteHead(final BaseShader baseShader, final NoteData note, final boolean hit,
			final boolean chordPart) {
		if (note.linkPrevious) {
			return;
		}

		Color color = getStringBasedColor(StringColorLabelType.NOTE, note.string, data.currentStrings());
		if (hit) {
			color = color.brighter();
		}

		final double y = getStringPositionWithBend(note.string, data.currentStrings(), note.prebend);
		final double z = getTimePosition(note.position - data.time);
		if (note.fretNumber == 0) {
			final Anchor anchor = findAnchorForPosition(note.position);
			final double x = getFretPosition(anchor.fret - 1);
			baseShader.setModelMatrix(moveMatrix(x, y, z));
			for (final Pair<Integer, List<Point3D>> points : getOpenNoteModel(anchor.fret - 1, anchor.topFret())
					.getPointsForModes()) {
				final BaseShaderDrawData drawData = baseShader.new BaseShaderDrawData();
				for (final Point3D point : points.b) {
					drawData.addVertex(point, color);
				}
				drawData.draw(points.a);
			}

			final double middleX = (getFretPosition(anchor.topFret()) + x) / 2;
			if (note.hopo != HOPO.NONE) {
				drawHOPO(baseShader, middleX, y, z, note.hopo);
			}
			if (note.harmonic != Harmonic.NONE) {
				drawHarmonic(baseShader, middleX, y, z, note.harmonic);
			}

			return;
		}

		final double x = getFretMiddlePosition(note.fretNumber);

		if (!note.accent) {
			baseShader.setModelMatrix(Matrix4.moveMatrix(x, y, z));
			final BaseShaderDrawData drawData = baseShader.new BaseShaderDrawData();
			drawData.addModel(FrettedNoteModel.instance, color);
			drawData.draw(FrettedNoteModel.instance.getDrawMode());
		} else {
			final Color accentColor = getStringBasedColor(StringColorLabelType.NOTE_ACCENT, note.string,
					data.currentStrings());
			baseShader.setModelMatrix(moveMatrix(x, y, z).multiply(scaleMatrix(1.1, 1.1, 1)));
			final BaseShaderDrawData drawData = baseShader.new BaseShaderDrawData();
			drawData.addModel(FrettedNoteModel.instance, accentColor);
			drawData.draw(FrettedNoteModel.instance.getDrawMode());
		}

		// TODO mutes
		if (note.hopo != HOPO.NONE) {
			drawHOPO(baseShader, x, y, z, note.hopo);
		}
		if (note.harmonic != Harmonic.NONE) {
			drawHarmonic(baseShader, x, y, z, note.harmonic);
		}

		if (!hit && !chordPart) {
			drawNoteShadow(baseShader, x, y, z, color);
		}
	}

	private void drawNoteTail(final BaseShader baseShader, final NoteData note) {
		if (note.length < 10) {
			return;
		}

		final double x0;
		final double x1;

		if (note.fretNumber == 0) {
			final Anchor anchor = findAnchorForPosition(note.position);
			x0 = getFretPosition(anchor.fret - 1);
			x1 = getFretPosition(anchor.fret + anchor.width - 1);
		} else {
			final double x = getFretMiddlePosition(note.fretNumber);
			x0 = x - FrettedNoteModel.width / 3;
			x1 = x + FrettedNoteModel.width / 3;
		}

		final BaseShaderDrawData drawData = baseShader.new BaseShaderDrawData();

		final Color color = getStringBasedColor(StringColorLabelType.NOTE_TAIL, note.string, data.currentStrings());

		final int tremoloSize = 100;
		for (int t = max(data.time, note.position); t <= note.position + note.length
				&& t < data.time + visibility; t += 1) {
			final double y = getNoteHeightAtTime(note, t);
			final double z = getTimePosition(t - data.time);

			if (note.tremolo) {
				final double xOffset = -0.05 * abs((double) (t % tremoloSize) / tremoloSize - 0.5);
				drawData.addVertex(new Point3D(x0 + xOffset, y, z), color)//
						.addVertex(new Point3D(x1 + xOffset, y, z), color);
			} else {
				drawData.addVertex(new Point3D(x0, y, z), color)//
						.addVertex(new Point3D(x1, y, z), color);
			}
		}

		baseShader.clearModelMatrix();
		drawData.draw(GL30.GL_TRIANGLE_STRIP);
		// TODO slides
	}

	private void drawNote(final BaseShader baseShader, final NoteData note, final boolean chordPart) {
		if (!note.linkPrevious && note.position <= data.time + anticipationWindow && note.position >= data.time) {
			// drawNoteAnticipation(baseShader, note);
		}

		if (note.position >= data.time) {
			drawNoteHead(baseShader, note, false, chordPart);
		} else if (!note.linkPrevious && note.position >= data.time - highlightWindow
				&& note.length < highlightWindow) {
			final int dt = data.time - note.position;
			note.position += dt;
			drawNoteHead(baseShader, note, true, chordPart);
			note.position -= dt;
		} else if (note.position + note.length >= data.time) {
			// drawNoteHold(baseShader, note);
		}

		drawNoteTail(baseShader, note);
	}

	private void drawSingleNote(final BaseShader baseShader, final ChordOrNote sound, final boolean lastWasLinkNext) {
		final NoteData noteData = new NoteData(0, sound.note.length(), sound.note, false, lastWasLinkNext);
		drawNote(baseShader, noteData, false);
	}

	private boolean isFirstChordInTheHandShape(final Chord chord) {
		final HandShape chordHandShape = findLastBeforeEqual(data.getCurrentArrangementLevel().handShapes,
				chord.position());
		if (chordHandShape == null || chordHandShape.chordId != chord.chordId
				|| chordHandShape.endPosition() < chord.position()) {
			return true;
		}

		final ArrayList2<Chord> chords = data.getCurrentArrangementLevel().chordsAndNotes.stream()//
				.filter(s -> s.isChord() && s.chord.chordId == chordHandShape.chordId)//
				.map(s -> s.chord)//
				.collect(Collectors.toCollection(ArrayList2::new));
		final Chord firstInHandShape = findFirstAfterEqual(chords, chordHandShape.position());
		return firstInHandShape.position() == chord.position();
	}

	private void drawChord(final BaseShader baseShader, final ChordOrNote sound, final boolean lastWasLinkNext) {
		final Chord chord = sound.chord;
		if (isFirstChordInTheHandShape(chord)) {
			final ChordTemplate chordTemplate = data.getCurrentArrangement().chordTemplates.get(chord.chordId);
			final ArrayList2<NoteData> notes = NoteData.fromChord(chord, chordTemplate, 0, sound.chord.length(), false,
					lastWasLinkNext, false);

			for (int j = notes.size() - 1; j >= 0; j--) {
				drawNote(baseShader, notes.get(j), true);
			}
		}

		if (!lastWasLinkNext && chord.position() + highlightWindow > data.time) {
			drawChordShadow(baseShader, chord.position());
		}
	}

	public void draw(final BaseShader baseShader) {
		final ArrayList2<ChordOrNote> sounds = data.getCurrentArrangementLevel().chordsAndNotes;

		int soundsFrom = findFirstIdAfterEqual(sounds, data.time - highlightWindow);
		if (soundsFrom == -1) {
			soundsFrom = 0;
		}
		final int soundsTo = findLastIdBefore(sounds, data.time + visibility);

		for (int i = soundsTo; i >= soundsFrom; i--) {
			final ChordOrNote sound = sounds.get(i);
			final boolean lastWasLinkNext = i > 0 && sounds.get(i - 1).asGuitarSound().linkNext;

			if (sound.isNote()) {
				drawSingleNote(baseShader, sound, lastWasLinkNext);
			} else {
				drawChord(baseShader, sound, lastWasLinkNext);
			}
		}
	}
}
