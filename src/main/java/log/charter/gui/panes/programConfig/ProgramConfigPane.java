package log.charter.gui.panes.programConfig;

import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.PagedDialog;
import log.charter.services.CharterContext;
import log.charter.services.utils.Framer;

public class ProgramConfigPane extends PagedDialog {
	private static final long serialVersionUID = -3193534671039163160L;

	private final Framer framer;

	private final ProgramGeneralConfigPage generalConfig;
	private final ProgramAudioConfigPage audioConfig;
	private final ProgramDisplayConfigPage displayConfig;

	public ProgramConfigPane(final CharterFrame charterFrame, final CharterContext context, final Framer framer) {
		this(charterFrame, context, framer, new ProgramGeneralConfigPage(), new ProgramAudioConfigPage(),
				new ProgramDisplayConfigPage());
	}

	private ProgramConfigPane(final CharterFrame charterFrame, final CharterContext context, final Framer framer,
			final ProgramGeneralConfigPage generalConfig, final ProgramAudioConfigPage audioConfig,
			final ProgramDisplayConfigPage displayConfig) {
		super(charterFrame, Label.CONFIG_PANE_TITLE, generalConfig, audioConfig, displayConfig);

		this.framer = framer;

		this.generalConfig = generalConfig;
		this.audioConfig = audioConfig;
		this.displayConfig = displayConfig;

		finishInit();
	}

	@Override
	protected boolean save() {
		generalConfig.save();
		audioConfig.save();
		displayConfig.save();

		Config.markChanged();
		Config.save();

		frame.updateSizes();
		frame.resize();

		framer.setFPS(Config.FPS);

		return true;
	}

	@Override
	protected boolean cancel() {
		return true;
	}
}
