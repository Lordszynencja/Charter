package log.charter.gui.menuHandlers;

import static javax.swing.KeyStroke.getKeyStroke;

import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import log.charter.data.config.Localization.Label;

abstract class CharterMenuHandler {
	protected static JMenuItem createItem(final Label label, final Runnable onAction) {
		return createItem(label.label(), onAction);
	}

	protected static JMenuItem createItem(final String label, final Runnable onAction) {
		final JMenuItem item = new JMenuItem(label);
		item.addActionListener(e -> onAction.run());
		return item;
	}

	protected static JMenuItem createItem(final Label label, final KeyStroke keyStroke, final Runnable onAction) {
		final JMenuItem item = new JMenuItem(label.label());
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
