package log.charter.gui.chartPanelDrawers.instruments;

import static java.lang.Math.min;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.laneHeight;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesTop;
import static log.charter.util.ScalingUtils.positionToX;
import static log.charter.util.ScalingUtils.xToPosition;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import log.charter.data.song.Showlight;
import log.charter.data.song.Showlight.ShowlightType;
import log.charter.gui.ChartPanel;
import log.charter.gui.chartPanelDrawers.common.BeatsDrawer;
import log.charter.gui.chartPanelDrawers.common.waveform.WaveFormDrawer;
import log.charter.gui.chartPanelDrawers.data.FrameData;
import log.charter.util.ColorUtils;

public class ShowlightsDrawer {
	private BeatsDrawer beatsDrawer;
	private ChartPanel chartPanel;
	private WaveFormDrawer waveFormDrawer;

	private void drawFogFromTo(final Graphics2D g, final double timeFrom, final int x0, final int x1,
			final ShowlightType fog) {
		final int w = x1 - x0;
		final BufferedImage img = new BufferedImage(w, laneHeight, BufferedImage.TYPE_INT_RGB);

		final int offset = positionToX(timeFrom);

		for (int y = 0; y < laneHeight; y++) {
			final double yMultiplier = 0.25 * Math.sin(y / 5.0);
			for (int x = 0; x < w; x++) {
				final double multiplier = 0.6 - Math.abs(0.5 * Math.sin((x + offset) / 10.0) + yMultiplier) / 2;
				final Color c = ColorUtils.multiplyColor(fog.color, multiplier);
				img.setRGB(x, y, c.getRGB());
			}
		}

		g.drawImage(img, null, x0, lanesTop);
	}

	private void drawFog(final FrameData frameData, final int width, final double timeFrom, final double timeTo) {
		int x = 0;
		ShowlightType fog = ShowlightType.FOG_GREEN;
		for (final Showlight showlight : frameData.showlights) {
			if (!showlight.type.isFog) {
				continue;
			}

			final double position = showlight.position(frameData.beats);
			if (position > timeFrom) {
				final int newX = positionToX(position, frameData.time);
				drawFogFromTo(frameData.g, timeFrom, x, min(newX, width), fog);
				x = newX;

				if (position > timeTo) {
					return;
				}
			}

			fog = showlight.type;
		}

		final int endX = positionToX(frameData.songLength, frameData.time);
		drawFogFromTo(frameData.g, timeFrom, x, min(endX, width), fog);
	}

	private void drawBeamsFromTo(final Graphics2D g, final double timeFrom, final int x0, final int x1,
			final ShowlightType beam, final boolean withLaser) {
		if (beam == ShowlightType.BEAMS_OFF) {
			return;
		}

		final int w = x1 - x0;
		final BufferedImage img = new BufferedImage(w, laneHeight, BufferedImage.TYPE_INT_RGB);
		final int offset = positionToX(timeFrom);

		for (int y = 0; y < laneHeight; y++) {
			final double yMultiplier = 0.25 * Math.sin(y / 5.0);
			for (int x = 0; x < w; x++) {
				final double multiplier = Math.pow(Math.abs(0.5 * Math.sin((x + offset) / 10.0) + yMultiplier), 2);
				final Color c = ColorUtils.multiplyColor(beam.color, multiplier);
				img.setRGB(x, y, c.getRGB());
			}
		}

		g.drawImage(img, null, x0, lanesTop + laneHeight);
	}

	private void drawBeams(final FrameData frameData, final int width, final double timeFrom, final double timeTo) {
		int x = 0;
		ShowlightType beam = ShowlightType.BEAMS_OFF;
		final boolean laser = false;
		for (final Showlight showlight : frameData.showlights) {
			if (!showlight.type.isBeam && showlight.type != ShowlightType.LASERS_ON
					&& showlight.type != ShowlightType.LASERS_OFF) {
				continue;
			}

			final double position = showlight.position(frameData.beats);
			if (position > timeFrom) {
				final int newX = positionToX(position, frameData.time);
				drawBeamsFromTo(frameData.g, timeFrom, x, min(newX, width), beam, laser);
				x = newX;

				if (position > timeTo) {
					return;
				}
			}

			beam = showlight.type;
		}

		final int endX = positionToX(frameData.songLength, frameData.time);
		drawBeamsFromTo(frameData.g, timeFrom, x, min(endX, width), beam, laser);
	}

	public void draw(final FrameData frameData) {
		waveFormDrawer.draw(frameData);
		beatsDrawer.draw(frameData);

		final int width = chartPanel.getWidth();
		final double timeFrom = xToPosition(-1, frameData.time);
		final double timeTo = xToPosition(width + 1, frameData.time);
		drawFog(frameData, width, timeFrom, timeTo);
		drawBeams(frameData, width, timeFrom, timeTo);
	}

}
