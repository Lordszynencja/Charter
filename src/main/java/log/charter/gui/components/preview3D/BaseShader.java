package log.charter.gui.components.preview3D;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL30;

import log.charter.gui.components.preview3D.shapes.Model;
import log.charter.io.Logger;

public class BaseShader {
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

	public class BaseShaderDrawData {
		private final List<Point3D> points = new ArrayList<>();
		private final List<Color> colors = new ArrayList<>();

		public BaseShaderDrawData addVertex(final Point3D point, final Color color) {
			points.add(point);
			colors.add(color);
			return this;
		}

		public BaseShaderDrawData addModel(final Model model, final Color color) {
			for (final Point3D point : model.getPoints()) {
				addVertex(point, color);
			}
			return this;
		}

		public void draw(final int mode) {
			setPosition(points.toArray(new Point3D[0]));
			setColors(colors.toArray(new Color[0]));

			GL30.glDrawArrays(mode, 0, points.size());
		}
	}

	private int program;
	private int vertexShader;
	private int fragmentShader;

	private final int[] vbo = new int[2];
	private int sceneMatrixUniform;
	private int modelMatrixUniform;

	private int compileShader(final int type, final String code) {
		final int shader = GL30.glCreateShader(type);
		GL30.glShaderSource(shader, code);
		GL30.glCompileShader(shader);

		return shader;
	}

	private void logShaderCompile(final String name, final int shader) {
		final int comp = GL30.glGetShaderi(shader, GL30.GL_COMPILE_STATUS);
		final int len = GL30.glGetShaderi(shader, GL30.GL_INFO_LOG_LENGTH);
		final String err = GL30.glGetShaderInfoLog(shader, len);
		if (err != null && err.length() != 0) {
			Logger.error(name + " shader compile log:\n" + err + "\n");
		}
		if (comp == GL30.GL_FALSE) {
			Logger.error("Could not compile " + name + " shader");
		}
	}

	public void init() {
		vertexShader = compileShader(GL30.GL_VERTEX_SHADER, vertexShaderCode);
		logShaderCompile("vertex", vertexShader);

		fragmentShader = compileShader(GL30.GL_FRAGMENT_SHADER, fragmentShaderCode);
		logShaderCompile("fragment", fragmentShader);

		program = GL30.glCreateProgram();
		GL30.glAttachShader(program, vertexShader);
		GL30.glAttachShader(program, fragmentShader);
		GL30.glLinkProgram(program);

		sceneMatrixUniform = GL30.glGetUniformLocation(program, "sceneMatrix");
		modelMatrixUniform = GL30.glGetUniformLocation(program, "modelMatrix");

		GL30.glGenBuffers(vbo);
		GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vbo[0]);
		GL30.glVertexAttribPointer(0, 3, GL30.GL_FLOAT, false, 0, 0L);
		GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vbo[1]);
		GL30.glVertexAttribPointer(1, 4, GL30.GL_FLOAT, false, 0, 0L);
		GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
	}

	public void use() {
		GL30.glUseProgram(program);
		GL30.glEnableVertexAttribArray(0);
		GL30.glEnableVertexAttribArray(1);
	}

	public void stopUsing() {
		GL30.glDisableVertexAttribArray(1);
		GL30.glDisableVertexAttribArray(0);
		GL30.glUseProgram(0);
	}

	public void setSceneMatrix(final Matrix4 sceneMatrix) {
		final float[] matrix = new float[16];
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {
				matrix[x * 4 + y] = (float) sceneMatrix.matrix[x][y];
			}
		}

		GL30.glUniformMatrix4fv(sceneMatrixUniform, false, matrix);
	}

	public void setModelMatrix(final Matrix4 sceneMatrix) {
		final float[] matrix = new float[16];
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {
				matrix[x * 4 + y] = (float) sceneMatrix.matrix[x][y];
			}
		}

		GL30.glUniformMatrix4fv(modelMatrixUniform, false, matrix);
	}

	public void clearModelMatrix() {
		setModelMatrix(Matrix4.identity);
	}

	public void setPosition(final Point3D[] points) {
		final float[] data = new float[points.length * 3];
		for (int i = 0; i < points.length; i++) {
			data[i * 3] = (float) points[i].x;
			data[i * 3 + 1] = (float) points[i].y;
			data[i * 3 + 2] = (float) points[i].z;
		}

		GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vbo[0]);
		GL30.glBufferData(GL30.GL_ARRAY_BUFFER, data, GL30.GL_STREAM_DRAW);
		GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
	}

	public void setColors(final Color[] colors) {
		final float[] data = new float[colors.length * 4];
		for (int i = 0; i < colors.length; i++) {
			data[i * 4] = colors[i].getRed() / 255f;
			data[i * 4 + 1] = colors[i].getGreen() / 255f;
			data[i * 4 + 2] = colors[i].getBlue() / 255f;
			data[i * 4 + 3] = colors[i].getAlpha() / 255f;
		}

		GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vbo[1]);
		GL30.glBufferData(GL30.GL_ARRAY_BUFFER, data, GL30.GL_STREAM_DRAW);
		GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
	}

	public void drawModel(final Model model, final Color color) {
		new BaseShaderDrawData().addModel(model, color).draw(model.getDrawMode());
	}
}
