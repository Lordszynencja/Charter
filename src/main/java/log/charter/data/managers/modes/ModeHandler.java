package log.charter.data.managers.modes;

import log.charter.gui.handlers.mouseAndKeyboard.MouseButtonPressReleaseHandler.MouseButtonPressReleaseData;

public abstract class ModeHandler {
	public abstract void handleEnd();

	public abstract void handleHome();

	public abstract void rightClick(MouseButtonPressReleaseData clickData);

	public abstract void changeLength(int change);
}
