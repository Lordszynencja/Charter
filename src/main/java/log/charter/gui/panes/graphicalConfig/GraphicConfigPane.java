package log.charter.gui.panes.graphicalConfig;

import log.charter.data.config.GraphicalConfig;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.PagedDialog;

public final class GraphicConfigPane extends PagedDialog {
	private static final long serialVersionUID = -3193534671039163160L;

	private final GraphicThemeConfigPage themeConfig;
	private final GraphicTexturesConfigPage texturesConfig;
	private final GraphicChartMapConfigPage chartMapConfig;

	public GraphicConfigPane(final CharterFrame frame) {
		this(frame, new GraphicThemeConfigPage(), //
				new GraphicTexturesConfigPage(), //
				new GraphicChartMapConfigPage()//
		);
	}

	private GraphicConfigPane(final CharterFrame frame, final GraphicThemeConfigPage themeConfig,
			final GraphicTexturesConfigPage texturesConfig, final GraphicChartMapConfigPage chartMapConfig) {
		super(frame, Label.GRAPHIC_CONFIG_PANE, themeConfig, texturesConfig, chartMapConfig);

		this.themeConfig = themeConfig;
		this.texturesConfig = texturesConfig;
		this.chartMapConfig = chartMapConfig;

		finishInit();
	}

	@Override
	protected boolean save() {
		themeConfig.save();
		texturesConfig.save(frame);
		chartMapConfig.save();

		GraphicalConfig.markChanged();
		GraphicalConfig.save();

		frame.updateEditAreaSizes();
		frame.resize();

		return true;
	}

	@Override
	protected boolean cancel() {
		return true;
	}
}
