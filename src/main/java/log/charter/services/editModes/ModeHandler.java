package log.charter.services.editModes;

import log.charter.services.mouseAndKeyboard.MouseButtonPressReleaseHandler.MouseButtonPressReleaseData;

public abstract class ModeHandler {
	public abstract void rightClick(MouseButtonPressReleaseData clickData);

	public abstract void changeLength(int change);
}
