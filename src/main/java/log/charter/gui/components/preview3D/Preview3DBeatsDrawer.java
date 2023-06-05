package log.charter.gui.components.preview3D;

import static log.charter.gui.components.preview3D.Preview3DUtils.getChartboardYPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getFretPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getTimePosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.visibility;
import static log.charter.song.notes.IPosition.findFirstIdAfterEqual;
import static log.charter.song.notes.IPosition.findLastIdBeforeEqual;

import java.awt.Color;

import org.lwjgl.opengl.GL30;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.components.preview3D.BaseShader.BaseShaderDrawData;
import log.charter.song.Beat;
import log.charter.util.CollectionUtils.ArrayList2;

public class Preview3DBeatsDrawer {
	private ChartData data;

	public Matrix4 currentMatrix;

	public void init(final ChartData data) {
		this.data = data;
	}

	public void draw(final BaseShader shader) {
		final BaseShaderDrawData drawData = shader.new BaseShaderDrawData();
		final ArrayList2<Beat> beats = data.songChart.beatsMap.beats;
		int beatsFrom = findFirstIdAfterEqual(beats, data.time);
		if (beatsFrom == -1) {
			beatsFrom = 0;
		}
		final int beatsTo = findLastIdBeforeEqual(beats, data.time + visibility);
		final Color color = new Color(64, 128, 255);
		ColorLabel.PREVIEW_3D_BEAT.color();
		final Color alpha = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);

		for (int i = beatsFrom; i <= beatsTo; i++) {
			final Beat beat = beats.get(i);

			final double x0 = getFretPosition(0);
			final double x1 = getFretPosition(Config.frets);
			final double y = getChartboardYPosition(data.currentStrings());
			final double z = getTimePosition(beat.position() - data.time);

			if (beat.firstInMeasure) {
				drawData.addVertex(new Point3D(x0, y, z - 0.2), alpha)//
						.addVertex(new Point3D(x1, y, z - 0.2), alpha)//
						.addVertex(new Point3D(x1, y, z - 0.1), color)//
						.addVertex(new Point3D(x0, y, z - 0.1), color)//

						.addVertex(new Point3D(x0, y, z - 0.1), color)//
						.addVertex(new Point3D(x1, y, z - 0.1), color)//
						.addVertex(new Point3D(x1, y, z + 0.1), color)//
						.addVertex(new Point3D(x0, y, z + 0.1), color)//

						.addVertex(new Point3D(x0, y, z + 0.1), color)//
						.addVertex(new Point3D(x1, y, z + 0.1), color)//
						.addVertex(new Point3D(x1, y, z + 0.2), alpha)//
						.addVertex(new Point3D(x0, y, z + 0.2), alpha);
			} else {
				drawData.addVertex(new Point3D(x0, y, z - 0.1), alpha)//
						.addVertex(new Point3D(x1, y, z - 0.1), alpha)//
						.addVertex(new Point3D(x1, y, z), color)//
						.addVertex(new Point3D(x0, y, z), color)//

						.addVertex(new Point3D(x0, y, z), color)//
						.addVertex(new Point3D(x1, y, z), color)//
						.addVertex(new Point3D(x1, y, z + 0.1), alpha)//
						.addVertex(new Point3D(x0, y, z + 0.1), alpha);
			}

		}

		GL30.glPointSize(10);
		drawData.draw(GL30.GL_QUADS);
	}
}
