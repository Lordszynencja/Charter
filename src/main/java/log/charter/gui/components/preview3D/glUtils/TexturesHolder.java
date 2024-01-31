package log.charter.gui.components.preview3D.glUtils;

import static java.util.Arrays.asList;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import log.charter.gui.components.preview3D.shapes.Texture;
import log.charter.util.CollectionUtils.Pair;

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

	private static final List<Pair<String, String>> texturesToLoad = asList(//
			new Pair<>("fingering", "textures/fingering.png"), //
			new Pair<>("inlay", "textures/inlay.png"));

	private Texture errorTexture;

	private final Map<String, Texture> textures = new HashMap<>();

	public void initGL() {
		texturesToLoad.forEach(textureData -> textures.put(textureData.a, new Texture(new File(textureData.b))));

		errorTexture = new Texture();
	}

	public int getTextureId(final String name) {
		return textures.containsKey(name) ? textures.get(name).textureId : errorTexture.textureId;
	}
}
