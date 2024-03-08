package log.charter.gui.handlers.mouseAndKeyboard;

import static java.awt.event.KeyEvent.VK_ALT;
import static java.awt.event.KeyEvent.VK_CONTROL;
import static java.awt.event.KeyEvent.VK_SHIFT;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Optional;

import log.charter.data.managers.ModeManager;
import log.charter.gui.handlers.Action;
import log.charter.gui.handlers.ActionHandler;

public class KeyboardHandler implements KeyListener {
	private ActionHandler actionHandler;
	private ModeManager modeManager;

	private boolean ctrl = false;
	private boolean alt = false;
	private boolean shift = false;

	private int heldNonModifierKey = -1;
	private Action heldAction = null;

	public void clearKeys() {
		ctrl = false;
		alt = false;
		shift = false;
		heldNonModifierKey = -1;
		heldAction = null;
	}

	public Optional<Action> heldAction() {
		return Optional.ofNullable(heldAction);
	}

	public boolean alt() {
		return alt;
	}

	public boolean ctrl() {
		return ctrl;
	}

	public boolean shift() {
		return shift;
	}

	private void replaceHeldAction() {
		heldAction = ShortcutConfig.getAction(modeManager.getMode(),
				new Shortcut(ctrl, shift, alt, heldNonModifierKey));
	}

	private void keyUsed(final KeyEvent e) {
		final int keyCode = e.getKeyCode();
		if (keyCode == KeyEvent.VK_UNDEFINED) {
			return;
		}

		if (keyCode == VK_CONTROL) {
			ctrl = true;
			replaceHeldAction();
			return;
		}
		if (keyCode == VK_SHIFT) {
			shift = true;
			replaceHeldAction();
			return;
		}
		if (keyCode == VK_ALT) {
			alt = true;
			replaceHeldAction();
			return;
		}

		heldNonModifierKey = keyCode;
		replaceHeldAction();
		if (heldAction != null) {
			actionHandler.fireAction(heldAction);
		}
	}

	@Override
	public void keyPressed(final KeyEvent e) {
		keyUsed(e);
		e.consume();
	}

	@Override
	public void keyReleased(final KeyEvent e) {
		final int keyCode = e.getKeyCode();
		switch (keyCode) {
			case KeyEvent.VK_CONTROL:
				ctrl = false;
				replaceHeldAction();
				break;
			case KeyEvent.VK_SHIFT:
				shift = false;
				replaceHeldAction();
				break;
			case KeyEvent.VK_ALT:
				alt = false;
				replaceHeldAction();
				break;
			default:
				if (heldNonModifierKey == keyCode) {
					heldNonModifierKey = -1;
					heldAction = null;
				}
				break;
		}

		e.consume();
	}

	@Override
	public void keyTyped(final KeyEvent e) {
	}
}
