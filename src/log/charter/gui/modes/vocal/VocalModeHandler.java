package log.charter.gui.modes.vocal;

import java.util.List;

import log.charter.gui.modes.ModeHandler;
import log.charter.song.Vocal;

public class VocalModeHandler extends ModeHandler {
	@Override
	public void handleHome() {
		if (!chartKeyboardHandler.ctrl()) {
			frame.setNextTime(0);
			return;
		}

		final List<Vocal> vocals = data.songChart.vocals.vocals;
		frame.setNextTime(vocals.isEmpty() ? 0 : vocals.get(0).position);
	}
}
