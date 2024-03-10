package log.charter.gui.components.preview3D;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JFrame;

import log.charter.data.config.Config;
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

		setLocation(Config.previewWindowPosX, Config.previewWindowPosY);
		setSize(Config.previewWindowWidth, Config.previewWindowHeight);
		if (Config.previewWindowBorderless) {
			setBorderlessFullScreen();
			setExtendedState(JFrame.MAXIMIZED_BOTH);
		} else {
			setExtendedState(Config.previewWindowExtendedState);
		}
	}

	public void setBorderlessFullScreen() {
		dispose();
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setUndecorated(true);
		setVisible(true);

		Config.previewWindowBorderless = true;
		Config.markChanged();
	}

	public void setWindowed() {
		dispose();
		setExtendedState(Config.previewWindowExtendedState);
		setUndecorated(false);
		setVisible(true);

		Config.previewWindowBorderless = false;
		Config.markChanged();
	}

	@Override
	public void componentHidden(final ComponentEvent e) {
	}

	@Override
	public void componentMoved(final ComponentEvent e) {
		Config.previewWindowPosX = e.getComponent().getX();
		Config.previewWindowPosY = e.getComponent().getY();
		Config.markChanged();
	}

	@Override
	public void componentResized(final ComponentEvent e) {
		Config.previewWindowExtendedState = getExtendedState();
		Config.previewWindowWidth = getWidth();
		Config.previewWindowHeight = getHeight();
		Config.markChanged();
	}

	@Override
	public void componentShown(final ComponentEvent e) {
	}
}
