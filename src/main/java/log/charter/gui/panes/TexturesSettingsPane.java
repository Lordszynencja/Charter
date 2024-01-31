package log.charter.gui.panes;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JComboBox;

import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.ParamsPane;
import log.charter.gui.components.preview3D.glUtils.TexturesHolder;

public class TexturesSettingsPane extends ParamsPane {
	private static final long serialVersionUID = -3193534671039163160L;

	private static PaneSizes getSizes() {
		final PaneSizes sizes = new PaneSizes();
		sizes.width = 450;

		return sizes;
	}

	private final CharterFrame frame;

	private String inlay = Config.inlay;
	private String texturePack = Config.texturePack;

	public TexturesSettingsPane(final CharterFrame frame) {
		super(frame, Label.ARRANGEMENT_OPTIONS_PANE, getSizes());

		this.frame = frame;

		final AtomicInteger row = new AtomicInteger(0);
		addInlaySelect(row);
		addTexturePackSelect(row);

		addDefaultFinish(row.incrementAndGet(), this::saveAndExit);
	}

	private void addInlaySelect(final AtomicInteger row) {
		final File[] files = new File(TexturesHolder.inlaysPath)
				.listFiles((final File file) -> file.isFile() && file.getName().endsWith(".png"));

		int selected = 0;
		final String[] fileNames = new String[files.length];
		for (int i = 0; i < files.length; i++) {
			final String name = files[i].getName();
			fileNames[i] = name.substring(0, name.length() - 4);
			if (fileNames[i].equals(inlay)) {
				selected = i;
			}
		}

		final JComboBox<String> inlaySelect = new JComboBox<>(fileNames);
		inlaySelect.setSelectedIndex(selected);
		inlaySelect.addActionListener(e -> inlay = (String) inlaySelect.getSelectedItem());
		addLabel(row.get(), 20, Label.TEXTURES_SETTINGS_PANE_INLAY);
		this.add(inlaySelect, 110, getY(row.getAndIncrement()), 200, 20);
	}

	private void addTexturePackSelect(final AtomicInteger row) {
		final File[] files = new File(TexturesHolder.texturePacksPath)
				.listFiles((final File file) -> file.isDirectory());

		int selected = 0;
		final String[] fileNames = new String[files.length];
		for (int i = 0; i < files.length; i++) {
			fileNames[i] = files[i].getName();
			if (fileNames[i].equals(inlay)) {
				selected = i;
			}
		}

		final JComboBox<String> texturePackSelect = new JComboBox<>(fileNames);
		texturePackSelect.setSelectedIndex(selected);
		texturePackSelect.addActionListener(e -> texturePack = (String) texturePackSelect.getSelectedItem());
		addLabel(row.get(), 20, Label.TEXTURES_SETTINGS_PANE_TEXTURE_PACK);
		this.add(texturePackSelect, 110, getY(row.getAndIncrement()), 300, 20);
	}

	private void saveAndExit() {
		boolean texturesChanged = false;
		if (!inlay.equals(Config.inlay)) {
			Config.inlay = inlay;
			texturesChanged = true;
		}
		if (!texturePack.equals(Config.texturePack)) {
			Config.texturePack = texturePack;
			texturesChanged = true;
		}

		if (texturesChanged) {
			Config.markChanged();
			frame.reloadTextures();
		}

	}
}
