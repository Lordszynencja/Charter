package log.charter.services.mouseAndKeyboard;

import static java.awt.event.KeyEvent.VK_ALT;
import static java.awt.event.KeyEvent.VK_CONTROL;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_SHIFT;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Optional;

import org.jcodec.common.logging.Logger;

import log.charter.services.Action;
import log.charter.services.ActionHandler;
import log.charter.services.editModes.ModeManager;

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

	public void setRewind() {
		heldNonModifierKey = VK_LEFT;
		heldAction = Action.MOVE_BACKWARD;
		actionHandler.fireAction(heldAction);
	}

	public void clearRewind() {
		if (heldAction == Action.MOVE_BACKWARD && heldNonModifierKey == VK_LEFT) {
			heldNonModifierKey = -1;
			heldAction = null;
		}
	}

	public void setFastForward() {
		heldNonModifierKey = VK_RIGHT;
		heldAction = Action.MOVE_FORWARD;
		actionHandler.fireAction(heldAction);
	}

	public void clearFastForward() {
		if (heldAction == Action.MOVE_FORWARD && heldNonModifierKey == VK_RIGHT) {
			heldNonModifierKey = -1;
			heldAction = null;
		}
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

	@Override
	public void keyPressed(final KeyEvent e) {
		try {
			int keyCode = e.getKeyCode();
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
			if (keyCode == KeyEvent.VK_ADD) {
				keyCode = KeyEvent.VK_PLUS;
			}
			if (keyCode == KeyEvent.VK_SUBTRACT) {
				keyCode = KeyEvent.VK_MINUS;
			}

			heldNonModifierKey = keyCode;
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
		try {
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
		} catch (final Exception ex) {
			Logger.error("Exception on key released " + KeyEvent.getKeyText(e.getKeyCode()), ex);
		}
	}

	@Override
	public void keyTyped(final KeyEvent e) {
	}
}
