package log.charter.gui.panes.graphicalConfig;

import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;

import log.charter.data.config.GraphicalConfig;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.Page;
import log.charter.gui.components.ParamsPane;

public final class GraphicConfigPane extends ParamsPane {
	private static final long serialVersionUID = -3193534671039163160L;

	private final GraphicThemeConfigPage themeConfig = new GraphicThemeConfigPage();
	private final GraphicTexturesConfigPage texturesConfig = new GraphicTexturesConfigPage();
	private final GraphicChartMapConfigPage chartMapConfig = new GraphicChartMapConfigPage();

	private void addPageSwitch(final int buttonPosition, final int row, final Label label, final Page page,
			final ButtonGroup buttonGroup) {
		final JToggleButton themeConfigSwitch = new JToggleButton(label.label());
		themeConfigSwitch.addActionListener(e -> {
			hideAll();
			page.show();
		});
		buttonGroup.add(themeConfigSwitch);
		this.add(themeConfigSwitch, 10 + 120 * buttonPosition, getY(row), 100, 20);
	}

	public GraphicConfigPane(final CharterFrame frame) {
		super(frame, Label.GRAPHIC_CONFIG_PANE, 600);

		final ButtonGroup buttonGroup = new ButtonGroup();

		int row = 0;
		int buttonPosition = 0;
		addPageSwitch(buttonPosition++, row, Label.GRAPHIC_CONFIG_THEME_PAGE, themeConfig, buttonGroup);
		addPageSwitch(buttonPosition++, row, Label.GRAPHIC_CONFIG_TEXTURES_PAGE, texturesConfig, buttonGroup);
		addPageSwitch(buttonPosition++, row, Label.GRAPHIC_CONFIG_CHART_MAP_PAGE, chartMapConfig, buttonGroup);

		row++;
		row++;

		themeConfig.init(this, row);
		texturesConfig.init(this, row);
		chartMapConfig.init(this, row);

		themeConfig.show();
		addDefaultFinish(10, this::saveAndExit);
	}

	private void hideAll() {
		themeConfig.hide();
		texturesConfig.hide();
		chartMapConfig.hide();
	}

	private void saveAndExit() {
		themeConfig.save();
		texturesConfig.save(frame);
		chartMapConfig.save();

		GraphicalConfig.markChanged();
		GraphicalConfig.save();

		frame.updateEditAreaSizes();
		frame.resize();
	}
}
