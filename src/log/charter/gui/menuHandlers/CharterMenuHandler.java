package log.charter.gui.menuHandlers;

import static javax.swing.KeyStroke.getKeyStroke;

import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

abstract class CharterMenuHandler {
	protected static JMenuItem createItem(final String name, final Runnable onAction) {
		final JMenuItem item = new JMenuItem(name);
		item.addActionListener(e -> onAction.run());
		return item;
	}

	protected static JMenuItem createItem(final String name, final KeyStroke keyStroke, final Runnable onAction) {
		final JMenuItem item = new JMenuItem(name);
		item.setAccelerator(keyStroke);
		item.addActionListener(e -> onAction.run());
		return item;
	}

	protected static KeyStroke button(final int keyCode) {
		return getKeyStroke(keyCode, 0);
	}

	protected static KeyStroke ctrl(final int keyCode) {
		return getKeyStroke(keyCode, KeyEvent.CTRL_DOWN_MASK);
	}

	protected static KeyStroke ctrlShift(final int keyCode) {
		return getKeyStroke(keyCode, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
	}

	abstract boolean isApplicable();

	abstract JMenu prepareMenu();
}
