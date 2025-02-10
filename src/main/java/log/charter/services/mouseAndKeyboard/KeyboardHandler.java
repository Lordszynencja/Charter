package log.charter.services.mouseAndKeyboard;

import static java.awt.event.KeyEvent.VK_ALT;
import static java.awt.event.KeyEvent.VK_CONTROL;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_META;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_SHIFT;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Optional;

import log.charter.io.Logger;
import log.charter.services.Action;
import log.charter.services.ActionHandler;
import log.charter.services.editModes.ModeManager;

public class KeyboardHandler implements KeyListener {
	private ActionHandler actionHandler;
	private ModeManager modeManager;

	private Shortcut shortcut = new Shortcut();

	private Action heldAction = null;

	public void clearKeys() {
		shortcut = new Shortcut();
		heldAction = null;
	}

	public void setRewind() {
		shortcut.key = VK_LEFT;
		heldAction = Action.MOVE_BACKWARD;
		actionHandler.fireAction(heldAction);
	}

	public void clearRewind() {
		if (heldAction == Action.MOVE_BACKWARD && shortcut.key == VK_LEFT) {
			shortcut.key = -1;
			heldAction = null;
		}
	}

	public void setFastForward() {
		shortcut.key = VK_RIGHT;
		heldAction = Action.MOVE_FORWARD;
		actionHandler.fireAction(heldAction);
	}

	public void clearFastForward() {
		if (heldAction == Action.MOVE_FORWARD && shortcut.key == VK_RIGHT) {
			shortcut.key = -1;
			heldAction = null;
		}
	}

	public Optional<Action> heldAction() {
		return Optional.ofNullable(heldAction);
	}

	public boolean alt() {
		return shortcut.alt;
	}

	public boolean ctrl() {
		return shortcut.ctrl;
	}

	public boolean shift() {
		return shortcut.shift;
	}

	private void replaceHeldAction() {
		heldAction = ShortcutConfig.getAction(modeManager.getMode(), shortcut);
	}

	@Override
	public void keyPressed(final KeyEvent e) {
		Logger.debug("Pressed " + e.getKeyChar());

		try {
			int keyCode = e.getKeyCode();
			if (keyCode == KeyEvent.VK_UNDEFINED) {
				return;
			}

			if (keyCode == VK_CONTROL) {
				shortcut.ctrl = true;
				replaceHeldAction();
				return;
			}
			if (keyCode == VK_SHIFT) {
				shortcut.shift = true;
				replaceHeldAction();
				return;
			}
			if (keyCode == VK_ALT) {
				shortcut.alt = true;
				replaceHeldAction();
				return;
			}
			if (keyCode == VK_META) {
				shortcut.command = true;
				replaceHeldAction();
				return;
			}
			if (keyCode == KeyEvent.VK_ADD) {
				keyCode = KeyEvent.VK_PLUS;
			}
			if (keyCode == KeyEvent.VK_SUBTRACT) {
				keyCode = KeyEvent.VK_MINUS;
			}

			shortcut.key = keyCode;
			replaceHeldAction();

			if (heldAction != null) {
				actionHandler.fireAction(heldAction);
			}

			e.consume();
		} catch (final Exception ex) {
			Logger.error("Exception on key pressed " + KeyEvent.getKeyText(e.getKeyCode()), ex);
		}
	}

	@Override
	public void keyReleased(final KeyEvent e) {
		Logger.debug("Released " + e.getKeyChar());

		try {
			final int keyCode = e.getKeyCode();
			switch (keyCode) {
				case VK_CONTROL:
					shortcut.ctrl = false;
					replaceHeldAction();
					break;
				case VK_SHIFT:
					shortcut.shift = false;
					replaceHeldAction();
					break;
				case VK_ALT:
					shortcut.alt = false;
					replaceHeldAction();
					break;
				case VK_META:
					shortcut.command = false;
					replaceHeldAction();
					break;
				default:
					if (shortcut.key == keyCode) {
						shortcut.key = -1;
						heldAction = null;
					}
					break;
			}

			e.consume();
		} catch (final Exception ex) {
			Logger.error("Exception on key released " + KeyEvent.getKeyText(e.getKeyCode()), ex);
		}
	}

	@Override
	public void keyTyped(final KeyEvent e) {
	}
}
