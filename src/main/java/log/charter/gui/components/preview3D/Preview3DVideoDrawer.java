package log.charter.gui.components.preview3D;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL30;

import log.charter.data.ChartData;
import log.charter.gui.components.preview3D.shaders.ShadersHolder;
import log.charter.gui.components.preview3D.shapes.Texture;

public class Preview3DVideoDrawer {
	private static class BufferData {
		public final ByteBuffer buffer;
		public final int width;
		public final int height;

		public BufferData(final ByteBuffer buffer, final int width, final int height) {
			this.buffer = buffer;
			this.width = width;
			this.height = height;
		}
	}

	@SuppressWarnings("unused")
	private ChartData data;

	private final BufferedImage image = new BufferedImage(16, 9, BufferedImage.TYPE_INT_RGB);
	private final boolean imageChanged = true;
	private BufferData bufferData = null;
	private Texture texture = null;

	public void init(final ChartData data) {
		this.data = data;

		image.setRGB(0, 0, 0);

		new Thread(this::playVideo).start();
	}

	public void initGL() {
		texture = new Texture(image);
	}

	private void playVideo() {
		long nextFrameTime = System.nanoTime() + 16_666_666;

		while (true) {
			try {
				Thread.sleep(Math.max(0, (nextFrameTime - System.nanoTime()) / 1_000_000));
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}

			if (imageChanged) {
				bufferData = new BufferData(Texture.getBuffer(image), image.getWidth(), image.getHeight());
			}
			nextFrameTime += 16_666_666;
		}
	}

	public void draw(final ShadersHolder shadersHolder, final int width, final int height) {
		final BufferData currentBufferData = bufferData;

		if (bufferData != null) {
			texture.replaceTexture(currentBufferData.width, currentBufferData.height, currentBufferData.buffer);
		}

		final double panelRatio = 1.0 * width / height;
		final double imageRatio = 1.0 * currentBufferData.width / currentBufferData.height;
		double x, y;
		if (panelRatio > imageRatio) {
			x = imageRatio / panelRatio;
			y = 1;
		} else {
			x = 1;
			y = panelRatio / imageRatio;
		}

		GL30.glDisable(GL30.GL_DEPTH_TEST);

		shadersHolder.new VideoShaderDrawData()//
				.addZQuad(-x, x, -y, y, 0, 1, 0, 1)//
				.draw(texture.textureId, new Color(255, 255, 255, 128));

		GL30.glEnable(GL30.GL_DEPTH_TEST);
	}
}
