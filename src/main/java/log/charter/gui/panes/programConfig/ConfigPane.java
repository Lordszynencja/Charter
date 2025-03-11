package log.charter.gui.panes.programConfig;

import log.charter.data.config.Config;
import log.charter.data.config.GraphicalConfig;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.PagedDialog;
import log.charter.services.CharterContext;
import log.charter.services.utils.Framer;

public class ConfigPane extends PagedDialog {
	private static final long serialVersionUID = -3193534671039163160L;

	private final Framer framer;

	private final ProgramGeneralConfigPage generalConfig;
	private final ProgramAudioConfigPage audioConfig;
	private final ProgramInstrumentConfigPage instrumentConfig;
	private final ProgramDisplayConfigPage displayConfig;

	public ConfigPane(final CharterFrame charterFrame, final CharterContext context, final Framer framer) {
		this(charterFrame, context, framer, new ProgramGeneralConfigPage(), new ProgramAudioConfigPage(),
				new ProgramInstrumentConfigPage(), new ProgramDisplayConfigPage());
	}

	private ConfigPane(final CharterFrame charterFrame, final CharterContext context, final Framer framer,
			final ProgramGeneralConfigPage generalConfig, final ProgramAudioConfigPage audioConfig,
			final ProgramInstrumentConfigPage instrumentConfig, final ProgramDisplayConfigPage displayConfig) {
		super(charterFrame, Label.CONFIG, generalConfig, audioConfig, instrumentConfig, displayConfig);

		this.framer = framer;

		this.generalConfig = generalConfig;
		this.audioConfig = audioConfig;
		this.instrumentConfig = instrumentConfig;
		this.displayConfig = displayConfig;

		finishInit();
	}

	@Override
	protected boolean save() {
		generalConfig.save();
		audioConfig.save();
		instrumentConfig.save();
		displayConfig.save();

		Config.markChanged();
		Config.save();

		GraphicalConfig.markChanged();
		GraphicalConfig.save();

		frame.updateSizes();
		frame.resize();

		framer.setFPS(GraphicalConfig.FPS);

		return true;
	}

	@Override
	protected boolean cancel() {
		return true;
	}
}
