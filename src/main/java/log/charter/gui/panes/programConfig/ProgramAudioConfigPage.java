package log.charter.gui.panes.programConfig;

import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.gui.components.containers.Page;
import log.charter.gui.components.containers.RowedPanel;
import log.charter.gui.components.utils.RowedPosition;
import log.charter.sound.SoundFileType;
import log.charter.sound.system.AudioSystemType;

public class ProgramAudioConfigPage implements Page {

	private final AudioSystemType audioSystemType = Config.audioSystemType;
	private final String audioSystemName = Config.audioSystemName;
	private final int leftOutChannelId = Config.leftOutChannelId;
	private final int rightOutChannelId = Config.rightOutChannelId;
	private final SoundFileType baseAudioFormat = Config.baseAudioFormat;
	private final int delay = Config.delay;
	private final int audioBufferMs = Config.audioBufferMs;
	private final int midiDelay = Config.midiDelay;

	@Override
	public void init(final RowedPanel panel, final RowedPosition position) {
		// TODO Auto-generated method stub

	}

	@Override
	public Label label() {
		return Label.CONFIG_AUDIO;
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	public void save() {

	}
}
