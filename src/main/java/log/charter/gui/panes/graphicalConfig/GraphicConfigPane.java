package log.charter.gui.panes.graphicalConfig;

import log.charter.data.config.GraphicalConfig;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.PagedDialog;
import log.charter.services.CharterContext;

public final class GraphicConfigPane extends PagedDialog {
	private static final long serialVersionUID = -3193534671039163160L;

	private final CharterContext context;

	private final GraphicThemeConfigPage themeConfig;
	private final GraphicTexturesConfigPage texturesConfig;
	private final GraphicChartMapConfigPage chartMapConfig;

	public GraphicConfigPane(final CharterFrame charterFrame, final CharterContext context) {
		this(charterFrame, context, new GraphicThemeConfigPage(), //
				new GraphicTexturesConfigPage(), //
				new GraphicChartMapConfigPage()//
		);
	}

	private GraphicConfigPane(final CharterFrame charterFrame, final CharterContext context,
			final GraphicThemeConfigPage themeConfig, final GraphicTexturesConfigPage texturesConfig,
			final GraphicChartMapConfigPage chartMapConfig) {
		super(charterFrame, Label.GRAPHIC_CONFIG_PANE, themeConfig, texturesConfig, chartMapConfig);

		this.context = context;

		this.themeConfig = themeConfig;
		this.texturesConfig = texturesConfig;
		this.chartMapConfig = chartMapConfig;

		finishInit();
	}

	@Override
	protected boolean save() {
		themeConfig.save();
		texturesConfig.save(context);
		chartMapConfig.save();

		GraphicalConfig.markChanged();
		GraphicalConfig.save();

		frame.updateSizes();
		frame.resize();

		return true;
	}

	@Override
	protected boolean cancel() {
		return true;
	}
}
