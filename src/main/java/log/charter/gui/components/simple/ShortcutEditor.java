package log.charter.gui.components.simple;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.KeyStroke;

import log.charter.services.Action;
import log.charter.services.mouseAndKeyboard.Shortcut;
import log.charter.services.mouseAndKeyboard.ShortcutConfig;

public class ShortcutEditor extends JButton implements ActionListener, FocusListener, KeyListener {
	private static final long serialVersionUID = -8841476344526552242L;

	private final Action action;
	public Shortcut shortcut;

	private void resetShortcut() {
		shortcut = new Shortcut(ShortcutConfig.getShortcut(action));
		resetText();
	}

	private void resetText() {
		String text = shortcut.name("-");
		if (!shortcut.isReady()) {
			text += "?";
		}

		setText(text);
	}

	public ShortcutEditor(final Action action) {
		this.action = action;
		resetShortcut();

		addActionListener(this);
		addFocusListener(this);
		getInputMap().put(KeyStroke.getKeyStroke("SPACE"), "none");
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		shortcut = new Shortcut();
		resetText();
		addKeyListener(this);
	}

	@Override
	public void focusGained(final FocusEvent e) {
	}

	@Override
	public void focusLost(final FocusEvent e) {
		if (shortcut.isReady()) {
			return;
		}

		resetShortcut();
	}

	@Override
	public void keyTyped(final KeyEvent e) {
	}

	@Override
	public void keyPressed(final KeyEvent e) {
		final int code = e.getKeyCode();
		if (code == KeyEvent.VK_UNDEFINED) {
			return;
		}
		if (code == KeyEvent.VK_ESCAPE) {
			resetShortcut();
			removeKeyListener(this);
			e.consume();
			return;
		}

		if (code == KeyEvent.VK_CONTROL) {
			shortcut.ctrl = true;
			resetText();
			return;
		}
		if (code == KeyEvent.VK_SHIFT) {
			shortcut.shift = true;
			resetText();
			return;
		}
		if (code == KeyEvent.VK_ALT) {
			shortcut.alt = true;
			resetText();
			return;
		}

		shortcut.key = code;
		removeKeyListener(this);
		resetText();
		e.consume();
	}

	@Override
	public void keyReleased(final KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_UNDEFINED) {
			return;
		}

		if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
			shortcut.ctrl = false;
			resetText();
			return;
		}
		if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
			shortcut.shift = false;
			resetText();
			return;
		}
		if (e.getKeyCode() == KeyEvent.VK_ALT) {
			shortcut.alt = false;
			resetText();
			return;
		}
	}

}
