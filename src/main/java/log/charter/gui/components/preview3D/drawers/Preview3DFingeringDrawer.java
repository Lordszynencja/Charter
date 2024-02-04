package log.charter.gui.components.preview3D.drawers;

import static log.charter.gui.components.preview3D.Preview3DUtils.getFretPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getStringPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.stringDistance;
import static log.charter.song.notes.IConstantPosition.findLastBeforeEqual;

import java.util.Map;

import org.lwjgl.opengl.GL30;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.gui.components.preview3D.data.HandShapeDrawData;
import log.charter.gui.components.preview3D.data.Preview3DDrawData;
import log.charter.gui.components.preview3D.glUtils.Matrix4;
import log.charter.gui.components.preview3D.glUtils.Point2D;
import log.charter.gui.components.preview3D.glUtils.TexturesHolder;
import log.charter.gui.components.preview3D.shaders.ShadersHolder;
import log.charter.gui.components.preview3D.shaders.ShadersHolder.BaseTextureShaderDrawData;
import log.charter.song.ChordTemplate;
import log.charter.song.Level;
import log.charter.song.notes.ChordOrNote;
import log.charter.util.IntRange;

public class Preview3DFingeringDrawer {
	private static final double size = stringDistance / 2;

	private static final Point2D fingerShapeSingle = new Point2D(0, 0);
	private static final Point2D fingerShapeEnd = new Point2D(0.25, 0);
	private static final Point2D fingerShapeMiddle = new Point2D(0.5, 0);

	private static final Point2D[] fingerTexturePositions = { //
			new Point2D(0.75, 0), // T
			new Point2D(0, 0.25), // 1
			new Point2D(0.25, 0.25), // 2
			new Point2D(0.5, 0.25), // 3
			new Point2D(0.75, 0.25)// 4
	};

	private ChartData data;
	private TexturesHolder texturesHolder;

	public void init(final ChartData data, final TexturesHolder texturesHolder) {
		this.data = data;
		this.texturesHolder = texturesHolder;
	}

	private void addQuad(final BaseTextureShaderDrawData drawData, final double x0, final double x1, final double y0,
			final double y1, final Point2D textureBase) {
		drawData.addZQuad(x0, x1, y0, y1, -0.01, textureBase.x + 0.001, textureBase.x + 0.249, textureBase.y + 0.001,
				textureBase.y + 0.249);
	}

	private void addFingerSpot(final BaseTextureShaderDrawData drawData, final int fret, final int string,
			final Point2D fingerShapePosition, final boolean invertShape, final Point2D fingerNamePosition) {
		final double x = (getFretPosition(fret - 1) + getFretPosition(fret)) / 2;
		final double x0 = x - size;
		final double x1 = x + size;

		final double yString = getStringPosition(string, data.currentStrings());
		final double y0 = yString + size;
		final double y1 = yString - size;

		if (invertShape) {
			addQuad(drawData, x0, x1, y1, y0, fingerShapePosition);
		} else {
			addQuad(drawData, x0, x1, y0, y1, fingerShapePosition);
		}

		if (fingerNamePosition != null) {
			addQuad(drawData, x0, x1, y0, y1, fingerNamePosition);
		}
	}

	private void addFinger(final BaseTextureShaderDrawData drawData, final int finger, final int fret,
			final IntRange strings) {
		if (strings.max == strings.min) {
			addFingerSpot(drawData, fret, strings.max, fingerShapeSingle, false, fingerTexturePositions[finger]);
			return;
		}

		final int topString = Config.invertStrings ? strings.max : strings.min;
		addFingerSpot(drawData, fret, topString, fingerShapeEnd, false, fingerTexturePositions[finger]);

		for (int i = strings.min + 1; i < strings.max; i++) {
			addFingerSpot(drawData, fret, i, fingerShapeMiddle, false, null);
		}

		final int bottomString = Config.invertStrings ? strings.min : strings.max;
		addFingerSpot(drawData, fret, bottomString, fingerShapeEnd, true, null);
	}

	private void addFingering(final BaseTextureShaderDrawData drawData, final Map<Integer, Integer> fingers,
			final Map<Integer, Integer> frets) {
		if (fingers == null || fingers.isEmpty()) {
			return;
		}

		final IntRange[] fingerRanges = new IntRange[5];
		final int[] fingerFrets = new int[5];

		fingers.forEach((string, finger) -> {
			if (fingerRanges[finger] == null) {
				fingerRanges[finger] = new IntRange(string, string);
			} else {
				fingerRanges[finger] = fingerRanges[finger].extend(string);
			}
			fingerFrets[finger] = frets.getOrDefault(string, 1);
		});

		for (int i = 0; i < 5; i++) {
			if (fingerRanges[i] != null) {
				addFinger(drawData, i, fingerFrets[i], fingerRanges[i]);
			}
		}
	}

	private ChordTemplate findTemplateToUse(final Preview3DDrawData drawData) {
		final HandShapeDrawData handShape = findLastBeforeEqual(drawData.handShapes, data.time + 20);
		if (handShape == null || handShape.timeTo < data.time) {
			return null;
		}

		final Level level = data.getCurrentArrangementLevel();
		final ChordOrNote sound = findLastBeforeEqual(level.chordsAndNotes, data.time + 20);
		if (sound == null || sound.position() < handShape.position() || sound.isNote()) {
			return handShape.template;
		}
		if (sound.chord.fullyMuted()) {
			return null;
		}

		return data.getCurrentArrangement().chordTemplates.get(sound.chord.templateId());
	}

	public void draw(final ShadersHolder shadersHolder, final Preview3DDrawData drawData) {
		final BaseTextureShaderDrawData shaderDrawData = shadersHolder.new BaseTextureShaderDrawData();

		final ChordTemplate template = findTemplateToUse(drawData);
		if (template == null) {
			return;
		}

		addFingering(shaderDrawData, template.fingers, template.frets);

		GL30.glDisable(GL30.GL_DEPTH_TEST);
		shaderDrawData.draw(GL30.GL_QUADS, Matrix4.identity, texturesHolder.getTextureId("fingering"));
		GL30.glEnable(GL30.GL_DEPTH_TEST);
	}
}
