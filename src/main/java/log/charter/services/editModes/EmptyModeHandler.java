package log.charter.services.editModes;

import log.charter.services.mouseAndKeyboard.MouseButtonPressReleaseHandler.MouseButtonPressReleaseData;

public class EmptyModeHandler implements ModeHandler {
	@Override
	public void rightClick(final MouseButtonPressReleaseData clickData) {
	}

	@Override
	public void changeLength(final int change) {
	}

	@Override
	public void handleNumber(final int number) {
	}

	@Override
	public void clearNumbers() {
	}
}
