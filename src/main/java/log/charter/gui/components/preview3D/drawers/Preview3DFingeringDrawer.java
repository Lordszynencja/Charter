package log.charter.gui.components.preview3D.drawers;

import static log.charter.gui.ChartPanelColors.getStringBasedColor;
import static log.charter.gui.components.preview3D.Preview3DUtils.getFretMiddlePosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getStringPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.noteHalfWidth;
import static log.charter.gui.components.preview3D.Preview3DUtils.stringDistance;
import static log.charter.gui.components.preview3D.glUtils.Matrix4.moveMatrix;
import static log.charter.util.CollectionUtils.lastBeforeEqual;

import java.awt.Color;

import org.lwjgl.opengl.GL30;

import log.charter.data.ChartData;
import log.charter.data.config.GraphicalConfig;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.Level;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.time.Position;
import log.charter.gui.ChartPanelColors.StringColorLabelType;
import log.charter.gui.components.preview3D.data.HandShapeDrawData;
import log.charter.gui.components.preview3D.data.Preview3DDrawData;
import log.charter.gui.components.preview3D.glUtils.Matrix4;
import log.charter.gui.components.preview3D.glUtils.Point2D;
import log.charter.gui.components.preview3D.glUtils.TexturesHolder;
import log.charter.gui.components.preview3D.shaders.ShadersHolder;
import log.charter.gui.components.preview3D.shaders.ShadersHolder.BaseTextureShaderDrawData;
import log.charter.gui.components.preview3D.shapes.NoteStatusModels;
import log.charter.gui.components.preview3D.shapes.NoteStatusModels.TextureAtlasPosition;
import log.charter.util.data.IntRange;

public class Preview3DFingeringDrawer {
	private static final double size = stringDistance / 2;

	private static Point2D textureGrid(final int x, final int y) {
		return new Point2D(x * 0.25, y * 0.25);
	}

	private static final Point2D fingerShapeSingle = textureGrid(0, 0);
	private static final Point2D fingerShapeEnd = textureGrid(1, 0);
	private static final Point2D fingerShapeMiddle = textureGrid(2, 0);

	private static final Point2D[] fingerTexturePositions = { //
			textureGrid(3, 0), // T
			textureGrid(0, 1), // 1
			textureGrid(1, 1), // 2
			textureGrid(2, 1), // 3
			textureGrid(3, 1)// 4
	};

	private ChartData chartData;
	private NoteStatusModels noteStatusModels;
	private TexturesHolder texturesHolder;

	public void init(final ChartData chartData, final NoteStatusModels noteStatusModels,
			final TexturesHolder texturesHolder) {
		this.chartData = chartData;
		this.noteStatusModels = noteStatusModels;
		this.texturesHolder = texturesHolder;
	}

	private void addQuad(final BaseTextureShaderDrawData drawData, final double x0, final double x1, final double y0,
			final double y1, final Point2D textureBase) {
		drawData.addZQuad(x0, x1, y0, y1, -0.01, textureBase.x + 0.001, textureBase.x + 0.249, textureBase.y + 0.001,
				textureBase.y + 0.249);
	}

	private void drawArpeggioPart(final ShadersHolder shadersHolder, final int string, final int fret,
			final TextureAtlasPosition texture, final double width, final double height) {
		final Matrix4 modelMatrix = moveMatrix(getFretMiddlePosition(fret), //
				getStringPosition(string, chartData.currentStrings()), //
				0);

		final Color color = getStringBasedColor(StringColorLabelType.NOTE, string, chartData.currentStrings());
		final int textureId = noteStatusModels.getTextureId(texture);

		shadersHolder.new ShadowHighlightTextureShaderDrawData()//
				.addZQuad(-width, width, height, -height, 0, 0, 1, 0, 1)//
				.draw(GL30.GL_QUADS, modelMatrix, textureId, color);
	}

	private void drawArpeggioFret(final ShadersHolder shadersHolder, final int string, final int fret) {
		drawArpeggioPart(shadersHolder, string, fret, TextureAtlasPosition.ARPEGGIO_FRET_BRACKET, noteHalfWidth,
				noteHalfWidth);
	}

	private void drawArpeggioOpen(final ShadersHolder shadersHolder, final Preview3DDrawData drawData, final int string,
			final int fret) {
		final IntRange frets = drawData.getFrets(0);
		if (frets == null) {
			return;
		}

		drawArpeggioPart(shadersHolder, string, frets.min, TextureAtlasPosition.ARPEGGIO_OPEN_BRACKET, noteHalfWidth,
				noteHalfWidth);
		drawArpeggioPart(shadersHolder, string, frets.max, TextureAtlasPosition.ARPEGGIO_OPEN_BRACKET, -noteHalfWidth,
				noteHalfWidth);
	}

	private void addArpeggioBrackets(final ShadersHolder shadersHolder, final Preview3DDrawData drawData,
			final ChordTemplate template) {
		if (!template.arpeggio) {
			return;
		}

		template.frets.forEach((string, fret) -> {
			if (fret <= drawData.capo) {
				drawArpeggioOpen(shadersHolder, drawData, string, fret);
			}
			if (fret > 0) {
				drawArpeggioFret(shadersHolder, string, fret);
			}
		});

	}

	private void addFingerSpot(final BaseTextureShaderDrawData drawData, final int fret, final int string,
			final Point2D fingerShapePosition, final boolean invertShape, final Point2D fingerNamePosition) {
		final double x = getFretMiddlePosition(fret);
		final double x0 = x - size;
		final double x1 = x + size;

		final double yString = getStringPosition(string, chartData.currentStrings());
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

		final int topString = GraphicalConfig.invertStrings ? strings.max : strings.min;
		addFingerSpot(drawData, fret, topString, fingerShapeEnd, false, fingerTexturePositions[finger]);

		for (int i = strings.min + 1; i < strings.max; i++) {
			addFingerSpot(drawData, fret, i, fingerShapeMiddle, false, null);
		}

		final int bottomString = GraphicalConfig.invertStrings ? strings.min : strings.max;
		addFingerSpot(drawData, fret, bottomString, fingerShapeEnd, true, null);
	}

	private void addFingering(final ShadersHolder shadersHolder, final ChordTemplate template) {
		if (template.fingers == null || template.fingers.isEmpty()) {
			return;
		}

		final BaseTextureShaderDrawData drawData = shadersHolder.new BaseTextureShaderDrawData();

		final IntRange[] fingerRanges = new IntRange[5];
		final int[] fingerFrets = new int[5];

		template.fingers.forEach((string, finger) -> {
			if (fingerRanges[finger] == null) {
				fingerRanges[finger] = new IntRange(string, string);
			} else {
				fingerRanges[finger] = fingerRanges[finger].extend(string);
			}
			fingerFrets[finger] = template.frets.getOrDefault(string, 1);
		});

		for (int i = 0; i < 5; i++) {
			if (fingerRanges[i] != null) {
				addFinger(drawData, i, fingerFrets[i], fingerRanges[i]);
			}
		}

		drawData.draw(GL30.GL_QUADS, Matrix4.identity, texturesHolder.getTextureId("fingering"));
	}

	private ChordTemplate findTemplateToUse(final Preview3DDrawData drawData) {
		final HandShapeDrawData handShape = lastBeforeEqual(drawData.handShapes, new Position(drawData.time + 20))
				.find();
		if (handShape == null || handShape.timeTo < drawData.time) {
			return null;
		}
		if (handShape.template.arpeggio) {
			return handShape.template;
		}

		final Level level = chartData.currentArrangementLevel();
		final ChordOrNote sound = lastBeforeEqual(level.sounds,
				FractionalPosition.fromTime(chartData.beats(), drawData.time + 20)).find();
		if (sound == null || sound.position(chartData.beats()) < handShape.timeFrom || sound.isNote()) {
			return handShape.template;
		}
		if (sound.chord().fullyMuted()) {
			return null;
		}

		return chartData.currentArrangement().chordTemplates.get(sound.chord().templateId());
	}

	public void draw(final ShadersHolder shadersHolder, final Preview3DDrawData drawData) {
		final ChordTemplate template = findTemplateToUse(drawData);
		if (template == null) {
			return;
		}

		GL30.glDisable(GL30.GL_DEPTH_TEST);
		addArpeggioBrackets(shadersHolder, drawData, template);
		addFingering(shadersHolder, template);
		GL30.glEnable(GL30.GL_DEPTH_TEST);
	}
}
