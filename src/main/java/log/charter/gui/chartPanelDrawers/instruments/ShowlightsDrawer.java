package log.charter.gui.chartPanelDrawers.instruments;

import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.laneHeight;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesTop;
import static log.charter.util.ScalingUtils.positionToX;
import static log.charter.util.ScalingUtils.xToPosition;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import log.charter.data.song.Showlight;
import log.charter.data.song.Showlight.ShowlightType;
import log.charter.gui.ChartPanel;
import log.charter.gui.chartPanelDrawers.common.BeatsDrawer;
import log.charter.gui.chartPanelDrawers.common.waveform.WaveFormDrawer;
import log.charter.gui.chartPanelDrawers.data.FrameData;
import log.charter.util.ColorUtils;

public class ShowlightsDrawer {
	private static int textureWidth = laneHeight * 2;
	private static final Map<ShowlightType, BufferedImage> showlightTextures = new HashMap<>();

	private static BufferedImage generateFogTexture(final Color color) {
		final BufferedImage img = new BufferedImage(textureWidth, laneHeight, BufferedImage.TYPE_INT_RGB);

		for (int y = 0; y < laneHeight; y++) {
			final double yMultiplier = 0.25 * sin(y * Math.PI * 2 / laneHeight);
			for (int x = 0; x < textureWidth; x++) {
				final double multiplier = 0.4 - abs(0.5 * sin(x * Math.PI * 2 / textureWidth) + yMultiplier) * 0.2;
				final Color c = ColorUtils.multiplyColor(color, multiplier);
				img.setRGB(x, y, c.getRGB());
			}
		}

		return img;
	}

	private static BufferedImage generateBeamTexture(final Color color) {
		final BufferedImage img = new BufferedImage(textureWidth, laneHeight, BufferedImage.TYPE_INT_RGB);

		for (int y = 0; y < laneHeight; y++) {
			final double yMultiplier = 0.25 * sin(y * Math.PI * 2 / laneHeight);
			for (int x = 0; x < textureWidth; x++) {
				final double multiplier = pow(0.5 * sin(x * Math.PI * 2 / textureWidth) + yMultiplier, 2);
				final Color c = ColorUtils.multiplyColor(color, multiplier);
				img.setRGB(x, y, c.getRGB());
			}
		}

		return img;
	}

	private static BufferedImage generateLaserTexture(final Color color) {
		final BufferedImage img = new BufferedImage(textureWidth, laneHeight, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D graphics = img.createGraphics();
		graphics.setColor(color);
		graphics.drawLine(textureWidth / 6, 0, textureWidth / 3, laneHeight);
		graphics.drawLine(textureWidth * 5 / 6, 0, textureWidth * 2 / 3, laneHeight);

		return img;
	}

	public static void reloadGraphics() {
		for (final ShowlightType type : ShowlightType.values()) {
			if (type == ShowlightType.BEAMS_OFF || type == ShowlightType.LASERS_OFF) {
				continue;
			}

			final BufferedImage img = type.isFog ? generateFogTexture(type.color)//
					: type.isBeam ? generateBeamTexture(type.color)//
							: generateLaserTexture(type.color);
			if (img != null) {
				showlightTextures.put(type, img);
			}
		}
	}

	private BeatsDrawer beatsDrawer;
	private ChartPanel chartPanel;
	private WaveFormDrawer waveFormDrawer;

	private void drawTextureFromTo(final Graphics2D g, final double timeFrom, final int x0, final int x1, final int y,
			final ShowlightType type) {
		final int w = x1 - x0;
		if (w <= 0) {
			return;
		}

		final BufferedImage texture = showlightTextures.get(type);
		if (texture == null) {
			return;
		}

		final Shape previousClip = g.getClip();
		g.setClip(x0, y, x1 - x0, laneHeight);

		int x = x0 - x0 % textureWidth - positionToX(timeFrom) % textureWidth - textureWidth;
		while (x < x1) {
			g.drawImage(texture, null, x, y);
			x += textureWidth;
		}

		g.setClip(previousClip);
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
				drawTextureFromTo(frameData.g, timeFrom, x, min(newX, width), lanesTop, fog);
				x = newX;

				if (position > timeTo) {
					return;
				}
			}

			fog = showlight.type;
		}

		final int endX = positionToX(frameData.songLength, frameData.time);
		drawTextureFromTo(frameData.g, timeFrom, x, min(endX, width), lanesTop, fog);
	}

	private void drawBeams(final FrameData frameData, final int width, final double timeFrom, final double timeTo) {
		int x = 0;
		ShowlightType beam = ShowlightType.BEAMS_OFF;
		boolean laser = false;
		for (final Showlight showlight : frameData.showlights) {
			if (!showlight.type.isBeam && showlight.type != ShowlightType.LASERS_ON
					&& showlight.type != ShowlightType.LASERS_OFF) {
				continue;
			}

			final double position = showlight.position(frameData.beats);
			if (position > timeFrom) {
				final int newX = positionToX(position, frameData.time);
				drawTextureFromTo(frameData.g, timeFrom, x, min(newX, width), lanesTop + laneHeight, beam);
				if (laser) {
					drawTextureFromTo(frameData.g, timeFrom, x, min(newX, width), lanesTop + laneHeight,
							ShowlightType.LASERS_ON);
				}
				x = newX;

				if (position > timeTo) {
					return;
				}
			}

			if (showlight.type.isBeam) {
				beam = showlight.type;
			} else if (showlight.type == ShowlightType.LASERS_ON) {
				laser = true;
			} else if (showlight.type == ShowlightType.LASERS_OFF) {
				laser = false;
			}
		}

		final int endX = positionToX(frameData.songLength, frameData.time);
		drawTextureFromTo(frameData.g, timeFrom, x, min(endX, width), lanesTop + laneHeight, beam);
		if (laser) {
			drawTextureFromTo(frameData.g, timeFrom, x, min(endX, width), lanesTop + laneHeight,
					ShowlightType.LASERS_ON);
		}
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
