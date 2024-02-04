package log.charter.gui.components.preview3D.drawers;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.util.Arrays.asList;
import static log.charter.gui.ChartPanelColors.getStringBasedColor;
import static log.charter.gui.components.preview3D.Preview3DUtils.bendHalfstepDistance;
import static log.charter.gui.components.preview3D.Preview3DUtils.fretLengthMultiplier;
import static log.charter.gui.components.preview3D.Preview3DUtils.getChartboardYPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getFretMiddlePosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getFretPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getStringPositionWithBend;
import static log.charter.gui.components.preview3D.Preview3DUtils.getTimePosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.noteHalfWidth;
import static log.charter.gui.components.preview3D.Preview3DUtils.tailHalfWidth;
import static log.charter.gui.components.preview3D.Preview3DUtils.topStringPosition;
import static log.charter.gui.components.preview3D.glUtils.Matrix4.moveMatrix;
import static log.charter.gui.components.preview3D.glUtils.Matrix4.rotationZMatrix;
import static log.charter.song.notes.IConstantPosition.findLastIdBeforeEqual;
import static log.charter.util.ColorUtils.setAlpha;
import static log.charter.util.ColorUtils.transparent;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL30;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.ChartPanelColors.StringColorLabelType;
import log.charter.gui.components.preview3D.data.ChordBoxDrawData;
import log.charter.gui.components.preview3D.data.NoteDrawData;
import log.charter.gui.components.preview3D.data.Preview3DDrawData;
import log.charter.gui.components.preview3D.glUtils.Matrix4;
import log.charter.gui.components.preview3D.glUtils.Point3D;
import log.charter.gui.components.preview3D.glUtils.TexturesHolder;
import log.charter.gui.components.preview3D.shaders.ShadersHolder;
import log.charter.gui.components.preview3D.shaders.ShadersHolder.BaseShaderDrawData;
import log.charter.gui.components.preview3D.shapes.CompositeModel;
import log.charter.gui.components.preview3D.shapes.NoteStatusModels;
import log.charter.gui.components.preview3D.shapes.OpenNoteModel;
import log.charter.song.BendValue;
import log.charter.song.enums.HOPO;
import log.charter.song.enums.Harmonic;
import log.charter.song.enums.Mute;
import log.charter.util.CollectionUtils.Pair;

public class Preview3DGuitarSoundsDrawer {
	public static final int tailBumpLength = 100;

	private interface SoundDrawObject extends Comparable<SoundDrawObject> {
		public int position();

		public void draw(ShadersHolder shadersHolder);

		public int drawOrder();

		@Override
		default int compareTo(final SoundDrawObject o) {
			final int positionDiff = Integer.compare(position(), o.position());
			if (positionDiff != 0) {
				return -positionDiff;
			}

			return Integer.compare(drawOrder(), o.drawOrder());
		}
	}

	private class NoteDrawObject implements SoundDrawObject {
		public final NoteDrawData note;
		private final boolean invertBend;

		public NoteDrawObject(final NoteDrawData note, final boolean invertBend) {
			this.note = note;
			this.invertBend = invertBend;
		}

		@Override
		public int position() {
			return note.position;
		}

		@Override
		public int drawOrder() {
			return 1;
		}

		@Override
		public void draw(final ShadersHolder shadersHolder) {
			drawNote(shadersHolder, note, invertBend);
		}
	}

	private class ChordBoxDrawObject implements SoundDrawObject {
		public final ChordBoxDrawData chordBox;

		public ChordBoxDrawObject(final ChordBoxDrawData chordBox) {
			this.chordBox = chordBox;
		}

		@Override
		public int position() {
			return chordBox.position;
		}

		@Override
		public int drawOrder() {
			return 0;
		}

		@Override
		public void draw(final ShadersHolder shadersHolder) {
			drawChordBox(shadersHolder, chordBox);
		}
	}

	private static final int anticipationWindow = 1_000;
	private static final int highlightWindow = 50;

	private ChartData data;
	private final NoteStatusModels noteStatusModels = new NoteStatusModels();

	private static double lastFretLengthMultiplier = fretLengthMultiplier;
	private final static Map<Integer, CompositeModel> openNoteSameFretsModels = new HashMap<>();
	private final static Map<Integer, Map<Integer, CompositeModel>> openNoteModels = new HashMap<>();
	private final static Map<Integer, Map<Integer, CompositeModel>> openNoteModelsLeftHanded = new HashMap<>();

	private static CompositeModel getOpenNoteModel(final int fret0, final int fret1) {
		if (fretLengthMultiplier != lastFretLengthMultiplier) {
			openNoteModels.clear();
			openNoteModelsLeftHanded.clear();
		}

		if (fretLengthMultiplier == 1) {
			final int fretsWidth = fret1 - fret0;
			if (openNoteSameFretsModels.get(fretsWidth) == null) {
				final double width = getFretPosition(fret1) - getFretPosition(fret0);
				openNoteSameFretsModels.put(fretsWidth, new OpenNoteModel(width));
			}

			return openNoteSameFretsModels.get(fretsWidth);
		}

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

	public void init(final ChartData data, final TexturesHolder texturesHolder) {
		this.data = data;

		noteStatusModels.init(texturesHolder);
	}

	private boolean invertBend(final int string) {
		return string < data.currentStrings() - 2 && (string <= 2 || string > data.currentStrings() / 2);
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
			bendValue += sin(dt * Math.PI / tailBumpLength) * bendHalfstepDistance / 2;
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
		double y1 = topStringPosition;
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

	private void drawNoteShadow(final ShadersHolder shadersHolder, final NoteDrawData note, final Color color) {
		final double x = getFretMiddlePosition(note.fret);
		final double y = getStringPositionWithBend(note.string, data.currentStrings(), note.prebend);
		final double z = getTimePosition(note.position - data.time);

		final double shadowBaseY = getChartboardYPosition(data.currentStrings());
		final Point3D shadowBaseP0 = new Point3D(x - noteHalfWidth / 2, shadowBaseY, z);
		final Point3D shadowBaseP1 = new Point3D(x, shadowBaseY, z);
		final Point3D shadowBaseP2 = new Point3D(x + noteHalfWidth / 2, shadowBaseY, z);
		final Point3D shadowP3 = new Point3D(x, y - 0.3, z);
		final Color shadowInvisibleColor = transparent(color);

		shadersHolder.new BaseShaderDrawData()//
				.addVertex(shadowBaseP1, color)//
				.addVertex(shadowBaseP0, shadowInvisibleColor)//
				.addVertex(shadowP3, shadowInvisibleColor)//
				.addVertex(shadowBaseP2, shadowInvisibleColor)//
				.draw(GL30.GL_TRIANGLE_FAN, Matrix4.identity);
	}

	private Color getNoteHeadColor(final NoteDrawData note, final boolean hit) {
		if (note.accent) {
			return getStringBasedColor(StringColorLabelType.NOTE_ACCENT, note.string, data.currentStrings());
		}

		Color color = getStringBasedColor(StringColorLabelType.NOTE, note.string, data.currentStrings());
		if (hit) {
			color = color.brighter();
		}
		return color;
	}

	private void drawNoteStatusesTexture(final ShadersHolder shadersHolder, final Matrix4 modelMatrix,
			final NoteDrawData note, final boolean hit, final int textureId) {
		final Color color = getNoteHeadColor(note, hit);

		shadersHolder.new ShadowHighlightTextureShaderDrawData()//
				.addZQuad(-noteHalfWidth, noteHalfWidth, noteHalfWidth, -noteHalfWidth, -0.01, 0, 1, 0, 1)//
				.draw(GL30.GL_QUADS, modelMatrix, textureId, color);
	}

	private void drawOpenStringNoteHead(final ShadersHolder shadersHolder, final int position, final NoteDrawData note,
			final boolean hit) {
		final Color color = getNoteHeadColor(note, hit);

		final double x = (getFretPosition(note.frets.min) + getFretPosition(note.frets.max)) / 2;
		final double y = getStringPositionWithBend(note.string, data.currentStrings(), note.prebend);
		final double z = getTimePosition(position - data.time);

		final Matrix4 modelMatrix = moveMatrix(x, y, z);
		for (final Pair<Integer, List<Point3D>> points : getOpenNoteModel(note.frets.min, note.frets.max)
				.getPointsForModes()) {
			final BaseShaderDrawData drawData = shadersHolder.new BaseShaderDrawData();
			for (final Point3D point : points.b) {
				drawData.addVertex(point, color);
			}

			drawData.draw(points.a, modelMatrix);
		}

		if (note.hopo != HOPO.NONE || note.mute != Mute.NONE || note.harmonic != Harmonic.NONE) {
			final int textureId = noteStatusModels.getTextureIdForOnlyStatuses(note);
			drawNoteStatusesTexture(shadersHolder, modelMatrix, note, hit, textureId);
		}
	}

	private Matrix4 getFrettedNoteModelMatrix(final double x, final double y, final double z, final NoteDrawData note) {
		Matrix4 modelMatrix = moveMatrix(x, y, z);
		if (!note.isChordNote) {
			final double rotation = max(-Math.PI / 2, min(0, -Math.PI * (note.position - data.time - 100) / 1000.0));
			modelMatrix = modelMatrix.multiply(rotationZMatrix(rotation));
		}

		return modelMatrix;
	}

	private void drawFrettedNoteHeadBase(final ShadersHolder shadersHolder, final NoteDrawData note,
			final boolean hit) {
		final double x0 = getFretPosition(note.fret - 1);
		final double x1 = getFretPosition(note.fret);
		final double x = (x0 + x1) / 2;
		final double y = getStringPositionWithBend(note.string, data.currentStrings(), note.prebend);
		final double z = getTimePosition(note.position - data.time);

		final Matrix4 modelMatrix = getFrettedNoteModelMatrix(x, y, z, note);

		final int textureId = noteStatusModels.getTextureId(note);
		drawNoteStatusesTexture(shadersHolder, modelMatrix, note, hit, textureId);
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

		if (!hit && !note.isChordNote) {
			drawNoteShadow(shadersHolder, note, getNoteHeadColor(note, hit));
		}

		drawFrettedNoteHeadBase(shadersHolder, note, hit);
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

	private void addNoteTailPart(final BaseShaderDrawData leftEdgeDrawData, final BaseShaderDrawData innerDrawData,
			final BaseShaderDrawData rightEdgeDrawData, final int t, final double x0, final double x1, final double x2,
			final double x3, final NoteDrawData note, final boolean invertBend, final Color tailEdgeColor,
			final Color tailInnerColor) {
		double xOffset = 0;
		if (note.slideTo != null) {
			xOffset += getNoteSlideOffsetAtTime(note, (double) (t - note.truePosition) / note.trueLength);
		}
		if (note.tremolo) {
			xOffset += tailHalfWidth * (abs((double) (t % tailBumpLength) / tailBumpLength - 0.5) - 0.25);
		}
		final double y = getNoteHeightAtTime(note, t, invertBend);
		final double z = getTimePosition(t - data.time);

		leftEdgeDrawData.addVertex(new Point3D(x0 + xOffset, y, z), tailEdgeColor)//
				.addVertex(new Point3D(x1 + xOffset, y, z), tailEdgeColor);
		innerDrawData.addVertex(new Point3D(x1 + xOffset, y, z), tailInnerColor)//
				.addVertex(new Point3D(x2 + xOffset, y, z), tailInnerColor);
		rightEdgeDrawData.addVertex(new Point3D(x2 + xOffset, y, z), tailEdgeColor)//
				.addVertex(new Point3D(x3 + xOffset, y, z), tailEdgeColor);
	}

	private void drawNoteTailForXPositions(final ShadersHolder shadersHolder, final double x0, final double x1,
			final double x2, final double x3, final NoteDrawData note, final boolean invertBend) {
		final BaseShaderDrawData leftEdgeDrawData = shadersHolder.new BaseShaderDrawData();
		final BaseShaderDrawData innerDrawData = shadersHolder.new BaseShaderDrawData();
		final BaseShaderDrawData rightEdgeDrawData = shadersHolder.new BaseShaderDrawData();

		final Color tailEdgeColor = getStringBasedColor(StringColorLabelType.NOTE_TAIL, note.string,
				data.currentStrings());
		final Color tailInnerColor = setAlpha(tailEdgeColor, 192);

		List<Integer> timeValuesToDraw = new ArrayList<>();
		if (note.tremolo || note.vibrato || note.slideTo != null) {
			for (int t = note.position + 1; t <= note.endPosition; t++) {
				timeValuesToDraw.add(t);
			}
		} else if (!note.bendValues.isEmpty()) {
			timeValuesToDraw.add(note.position + 1);
			final int bendsfrom = note.position - note.truePosition + 1;
			final int bendsTo = note.endPosition - note.truePosition;
			for (final BendValue bendValue : note.bendValues) {
				if (bendValue.position() > bendsfrom && bendValue.position() < bendsTo) {
					timeValuesToDraw.add(note.truePosition + bendValue.position());
				}
			}
			timeValuesToDraw.add(note.endPosition);
		} else {
			timeValuesToDraw = asList(note.position + 1, note.endPosition);
		}

		for (final Integer t : timeValuesToDraw) {
			addNoteTailPart(leftEdgeDrawData, innerDrawData, rightEdgeDrawData, t, x0, x1, x2, x3, note, invertBend,
					tailEdgeColor, tailInnerColor);
		}

		leftEdgeDrawData.draw(GL30.GL_TRIANGLE_STRIP, Matrix4.identity);
		rightEdgeDrawData.draw(GL30.GL_TRIANGLE_STRIP, Matrix4.identity);
		innerDrawData.draw(GL30.GL_TRIANGLE_STRIP, Matrix4.identity);
	}

	private void drawOpenNoteTail(final ShadersHolder shadersHolder, final NoteDrawData note) {
		final double x0 = getFretPosition(note.frets.min) + 0.2;
		final double x1 = x0 + 0.2;
		final double x3 = getFretPosition(note.frets.max) - 0.2;
		final double x2 = x3 - 0.2;

		drawNoteTailForXPositions(shadersHolder, x0, x1, x2, x3, note, false);
	}

	private void drawFrettedNoteTail(final ShadersHolder shadersHolder, final NoteDrawData note,
			final boolean invertBend) {
		final double x = getFretMiddlePosition(note.fret);
		final double x0 = x - tailHalfWidth;
		final double x1 = x0 + tailHalfWidth / 2;
		final double x3 = x + tailHalfWidth;
		final double x2 = x3 - tailHalfWidth / 2;

		drawNoteTailForXPositions(shadersHolder, x0, x1, x2, x3, note, invertBend);
	}

	private void drawNoteTail(final ShadersHolder shadersHolder, final NoteDrawData note, final boolean invertBend) {
		if (note.trueLength < 10) {
			return;
		}

		if (note.fret == 0) {
			drawOpenNoteTail(shadersHolder, note);
		} else {
			drawFrettedNoteTail(shadersHolder, note, invertBend);
		}
	}

	private void drawNote(final ShadersHolder shadersHolder, final NoteDrawData note, final boolean invertBend) {
		drawNoteTail(shadersHolder, note, invertBend);

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
	}

	public void draw(final ShadersHolder shadersHolder, final Preview3DDrawData drawData) {
		final List<SoundDrawObject> objectsToDraw = new ArrayList<>(1000);

		for (int string = 0; string < data.currentStrings(); string++) {
			final boolean shouldBendDownwards = invertBend(string);

			drawData.notes.notes.get(string)
					.forEach(note -> objectsToDraw.add(new NoteDrawObject(note, shouldBendDownwards)));
		}
		drawData.notes.chords.forEach(chordBox -> objectsToDraw.add(new ChordBoxDrawObject(chordBox)));

		objectsToDraw.sort(null);
		objectsToDraw.forEach(object -> object.draw(shadersHolder));
	}

}
