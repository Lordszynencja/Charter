package log.charter.gui.panes.graphicalConfig;

import java.io.File;

import javax.swing.JComboBox;

import log.charter.data.config.GraphicalConfig;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.FieldWithLabel;
import log.charter.gui.components.FieldWithLabel.LabelPosition;
import log.charter.gui.components.Page;
import log.charter.gui.components.preview3D.glUtils.TexturesHolder;

public class GraphicTexturesConfigPage implements Page {
	private String inlay = GraphicalConfig.inlay;
	private String texturePack = GraphicalConfig.texturePack;

	private FieldWithLabel<JComboBox<String>> inlayField;
	private FieldWithLabel<JComboBox<String>> texturePackField;

	public void init(final GraphicConfigPane parent, int row) {
		addInlaySelect(parent, row++);
		addTexturePackSelect(parent, row++);

		hide();
	}

	private void addInlaySelect(final GraphicConfigPane parent, final int row) {
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

		inlayField = new FieldWithLabel<>(Label.GRAPHIC_CONFIG_INLAY, 90, 150, 20, inlaySelect, LabelPosition.LEFT);
		inlayField.setLocation(10, parent.getY(row));
		parent.add(inlayField);
	}

	private void addTexturePackSelect(final GraphicConfigPane parent, final int row) {
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

		texturePackField = new FieldWithLabel<>(Label.GRAPHIC_CONFIG_TEXTURE_PACK, 90, 150, 20, texturePackSelect,
				LabelPosition.LEFT);
		texturePackField.setLocation(10, parent.getY(row));
		parent.add(texturePackField);
	}

	@Override
	public void show() {
		inlayField.setVisible(true);
		texturePackField.setVisible(true);
	}

	@Override
	public void hide() {
		inlayField.setVisible(false);
		texturePackField.setVisible(false);
	}

	public void save(final CharterFrame frame) {
		boolean texturesChanged = false;
		if (!inlay.equals(GraphicalConfig.inlay)) {
			GraphicalConfig.inlay = inlay;
			texturesChanged = true;
		}
		if (!texturePack.equals(GraphicalConfig.texturePack)) {
			GraphicalConfig.texturePack = texturePack;
			texturesChanged = true;
		}

		if (texturesChanged) {
			frame.reloadTextures();
		}
	}
}
