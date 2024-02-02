package log.charter.gui.components.preview3D.drawers;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static log.charter.gui.ChartPanelColors.getStringBasedColor;
import static log.charter.gui.components.preview3D.Preview3DUtils.getChartboardYPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getFretMiddlePosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getFretPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getStringPositionWithBend;
import static log.charter.gui.components.preview3D.Preview3DUtils.getTimePosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.visibility;
import static log.charter.gui.components.preview3D.data.AnchorDrawData.getAnchorsForTimeSpanWithRepeats;
import static log.charter.gui.components.preview3D.glUtils.Matrix4.moveMatrix;
import static log.charter.gui.components.preview3D.glUtils.Matrix4.rotationZMatrix;
import static log.charter.gui.components.preview3D.glUtils.Matrix4.scaleMatrix;
import static log.charter.song.notes.IConstantPosition.findLastBeforeEqual;
import static log.charter.song.notes.IConstantPosition.findLastIdBeforeEqual;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL30;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.managers.RepeatManager;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.ChartPanelColors.StringColorLabelType;
import log.charter.gui.components.preview3D.Preview3DUtils;
import log.charter.gui.components.preview3D.data.AnchorDrawData;
import log.charter.gui.components.preview3D.data.ChordBoxDrawData;
import log.charter.gui.components.preview3D.data.NoteDrawData;
import log.charter.gui.components.preview3D.data.Preview3DNotesData;
import log.charter.gui.components.preview3D.glUtils.Matrix4;
import log.charter.gui.components.preview3D.glUtils.Point3D;
import log.charter.gui.components.preview3D.shaders.ShadersHolder;
import log.charter.gui.components.preview3D.shaders.ShadersHolder.BaseShaderDrawData;
import log.charter.gui.components.preview3D.shapes.CompositeModel;
import log.charter.gui.components.preview3D.shapes.FrettedNoteModel;
import log.charter.gui.components.preview3D.shapes.Model;
import log.charter.gui.components.preview3D.shapes.NoteStatusModels;
import log.charter.gui.components.preview3D.shapes.OpenNoteModel;
import log.charter.song.BendValue;
import log.charter.song.enums.HOPO;
import log.charter.song.enums.Harmonic;
import log.charter.song.enums.Mute;
import log.charter.util.CollectionUtils.Pair;

public class Preview3DGuitarSoundsDrawer {
	private static final int anticipationWindow = 1_000;
	private static final int highlightWindow = 50;

	private ChartData data;
	private RepeatManager repeatManager;

	private final static Map<Integer, Map<Integer, CompositeModel>> openNoteModels = new HashMap<>();
	private final static Map<Integer, Map<Integer, CompositeModel>> openNoteModelsLeftHanded = new HashMap<>();

	private static CompositeModel getOpenNoteModel(final int fret0, final int fret1) {
		final Map<Integer, Map<Integer, CompositeModel>> currentMap = Config.leftHanded ? openNoteModelsLeftHanded
				: openNoteModels;

		if (currentMap.get(fret0) == null) {
			currentMap.put(fret0, new HashMap<>());
		}
		if (currentMap.get(fret0).get(fret1) == null) {
			final double width = getFretPosition(fret1) - getFretPosition(fret0);
			currentMap.get(fret0).put(fret1, new OpenNoteModel(width));
		}

		return currentMap.get(fret0).get(fret1);
	}

	public void init(final ChartData data, final RepeatManager repeatManager) {
		this.data = data;
		this.repeatManager = repeatManager;
	}

	private boolean invertBend(final int string) {
		return string < data.currentStrings() - 2 && (string <= 2 || string > data.currentStrings() / 2);
	}

	private AnchorDrawData findAnchorForPosition(final int position) {
		final List<AnchorDrawData> anchors = getAnchorsForTimeSpanWithRepeats(data, repeatManager, position, position);
		final AnchorDrawData anchor = findLastBeforeEqual(anchors, position);
		return anchor == null ? new AnchorDrawData(position, position, 0, 4) : anchor;
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
	private double getNoteHeightAtTime(final NoteDrawData note, final int t, final boolean invertBend) {
		final int dt = t - note.truePosition;

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
		if (invertBend) {
			bendValue = -bendValue;
		}

		if (note.vibrato) {
			bendValue += sin(dt * Math.PI / 100) * 0.2;
		}

		return getStringPositionWithBend(note.string, data.currentStrings(), bendValue);
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

	private void drawFullChordMute(final ShadersHolder shadersHolder, final double x0, final double x1, final double y0,
			final double y1, double z) {
		final double x = (x0 + x1) / 2;
		final double y = (y0 + y1) / 2;
		final double d0y = 0.8 * (y1 - y);
		final double d1y = 0.9 * (y1 - y);
		final double d0x = d0y / 10;
		final double d1x = d1y / 10;
		z -= 0.001;

		final Color color = ColorLabel.PREVIEW_3D_CHORD_FULL_MUTE.color();
		shadersHolder.new BaseShaderDrawData()//
				.addVertex(new Point3D(x - d1x, y + d0y, z), color)//
				.addVertex(new Point3D(x - d0x, y + d1y, z), color)//
				.addVertex(new Point3D(x + d1x, y - d0y, z), color)//
				.addVertex(new Point3D(x + d0x, y - d1y, z), color)//

				.addVertex(new Point3D(x + d1x, y + d0y, z), color)//
				.addVertex(new Point3D(x - d0x, y - d1y, z), color)//
				.addVertex(new Point3D(x - d1x, y - d0y, z), color)//
				.addVertex(new Point3D(x + d0x, y + d1y, z), color)//
				.draw(GL30.GL_QUADS, Matrix4.identity);
	}

	private void drawPalmChordMute(final ShadersHolder shadersHolder, final double x0, final double x1, final double y0,
			final double y1, double z) {
		final double x = (x0 + x1) / 2;
		final double y = (y0 + y1) / 2;
		final double d0x = 0.8 * (x1 - x);
		final double d1x = 0.9 * (x1 - x);
		final double d0y = 0.8 * (y1 - y);
		final double d1y = 0.9 * (y1 - y);
		z -= 0.001;

		final Color color = ColorLabel.PREVIEW_3D_CHORD_FULL_MUTE.color();
		shadersHolder.new BaseShaderDrawData()//
				.addVertex(new Point3D(x - d1x, y + d0y, z), color)//
				.addVertex(new Point3D(x - d0x, y + d1y, z), color)//
				.addVertex(new Point3D(x + d1x, y - d0y, z), color)//
				.addVertex(new Point3D(x + d0x, y - d1y, z), color)//

				.addVertex(new Point3D(x + d1x, y + d0y, z), color)//
				.addVertex(new Point3D(x - d0x, y - d1y, z), color)//
				.addVertex(new Point3D(x - d1x, y - d0y, z), color)//
				.addVertex(new Point3D(x + d0x, y + d1y, z), color)//
				.draw(GL30.GL_QUADS, Matrix4.identity);
	}

	private void drawChordBox(final ShadersHolder shadersHolder, final ChordBoxDrawData chordBox) {
		final double x0 = getFretPosition(chordBox.frets.min);
		final double x1 = getFretPosition(chordBox.frets.max);
		final double y0 = getChartboardYPosition(data.currentStrings());
		double y1 = Preview3DUtils.topStringPosition;
		final double z = max(0, getTimePosition(chordBox.position - data.time));

		if (chordBox.onlyBox) {
			y1 = (y0 + y1 * 2) / 3;
		}

		final Point3D p00 = new Point3D(x0, y0, z);
		final Point3D p01 = new Point3D((x1 + x0) / 2, y0, z);
		final Point3D p02 = new Point3D(x1, y0, z);
		final Point3D p10 = new Point3D(x0, y1, z);
		final Point3D p12 = new Point3D(x1, y1, z);
		final Color color = ColorLabel.PREVIEW_3D_CHORD_BOX.color();
		final Color shadowInvisibleColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);

		shadersHolder.new BaseShaderDrawData()//
				.addVertex(p00, color)//
				.addVertex(p01, shadowInvisibleColor)//
				.addVertex(p10, shadowInvisibleColor)//
				.addVertex(p02, color)//
				.addVertex(p01, shadowInvisibleColor)//
				.addVertex(p12, shadowInvisibleColor)//
				.draw(GL30.GL_TRIANGLES, Matrix4.identity);

		if (chordBox.mute == Mute.FULL) {
			drawFullChordMute(shadersHolder, x0, x1, y0, y1, z);
		} else if (chordBox.mute == Mute.PALM) {
			drawPalmChordMute(shadersHolder, x0, x1, y0, y1, z);
		}
	}

	private void drawNoteShadow(final ShadersHolder shadersHolder, final double x, final double y, final double z,
			final Color color) {
		final double shadowBaseY = getChartboardYPosition(data.currentStrings());
		final Point3D shadowBaseP0 = new Point3D(x - 0.02, shadowBaseY, z);
		final Point3D shadowBaseP1 = new Point3D(x, shadowBaseY, z);
		final Point3D shadowBaseP2 = new Point3D(x + 0.02, shadowBaseY, z);
		final Point3D shadowP3 = new Point3D(x, y - 0.3, z);
		final Color shadowInvisibleColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);

		final BaseShaderDrawData shadowDrawData = shadersHolder.new BaseShaderDrawData();
		shadowDrawData.addVertex(shadowBaseP1, color)//
				.addVertex(shadowBaseP0, shadowInvisibleColor)//
				.addVertex(shadowP3, shadowInvisibleColor)//
				.addVertex(shadowBaseP2, shadowInvisibleColor)//
				.draw(GL30.GL_TRIANGLE_FAN, Matrix4.identity);
	}

	private void drawMuteForSingleNote(final ShadersHolder shadersHolder, final Mute mute, final Matrix4 modelMatrix) {
		final Model model = NoteStatusModels.mutesModels.get(mute);
		final Color color = NoteStatusModels.mutesColors.get(mute).color();
		shadersHolder.new BaseShaderDrawData().addModel(model, color).draw(model.getDrawMode(), modelMatrix);
	}

	private void drawHOPO(final ShadersHolder shadersHolder, final HOPO hopo, final Matrix4 modelMatrix) {
		final Model model = NoteStatusModels.hoposModels.get(hopo);
		final Color color = NoteStatusModels.hoposColors.get(hopo).color();
		shadersHolder.new BaseShaderDrawData().addModel(model, color).draw(model.getDrawMode(), modelMatrix);
	}

	private void drawHarmonic(final ShadersHolder shadersHolder, final Harmonic harmonic, final Matrix4 modelMatrix) {
		final Model model = NoteStatusModels.harmonicsModels.get(harmonic);
		final Color color = NoteStatusModels.harmonicsColors.get(harmonic).color();
		shadersHolder.new BaseShaderDrawData().addModel(model, color).draw(model.getDrawMode(), modelMatrix);
	}

	private void drawOpenStringNoteHead(final ShadersHolder shadersHolder, final int position, final NoteDrawData note,
			final boolean hit) {
		Color color = getStringBasedColor(StringColorLabelType.NOTE, note.string, data.currentStrings());
		if (hit) {
			color = color.brighter();
		}

		final double y = getStringPositionWithBend(note.string, data.currentStrings(), note.prebend);
		final double z = getTimePosition(position - data.time);

		final double x = getFretPosition(note.frets.min);
		final double middleX = (getFretPosition(note.frets.max) + x) / 2;
		final Matrix4 modelMatrix = moveMatrix(middleX, y, z);
		for (final Pair<Integer, List<Point3D>> points : getOpenNoteModel(note.frets.min, note.frets.max)
				.getPointsForModes()) {
			final BaseShaderDrawData drawData = shadersHolder.new BaseShaderDrawData();
			for (final Point3D point : points.b) {
				drawData.addVertex(point, color);
			}

			drawData.draw(points.a, modelMatrix);
		}

		// modelMatrix = moveMatrix(middleX, y, z);
		if (note.mute != Mute.NONE) {
			drawMuteForSingleNote(shadersHolder, note.mute, modelMatrix);
		}
		if (note.hopo != HOPO.NONE) {
			drawHOPO(shadersHolder, note.hopo, modelMatrix);
		}
		if (note.harmonic != Harmonic.NONE) {
			drawHarmonic(shadersHolder, note.harmonic, modelMatrix);
		}
	}

	private void drawNoteHead(final ShadersHolder shadersHolder, final int position, final NoteDrawData note,
			final boolean hit) {
		if (note.withoutHead) {
			return;
		}
		if (note.fret == 0) {
			drawOpenStringNoteHead(shadersHolder, position, note, hit);
			return;
		}

		Color color = getStringBasedColor(StringColorLabelType.NOTE, note.string, data.currentStrings());
		if (hit) {
			color = color.brighter();
		}

		final double x = getFretMiddlePosition(note.fret);
		final double y = getStringPositionWithBend(note.string, data.currentStrings(), note.prebend);
		final double z = getTimePosition(position - data.time);

		final double rotation = (note.isChordNote || note.fret == 0) ? 0
				: max(-Math.PI / 2, min(0, -Math.PI * (note.position - data.time) / 1000.0));
		final Matrix4 modelMatrix = moveMatrix(x, y, z)//
				.multiply(scaleMatrix(0.1, 1, 0.5))//
				.multiply(rotationZMatrix(rotation))//
				.multiply(scaleMatrix(10, 1, 2));

		if (!note.accent) {
			shadersHolder.new BaseShaderDrawData()//
					.addModel(FrettedNoteModel.instance, color)//
					.draw(FrettedNoteModel.instance.getDrawMode(), modelMatrix);
		} else {
			final Color accentColor = getStringBasedColor(StringColorLabelType.NOTE_ACCENT, note.string,
					data.currentStrings());
			shadersHolder.new BaseShaderDrawData()//
					.addModel(FrettedNoteModel.instance, accentColor)//
					.draw(FrettedNoteModel.instance.getDrawMode(), modelMatrix);
		}

		if (note.mute != Mute.NONE) {
			drawMuteForSingleNote(shadersHolder, note.mute, modelMatrix);
		}
		if (note.hopo != HOPO.NONE) {
			drawHOPO(shadersHolder, note.hopo, modelMatrix);
		}
		if (note.harmonic != Harmonic.NONE) {
			drawHarmonic(shadersHolder, note.harmonic, modelMatrix);
		}

		if (!hit && !note.isChordNote) {
			drawNoteShadow(shadersHolder, x, y, z, color);
		}
	}

	private double getNoteSlideOffsetAtTime(final NoteDrawData note, final double progress) {
		if (note.slideTo == null) {
			return 0;
		}

		final double startPosition = getFretMiddlePosition(note.fret);
		final double endPosition = getFretMiddlePosition(note.slideTo);

		final double weight = note.unpitchedSlide//
				? 1 - sin((1 - progress) * Math.PI / 2)//
				: pow(sin(progress * Math.PI / 2), 3);
		return (endPosition - startPosition) * weight;
	}

	private void drawNoteTail(final ShadersHolder shadersHolder, final NoteDrawData note, final boolean invertBend) {
		if (note.trueLength < 10) {
			return;
		}

		final double x0;
		final double x1;

		if (note.fret == 0) {
			final AnchorDrawData anchor = findAnchorForPosition(note.position);
			x0 = getFretPosition(anchor.fretFrom);
			x1 = getFretPosition(anchor.fretTo);
		} else {
			final double x = getFretMiddlePosition(note.fret);
			x0 = x - FrettedNoteModel.width / 3;
			x1 = x + FrettedNoteModel.width / 3;
		}

		final BaseShaderDrawData drawData = shadersHolder.new BaseShaderDrawData();

		final Color color = getStringBasedColor(StringColorLabelType.NOTE_TAIL, note.string, data.currentStrings());

		final int tremoloSize = 100;
		for (int t = note.position; t <= note.endPosition; t++) {
			final double xSlideOffset = getNoteSlideOffsetAtTime(note,
					(double) (t - note.truePosition) / note.trueLength);
			final double y = getNoteHeightAtTime(note, t, invertBend);
			final double z = getTimePosition(t - data.time);

			if (note.tremolo) {
				final double xOffset = -0.05 * abs((double) (t % tremoloSize) / tremoloSize - 0.5);
				drawData.addVertex(new Point3D(x0 + xOffset + xSlideOffset, y, z), color)//
						.addVertex(new Point3D(x1 + xOffset + xSlideOffset, y, z), color);
			} else {
				drawData.addVertex(new Point3D(x0 + xSlideOffset, y, z), color)//
						.addVertex(new Point3D(x1 + xSlideOffset, y, z), color);
			}
		}

		drawData.draw(GL30.GL_TRIANGLE_STRIP, Matrix4.identity);
	}

	private void drawNote(final ShadersHolder shadersHolder, final NoteDrawData note, final boolean invertBend) {
		if (!note.withoutHead && note.position <= data.time + anticipationWindow && note.position >= data.time) {
			// drawNoteAnticipation(baseShader, note);
		}

		if (note.position >= data.time) {
			drawNoteHead(shadersHolder, note.position, note, false);
		} else if (!note.withoutHead && note.truePosition >= data.time - highlightWindow
				&& note.endPosition - note.position < highlightWindow) {
			drawNoteHead(shadersHolder, data.time, note, true);
		} else if (note.endPosition >= data.time) {
			// drawNoteHold(baseShader, note);
		}

		drawNoteTail(shadersHolder, note, invertBend);
	}

	public void draw(final ShadersHolder shadersHolder) {
		final Preview3DNotesData notesData = Preview3DNotesData.getNotesForTimeSpanWithRepeats(data, repeatManager,
				data.time, data.time + visibility);

		for (int string = 0; string < data.currentStrings(); string++) {
			final boolean shouldBendDownwards = invertBend(string);

			for (final NoteDrawData note : notesData.notes.get(string)) {
				drawNote(shadersHolder, note, shouldBendDownwards);
			}
		}

		for (int i = notesData.chords.size() - 1; i >= 0; i--) {
			drawChordBox(shadersHolder, notesData.chords.get(i));
		}
	}

}
