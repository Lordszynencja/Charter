package log.charter.gui.chartPanelDrawers.instruments;

import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.laneHeight;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesHeight;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesTop;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.strokedRectangle;
import static log.charter.util.ScalingUtils.positionToX;
import static log.charter.util.ScalingUtils.xToPosition;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import log.charter.data.config.ChartPanelColors.ColorLabel;
import log.charter.data.song.Showlight;
import log.charter.data.song.Showlight.ShowlightType;
import log.charter.data.types.PositionType;
import log.charter.gui.ChartPanel;
import log.charter.gui.chartPanelDrawers.common.BeatsDrawer;
import log.charter.gui.chartPanelDrawers.common.waveform.WaveFormDrawer;
import log.charter.gui.chartPanelDrawers.data.FrameData;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePositionWithSize;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapeSize;
import log.charter.gui.chartPanelDrawers.drawableShapes.TextWithBackground;
import log.charter.services.data.selection.ISelectionAccessor;
import log.charter.util.ColorUtils;
import log.charter.util.data.Position2D;

public class ShowlightsDrawer {
	private static int textureWidth = laneHeight * 2;
	private static int fogLaneY = lanesTop + laneHeight / 3;
	private static int fogLabelY = lanesTop + laneHeight * 2 / 3;
	private static int beamLaneY = lanesTop + laneHeight * 5 / 3;
	private static int beamLabelY = lanesTop + laneHeight * 6 / 3;

	private static final int labelTextSpace = 2;
	private static Font labelFont = new Font(Font.SANS_SERIF, Font.BOLD, laneHeight / 4);

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

	private static BufferedImage generateExtendedBeamTexture(final Color color) {
		final BufferedImage img = new BufferedImage(textureWidth, laneHeight, BufferedImage.TYPE_INT_RGB);

		for (int y = 0; y < laneHeight; y++) {
			final double yMultiplier = 0.25 * abs(sin(y * Math.PI * 2 / laneHeight));
			for (int x = 0; x < textureWidth; x++) {
				final double multiplier = pow(0.5 * abs(sin(x * Math.PI * 2 / textureWidth)) + yMultiplier, 2);
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

	private static BufferedImage generateTexture(final ShowlightType type) {
		if (type.isFog) {
			return generateFogTexture(type.color);
		}
		if (type.isExtendedBeam) {
			return generateExtendedBeamTexture(type.color);
		}
		if (type.isBeam) {
			return generateBeamTexture(type.color);
		}

		return generateLaserTexture(type.color);
	}

	public static void reloadGraphics() {
		textureWidth = laneHeight * 2;
		fogLaneY = lanesTop + laneHeight / 3;
		fogLabelY = lanesTop + laneHeight * 2 / 3;
		beamLaneY = lanesTop + laneHeight * 5 / 3;
		beamLabelY = lanesTop + laneHeight * 6 / 3;

		labelFont = new Font(Font.SANS_SERIF, Font.BOLD, laneHeight / 4);

		for (final ShowlightType type : ShowlightType.values()) {
			if (type == ShowlightType.BEAMS_OFF || type == ShowlightType.LASERS_OFF) {
				continue;
			}

			showlightTextures.put(type, generateTexture(type));
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

	private void drawShowlights(final FrameData frameData, final List<Showlight> showlights,
			final ShowlightType defaultValue, final int width, final int y, final double timeFrom,
			final double timeTo) {
		int x = Math.max(0, positionToX(0, frameData.time));
		ShowlightType type = defaultValue;
		for (final Showlight showlight : showlights) {
			final double position = showlight.position(frameData.beats);
			final int newX = positionToX(position, frameData.time);
			if (position > timeFrom) {
				drawTextureFromTo(frameData.g, timeFrom, x, min(newX, width), y, type);

				if (position > timeTo) {
					return;
				}
			}

			x = newX;
			for (final ShowlightType nextType : showlight.types) {
				type = nextType;
			}
		}

		final int endX = positionToX(frameData.songLength, frameData.time);
		drawTextureFromTo(frameData.g, timeFrom, x, min(endX, width), y, type);
	}

	private void drawShowlightMarkers(final FrameData frameData, final int width, final double timeFrom,
			final double timeTo) {
		frameData.g.setColor(new Color(160, 160, 160));
		for (final Showlight showlight : frameData.showlights) {
			final double position = showlight.position(frameData.beats);
			final int x = positionToX(position, frameData.time);
			if (position < timeFrom) {
				continue;
			}
			if (position > timeTo) {
				return;
			}

			frameData.g.drawLine(x, lanesTop, x, lanesTop + lanesHeight);
		}
	}

	private void splitShowlights(final List<Showlight> showlights, final List<Showlight> fog,
			final List<Showlight> nonFog) {
		for (final Showlight showlight : showlights) {
			final List<ShowlightType> fogTypes = new ArrayList<>();
			final List<ShowlightType> nonFogTypes = new ArrayList<>();
			for (final ShowlightType type : showlight.types) {
				(type.isFog ? fogTypes : nonFogTypes).add(type);
			}

			if (!fogTypes.isEmpty()) {
				fog.add(new Showlight(showlight.position(), fogTypes));
			}
			if (!nonFogTypes.isEmpty()) {
				nonFog.add(new Showlight(showlight.position(), nonFogTypes));
			}
		}
	}

	private String typesToText(final List<ShowlightType> types) {
		return types.stream().map(t -> t.label.label()).collect(Collectors.joining(", "));
	}

	private void drawShowlightText(final Graphics2D g, final String text, final int x, final int y) {
		new TextWithBackground(new Position2D(x, y), labelFont, text, ColorLabel.SHOWLIGHT_LABEL_TEXT,
				ColorLabel.SHOWLIGHT_LABEL_BG, labelTextSpace, ColorLabel.BASE_BORDER).draw(g);
	}

	private void drawLeftSideLabel(final Graphics2D g, final Showlight showlight, final int nextX, final int y) {
		final String text = typesToText(showlight.types);
		final ShapeSize expectedSize = TextWithBackground.getExpectedSize(g, labelFont, text, labelTextSpace);
		final int x = min(0, nextX - expectedSize.width);
		drawShowlightText(g, text, x, y);
	}

	private void drawLabels(final FrameData frameData, final List<Showlight> showlights, final int width, final int y,
			final double timeFrom, final double timeTo) {
		Showlight last = null;
		boolean leftSideDrawn = false;
		for (final Showlight showlight : showlights) {
			final double position = showlight.position(frameData.beats);
			if (position > timeFrom) {
				if (position > timeTo) {
					if (!leftSideDrawn) {
						if (last != null) {
							drawLeftSideLabel(frameData.g, last, width, y);
						}
					}
					return;
				}

				final String text = typesToText(showlight.types);
				final int x = positionToX(position, frameData.time);
				drawShowlightText(frameData.g, text, x, y);

				if (!leftSideDrawn) {
					if (last != null) {
						drawLeftSideLabel(frameData.g, last, x, y);
					}
					leftSideDrawn = true;
				}
			}

			last = showlight;
		}

		if (!leftSideDrawn) {
			if (last != null) {
				drawLeftSideLabel(frameData.g, last, width, y);
			}
		}
	}

	private void drawShowlightsInfo(final FrameData frameData, final int width, final double timeFrom,
			final double timeTo) {
		drawShowlightMarkers(frameData, width, timeFrom, timeTo);

		final List<Showlight> fog = new ArrayList<>(frameData.showlightsFog.size());
		final List<Showlight> nonFog = new ArrayList<>(frameData.showlights.size());
		splitShowlights(frameData.showlights, fog, nonFog);
		drawLabels(frameData, fog, width, fogLabelY, timeFrom, timeTo);
		drawLabels(frameData, nonFog, width, beamLabelY, timeFrom, timeTo);
	}

	private void drawHighlight(final Graphics2D g, final int x, final ColorLabel color) {
		final ShapePositionWithSize position = new ShapePositionWithSize(x - 1, lanesTop, 2, lanesHeight);
		strokedRectangle(position, color).draw(g);
	}

	@SuppressWarnings("unchecked")
	private void drawSelected(final FrameData frameData, final int width, final double timeFrom, final double timeTo) {
		if (!frameData.selection.isSelected() || frameData.selection.type() != PositionType.SHOWLIGHT) {
			return;
		}

		final List<Showlight> selectedShowlights = ((ISelectionAccessor<Showlight>) frameData.selection)
				.getSelectedElements();

		for (final Showlight showlight : selectedShowlights) {
			final double position = showlight.position().position(frameData.beats);
			if (position < timeFrom) {
				continue;
			}
			if (position > timeTo) {
				return;
			}

			final int x = positionToX(position, frameData.time);
			drawHighlight(frameData.g, x, ColorLabel.SELECT);
		}
	}

	private void drawHighlight(final FrameData frameData) {
		if (frameData.highlightData.type != PositionType.SHOWLIGHT) {
			return;
		}

		if (frameData.highlightData.id.isPresent()) {
			final Showlight showlight = frameData.showlights.get(frameData.highlightData.id.get().id);
			final int x = positionToX(showlight.position(frameData.beats), frameData.time);
			drawHighlight(frameData.g, x, ColorLabel.BASE_HIGHLIGHT);
		} else {
			frameData.highlightData.highlightedNonIdPositions.forEach(highlightPosition -> {
				final int x = positionToX(highlightPosition.position, frameData.time);
				drawHighlight(frameData.g, x, ColorLabel.BASE_HIGHLIGHT);
			});
		}
	}

	public void draw(final FrameData frameData) {
		waveFormDrawer.draw(frameData);
		beatsDrawer.draw(frameData);

		final int width = chartPanel.getWidth();
		final double timeFrom = xToPosition(-1, frameData.time);
		final double timeTo = xToPosition(width + 1, frameData.time);

		drawShowlights(frameData, frameData.showlightsFog, ShowlightType.FOG_GREEN, width, fogLaneY, timeFrom, timeTo);
		drawShowlights(frameData, frameData.showlightsBeam, ShowlightType.BEAMS_OFF, width, beamLaneY, timeFrom,
				timeTo);
		drawShowlights(frameData, frameData.showlightsLaser, ShowlightType.LASERS_OFF, width, beamLaneY, timeFrom,
				timeTo);
		drawShowlightsInfo(frameData, width, timeFrom, timeTo);
		drawSelected(frameData, width, timeFrom, timeTo);
		drawHighlight(frameData);
	}

}
