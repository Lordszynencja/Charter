package log.charter.gui.components.preview3D.glUtils;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import log.charter.gui.components.preview3D.shapes.Texture;

public class BufferedTextureData {
	public final ByteBuffer buffer;
	public final int width;
	public final int height;

	public BufferedTextureData(final ByteBuffer buffer, final int width, final int height) {
		this.buffer = buffer;
		this.width = width;
		this.height = height;
	}

	public BufferedTextureData(final BufferedImage img) {
		this(Texture.getBuffer(img), img.getWidth(), img.getHeight());
	}
}