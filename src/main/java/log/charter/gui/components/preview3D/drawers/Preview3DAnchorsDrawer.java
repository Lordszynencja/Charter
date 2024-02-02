package log.charter.gui.components.preview3D.drawers;

import static log.charter.gui.components.preview3D.Preview3DUtils.closeDistance;
import static log.charter.gui.components.preview3D.Preview3DUtils.closeDistanceZ;
import static log.charter.gui.components.preview3D.Preview3DUtils.getChartboardYPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getFretPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getTimePosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.visibility;
import static log.charter.util.Utils.isDottedFret;

import java.awt.Color;
import java.util.List;

import org.lwjgl.opengl.GL30;

import log.charter.data.ChartData;
import log.charter.data.managers.RepeatManager;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.components.preview3D.data.AnchorDrawData;
import log.charter.gui.components.preview3D.glUtils.Matrix4;
import log.charter.gui.components.preview3D.glUtils.Point3D;
import log.charter.gui.components.preview3D.shaders.ShadersHolder;
import log.charter.gui.components.preview3D.shaders.ShadersHolder.BaseShaderDrawData;

public class Preview3DAnchorsDrawer {
	private ChartData data;
	private RepeatManager repeatManager;

	public void init(final ChartData data, final RepeatManager repeatManager) {
		this.data = data;
		this.repeatManager = repeatManager;
	}

	private Color getColorWithTransparency(final Color color, final int time) {
		if (time > closeDistance) {
			return color;
		}

		final int alpha = Math.max(0, Math.min(color.getAlpha(), color.getAlpha() * time / closeDistance));
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}

	private void addAnchor(final BaseShaderDrawData drawData, final AnchorDrawData anchor) {
		if (anchor.timeTo < anchor.timeFrom) {
			return;
		}

		final double y = getChartboardYPosition(data.currentStrings()) - 0.001;
		final int t0 = anchor.timeFrom - data.time;
		final int t1 = anchor.timeTo - data.time;
		final double z0 = getTimePosition(t0);
		final double z1 = getTimePosition(t1);

		for (int fret = anchor.fretFrom + 1; fret <= anchor.fretTo; fret++) {
			final double x0 = getFretPosition(fret - 1);
			final double x1 = getFretPosition(fret);
			final Color color = (isDottedFret(fret) ? ColorLabel.PREVIEW_3D_LANE_DOTTED : ColorLabel.PREVIEW_3D_LANE)
					.color();

			if (t0 > closeDistance) {
				drawData.addVertex(new Point3D(x0, y, z0), color)//
						.addVertex(new Point3D(x1, y, z0), color)//
						.addVertex(new Point3D(x1, y, z1), color)//
						.addVertex(new Point3D(x0, y, z1), color);
			} else if (t1 < closeDistance) {
				final Color color0 = getColorWithTransparency(color, t0);
				final Color color1 = getColorWithTransparency(color, t1);

				drawData.addVertex(new Point3D(x0, y, z0), color0)//
						.addVertex(new Point3D(x1, y, z0), color0)//
						.addVertex(new Point3D(x1, y, z1), color1)//
						.addVertex(new Point3D(x0, y, z1), color1);
			} else {
				final Color color0 = getColorWithTransparency(color, t0);
				drawData.addVertex(new Point3D(x0, y, z0), color0)//
						.addVertex(new Point3D(x1, y, z0), color0)//
						.addVertex(new Point3D(x1, y, closeDistanceZ), color)//
						.addVertex(new Point3D(x0, y, closeDistanceZ), color)
						.addVertex(new Point3D(x0, y, closeDistanceZ), color)//
						.addVertex(new Point3D(x1, y, closeDistanceZ), color)//
						.addVertex(new Point3D(x1, y, z1), color)//
						.addVertex(new Point3D(x0, y, z1), color);
			}
		}
	}

	public void draw(final ShadersHolder shadersHolder) {
		final BaseShaderDrawData drawData = shadersHolder.new BaseShaderDrawData();

		final List<AnchorDrawData> anchorsToDraw = AnchorDrawData.getAnchorsForTimeSpanWithRepeats(data, repeatManager,
				data.time, data.time + visibility);
		anchorsToDraw.forEach(anchorToDraw -> addAnchor(drawData, anchorToDraw));

		drawData.draw(GL30.GL_QUADS, Matrix4.identity);
	}
}
