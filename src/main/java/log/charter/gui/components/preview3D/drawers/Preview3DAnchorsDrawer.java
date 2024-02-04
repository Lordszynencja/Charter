package log.charter.gui.components.preview3D.drawers;

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
import log.charter.gui.components.preview3D.shaders.ShadersHolder.FadingShaderDrawData;

public class Preview3DAnchorsDrawer {
	private ChartData data;
	private RepeatManager repeatManager;

	public void init(final ChartData data, final RepeatManager repeatManager) {
		this.data = data;
		this.repeatManager = repeatManager;
	}

	private void addAnchor(final FadingShaderDrawData drawData, final AnchorDrawData anchor) {
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

			drawData.addVertex(new Point3D(x0, y, z0), color)//
					.addVertex(new Point3D(x1, y, z0), color)//
					.addVertex(new Point3D(x1, y, z1), color)//
					.addVertex(new Point3D(x0, y, z1), color);
		}
	}

	public void draw(final ShadersHolder shadersHolder) {
		final FadingShaderDrawData drawData = shadersHolder.new FadingShaderDrawData();

		final List<AnchorDrawData> anchorsToDraw = AnchorDrawData.getAnchorsForTimeSpanWithRepeats(data, repeatManager,
				data.time, data.time + visibility);
		anchorsToDraw.forEach(anchorToDraw -> addAnchor(drawData, anchorToDraw));

		drawData.draw(GL30.GL_QUADS, Matrix4.identity, closeDistanceZ, 0);
	}
}
