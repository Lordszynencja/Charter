package log.charter.gui.components.preview3D.drawers;

import static log.charter.gui.components.preview3D.Preview3DUtils.closeDistanceZ;
import static log.charter.gui.components.preview3D.Preview3DUtils.fadedDistanceZ;
import static log.charter.gui.components.preview3D.Preview3DUtils.getChartboardYPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getFretPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getTimePosition;
import static log.charter.util.Utils.isDottedFret;

import java.awt.Color;

import org.lwjgl.opengl.GL30;

import log.charter.data.ChartData;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.components.preview3D.data.FHPDrawData;
import log.charter.gui.components.preview3D.data.Preview3DDrawData;
import log.charter.gui.components.preview3D.glUtils.Matrix4;
import log.charter.gui.components.preview3D.glUtils.Point3D;
import log.charter.gui.components.preview3D.shaders.ShadersHolder;
import log.charter.gui.components.preview3D.shaders.ShadersHolder.FadingShaderDrawData;

public class Preview3DFHPsDrawer {
	private ChartData chartData;

	public void init(final ChartData data) {
		chartData = data;
	}

	private void addAnchor(final FadingShaderDrawData shaderDrawData, final FHPDrawData anchor, final double time) {
		if (anchor.timeTo < anchor.timeFrom) {
			return;
		}

		final double y = getChartboardYPosition(chartData.currentStrings()) - 0.001;
		final double t0 = anchor.timeFrom - time;
		final double t1 = anchor.timeTo - time;
		final double z0 = getTimePosition(t0);
		final double z1 = getTimePosition(t1);

		for (int fret = anchor.fretFrom + 1; fret <= anchor.fretTo; fret++) {
			final double x0 = getFretPosition(fret - 1);
			final double x1 = getFretPosition(fret);
			final Color color = (isDottedFret(fret) ? ColorLabel.PREVIEW_3D_LANE_DOTTED : ColorLabel.PREVIEW_3D_LANE)
					.color();

			shaderDrawData.addVertex(new Point3D(x0, y, z0), color)//
					.addVertex(new Point3D(x1, y, z0), color)//
					.addVertex(new Point3D(x1, y, z1), color)//
					.addVertex(new Point3D(x0, y, z1), color);
		}
	}

	public void draw(final ShadersHolder shadersHolder, final Preview3DDrawData drawData) {
		final FadingShaderDrawData shaderDrawData = shadersHolder.new FadingShaderDrawData();

		drawData.fhps.forEach(anchorToDraw -> addAnchor(shaderDrawData, anchorToDraw, drawData.time));

		shaderDrawData.draw(GL30.GL_QUADS, Matrix4.identity, closeDistanceZ, fadedDistanceZ);
	}
}
