package log.charter.services.editModes;

import log.charter.services.mouseAndKeyboard.MouseButtonPressReleaseHandler.MouseButtonPressReleaseData;

public interface ModeHandler {
	public void rightClick(MouseButtonPressReleaseData clickData);

	public void changeLength(int change);

	public void handleNumber(int number);

	public void clearNumbers();
}
