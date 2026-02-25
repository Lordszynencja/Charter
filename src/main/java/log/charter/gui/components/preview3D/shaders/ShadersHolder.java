package log.charter.gui.components.preview3D.shaders;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL30;

import log.charter.gui.components.preview3D.glUtils.Matrix4;
import log.charter.gui.components.preview3D.glUtils.Point2D;
import log.charter.gui.components.preview3D.glUtils.Point3D;
import log.charter.gui.components.preview3D.shapes.Model;

public class ShadersHolder {
	public static final String BASE_SHADER_NAME = "baseShader";
	public static final String BASE_TEXTURE_SHADER_NAME = "baseTextureShader";
	public static final String VIDEO_SHADER_NAME = "videoShader";
	public static final String SHADOW_HIGHLIGHT_TEXTURE_SHADER_NAME = "shadowHighlightTextureShader";
	public static final String FADING_SHADER_NAME = "fadingShader";

	private final BaseShader baseShader = new BaseShader();
	private final BaseTextureShader baseTextureShader = new BaseTextureShader();
	private final VideoShader videoShader = new VideoShader();
	private final ShadowHighlightTextureShader shadowHighlightTextureShader = new ShadowHighlightTextureShader();
	private final FadingShader fadingShader = new FadingShader();

	private final Map<String, Shader> shadersMap = new HashMap<>();

	private String shaderInUse = null;
	private Matrix4 backgroundMatrix = Matrix4.identity;
	private Matrix4 fretboardMatrix = Matrix4.identity;

	public void initGL() {
		shadersMap.put(BASE_SHADER_NAME, baseShader);
		shadersMap.put(BASE_TEXTURE_SHADER_NAME, baseTextureShader);
		shadersMap.put(VIDEO_SHADER_NAME, videoShader);
		shadersMap.put(SHADOW_HIGHLIGHT_TEXTURE_SHADER_NAME, shadowHighlightTextureShader);
		shadersMap.put(FADING_SHADER_NAME, fadingShader);

		shadersMap.values().forEach(Shader::init);
	}

	public void useShader(final String shaderName) {
		if (shaderInUse != null && shaderInUse.equals(shaderName)) {
			return;
		}

		clearShader();
		shadersMap.get(shaderName).use();
		shaderInUse = shaderName;
	}

	public void clearShader() {
		if (shaderInUse == null) {
			return;
		}

		shadersMap.get(shaderInUse).stopUsing();
		shaderInUse = null;
	}

	public class BaseShaderDrawData {
		private final List<Point3D> points = new ArrayList<>();
		private final List<Color> colors = new ArrayList<>();

		public BaseShaderDrawData addVertex(final Point3D point, final Color color) {
			points.add(point);
			colors.add(color);
			return this;
		}

		public BaseShaderDrawData addPoints(final List<Point3D> points, final Color color) {
			for (final Point3D point : points) {
				addVertex(point, color);
			}

			return this;
		}

		public BaseShaderDrawData addModel(final Model model, final Color color) {
			return addPoints(model.getPoints(), color);
		}

		public BaseShaderDrawData draw(final int mode, final Matrix4 modelMatrix) {
			useShader(BASE_SHADER_NAME);

			baseShader.setModelMatrix(modelMatrix);

			baseShader.setPosition(points.toArray(new Point3D[0]));
			baseShader.setColors(colors.toArray(new Color[0]));

			GL30.glDrawArrays(mode, 0, points.size());

			return this;
		}
	}

	public class BaseTextureShaderDrawData {
		private final List<Point3D> points = new ArrayList<>();
		private final List<Point2D> texturePoints = new ArrayList<>();

		public BaseTextureShaderDrawData addVertex(final Point3D point, final Point2D texturePoint) {
			points.add(point);
			texturePoints.add(texturePoint);
			return this;
		}

		public BaseTextureShaderDrawData addZQuad(final double x0, final double x1, final double y0, final double y1,
				final double z, final double tx0, final double tx1, final double ty0, final double ty1) {
			return addVertex(new Point3D(x0, y0, z), new Point2D(tx0, ty0))//
					.addVertex(new Point3D(x0, y1, z), new Point2D(tx0, ty1))//
					.addVertex(new Point3D(x1, y1, z), new Point2D(tx1, ty1))//
					.addVertex(new Point3D(x1, y0, z), new Point2D(tx1, ty0));
		}

		public void draw(final int mode, final Matrix4 modelMatrix, final int textureId) {
			useShader(BASE_TEXTURE_SHADER_NAME);

			baseTextureShader.setModelMatrix(modelMatrix);
			baseTextureShader.setTextureId(textureId);

			baseTextureShader.setPosition(points.toArray(new Point3D[0]));
			baseTextureShader.setTexturePosition(texturePoints.toArray(new Point2D[0]));

			GL30.glDrawArrays(mode, 0, points.size());
		}
	}

	public class VideoShaderDrawData {
		private final List<Point2D> points = new ArrayList<>();
		private final List<Point2D> texturePoints = new ArrayList<>();

		public VideoShaderDrawData addVertex(final Point2D point, final Point2D texturePoint) {
			points.add(point);
			texturePoints.add(texturePoint);
			return this;
		}

		public VideoShaderDrawData addZQuad(final double x0, final double x1, final double y0, final double y1,
				final double tx0, final double tx1, final double ty0, final double ty1) {
			return addVertex(new Point2D(x0, y0), new Point2D(tx0, ty0))//
					.addVertex(new Point2D(x0, y1), new Point2D(tx0, ty1))//
					.addVertex(new Point2D(x1, y1), new Point2D(tx1, ty1))//
					.addVertex(new Point2D(x1, y0), new Point2D(tx1, ty0));
		}

		public void draw(final int textureId, final Color colorMultiplier) {
			useShader(VIDEO_SHADER_NAME);

			videoShader.setTextureId(textureId);
			videoShader.setColorMultiplier(colorMultiplier);

			videoShader.setPosition(points.toArray(new Point2D[0]));
			videoShader.setTexturePosition(texturePoints.toArray(new Point2D[0]));

			GL30.glDrawArrays(GL30.GL_QUADS, 0, points.size());
		}
	}

	public class ShadowHighlightTextureShaderDrawData {
		private final List<Point3D> points = new ArrayList<>();
		private final List<Point2D> texturePoints = new ArrayList<>();

		public ShadowHighlightTextureShaderDrawData addVertex(final Point3D point, final Point2D texturePoint) {
			points.add(point);
			texturePoints.add(texturePoint);
			return this;
		}

		public ShadowHighlightTextureShaderDrawData addZQuad(final double x0, final double x1, final double y0,
				final double y1, final double z, final double tx0, final double tx1, final double ty0,
				final double ty1) {
			return addVertex(new Point3D(x0, y0, z), new Point2D(tx0, ty0))//
					.addVertex(new Point3D(x0, y1, z), new Point2D(tx0, ty1))//
					.addVertex(new Point3D(x1, y1, z), new Point2D(tx1, ty1))//
					.addVertex(new Point3D(x1, y0, z), new Point2D(tx1, ty0));
		}

		public void draw(final int mode, final Matrix4 modelMatrix, final int textureId, final Color color) {
			useShader(SHADOW_HIGHLIGHT_TEXTURE_SHADER_NAME);

			shadowHighlightTextureShader.setModelMatrix(modelMatrix);
			shadowHighlightTextureShader.setTextureId(textureId);
			shadowHighlightTextureShader.setColor(color);

			shadowHighlightTextureShader.setPosition(points.toArray(new Point3D[0]));
			shadowHighlightTextureShader.setTexturePosition(texturePoints.toArray(new Point2D[0]));

			GL30.glDrawArrays(mode, 0, points.size());
		}
	}

	public class FadingShaderDrawData {
		private final List<Point3D> points = new ArrayList<>();
		private final List<Color> colors = new ArrayList<>();

		public FadingShaderDrawData addVertex(final Point3D point, final Color color) {
			points.add(point);
			colors.add(color);
			return this;
		}

		public FadingShaderDrawData addModel(final Model model, final Color color) {
			for (final Point3D point : model.getPoints()) {
				addVertex(point, color);
			}

			return this;
		}

		public void draw(final int mode, final Matrix4 modelMatrix, final float fadeStart, final float fadeEnd) {
			useShader(FADING_SHADER_NAME);

			fadingShader.setModelMatrix(modelMatrix);
			fadingShader.setFadeStart(fadeStart);
			fadingShader.setFadeEnd(fadeEnd);

			fadingShader.setPosition(points.toArray(new Point3D[0]));
			fadingShader.setColors(colors.toArray(new Color[0]));

			GL30.glDrawArrays(mode, 0, points.size());
		}
	}

	public void setBackgroundMatrix(final Matrix4 matrix) {
		backgroundMatrix = matrix;
	}

	public void setFretboardMatrix(final Matrix4 matrix) {
		fretboardMatrix = matrix;
	}

	private void useMatrix(final Matrix4 matrix) {
		baseShader.use();
		baseShader.setSceneMatrix(matrix);

		baseTextureShader.use();
		baseTextureShader.setSceneMatrix(matrix);

		shadowHighlightTextureShader.use();
		shadowHighlightTextureShader.setSceneMatrix(matrix);

		fadingShader.use();
		fadingShader.setSceneMatrix(matrix);
	}

	public void useBackgroundMatrix() {
		useMatrix(backgroundMatrix);
	}

	public void useFretboardMatrix() {
		useMatrix(fretboardMatrix);
	}
}
