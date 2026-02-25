package log.charter.gui.components.preview3D.drawers;

import static java.util.Arrays.asList;
import static log.charter.gui.components.preview3D.glUtils.Matrix4.moveMatrix;
import static log.charter.gui.components.preview3D.glUtils.Matrix4.rotationXMatrix;
import static log.charter.gui.components.preview3D.glUtils.Matrix4.rotationYMatrix;
import static log.charter.gui.components.preview3D.glUtils.Matrix4.scaleMatrix;
import static log.charter.util.CollectionUtils.lastBeforeEqual;

import java.awt.Color;
import java.util.List;

import org.lwjgl.opengl.GL30;

import log.charter.data.ChartData;
import log.charter.data.song.Showlight;
import log.charter.data.song.Showlight.ShowlightType;
import log.charter.data.song.position.FractionalPosition;
import log.charter.gui.components.preview3D.data.Preview3DDrawData;
import log.charter.gui.components.preview3D.glUtils.Matrix4;
import log.charter.gui.components.preview3D.glUtils.Point3D;
import log.charter.gui.components.preview3D.shaders.ShadersHolder;
import log.charter.gui.components.preview3D.shaders.ShadersHolder.BaseShaderDrawData;
import log.charter.util.ColorUtils;

public class Preview3DShowlightsDrawer {
	private static final List<Point3D> beamPoints = asList(//
			new Point3D(-30, 23, 40), //
			new Point3D(-15, 24, 40), //
			new Point3D(0, 25, 40), //
			new Point3D(15, 24, 40), //
			new Point3D(30, 23, 40));

	private ChartData chartData;

	public void init(final ChartData chartData) {
		this.chartData = chartData;
	}

	private ShowlightType currentFog(final double time) {
		final Showlight fog = lastBeforeEqual(chartData.showlightsFog(),
				FractionalPosition.fromTime(chartData.beats(), time)).find();
		ShowlightType fogType = ShowlightType.FOG_GREEN;
		if (fog != null) {
			for (final ShowlightType type : fog.types) {
				if (type.isFog) {
					fogType = type;
				}
			}
		}

		return fogType;
	}

	private void drawFog(final ShadersHolder shadersHolder, final Preview3DDrawData drawData, final Matrix4 rotation,
			final ShowlightType fog) {
		final Color baseColor1 = ColorUtils.setAlpha(fog.color.darker().darker().darker(), 128);
		final Color baseColor2 = ColorUtils.setAlpha(baseColor1, 96);
		final Color baseColor3 = ColorUtils.setAlpha(baseColor1, 64);
		final double x0 = -100;
		final double x1 = 100;
		final double y0 = -20;
		final double y1 = 0;
		final double y2 = 80;
		final double z0 = -10;
		final double z1 = 50;

		final Point3D p000 = new Point3D(x0, y0, z0);
		final Point3D p100 = new Point3D(x1, y0, z0);
		final Point3D p001 = new Point3D(x0, y0, z1);
		final Point3D p101 = new Point3D(x1, y0, z1);
		final Point3D p011 = new Point3D(x0, y1, z1);
		final Point3D p111 = new Point3D(x1, y1, z1);
		final Point3D p021 = new Point3D(x0, y2, z1);
		final Point3D p121 = new Point3D(x1, y2, z1);
		final Point3D p020 = new Point3D(x0, y2, z0);
		final Point3D p120 = new Point3D(x1, y2, z0);
		final Point3D p010 = new Point3D(x0, y1, z0);
		final Point3D p110 = new Point3D(x1, y1, z0);

		shadersHolder.new BaseShaderDrawData()//
				.addVertex(p000, baseColor3).addVertex(p100, baseColor3)//
				.addVertex(p001, baseColor1).addVertex(p101, baseColor1)//
				.addVertex(p011, baseColor2).addVertex(p111, baseColor2)//
				.addVertex(p021, baseColor1).addVertex(p121, baseColor1)//
				.addVertex(p020, baseColor3).addVertex(p120, baseColor3)//
				.draw(GL30.GL_QUAD_STRIP, rotation);
		shadersHolder.new BaseShaderDrawData()//
				.addVertex(p000, baseColor3).addVertex(p001, baseColor1)//
				.addVertex(p010, baseColor3).addVertex(p011, baseColor2)//
				.addVertex(p020, baseColor3).addVertex(p021, baseColor1)//
				.draw(GL30.GL_QUAD_STRIP, rotation);
		shadersHolder.new BaseShaderDrawData()//
				.addVertex(p100, baseColor3).addVertex(p101, baseColor1)//
				.addVertex(p110, baseColor3).addVertex(p111, baseColor2)//
				.addVertex(p120, baseColor3).addVertex(p121, baseColor1)//
				.draw(GL30.GL_QUAD_STRIP, rotation);
	}

	private BaseShaderDrawData generateBeamDrawData(final ShadersHolder shadersHolder, final double size,
			final Color color) {
		final Color fadeColor1 = ColorUtils.setAlpha(color, 32);
		final Color fadeColor2 = ColorUtils.setAlpha(color, 0);
		return shadersHolder.new BaseShaderDrawData()//
				.addVertex(new Point3D(0, 0, 0), color)//
				.addVertex(new Point3D(-size, 0, 0), fadeColor1)//
				.addVertex(new Point3D(-size, size, 0), fadeColor2)//
				.addVertex(new Point3D(0, size, 0), fadeColor1)//
				.addVertex(new Point3D(size, size, 0), fadeColor2)//
				.addVertex(new Point3D(size, 0, 0), fadeColor1)//
				.addVertex(new Point3D(size, -size, 0), fadeColor2)//
				.addVertex(new Point3D(0, -size, 0), fadeColor1)//
				.addVertex(new Point3D(-size, -size, 0), fadeColor2)//
				.addVertex(new Point3D(-size, 0, 0), fadeColor1);
	}

	private void drawBeams(final ShadersHolder shadersHolder, final Preview3DDrawData drawData,
			final Matrix4 rotation) {
		final Showlight beams = lastBeforeEqual(chartData.showlightsBeam(),
				FractionalPosition.fromTime(chartData.beats(), drawData.time)).find();
		ShowlightType beamsType = ShowlightType.BEAMS_OFF;
		if (beams != null) {
			for (final ShowlightType type : beams.types) {
				if (type.isBeam) {
					beamsType = type;
				}
			}
		}

		if (beamsType == ShowlightType.BEAMS_OFF) {
			return;
		}

		final BaseShaderDrawData shadersDrawData = generateBeamDrawData(shadersHolder, 3, beamsType.color);

		for (final Point3D p : beamPoints) {
			shadersDrawData.draw(GL30.GL_TRIANGLE_FAN, moveMatrix(p));
		}

		if (beamsType.name().contains("PLUS")) {
			final Matrix4 matrix = moveMatrix(-80, 40, 45)//
					.multiply(scaleMatrix(3, 3, 1))//
					.multiply(Matrix4.rotationXMatrix(0.3))//
					.multiply(Matrix4.rotationYMatrix(-0.5));
			shadersDrawData.draw(GL30.GL_TRIANGLE_FAN, matrix);
		}
	}

	private void drawLasers(final ShadersHolder shadersHolder, final Preview3DDrawData drawData, final Matrix4 rotation,
			final ShowlightType fog) {
		final Showlight lasers = lastBeforeEqual(chartData.showlightsLaser(),
				FractionalPosition.fromTime(chartData.beats(), drawData.time)).find();
		ShowlightType lasersType = ShowlightType.LASERS_OFF;
		if (lasers != null) {
			for (final ShowlightType type : lasers.types) {
				if (type.isLaser) {
					lasersType = type;
				}
			}
		}

		if (lasersType == ShowlightType.LASERS_OFF) {
			return;
		}

		final Color color = ColorUtils.setAlpha(fog.color, 192);
		final Color fadeColor1 = ColorUtils.setAlpha(color, 128);
		final Color fadeColor2 = ColorUtils.setAlpha(color, 0);

		final BaseShaderDrawData shadersDrawData = shadersHolder.new BaseShaderDrawData()//
				.addVertex(new Point3D(0, 0, -5), fadeColor1)//
				.addVertex(new Point3D(0, 0, -10), fadeColor2)//
				.addVertex(new Point3D(-0.25, 0, -5), fadeColor2)//
				.addVertex(new Point3D(-0.25, 0, 0), fadeColor2)//
				.addVertex(new Point3D(0, 0, 0), color)//
				.addVertex(new Point3D(0.25, 0, 0), fadeColor2)//
				.addVertex(new Point3D(0.25, 0, -5), fadeColor2)//
				.addVertex(new Point3D(0, 0, -10), fadeColor2);

		final Matrix4 movement = moveMatrix(40, 30, 35);
		final Matrix4 rotationY = rotationYMatrix(0.8);
		final Matrix4 rotationX = rotationXMatrix(0.8);
		final Matrix4 scaling = scaleMatrix(1, 1, 5);

		final Matrix4 matrix = Matrix4.create(movement, //
				rotationY, //
				rotationX, //
				scaling);

		final int rays = 8;
		final double multiplier = Math.PI / rays * 2;
		for (int i = 0; i < rays; i++) {
			final double x = Math.sin(i * multiplier) * 5;
			final double y = Math.cos(i * multiplier) * 5;
			final Matrix4 rotationZ = Matrix4.rotationZMatrix((i + 0.5) * multiplier);
			final Matrix4 rayMatrix = Matrix4.create(Matrix4.moveMatrix(x, y, 0), //
					movement, //
					rotationY, //
					rotationX, //
					rotationZ, //
					scaling);
			shadersDrawData.draw(GL30.GL_TRIANGLE_FAN, rayMatrix);
		}
	}

	public void draw(final ShadersHolder shadersHolder, final Preview3DDrawData drawData) {
		final Matrix4 rotation = Matrix4.rotationXMatrix(Math.sin(System.currentTimeMillis() / 1500.0) / 100)
				.multiply(Matrix4.rotationYMatrix(Math.sin(System.currentTimeMillis() / 2500.0) / 100));
		final ShowlightType fog = currentFog(drawData.time);

		GL30.glDisable(GL30.GL_DEPTH_TEST);
		drawFog(shadersHolder, drawData, rotation, fog);
		drawBeams(shadersHolder, drawData, rotation);
		drawLasers(shadersHolder, drawData, rotation, fog);
		GL30.glEnable(GL30.GL_DEPTH_TEST);
	}
}
