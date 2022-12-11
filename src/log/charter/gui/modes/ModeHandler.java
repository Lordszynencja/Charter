package log.charter.gui.modes;

import log.charter.data.ChartData;
import log.charter.gui.ChartKeyboardHandler;
import log.charter.gui.CharterFrame;

public abstract class ModeHandler {
	protected ChartData data;
	protected CharterFrame frame;
	protected ChartKeyboardHandler chartKeyboardHandler;

	public void init(final ChartData data, final CharterFrame frame, final ChartKeyboardHandler chartEventsHandler) {
		this.data = data;
		this.frame = frame;
		chartKeyboardHandler = chartEventsHandler;
	}

	public abstract void handleHome();
}
