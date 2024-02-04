package log.charter.gui.components.preview3D.shaders;

import static java.util.Arrays.asList;

import java.awt.Color;

import org.lwjgl.opengl.GL30;

import log.charter.gui.components.preview3D.glUtils.Point2D;

public class VideoShader extends Shader {
	private final static String vertexShaderCode = "#version 330\n"//
			+ "layout(location = 5) in vec2 position;\n" //
			+ "layout(location = 6) in vec2 texturePosition;\n" //
			+ "\n" //
			+ "\n" //
			+ "out vec2 outTexturePosition;\n" //
			+ "" //
			+ "void main() {\n" //
			+ "  gl_Position = vec4(position, 1.0, 1.0);\n" //
			+ "  outTexturePosition = texturePosition;\n" //
			+ "}";

	private final static String fragmentShaderCode = "#version 330\n"//
			+ "uniform sampler2D textureId;\n" //
			+ "uniform vec4 colorMultiplier;\n" //
			+ "\n"//
			+ "in vec2 outTexturePosition;\n"//
			+ "\n"//
			+ "out vec4 fragColor;\n"//
			+ "" //
			+ "void main() {\n" //
			+ "  fragColor = texture(textureId, outTexturePosition) * colorMultiplier;\n" //
			+ "}";

	public VideoShader() {
		super(vertexShaderCode, fragmentShaderCode, //
				asList("textureId", "colorMultiplier"), //
				new int[] { 5, 6 }, //
				new int[] { 2, 2 });
	}

	public void setTextureId(final int textureId) {
		GL30.glActiveTexture(GL30.GL_TEXTURE0);
		GL30.glBindTexture(GL30.GL_TEXTURE_2D, textureId);
		setUniform1ui("textureId", textureId);
	}

	public void setColorMultiplier(final Color color) {
		setUniformColorRGBA("colorMultiplier", color);
	}

	public void setPosition(final Point2D[] points) {
		setVBOPoint2DData(0, points);
	}

	public void setTexturePosition(final Point2D[] points) {
		setVBOPoint2DData(1, points);
	}
}
