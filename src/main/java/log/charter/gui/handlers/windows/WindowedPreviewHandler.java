package log.charter.gui.handlers.windows;

import log.charter.data.managers.CharterContext;
import log.charter.data.managers.CharterContext.Initiable;
import log.charter.gui.components.preview3D.Preview3DFrame;
import log.charter.gui.components.preview3D.Preview3DPanel;
import log.charter.io.Logger;

public class WindowedPreviewHandler implements Initiable {
	private CharterContext charterContext;

	private final Preview3DFrame windowedPreviewFrame = new Preview3DFrame();
	private final Preview3DPanel windowedPreview3DPanel = new Preview3DPanel();

	@Override
	public void init() {
		charterContext.initObject(windowedPreview3DPanel);
		charterContext.initObject(windowedPreviewFrame);
		windowedPreviewFrame.initWith(windowedPreview3DPanel);
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
