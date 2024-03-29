package log.charter.gui.components.preview3D.shaders;

import static java.util.Arrays.asList;

import org.lwjgl.opengl.GL30;

import log.charter.gui.components.preview3D.glUtils.Matrix4;
import log.charter.gui.components.preview3D.glUtils.Point2D;
import log.charter.gui.components.preview3D.glUtils.Point3D;

public class BaseTextureShader extends Shader {
	private final static String vertexShaderCode = "#version 330\n"//
			+ "layout(location = 3) in vec3 position;\n" //
			+ "layout(location = 4) in vec2 texturePosition;\n" //
			+ "\n" //
			+ "uniform mat4 sceneMatrix;\n" //
			+ "uniform mat4 modelMatrix;\n" //
			+ "\n" //
			+ "out vec2 outTexturePosition;\n" //
			+ "" //
			+ "void main() {\n" //
			+ "  gl_Position = vec4(position, 1.0) * modelMatrix * sceneMatrix;\n" //
			+ "  outTexturePosition = texturePosition;\n" //
			+ "}";

	private final static String fragmentShaderCode = "#version 330\n"//
			+ "uniform sampler2D textureId;\n" //
			+ "\n"//
			+ "in vec2 outTexturePosition;\n"//
			+ "\n"//
			+ "out vec4 fragColor;\n"//
			+ "" //
			+ "void main() {\n" //
			+ "  fragColor = texture(textureId, outTexturePosition);\n" //
			+ "}";

	public BaseTextureShader() {
		super(vertexShaderCode, fragmentShaderCode, //
				asList("sceneMatrix", "modelMatrix", "textureId"), //
				new int[] { 3, 4 }, //
				new int[] { 3, 2 });
	}

	public void setSceneMatrix(final Matrix4 sceneMatrix) {
		setUniformMatrix4fv("sceneMatrix", sceneMatrix);
	}

	public void setModelMatrix(final Matrix4 sceneMatrix) {
		setUniformMatrix4fv("modelMatrix", sceneMatrix);
	}

	public void clearModelMatrix() {
		setModelMatrix(Matrix4.identity);
	}

	public void setTextureId(final int textureId) {
		GL30.glActiveTexture(GL30.GL_TEXTURE0);
		GL30.glBindTexture(GL30.GL_TEXTURE_2D, textureId);
		setUniform1ui("textureId", textureId);
	}

	public void setPosition(final Point3D[] points) {
		setVBOPoint3DData(0, points);
	}

	public void setTexturePosition(final Point2D[] points) {
		setVBOPoint2DData(1, points);
	}
}
