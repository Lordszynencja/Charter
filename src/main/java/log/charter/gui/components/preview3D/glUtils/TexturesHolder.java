package log.charter.gui.components.preview3D.glUtils;

import static log.charter.data.config.GraphicalConfig.inlay;
import static log.charter.data.config.GraphicalConfig.texturePack;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.imageio.ImageIO;

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
	private static final Map<String, Supplier<String>> defaultTextureFileSupplier = new HashMap<>();
	static {
		textureFileSupplier.put("fingering", () -> texturePacksPath + texturePack + "/fingering.png");
		textureFileSupplier.put("inlay", () -> inlaysPath + inlay + ".png");

		defaultTextureFileSupplier.put("fingering", () -> texturePacksPath + "default/fingering.png");
		defaultTextureFileSupplier.put("inlay", () -> inlaysPath + "default.png");
	}

	private Texture errorTexture;

	private boolean reloadNeeded = false;
	private final Map<String, Texture> textures = new HashMap<>();
	private final Map<String, String> lastTexturePaths = new HashMap<>();

	public void initGL() {
		errorTexture = new Texture();
		textures.put("error", errorTexture);

		textureFileSupplier.forEach((texture, pathMaker) -> {
			String path = pathMaker.get();
			File f = new File(path);
			if (!f.exists()) {
				path = defaultTextureFileSupplier.get(texture).get();
				f = new File(path);
			}

			textures.put(texture, new Texture(f));
			lastTexturePaths.put(texture, path);
		});
	}

	public void reloadTextures() {
		reloadNeeded = true;
	}

	public int getTextureId(final String name) {
		if (reloadNeeded) {
			textureFileSupplier.forEach((texture, pathMaker) -> {
				String path = pathMaker.get();
				File f = new File(path);
				if (!f.exists()) {
					path = defaultTextureFileSupplier.get(texture).get();
					f = new File(path);
				}
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

	public int addTexture(final String name, final BufferedImage img, final boolean replace) {
		if (!textures.containsKey(name)) {
			textures.put(name, new Texture(img));
		} else if (replace) {
			textures.get(name).replaceTexture(img);
		}

		return getTextureId(name);
	}
}
