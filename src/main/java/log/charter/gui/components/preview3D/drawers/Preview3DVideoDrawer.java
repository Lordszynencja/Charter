package log.charter.gui.components.preview3D.drawers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.lwjgl.opengl.GL30;

import log.charter.data.ChartData;
import log.charter.data.config.values.DebugConfig;
import log.charter.gui.components.preview3D.glUtils.BufferedTextureData;
import log.charter.gui.components.preview3D.glUtils.Texture;
import log.charter.gui.components.preview3D.shaders.ShadersHolder;
import log.charter.sound.asio.ASIOHandler;
import log.charter.util.ExitActions;

public class Preview3DVideoDrawer {
	@SuppressWarnings("unused")
	private ChartData data;

	private BufferedImage image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
	private boolean imageChanged = true;
	private BufferedTextureData bufferData = null;
	private Texture texture = null;
	private Thread videoPlayingThread;

	public void init(final ChartData data) {
		this.data = data;

		image.setRGB(0, 0, 0);

		videoPlayingThread = new Thread(this::playVideo, "3D Video drawer");
		videoPlayingThread.start();

		ExitActions.addOnExit(() -> videoPlayingThread.interrupt());
	}

	public void initGL() {
		texture = new Texture(image);
	}

	private void drawAudio(final Graphics2D graphics) {
		graphics.setColor(Color.RED);
		final float[] buffer = ASIOHandler.inputBuffer;
		for (int i = 0; i < buffer.length - 1; i++) {
			final int x0 = i * 1000 / (buffer.length - 1);
			final int x1 = (i + 1) * 1000 / (buffer.length - 1);
			final int y0 = (int) (buffer[i] * 400) + 500;
			final int y1 = (int) (buffer[i + 1] * 400) + 500;

			graphics.drawLine(x0, y0, x1, y1);
		}
	}

	private void drawFFT(final Graphics2D graphics) {
		graphics.setColor(Color.GREEN);
		final double[] buffer = ASIOHandler.fft.magnitudes;
		for (int i = 0; i < buffer.length - 1; i++) {
			final int x0 = i * 1000 / (buffer.length - 1);
			final int x1 = (i + 1) * 1000 / (buffer.length - 1);
			final int y0 = (int) (buffer[i] * 400) + 500;
			final int y1 = (int) (buffer[i + 1] * 400) + 500;

			graphics.drawLine(x0, y0, x1, y1);
		}
	}

	private void generateImage() {
		final BufferedImage newImage = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);

		final Graphics2D graphics = newImage.createGraphics();
		graphics.clearRect(0, 0, 1000, 1000);
		graphics.setStroke(new BasicStroke(10));

		if (DebugConfig.showInputGraph) {
			drawAudio(graphics);
		}
		if (DebugConfig.showFTGraph) {
			drawFFT(graphics);
		}

		image = newImage;
		imageChanged = true;
	}

	private void playVideo() {
		long nextFrameTime = System.nanoTime() + 16_666_666;

		while (!videoPlayingThread.isInterrupted()) {
			try {
				Thread.sleep(Math.max(0, (nextFrameTime - System.nanoTime()) / 1_000_000));
			} catch (final InterruptedException e) {
				return;
			}

			generateImage();

			if (imageChanged) {
				bufferData = new BufferedTextureData(image);
				imageChanged = false;
			}
			nextFrameTime += 16_666_666;
		}
	}

	public void draw(final ShadersHolder shadersHolder, final int width, final int height) {
		final BufferedTextureData currentBufferData = bufferData;

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
