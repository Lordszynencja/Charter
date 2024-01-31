package log.charter.gui.components.preview3D.glUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.imageio.ImageIO;

import log.charter.data.config.Config;
import log.charter.gui.components.preview3D.shapes.Texture;
import log.charter.io.Logger;

public class TexturesHolder {
	public static final String texturePacksPath = "textures/";
	public static final String inlaysPath = "inlays/";

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

	private static final Map<String, Supplier<String>> textureFileSupplier = new HashMap<>();
	static {
		textureFileSupplier.put("fingering", () -> texturePacksPath + Config.texturePack + "/fingering.png");
		textureFileSupplier.put("inlay", () -> inlaysPath + Config.inlay + ".png");
	}

	private Texture errorTexture;

	private boolean reloadNeeded = false;
	private final Map<String, Texture> textures = new HashMap<>();
	private final Map<String, String> lastTexturePaths = new HashMap<>();

	public void initGL() {
		errorTexture = new Texture();

		textureFileSupplier.forEach((texture, pathMaker) -> {
			final String path = pathMaker.get();
			textures.put(texture, new Texture(new File(path)));
			lastTexturePaths.put(texture, path);
		});
	}

	public void reloadTextures() {
		reloadNeeded = true;
	}

	public int getTextureId(final String name) {
		if (reloadNeeded) {
			textureFileSupplier.forEach((texture, pathMaker) -> {
				final String path = pathMaker.get();
				if (path.equals(lastTexturePaths.get(texture))) {
					return;
				}

				try {
					final BufferedImage image = ImageIO.read(new File(path));
					textures.get(texture).replaceTexture(image);
					lastTexturePaths.put(texture, path);
				} catch (final IOException e) {
					Logger.error("Couldn't reload texture " + texture + " with path " + path, e);
				}
			});

			reloadNeeded = false;
		}

		return textures.containsKey(name) ? textures.get(name).textureId : errorTexture.textureId;
	}
}
