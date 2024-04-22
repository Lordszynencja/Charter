package log.charter.gui.components.preview3D.glUtils;

import static log.charter.data.config.GraphicalConfig.inlay;
import static log.charter.data.config.GraphicalConfig.texturePack;
import static log.charter.util.FileUtils.inlaysFolder;
import static log.charter.util.FileUtils.texturesFolder;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import log.charter.io.Logger;

public class TexturesHolder {
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

	private static final Map<String, TextureFileSupplier> textureFileSuppliers = new HashMap<>();
	static {
		textureFileSuppliers.put("fingering",
				new TextureFileSupplier(texturesFolder, () -> texturePack, name -> name + "/fingering.png"));
		textureFileSuppliers.put("inlay", new TextureFileSupplier(inlaysFolder, () -> inlay, name -> name + ".png"));
	}

	private Texture errorTexture;

	private boolean reloadNeeded = false;
	private final Map<String, Texture> textures = new HashMap<>();
	private final Map<String, String> lastTexturePaths = new HashMap<>();

	public void initGL() {
		errorTexture = new Texture();
		textures.clear();
		textures.put("error", errorTexture);

		textureFileSuppliers.forEach((texture, fileSupplier) -> {
			final File f = fileSupplier.getFile();
			textures.put(texture, new Texture(f));
			lastTexturePaths.put(texture, f.getPath());
		});
	}

	public void reloadTextures() {
		reloadNeeded = true;
	}

	public int getTextureId(final String name) {
		if (reloadNeeded) {
			textureFileSuppliers.forEach((texture, fileSupplier) -> {
				final File f = fileSupplier.getFile();
				if (f.getPath().equals(lastTexturePaths.get(texture))) {
					return;
				}

				try {
					final BufferedImage image = ImageIO.read(f);
					textures.get(texture).replaceTexture(image);
					lastTexturePaths.put(texture, f.getPath());
				} catch (final IOException e) {
					Logger.error("Couldn't reload texture " + texture + " with path " + f.getPath(), e);
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
