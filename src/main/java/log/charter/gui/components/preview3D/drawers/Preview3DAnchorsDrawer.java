package log.charter.gui.components.preview3D.drawers;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.gui.components.preview3D.Preview3DUtils.closeDistance;
import static log.charter.gui.components.preview3D.Preview3DUtils.closeDistanceZ;
import static log.charter.gui.components.preview3D.Preview3DUtils.getChartboardYPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getFretPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getTimePosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.visibility;
import static log.charter.song.notes.IPosition.findFirstAfter;
import static log.charter.song.notes.IPosition.findLastIdBeforeEqual;
import static log.charter.util.Utils.isDottedFret;

import java.awt.Color;

import org.lwjgl.opengl.GL30;

import log.charter.data.ChartData;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.components.preview3D.glUtils.Matrix4;
import log.charter.gui.components.preview3D.glUtils.Point3D;
import log.charter.gui.components.preview3D.shaders.ShadersHolder;
import log.charter.gui.components.preview3D.shaders.ShadersHolder.BaseShaderDrawData;
import log.charter.song.Anchor;
import log.charter.song.EventPoint;
import log.charter.util.CollectionUtils.ArrayList2;

public class Preview3DAnchorsDrawer {
	private ChartData data;

	public void init(final ChartData data) {
		this.data = data;
	}

	private Color getColorWithTransparency(final Color color, final int time) {
		if (time > closeDistance) {
			return color;
		}

		final int alpha = Math.max(0, Math.min(color.getAlpha(), color.getAlpha() * time / closeDistance));
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}

	private void addAnchor(final BaseShaderDrawData drawData, final Anchor anchor, final int anchorEnd) {
		final double y = getChartboardYPosition(data.currentStrings()) - 0.001;
		final int t0 = max(0, anchor.position() - data.time);
		final int t1 = min(visibility, anchorEnd - data.time);
		final double z0 = getTimePosition(t0);
		final double z1 = getTimePosition(t1);

		if (z1 < z0) {
			return;
		}

		for (int fret = anchor.fret; fret < anchor.fret + anchor.width; fret++) {
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

			addAnchor(drawData, anchor, timeTo);
		}

		drawData.draw(GL30.GL_QUADS, Matrix4.identity);
	}
}
