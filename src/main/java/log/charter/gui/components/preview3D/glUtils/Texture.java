package log.charter.gui.components.preview3D.glUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;

import log.charter.io.Logger;

public class Texture {
	private static final int errorTextureResolution = 64;
	private static final BufferedImage emptyTexture = new BufferedImage(errorTextureResolution, errorTextureResolution,
			BufferedImage.TYPE_4BYTE_ABGR);
	static {
		for (int x = 0; x < errorTextureResolution; x++) {
			for (int y = x % 2; y < errorTextureResolution; y += 2) {
				emptyTexture.setRGB(x, y, 0xFFFF00FF);
			}
		}
	}

	private static BufferedImage readImage(final File file) {
		try {
			return ImageIO.read(file);
		} catch (final IOException e) {
			Logger.error("Couldn't load texture " + file.getName(), e);
			return emptyTexture;
		}
	}

	public static ByteBuffer getBuffer(final BufferedImage image) {
		final int[] pixels = new int[image.getWidth() * image.getHeight()];
		image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

		final ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);

		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				final int pixel = pixels[y * image.getWidth() + x];
				buffer.put((byte) ((pixel >> 16) & 0xFF));
				buffer.put((byte) ((pixel >> 8) & 0xFF));
				buffer.put((byte) (pixel & 0xFF));
				buffer.put((byte) ((pixel >> 24) & 0xFF));
			}
		}

		buffer.flip(); // FOR THE LOVE OF GOD DO NOT FORGET THIS

		return buffer;
	}

	private static int replaceDataInMemory(final int textureId, final int width, final int height,
			final ByteBuffer buffer) {
		GL30.glBindTexture(GL30.GL_TEXTURE_2D, textureId); // Bind texture ID
		GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGBA8, width, height, 0, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE,
				buffer);

		return textureId;
	}

	private static int putDataIntoMemory(final int width, final int height, final ByteBuffer buffer) {
		final int textureId = GL30.glGenTextures(); // Generate texture ID
		GL30.glBindTexture(GL30.GL_TEXTURE_2D, textureId); // Bind texture ID

		GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_S, GL30.GL_CLAMP_TO_EDGE);
		GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_T, GL30.GL_CLAMP_TO_EDGE);

		GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR);
		GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_LINEAR);

		GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGBA8, width, height, 0, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE,
				buffer);

		return textureId;
	}

	public final int textureId;

	public Texture() {
		this(emptyTexture);
	}

	public Texture(final File file) {
		this(readImage(file));
	}

	public Texture(final BufferedImage image) {
		textureId = putDataIntoMemory(image.getWidth(), image.getHeight(), getBuffer(image));
	}

	public void replaceTexture(final BufferedImage newTexture) {
		replaceTexture(new BufferedTextureData(newTexture));
	}

	public void replaceTexture(final BufferedTextureData newTexture) {
		replaceTexture(newTexture.width, newTexture.height, newTexture.buffer);
	}

	public void replaceTexture(final int width, final int height, final ByteBuffer buffer) {
		replaceDataInMemory(textureId, width, height, buffer);
	}
}
