package log.charter.gui.handlers.windows;

import log.charter.data.ChartData;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.RepeatManager;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.preview3D.Preview3DFrame;
import log.charter.gui.components.preview3D.Preview3DPanel;
import log.charter.gui.handlers.data.ChartTimeHandler;
import log.charter.gui.handlers.mouseAndKeyboard.KeyboardHandler;
import log.charter.io.Logger;

public class WindowedPreviewHandler {
	private final Preview3DFrame windowedPreviewFrame = new Preview3DFrame();
	private final Preview3DPanel windowedPreview3DPanel = new Preview3DPanel();

	public void init(final ChartData chartData, final CharterFrame charterFrame,
			final ChartTimeHandler chartTimeHandler, final KeyboardHandler keyboardHandler,
			final ModeManager modeManager, final RepeatManager repeatManager) {
		windowedPreview3DPanel.init(chartData, chartTimeHandler, keyboardHandler, modeManager, repeatManager);
		windowedPreviewFrame.init(charterFrame, keyboardHandler, windowedPreview3DPanel);
	}

	public void reloadTextures() {
		windowedPreview3DPanel.reloadTextures();
	}

	public void switchWindowedPreview() {
		windowedPreviewFrame.setVisible(!windowedPreviewFrame.isVisible());
	}

	public void switchBorderlessWindowedPreview() {
		if (windowedPreviewFrame.isUndecorated()) {
			windowedPreviewFrame.setWindowed();
		} else {
			windowedPreviewFrame.setBorderlessFullScreen();
		}
	}

	public void paintFrame() {
		try {
			if (windowedPreviewFrame.isShowing()) {
				windowedPreviewFrame.repaint();
				windowedPreview3DPanel.repaint();
			}
		} catch (final Exception e) {
			Logger.error("Exception in frame()", e);
		}
	}

	public boolean temporaryDispose() {
		if (!windowedPreviewFrame.isVisible() || !windowedPreviewFrame.isFocused()) {
			return false;
		}

		windowedPreviewFrame.dispose();
		return true;
	}

	public void restore() {
		windowedPreviewFrame.setVisible(true);
	}
}
