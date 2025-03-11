package log.charter.gui.components.preview3D;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JFrame;

import log.charter.data.config.Config;
import log.charter.data.config.values.WindowStateConfig;
import log.charter.gui.CharterFrame;
import log.charter.services.CharterFrameWindowFocusListener;
import log.charter.services.mouseAndKeyboard.KeyboardHandler;

public class Preview3DFrame extends JFrame implements ComponentListener {
	private static final long serialVersionUID = 7948615183140664734L;

	private CharterFrame charterFrame;
	private KeyboardHandler keyboardHandler;

	public void initWith(final Preview3DPanel preview3DPanel) {
		setIconImages(charterFrame.getIconImages());

		addKeyListener(keyboardHandler);
		addWindowFocusListener(new CharterFrameWindowFocusListener(keyboardHandler));
		addComponentListener(this);
		add(preview3DPanel);

		setLocation(WindowStateConfig.previewX, WindowStateConfig.previewY);
		setSize(WindowStateConfig.previewWidth, WindowStateConfig.previewHeight);
		if (WindowStateConfig.previewBorderless) {
			setBorderlessFullScreen();
			setExtendedState(JFrame.MAXIMIZED_BOTH);
		} else {
			setExtendedState(WindowStateConfig.previewExtendedState);
		}
	}

	public void setBorderlessFullScreen() {
		dispose();
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setUndecorated(true);
		setVisible(true);

		WindowStateConfig.previewBorderless = true;
		Config.markChanged();
	}

	public void setWindowed() {
		dispose();
		setExtendedState(WindowStateConfig.previewExtendedState);
		setLocation(WindowStateConfig.previewX, WindowStateConfig.previewY);
		setSize(WindowStateConfig.previewWidth, WindowStateConfig.previewHeight);
		setUndecorated(false);
		setVisible(true);

		WindowStateConfig.previewBorderless = false;
		Config.markChanged();
	}

	@Override
	public void componentHidden(final ComponentEvent e) {
	}

	@Override
	public void componentMoved(final ComponentEvent e) {
		if (WindowStateConfig.previewBorderless) {
			return;
		}

		WindowStateConfig.previewX = e.getComponent().getX();
		WindowStateConfig.previewY = e.getComponent().getY();
		Config.markChanged();
	}

	@Override
	public void componentResized(final ComponentEvent e) {
		if (WindowStateConfig.previewBorderless) {
			return;
		}

		WindowStateConfig.previewExtendedState = getExtendedState();
		WindowStateConfig.previewWidth = getWidth();
		WindowStateConfig.previewHeight = getHeight();
		Config.markChanged();
	}

	@Override
	public void componentShown(final ComponentEvent e) {
	}
}
