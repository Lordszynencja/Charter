package log.charter.gui.components.preview3D.glUtils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TextTexturesHolder {
	private static class TextProperties {
		public final String text;
		public final float fontSize;
		public final Color color;
		public final boolean bold;

		public TextProperties(final String text, final float fontSize, final Color color, final boolean bold) {
			this.text = text;
			this.fontSize = fontSize;
			this.color = color;
			this.bold = bold;
		}

		@Override
		public int hashCode() {
			return Objects.hash(color, fontSize, text, bold);
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}

			final TextProperties other = (TextProperties) obj;
			return Objects.equals(color, other.color)
					&& Float.floatToIntBits(fontSize) == Float.floatToIntBits(other.fontSize)
					&& Objects.equals(text, other.text) && bold == other.bold;
		}

	}

	private final Map<TextProperties, BufferedTextureData> texturesMap = new HashMap<>();
	private final List<TextProperties> textures = new LinkedList<>();

	private Texture texture = null;

	public void initGL() {
		texture = new Texture();
	}

	public int getTextureId() {
		return texture.textureId;
	}

	private BufferedImage generateImage(final TextProperties properties) {
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics graphics = img.getGraphics();
		final Font font = graphics.getFont().deriveFont(properties.fontSize).deriveFont(Font.BOLD);
		graphics.setFont(font);
		final Rectangle2D stringBounds = graphics.getFontMetrics().getStringBounds(properties.text, graphics);
		final int width = (int) Math.ceil(stringBounds.getWidth());
		final int height = (int) Math.ceil(stringBounds.getHeight());

		img = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		graphics = img.getGraphics();
		graphics.setFont(font);
		graphics.setColor(properties.color);
		graphics.drawString(properties.text, 0, (int) -stringBounds.getY());

		return img;
	}

	private void removeTexture(final TextProperties properties) {
		textures.remove(properties);
		texturesMap.remove(properties);
	}

	private void addText(final TextProperties properties) {
		if (textures.size() > 1000) {
			removeTexture(textures.get(0));
		}

		texturesMap.put(properties, new BufferedTextureData(generateImage(properties)));
		textures.add(properties);
	}

	public BufferedTextureData setTextInTexture(final String text, final float fontSize, final Color color) {
		return setTextInTexture(text, fontSize, color, false);
	}

	public BufferedTextureData setTextInTexture(final String text, final float fontSize, final Color color,
			final boolean bold) {
		final TextProperties properties = new TextProperties(text, fontSize, color, bold);

		if (!texturesMap.containsKey(properties)) {
			addText(properties);
		}

		final BufferedTextureData textureData = texturesMap.get(properties);
		texture.replaceTexture(textureData);
		return textureData;
	}
}
