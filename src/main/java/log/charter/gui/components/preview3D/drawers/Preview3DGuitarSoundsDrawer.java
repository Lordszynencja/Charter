package log.charter.gui.components.preview3D.drawers;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.util.Arrays.asList;
import static log.charter.data.config.ChartPanelColors.getStringBasedColor;
import static log.charter.gui.components.preview3D.Preview3DUtils.bendHalfstepDistance;
import static log.charter.gui.components.preview3D.Preview3DUtils.chartboardYPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.fretLengthMultiplier;
import static log.charter.gui.components.preview3D.Preview3DUtils.getFretMiddlePosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getFretPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getStringPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getStringPositionWithBend;
import static log.charter.gui.components.preview3D.Preview3DUtils.getTimePosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.noteHalfWidth;
import static log.charter.gui.components.preview3D.Preview3DUtils.tailHalfWidth;
import static log.charter.gui.components.preview3D.glUtils.Matrix4.moveMatrix;
import static log.charter.gui.components.preview3D.glUtils.Matrix4.rotationZMatrix;
import static log.charter.gui.components.preview3D.glUtils.Matrix4.scaleMatrix;
import static log.charter.util.CollectionUtils.lastBeforeEqual;
import static log.charter.util.ColorUtils.setAlpha;
import static log.charter.util.ColorUtils.transparent;
import static log.charter.util.Utils.mix;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.lwjgl.opengl.GL30;

import log.charter.data.ChartData;
import log.charter.data.config.ChartPanelColors.StringColorLabelType;
import log.charter.data.config.values.InstrumentConfig;
import log.charter.data.config.values.SecretsConfig;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.BendValue;
import log.charter.data.song.Level;
import log.charter.data.song.enums.HOPO;
import log.charter.data.song.enums.Mute;
import log.charter.data.song.notes.Chord.ChordNotesVisibility;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.position.FractionalPosition;
import log.charter.gui.components.preview3D.camera.Preview3DCameraHandler;
import log.charter.gui.components.preview3D.data.ChordBoxDrawData;
import log.charter.gui.components.preview3D.data.NoteDrawData;
import log.charter.gui.components.preview3D.data.Preview3DDrawData;
import log.charter.gui.components.preview3D.glUtils.Matrix4;
import log.charter.gui.components.preview3D.glUtils.Point3D;
import log.charter.gui.components.preview3D.shaders.ShadersHolder;
import log.charter.gui.components.preview3D.shaders.ShadersHolder.BaseShaderDrawData;
import log.charter.gui.components.preview3D.shapes.CompositeModel;
import log.charter.gui.components.preview3D.shapes.NoteStatusModels;
import log.charter.gui.components.preview3D.shapes.NoteStatusModels.TextureAtlasPosition;
import log.charter.gui.components.preview3D.shapes.OpenNoteModel;
import log.charter.util.collections.Pair;
import log.charter.util.data.IntRange;

public class Preview3DGuitarSoundsDrawer {
	private static final double noteHeadOffset = -0.03;

	private static final double tailBumpLengthVibrato = 80;
	private static final double tailBumpLengthTremolo = 60;
	private static final double anticipationWindow = 500;

	private interface SoundDrawObject extends Comparable<SoundDrawObject> {
		public double position();

		public void draw(ShadersHolder shadersHolder, final Preview3DDrawData drawData);

		public int drawOrder();

		@Override
		default int compareTo(final SoundDrawObject o) {
			final int positionDiff = Double.compare(position(), o.position());
			if (positionDiff != 0) {
				return -positionDiff;
			}

			return Integer.compare(drawOrder(), o.drawOrder());
		}
	}

	private class NoteHitDrawObject implements SoundDrawObject {
		public final long startTime = System.nanoTime();
		public final int string;
		public final int fret;

		public NoteHitDrawObject(final int string, final int fret) {
			this.string = string;
			this.fret = fret;
		}

		@Override
		public double position() {
			return 0;
		}

		@Override
		public int drawOrder() {
			return 1;
		}

		@Override
		public void draw(final ShadersHolder shadersHolder, final Preview3DDrawData drawData) {
			drawNoteHit(shadersHolder, drawData, startTime, string, fret);
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
		public double position() {
			return note.position;
		}

		@Override
		public int drawOrder() {
			return 1;
		}

		@Override
		public void draw(final ShadersHolder shadersHolder, final Preview3DDrawData drawData) {
			drawNote(shadersHolder, drawData, note, invertBend);
		}
	}

	private class ChordBoxDrawObject implements SoundDrawObject {
		public final ChordBoxDrawData chordBox;

		public ChordBoxDrawObject(final ChordBoxDrawData chordBox) {
			this.chordBox = chordBox;
		}

		@Override
		public double position() {
			return chordBox.position;
		}

		@Override
		public int drawOrder() {
			return 0;
		}

		@Override
		public void draw(final ShadersHolder shadersHolder, final Preview3DDrawData drawData) {
			preview3DChordBoxDrawer.drawChordBox(shadersHolder, drawData, chordBox);
		}
	}

	private static double lastFretLengthMultiplier = fretLengthMultiplier;
	private final static Map<Integer, CompositeModel> openNoteSameFretsModels = new HashMap<>();
	private final static Map<Integer, Map<Integer, CompositeModel>> openNoteModels = new HashMap<>();
	private final static Map<Integer, Map<Integer, CompositeModel>> openNoteModelsLeftHanded = new HashMap<>();

	private Preview3DCameraHandler cameraHandler;
	private ChartData chartData;
	private NoteStatusModels noteStatusModels;

	private final Preview3DChordBoxDrawer preview3DChordBoxDrawer = new Preview3DChordBoxDrawer();

	private double lastTime;
	private final List<NoteHitDrawObject> explosions = new ArrayList<>();
	private int lastSound = -1;

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

		final Map<Integer, Map<Integer, CompositeModel>> currentMap = InstrumentConfig.leftHanded
				? openNoteModelsLeftHanded
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

	public void init(final ChartData chartData, final NoteStatusModels noteStatusModels,
			final Preview3DCameraHandler cameraHandler) {
		this.cameraHandler = cameraHandler;
		this.chartData = chartData;
		this.noteStatusModels = noteStatusModels;

		preview3DChordBoxDrawer.init(chartData);
	}

	private boolean invertBend(final int string) {
		return string < chartData.currentStrings() - 2 && (string <= 2 || string > chartData.currentStrings() / 2);
	}

	private double getBendValue(final NoteDrawData note, final double t) {
		if (note.bendValues.isEmpty()) {
			return 0;
		}

		final FractionalPosition timePosition = FractionalPosition.fromTime(chartData.beats(), t);

		final Integer lastBendId = lastBeforeEqual(note.bendValues, timePosition).findId();
		if (lastBendId != null && lastBendId >= note.bendValues.size() - 1) {
			return note.bendValues.get(lastBendId).bendValue.doubleValue();
		}

		final double bendAValue;
		final double bendAPosition;
		if (lastBendId == null) {
			bendAValue = 0;
			bendAPosition = note.originalPosition;
		} else {
			final BendValue bend = note.bendValues.get(lastBendId);

			bendAValue = bend.bendValue.doubleValue();
			bendAPosition = bend.position(chartData.beats());
		}

		if (t == bendAPosition) {
			return bendAValue;
		}

		final BendValue nextBend = note.bendValues.get(lastBendId == null ? 0 : (lastBendId + 1));
		final double bendBValue = nextBend.bendValue.doubleValue();
		final double bendBPosition = nextBend.position(chartData.beats());
		return mix(bendAPosition, bendBPosition, t, bendAValue, bendBValue);
	}

	private double getNoteHeightAtTime(final NoteDrawData note, final double t, final boolean invertBend) {
		double bendValue = getBendValue(note, t);
		if (invertBend) {
			bendValue = -bendValue;
		}
		if (note.vibrato) {
			bendValue += sin((t - note.originalPosition) * Math.PI / tailBumpLengthVibrato) * bendHalfstepDistance;
		}

		return getStringPositionWithBend(note.string, chartData.currentStrings(), bendValue);
	}

	private void drawNoteShadow(final ShadersHolder shadersHolder, final double time, final NoteDrawData note,
			final Color color, final boolean invertBend) {
		final double x = getFretMiddlePosition(note.fret);
		final double y = getNoteHeightAtTime(note, note.position, invertBend);
		final double z = getTimePosition(note.position - time);

		final double shadowBaseY = chartboardYPosition;
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

	private Color getNoteHeadColor(final NoteDrawData note) {
		return getStringBasedColor(StringColorLabelType.NOTE, note.string, chartData.currentStrings());
	}

	private void drawTexture(final ShadersHolder shadersHolder, final Matrix4 modelMatrix, final double z,
			final NoteDrawData note, final int textureId, final Color color) {
		shadersHolder.new ShadowHighlightTextureShaderDrawData()//
				.addZQuad(-noteHalfWidth, noteHalfWidth, noteHalfWidth, -noteHalfWidth, z, 0, 1, 0, 1)//
				.draw(GL30.GL_QUADS, modelMatrix, textureId, color);
	}

	private void drawTexture(final ShadersHolder shadersHolder, final Matrix4 modelMatrix, final double z,
			final NoteDrawData note, final int textureId) {
		drawTexture(shadersHolder, modelMatrix, z, note, textureId, getNoteHeadColor(note));
	}

	private void drawTexture(final ShadersHolder shadersHolder, final Matrix4 modelMatrix, final double z,
			final NoteDrawData note, final TextureAtlasPosition atlasPosition) {
		final int textureId = noteStatusModels.getTextureId(atlasPosition);
		drawTexture(shadersHolder, modelMatrix, z, note, textureId);
	}

	private void drawOpenStringNoteHead(final ShadersHolder shadersHolder, final Preview3DDrawData drawData,
			final double position, final NoteDrawData note, final boolean hit) {
		final Color color = getNoteHeadColor(note);
		final IntRange frets = drawData.getFrets(note.originalPosition);
		if (frets == null) {
			return;
		}

		final double x = (getFretPosition(frets.min - 1) + getFretPosition(frets.max)) / 2;
		final double y = getStringPositionWithBend(note.string, chartData.currentStrings(), note.prebend);
		final double z = getTimePosition(position - drawData.time) + noteHeadOffset;

		final Matrix4 modelMatrix = moveMatrix(x, y, z);
		for (final Pair<Integer, List<Point3D>> points : getOpenNoteModel(frets.min - 1, frets.max)
				.getPointsForModes()) {
			final BaseShaderDrawData shaderDrawData = shadersHolder.new BaseShaderDrawData();
			for (final Point3D point : points.b) {
				shaderDrawData.addVertex(point, color);
			}

			shaderDrawData.draw(points.a, modelMatrix);
		}

		if (note.hopo == HOPO.PULL_OFF) {
			drawTexture(shadersHolder, modelMatrix, noteHeadOffset * 2, note, TextureAtlasPosition.PULL_OFF);
		}

		if (note.mute == Mute.PALM) {
			drawTexture(shadersHolder, modelMatrix, noteHeadOffset * 2, note, TextureAtlasPosition.PALM_MUTE);
		} else if (note.mute == Mute.FULL) {
			drawTexture(shadersHolder, modelMatrix, noteHeadOffset * 2, note, TextureAtlasPosition.FULL_MUTE);
		}
	}

	private Matrix4 getFrettedNoteRotatedModelMatrix(final Matrix4 baseMatrix, final double time,
			final NoteDrawData note) {
		if (note.isChordNote) {
			return baseMatrix;
		}

		final double rotation = max(-Math.PI / 2, min(0, -Math.PI * (note.position - time - 100) / 1000.0));
		return baseMatrix.multiply(rotationZMatrix(rotation));
	}

	private void drawNoteAnticipation(final ShadersHolder shadersHolder, final double time, final NoteDrawData note,
			final double x, final double y) {
		final double dt = note.position - time;
		double scale = min(1, 1.0 - 0.5 * (dt - 250) / anticipationWindow);
		scale *= scale;

		final Matrix4 modelMatrix = moveMatrix(x, y, 0)//
				.multiply(scaleMatrix(scale, scale, 0));

		final int textureId = noteStatusModels.getTextureId(TextureAtlasPosition.NOTE_ANTICIPATION);
		final Color color = setAlpha(getNoteHeadColor(note), min(255, 500 - (int) dt));
		drawTexture(shadersHolder, modelMatrix, 0, note, textureId, color);
	}

	private void drawFrettedNoteHead(final ShadersHolder shadersHolder, final double time, final NoteDrawData note,
			final boolean hit, final boolean invertBend) {
		final double x = getFretMiddlePosition(note.fret);
		final double y = getNoteHeightAtTime(note, note.position, invertBend);
		final double z = getTimePosition(note.position - time);

		final Matrix4 modelMatrix = moveMatrix(x, y, z);
		final Matrix4 baseNoteMatrix = getFrettedNoteRotatedModelMatrix(modelMatrix, time, note);

		final int textureId = noteStatusModels.getFrettedNoteTextureId(note);
		drawTexture(shadersHolder, baseNoteMatrix, noteHeadOffset, note, textureId);

		if (note.mute == Mute.FULL) {
			drawTexture(shadersHolder, modelMatrix, noteHeadOffset * 2, note, TextureAtlasPosition.FULL_MUTE);
		}

		if (note.hopo == HOPO.HAMMER_ON) {
			drawTexture(shadersHolder, modelMatrix, noteHeadOffset * 2, note, TextureAtlasPosition.HAMMER_ON);
		} else if (note.hopo == HOPO.PULL_OFF) {
			drawTexture(shadersHolder, modelMatrix, noteHeadOffset * 2, note, TextureAtlasPosition.PULL_OFF);
		}

		if (note.position - time < anticipationWindow) {
			drawNoteAnticipation(shadersHolder, time, note, x, y);
		}
	}

	private void drawNoteHead(final ShadersHolder shadersHolder, final Preview3DDrawData drawData,
			final double position, final NoteDrawData note, final boolean hit, final boolean invertBend) {
		if (note.withoutHead) {
			return;
		}
		if (note.fret <= drawData.capo) {
			drawOpenStringNoteHead(shadersHolder, drawData, position, note, hit);
			return;
		}

		if (!hit && !note.isChordNote) {
			drawNoteShadow(shadersHolder, drawData.time, note, getNoteHeadColor(note), invertBend);
		}

		drawFrettedNoteHead(shadersHolder, drawData.time, note, hit, invertBend);
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

	private void addNoteTailPart(final double time, final BaseShaderDrawData leftEdgeDrawData,
			final BaseShaderDrawData innerDrawData, final BaseShaderDrawData rightEdgeDrawData, final double pointTime,
			final double x0, final double x1, final double x2, final double x3, final NoteDrawData note,
			final boolean invertBend, final Color tailEdgeColor, final Color tailInnerColor) {
		double xOffset = 0;
		if (note.slideTo != null) {
			xOffset += getNoteSlideOffsetAtTime(note, (double) (pointTime - note.originalPosition) / note.trueLength);
		}
		if (note.tremolo) {
			xOffset += tailHalfWidth
					* (abs((double) (pointTime % tailBumpLengthTremolo) / tailBumpLengthTremolo - 0.5) - 0.25) * 3;
		}
		final double y = getNoteHeightAtTime(note, pointTime, invertBend);
		final double z = getTimePosition(pointTime - time);

		leftEdgeDrawData.addVertex(new Point3D(x0 + xOffset, y, z), tailEdgeColor)//
				.addVertex(new Point3D(x1 + xOffset, y, z), tailEdgeColor);
		innerDrawData.addVertex(new Point3D(x1 + xOffset, y, z), tailInnerColor)//
				.addVertex(new Point3D(x2 + xOffset, y, z), tailInnerColor);
		rightEdgeDrawData.addVertex(new Point3D(x2 + xOffset, y, z), tailEdgeColor)//
				.addVertex(new Point3D(x3 + xOffset, y, z), tailEdgeColor);
	}

	private List<Double> getTimeValuesToDrawForEveryPoint(final NoteDrawData note) {
		final List<Double> timeValuesToDraw = new ArrayList<>();

		for (double t = note.position + 1; t <= note.endPosition; t++) {
			timeValuesToDraw.add(t);
		}

		return timeValuesToDraw;
	}

	private List<Double> getTimeValuesToDrawForBendPoints(final NoteDrawData note) {
		final List<Double> timeValuesToDraw = new ArrayList<>();
		timeValuesToDraw.add(note.position);

		final double bendsFrom = note.position;
		final double bendsTo = note.endPosition;
		for (final BendValue bendValue : note.bendValues) {
			final double bendValuePosition = bendValue.position(chartData.beats());
			if (bendValuePosition > bendsFrom && bendValuePosition < bendsTo) {
				timeValuesToDraw.add(bendValuePosition);
			}
		}
		timeValuesToDraw.add(note.endPosition);

		return timeValuesToDraw;
	}

	private List<Double> getTimeValuesToDrawForNoteTail(final NoteDrawData note) {
		if (note.tremolo || note.vibrato || note.slideTo != null) {
			return getTimeValuesToDrawForEveryPoint(note);
		}
		if (!note.bendValues.isEmpty()) {
			return getTimeValuesToDrawForBendPoints(note);
		}

		return asList(note.position + 1, note.endPosition);
	}

	private void drawNoteTailForXPositions(final ShadersHolder shadersHolder, final double time, final double x0,
			final double x1, final double x2, final double x3, final NoteDrawData note, final boolean invertBend) {
		final BaseShaderDrawData leftEdgeDrawData = shadersHolder.new BaseShaderDrawData();
		final BaseShaderDrawData innerDrawData = shadersHolder.new BaseShaderDrawData();
		final BaseShaderDrawData rightEdgeDrawData = shadersHolder.new BaseShaderDrawData();

		final Color tailEdgeColor = getStringBasedColor(StringColorLabelType.NOTE_TAIL, note.string,
				chartData.currentStrings());
		final Color tailInnerColor = setAlpha(tailEdgeColor, 192);

		final List<Double> timeValuesToDraw = getTimeValuesToDrawForNoteTail(note);

		for (final Double pointTime : timeValuesToDraw) {
			addNoteTailPart(time, leftEdgeDrawData, innerDrawData, rightEdgeDrawData, pointTime, x0, x1, x2, x3, note,
					invertBend, tailEdgeColor, tailInnerColor);
		}

		leftEdgeDrawData.draw(GL30.GL_TRIANGLE_STRIP, Matrix4.identity);
		rightEdgeDrawData.draw(GL30.GL_TRIANGLE_STRIP, Matrix4.identity);
		innerDrawData.draw(GL30.GL_TRIANGLE_STRIP, Matrix4.identity);
	}

	private void drawOpenNoteTail(final ShadersHolder shadersHolder, final Preview3DDrawData drawData,
			final NoteDrawData note) {
		final IntRange frets = drawData.getFrets(note.originalPosition);
		if (frets == null) {
			return;
		}

		final double x0 = getFretPosition(frets.min - 1) + 0.2;
		final double x1 = x0 + 0.2;
		final double x3 = getFretPosition(frets.max) - 0.2;
		final double x2 = x3 - 0.2;

		drawNoteTailForXPositions(shadersHolder, drawData.time, x0, x1, x2, x3, note, false);
	}

	private void drawFrettedNoteTail(final ShadersHolder shadersHolder, final double time, final NoteDrawData note,
			final boolean invertBend) {
		final double x = getFretMiddlePosition(note.fret);
		final double x0 = x - tailHalfWidth;
		final double x1 = x0 + tailHalfWidth / 2;
		final double x3 = x + tailHalfWidth;
		final double x2 = x3 - tailHalfWidth / 2;

		drawNoteTailForXPositions(shadersHolder, time, x0, x1, x2, x3, note, invertBend);
	}

	private void drawNoteTail(final ShadersHolder shadersHolder, final Preview3DDrawData drawData,
			final NoteDrawData note, final boolean invertBend) {
		if (note.trueLength < 10) {
			return;
		}

		if (note.fret <= drawData.capo) {
			drawOpenNoteTail(shadersHolder, drawData, note);
		} else {
			drawFrettedNoteTail(shadersHolder, drawData.time, note, invertBend);
		}
	}

	private void drawNote(final ShadersHolder shadersHolder, final Preview3DDrawData drawData, final NoteDrawData note,
			final boolean invertBend) {
		drawNoteTail(shadersHolder, drawData, note, invertBend);
		drawNoteHead(shadersHolder, drawData, note.position, note, false, invertBend);
	}

	private void drawNoteHit(final ShadersHolder shadersHolder, final Preview3DDrawData drawData, final long startTime,
			final int string, final int fret) {
		final double x, l;
		if (fret == 0) {
			final IntRange frets = drawData.getFrets(drawData.time);
			if (frets == null) {
				return;
			}

			x = getFretPosition(frets.min - 1);
			l = getFretPosition(frets.max) - x;
		} else {
			x = getFretMiddlePosition(fret);
			l = 0;
		}

		final double y = getStringPosition(string, chartData.currentStrings());
		final double z = 0;

		final Matrix4 modelMatrix = moveMatrix(x, y, z);
		final BaseShaderDrawData shaderDrawData = shadersHolder.new BaseShaderDrawData();

		final double explosionLifetime = 500_000_000;
		final double explosionTime = max(0, min(1, (System.nanoTime() - startTime) / explosionLifetime));

		final int r = (int) (255 * (1 - explosionTime));
		final int g = (int) (255 * (1 - min(1, explosionTime * 2)));
		final int b = 0;
		final int a = (int) (255 * (1 - explosionTime));
		final Color particleColor = new Color(r, g, b, a);

		final Random random = new Random(startTime);
		for (int i = 0; i < 100; i++) {
			final double dx = (random.nextDouble() - 0.5) * 2;
			final double dy = random.nextDouble();
			final double dz = (random.nextDouble() - 0.5) * 0.5;

			final double px = random.nextDouble() * l + dx * explosionTime;
			final double py = dy * explosionTime - explosionTime * explosionTime;
			final double pz = dz * explosionTime;

			shaderDrawData.addVertex(new Point3D(px, py, pz), particleColor);
		}

		GL30.glPointSize(5);
		shaderDrawData.draw(GL30.GL_POINTS, modelMatrix);
	}

	/**
	 * @return if new current note was set while moving forward
	 */
	private boolean updateLastSound(final double time) {
		final boolean movedForward = lastTime < time;
		lastTime = time;
		final ImmutableBeatsMap beats = chartData.beats();
		final List<ChordOrNote> sounds = chartData.currentSounds();
		if (!movedForward) {
			while (lastSound >= sounds.size() || (lastSound >= 0 && sounds.get(lastSound).position(beats) > time)) {
				lastSound--;
			}

			return false;
		}

		boolean newSound = false;
		while (lastSound + 1 < sounds.size() && sounds.get(lastSound + 1).position(beats) < time) {
			lastSound++;
			newSound = true;
		}

		return newSound;
	}

	private void addNotesToDraw(final List<SoundDrawObject> objectsToDraw, final Preview3DDrawData drawData) {
		for (int string = 0; string < chartData.currentStrings(); string++) {
			final boolean shouldBendDownwards = invertBend(string);

			final List<NoteDrawData> stringNotes = drawData.notes.notes.get(string);
			stringNotes.forEach(note -> objectsToDraw.add(new NoteDrawObject(note, shouldBendDownwards)));
		}
	}

	private boolean explosionForNotes(final ChordOrNote sound) {
		if (!sound.isChord()) {
			return true;
		}

		final ImmutableBeatsMap beats = chartData.beats();
		final Level level = chartData.currentArrangementLevel();

		final boolean shouldAddNotesByDefault = level.shouldChordShowNotes(beats, lastSound);
		final ChordNotesVisibility chordNotesVisibility = sound.chord().chordNotesVisibility(shouldAddNotesByDefault);
		return chordNotesVisibility != ChordNotesVisibility.NONE;
	}

	private void addExplosionsForNotes(final ChordOrNote sound) {
		sound.notesWithFrets(chartData.currentChordTemplates()).forEach(n -> {
			if (!ChordOrNote.isLinkedToPrevious(n.string(), lastSound, chartData.currentSounds())) {
				explosions.add(new NoteHitDrawObject(n.string(), n.fret()));
				cameraHandler.shakeCamera(sound.isChord() ? sound.chord().chordNotes.size() : 1);
			}
		});
	}

	private void addExplosionsForStrings(final ChordOrNote sound) {
		final int size = !sound.isChord() ? 0 : chartData.currentStrings() / 2;
		for (int i = chartData.currentStrings() - size; i < chartData.currentStrings(); i++) {
			explosions.add(new NoteHitDrawObject(i, 0));
		}

		cameraHandler.shakeCamera(sound.isChord() ? sound.chord().chordNotes.size() : 0);
	}

	private void addExplosionsToDraw(final List<SoundDrawObject> objectsToDraw, final Preview3DDrawData drawData) {
		final boolean addExplosions = updateLastSound(drawData.time);

		if (SecretsConfig.explosionsEnabled() && addExplosions) {
			final ChordOrNote sound = chartData.currentSounds().get(lastSound);
			final boolean explosionForNotes = explosionForNotes(sound);

			if (explosionForNotes) {
				addExplosionsForNotes(sound);
			} else {
				addExplosionsForStrings(sound);
			}
		}

		explosions.removeIf(o -> o.startTime < System.nanoTime() - 500_000_000);
		objectsToDraw.addAll(explosions);
	}

	public void draw(final ShadersHolder shadersHolder, final Preview3DDrawData drawData) {
		final List<SoundDrawObject> objectsToDraw = new ArrayList<>(100);
		final List<SoundDrawObject> transparentObjectsToDraw = new ArrayList<>(20);

		addNotesToDraw(objectsToDraw, drawData);
		drawData.notes.chords.forEach(chordBox -> transparentObjectsToDraw.add(new ChordBoxDrawObject(chordBox)));
		addExplosionsToDraw(objectsToDraw, drawData);

		objectsToDraw.sort(SoundDrawObject::compareTo);
		objectsToDraw.forEach(object -> object.draw(shadersHolder, drawData));
		transparentObjectsToDraw.sort(SoundDrawObject::compareTo);
		transparentObjectsToDraw.forEach(object -> object.draw(shadersHolder, drawData));
	}

}
