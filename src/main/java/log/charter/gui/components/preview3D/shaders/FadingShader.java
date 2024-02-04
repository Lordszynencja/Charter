package log.charter.gui.components.preview3D.shaders;

import static java.util.Arrays.asList;

import java.awt.Color;

import log.charter.gui.components.preview3D.glUtils.Matrix4;
import log.charter.gui.components.preview3D.glUtils.Point3D;

public class FadingShader extends Shader {
	private final static String vertexShaderCode = "#version 330\n"//
			+ "layout(location = 9) in vec3 position;\n" //
			+ "layout(location = 10) in vec4 color;\n" //
			+ "\n" //
			+ "uniform mat4 sceneMatrix;\n" //
			+ "uniform mat4 modelMatrix;\n" //
			+ "\n" //
			+ "out vec4 outColor;\n" //
			+ "out float outZ;\n" //
			+ "" //
			+ "void main() {\n" //
			+ "  gl_Position = vec4(position, 1.0) * modelMatrix * sceneMatrix;\n" //
			+ "  outColor = color;\n" //
			+ "  outZ = position.z;\n" //
			+ "}";

	private final static String fragmentShaderCode = "#version 330\n"//
			+ "uniform float fadeStart;\n"//
			+ "uniform float fadeEnd;\n"//
			+ ""//
			+ "in vec4 outColor;\n"//
			+ "in float outZ;\n"//
			+ ""//
			+ "out vec4 fragColor;\n"//
			+ "" //
			+ "void main() {\n" //
			+ "  fragColor = outColor * clamp((outZ - fadeEnd) / (fadeStart - fadeEnd), 0, 1);\n" //
			+ "}";

	public FadingShader() {
		super(vertexShaderCode, fragmentShaderCode, //
				asList("sceneMatrix", "modelMatrix", "fadeStart", "fadeEnd"), //
				new int[] { 9, 10 }, //
				new int[] { 3, 4 });
	}

	public void setSceneMatrix(final Matrix4 matrix) {
		setUniformMatrix4fv("sceneMatrix", matrix);
	}

	public void setModelMatrix(final Matrix4 matrix) {
		setUniformMatrix4fv("modelMatrix", matrix);
	}

	public void clearModelMatrix() {
		setModelMatrix(Matrix4.identity);
	}

	public void setFadeStart(final float fadeStart) {
		setUniform1f("fadeStart", fadeStart);
	}

	public void setFadeEnd(final float fadeEnd) {
		setUniform1f("fadeEnd", fadeEnd);
	}

	public void setPosition(final Point3D[] points) {
		setVBOPoint3DData(0, points);
	}

	public void setColors(final Color[] colors) {
		setVBORGBAData(1, colors);
	}

}
