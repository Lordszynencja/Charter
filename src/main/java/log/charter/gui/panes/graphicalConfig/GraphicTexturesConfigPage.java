package log.charter.gui.panes.graphicalConfig;

import static log.charter.util.FileUtils.listDirectories;
import static log.charter.util.FileUtils.listFiles;

import java.util.List;
import java.util.stream.Stream;

import log.charter.data.config.GraphicalConfig;
import log.charter.data.config.Localization.Label;
import log.charter.data.managers.CharterContext;
import log.charter.gui.components.containers.Page;
import log.charter.gui.components.containers.RowedPanel;
import log.charter.gui.components.simple.CharterSelect;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.utils.RowedPosition;
import log.charter.util.FileUtils;

public class GraphicTexturesConfigPage implements Page {
	private FieldWithLabel<CharterSelect<String>> inlayField;
	private FieldWithLabel<CharterSelect<String>> texturePackField;

	@Override
	public Label label() {
		return Label.PAGE_TEXTURES;
	}

	@Override
	public void init(final RowedPanel panel, final RowedPosition position) {
		addInlaySelect(panel, position);
		position.newRow();

		addTexturePackSelect(panel, position);

		hide();
	}

	private void addInlaySelect(final RowedPanel panel, final RowedPosition position) {
		final Stream<String> names = listFiles(FileUtils.inlaysFolder, f -> f.getName().endsWith(".png"))//
				.map(name -> name.substring(0, name.length() - 4));

		final CharterSelect<String> inlaySelect = new CharterSelect<>(names, GraphicalConfig.inlay);
		inlayField = new FieldWithLabel<>(Label.GRAPHIC_CONFIG_INLAY, 90, 150, 20, inlaySelect, LabelPosition.LEFT);
		panel.add(inlayField, position);
	}

	private void addTexturePackSelect(final RowedPanel panel, final RowedPosition position) {
		final List<String> names = listDirectories(FileUtils.texturesFolder);

		final CharterSelect<String> texturePackSelect = new CharterSelect<>(names, GraphicalConfig.texturePack);
		texturePackField = new FieldWithLabel<>(Label.GRAPHIC_CONFIG_TEXTURE_PACK, 90, 150, 20, texturePackSelect,
				LabelPosition.LEFT);
		panel.add(texturePackField, position);
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

	public void save(final CharterContext context) {
		boolean texturesChanged = false;
		if (!inlayField.field.getSelectedItem().equals(GraphicalConfig.inlay)) {
			GraphicalConfig.inlay = inlayField.field.getSelectedItem();
			texturesChanged = true;
		}
		if (!texturePackField.field.getSelectedItem().equals(GraphicalConfig.texturePack)) {
			GraphicalConfig.texturePack = texturePackField.field.getSelectedItem();
			texturesChanged = true;
		}

		if (texturesChanged) {
			context.reloadTextures();
		}
	}
}
