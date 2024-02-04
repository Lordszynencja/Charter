package log.charter.gui.components.preview3D.shaders;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL30;

import log.charter.gui.components.preview3D.glUtils.Matrix4;
import log.charter.gui.components.preview3D.glUtils.Point2D;
import log.charter.gui.components.preview3D.glUtils.Point3D;
import log.charter.io.Logger;

public abstract class Shader {
	private static int compileShader(final int type, final String code) {
		final int shader = GL30.glCreateShader(type);
		GL30.glShaderSource(shader, code);
		GL30.glCompileShader(shader);

		return shader;
	}

	private static void logShaderCompile(final String name, final int shader) {
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

	private final String vertexShaderCode;
	private final String fragmentShaderCode;
	private final List<String> uniformNames;

	private int program;
	private int vertexShader;
	private int fragmentShader;

	private final Map<String, Integer> uniforms = new HashMap<>();
	private final int[] vbo;
	private final int[] vboLocations;
	private final int[] vboSizes;

	public Shader(final String vertexShaderCode, final String fragmentShaderCode, final List<String> uniformNames,
			final int[] vboLocations, final int[] vboSizes) {
		this.vertexShaderCode = vertexShaderCode;
		this.fragmentShaderCode = fragmentShaderCode;
		this.uniformNames = uniformNames;
		vbo = new int[vboSizes.length];
		this.vboLocations = vboLocations;
		this.vboSizes = vboSizes;
	}

	private void prepareShadersAndProgram() {
		vertexShader = compileShader(GL30.GL_VERTEX_SHADER, vertexShaderCode);
		logShaderCompile("vertex", vertexShader);

		fragmentShader = compileShader(GL30.GL_FRAGMENT_SHADER, fragmentShaderCode);
		logShaderCompile("fragment", fragmentShader);

		program = GL30.glCreateProgram();
		GL30.glAttachShader(program, vertexShader);
		GL30.glAttachShader(program, fragmentShader);
		GL30.glLinkProgram(program);
	}

	public void init() {
		prepareShadersAndProgram();

		for (final String uniformName : uniformNames) {
			uniforms.put(uniformName, GL30.glGetUniformLocation(program, uniformName));
		}

		for (int i = 0; i < vbo.length; i++) {
			vbo[i] = GL30.glGenBuffers();
			GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vbo[i]);
			GL30.glVertexAttribPointer(vboLocations[i], vboSizes[i], GL30.GL_FLOAT, false, 0, 0L);
		}

		GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
	}

	public void use() {
		GL30.glUseProgram(program);
		for (int i = 0; i < vbo.length; i++) {
			GL30.glEnableVertexAttribArray(vboLocations[i]);
		}

		GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
	}

	public void stopUsing() {
		for (int i = 0; i < vbo.length; i++) {
			GL30.glDisableVertexAttribArray(vboLocations[i]);
		}

		GL30.glUseProgram(0);
	}

	protected void setUniform1ui(final String uniformName, final int value) {
		GL30.glUniform1ui(uniforms.get(uniformName), value);
	}

	protected void setUniform1f(final String uniformName, final float value) {
		GL30.glUniform1f(uniforms.get(uniformName), value);
	}

	protected void setUniform4fv(final String uniformName, final float[] v) {
		if (v.length != 4) {
			throw new IllegalArgumentException("wrong size of uniform4 values array: " + v.length);
		}

		GL30.glUniform4fv(uniforms.get(uniformName), v);
	}

	protected void setUniformColorRGBA(final String uniformName, final Color color) {
		setUniform4fv(uniformName, color.getComponents(null));
	}

	protected void setUniformMatrix4fv(final String uniformName, final Matrix4 matrix) {
		final float[] matrixData = new float[16];
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {
				matrixData[x * 4 + y] = (float) matrix.matrix[x][y];
			}
		}

		GL30.glUniformMatrix4fv(uniforms.get(uniformName), false, matrixData);
	}

	protected void setVBOPoint2DData(final int vboId, final Point2D[] points) {
		final float[] data = new float[points.length * 2];
		for (int i = 0; i < points.length; i++) {
			data[i * 2] = (float) points[i].x;
			data[i * 2 + 1] = (float) points[i].y;
		}

		GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vbo[vboId]);
		GL30.glBufferData(GL30.GL_ARRAY_BUFFER, data, GL30.GL_STREAM_DRAW);
		GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
	}

	protected void setVBOPoint3DData(final int vboId, final Point3D[] points) {
		final float[] data = new float[points.length * 3];
		for (int i = 0; i < points.length; i++) {
			data[i * 3] = (float) points[i].x;
			data[i * 3 + 1] = (float) points[i].y;
			data[i * 3 + 2] = (float) points[i].z;
		}

		GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vbo[vboId]);
		GL30.glBufferData(GL30.GL_ARRAY_BUFFER, data, GL30.GL_STREAM_DRAW);
		GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
	}

	protected void setVBORGBAData(final int vboId, final Color[] colors) {
		final float[] data = new float[colors.length * 4];
		for (int i = 0; i < colors.length; i++) {
			data[i * 4] = colors[i].getRed() / 255f;
			data[i * 4 + 1] = colors[i].getGreen() / 255f;
			data[i * 4 + 2] = colors[i].getBlue() / 255f;
			data[i * 4 + 3] = colors[i].getAlpha() / 255f;
		}

		GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vbo[vboId]);
		GL30.glBufferData(GL30.GL_ARRAY_BUFFER, data, GL30.GL_STREAM_DRAW);
		GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
	}
}
