package log.charter.gui.components.simple;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.KeyStroke;

import log.charter.gui.panes.shortcuts.ShortcutConfigPane;
import log.charter.services.Action;
import log.charter.services.mouseAndKeyboard.Shortcut;
import log.charter.services.mouseAndKeyboard.ShortcutConfig;

public class ShortcutEditor extends JButton implements ActionListener, FocusListener, KeyListener {
	private static final long serialVersionUID = -8841476344526552242L;

	private final ShortcutConfigPane parent;
	private final Action action;
	public Shortcut shortcut;

	private Color validColor;
	private boolean validShortcut = true;

	public ShortcutEditor(final ShortcutConfigPane parent, final Action action) {
		this.parent = parent;
		this.action = action;

		resetShortcut();

		addActionListener(this);
		addFocusListener(this);
		getInputMap().put(KeyStroke.getKeyStroke("SPACE"), "none");
	}

	private void resetText() {
		if (shortcut == null) {
			setText("");
			return;
		}

		String text = shortcut.name("-");
		if (!shortcut.isReady()) {
			text += "?";
		}

		setText(text);
	}

	public void setShortcut(final Shortcut newShortcut) {
		shortcut = newShortcut;
		resetText();
	}

	private void resetShortcut() {
		setShortcut(ShortcutConfig.shortcuts.get(action));
		validateShortcut();
	}

	public void validateShortcut() {
		final boolean previousValidationResult = validShortcut;
		validShortcut = parent.validShortcut(action, shortcut);
		if (previousValidationResult == validShortcut) {
			return;
		}

		if (!validShortcut) {
			if (validColor == null) {
				validColor = getBackground();
			}

			setBackground(Color.RED);
			setOpaque(validShortcut);
		} else if (validColor != null) {
			setBackground(validColor);
			validColor = null;
		}

		repaint();
	}

	public boolean isValidShortcut() {
		return validShortcut;
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
		if (code == KeyEvent.VK_META) {
			shortcut.command = true;
			resetText();
			return;
		}

		shortcut.key = code;
		removeKeyListener(this);
		parent.validateShortcuts();
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
		if (e.getKeyCode() == KeyEvent.VK_META) {
			shortcut.command = false;
			resetText();
			return;
		}
	}

}
