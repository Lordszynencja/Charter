package log.charter.services;

import static log.charter.data.config.SystemType.MAC;

import log.charter.data.config.SystemType;
import log.charter.gui.components.preview3D.Preview3DFrame;
import log.charter.gui.components.preview3D.Preview3DPanel;
import log.charter.io.Logger;
import log.charter.services.CharterContext.Initiable;

public class WindowedPreviewHandler implements Initiable {
	private CharterContext charterContext;

	private final Preview3DFrame windowedPreviewFrame = SystemType.is(MAC) ? null : new Preview3DFrame();
	private final Preview3DPanel windowedPreview3DPanel = SystemType.is(MAC) ? null : new Preview3DPanel();

	@Override
	public void init() {
		if (SystemType.is(MAC)) {
			return;
		}

		charterContext.initObject(windowedPreview3DPanel);
		charterContext.initObject(windowedPreviewFrame);
		windowedPreviewFrame.initWith(windowedPreview3DPanel);
	}

	public void reloadTextures() {
		if (SystemType.is(MAC)) {
			return;
		}

		windowedPreview3DPanel.reloadTextures();
	}

	public void switchWindowedPreview() {
		if (SystemType.is(MAC)) {
			return;
		}

		windowedPreviewFrame.setVisible(!windowedPreviewFrame.isVisible());
	}

	public void switchBorderlessWindowedPreview() {
		if (SystemType.is(MAC)) {
			return;
		}

		if (windowedPreviewFrame.isUndecorated()) {
			windowedPreviewFrame.setWindowed();
		} else {
			windowedPreviewFrame.setBorderlessFullScreen();
		}
	}

	public void paintFrame() {
		if (SystemType.is(MAC)) {
			return;
		}

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
		if (SystemType.is(MAC)) {
			return false;
		}

		if (!windowedPreviewFrame.isVisible() || !windowedPreviewFrame.isFocused()) {
			return false;
		}

		windowedPreviewFrame.dispose();
		return true;
	}

	public void restore() {
		if (SystemType.is(MAC)) {
			return;
		}

		windowedPreviewFrame.setVisible(true);
	}

	public boolean isPreviewVisible() {
		if (SystemType.is(MAC)) {
			return false;
		}

		return windowedPreviewFrame.isShowing();
	}
}
