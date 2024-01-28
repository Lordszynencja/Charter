package log.charter.gui.components.preview3D.shaders;

import static java.util.Arrays.asList;

import java.awt.Color;

import log.charter.gui.components.preview3D.glUtils.Matrix4;
import log.charter.gui.components.preview3D.glUtils.Point3D;

public class BaseShader extends Shader {
	private final static String vertexShaderCode = "#version 330\n"//
			+ "layout(location = 0) in vec3 position;\n" //
			+ "layout(location = 1) in vec4 color;\n" //
			+ "\n" //
			+ "uniform mat4 sceneMatrix;\n" //
			+ "uniform mat4 modelMatrix;\n" //
			+ "\n" //
			+ "out vec4 outColor;\n" //
			+ "" //
			+ "void main() {\n" //
			+ "  gl_Position = vec4(position, 1.0) * modelMatrix * sceneMatrix;\n" //
			+ "  outColor = color;\n" //
			+ "}";

	private final static String fragmentShaderCode = "#version 330\n"//
			+ ""//
			+ "in vec4 outColor;\n"//
			+ ""//
			+ "out vec4 fragColor;\n"//
			+ "" //
			+ "void main() {\n" //
			+ "  fragColor = outColor;\n" //
			+ "}";

	public BaseShader() {
		super(vertexShaderCode, fragmentShaderCode, //
				asList("sceneMatrix", "modelMatrix"), //
				new int[] { 0, 1 }, //
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

	public void setPosition(final Point3D[] points) {
		setVBOPoint3DData(0, points);
	}

	public void setColors(final Color[] colors) {
		setVBORGBAData(1, colors);
	}

}
