package log.charter.gui.components.preview3D;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.gui.components.preview3D.Preview3DUtils.getChartboardYPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getFretPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getTimePosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.visibility;
import static log.charter.gui.components.preview3D.Preview3DUtils.visibilityZ;
import static log.charter.song.notes.IPosition.findFirstAfter;
import static log.charter.song.notes.IPosition.findLastIdBeforeEqual;

import java.awt.Color;

import org.lwjgl.opengl.GL30;

import log.charter.data.ChartData;
import log.charter.gui.components.preview3D.BaseShader.BaseShaderDrawData;
import log.charter.song.Anchor;
import log.charter.song.EventPoint;
import log.charter.util.CollectionUtils.ArrayList2;

public class Preview3DAnchorsDrawer {
	private ChartData data;

	public Matrix4 currentMatrix;

	public void init(final ChartData data) {
		this.data = data;
	}

	public void draw(final BaseShader shader) {
		final BaseShaderDrawData drawData = shader.new BaseShaderDrawData();
		final ArrayList2<Anchor> anchors = data.getCurrentArrangementLevel().anchors;
		int anchorsFrom = findLastIdBeforeEqual(anchors, data.time);
		if (anchorsFrom == -1) {
			anchorsFrom = 0;
		}
		final int anchorsTo = findLastIdBeforeEqual(anchors, data.time + visibility);

		for (int i = anchorsFrom; i <= anchorsTo; i++) {
			final Anchor anchor = anchors.get(i);

			int timeTo;
			if (i < anchors.size() - 1) {
				timeTo = anchors.get(i + 1).position() - 1;
			} else {
				timeTo = data.songChart.beatsMap.songLengthMs;
			}

			final EventPoint nextPhraseIteration = findFirstAfter(
					data.getCurrentArrangement().getFilteredEventPoints(p -> p.phrase != null), anchor.position());
			if (nextPhraseIteration != null) {
				timeTo = min(timeTo, nextPhraseIteration.position());
			}

			drawAnchor(drawData, anchor, timeTo);
		}

		drawData.draw(GL30.GL_QUADS);
	}

	private void drawAnchor(final BaseShaderDrawData drawData, final Anchor anchor, final int anchorEnd) {
		final double x0 = getFretPosition(anchor.fret - 1);
		final double x1 = getFretPosition(anchor.fret + anchor.width - 1);
		final double y = getChartboardYPosition(data.currentStrings()) - 0.001;
		final double z0 = max(0, getTimePosition(anchor.position() - data.time));
		final double z1 = min(visibilityZ, getTimePosition(anchorEnd - data.time));

		final Color color = new Color(0, 0, 255, 64);
		drawData.addVertex(new Point3D(x0, y, z0), color)//
				.addVertex(new Point3D(x1, y, z0), color)//
				.addVertex(new Point3D(x1, y, z1), color)//
				.addVertex(new Point3D(x0, y, z1), color);
	}
}
