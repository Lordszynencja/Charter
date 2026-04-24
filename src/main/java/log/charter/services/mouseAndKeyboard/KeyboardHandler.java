package log.charter.services.mouseAndKeyboard;

import static java.awt.event.KeyEvent.VK_ALT;
import static java.awt.event.KeyEvent.VK_CONTROL;
import static java.awt.event.KeyEvent.VK_INSERT;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_META;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_SHIFT;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Optional;

import log.charter.data.config.values.SecretsConfig;
import log.charter.gui.components.toolbar.ChartToolbar;
import log.charter.io.Logger;
import log.charter.services.Action;
import log.charter.services.ActionHandler;
import log.charter.services.editModes.ModeManager;

public class KeyboardHandler implements KeyListener {
	private static final int[] secretOptionsUnlockSequence = { //
			KeyEvent.VK_UP, //
			KeyEvent.VK_UP, //
			KeyEvent.VK_DOWN, //
			KeyEvent.VK_DOWN, //
			KeyEvent.VK_LEFT, //
			KeyEvent.VK_RIGHT, //
			KeyEvent.VK_LEFT, //
			KeyEvent.VK_RIGHT, //
			KeyEvent.VK_B, //
			KeyEvent.VK_A, //
			KeyEvent.VK_ENTER };
	private static int secretOptionsUnlockSequencePosition = 0;

	private ActionHandler actionHandler;
	private ChartToolbar chartToolbar;
	private ModeManager modeManager;

	private Shortcut shortcut = new Shortcut();

	private boolean scrollLock = false;

	private Action heldAction = null;

	private void setScrollLock() {
		scrollLock = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_SCROLL_LOCK);
		chartToolbar.setChartLockIcon();
	}

	public void clearKeys() {
		shortcut = new Shortcut();
		setScrollLock();
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

	public boolean ctrl() {
		return shortcut.ctrl;
	}

	public boolean shift() {
		return shortcut.shift;
	}

	public boolean alt() {
		return shortcut.alt;
	}

	public boolean insert() {
		return shortcut.insert;
	}

	public boolean scrollLock() {
		return scrollLock;
	}

	private void replaceHeldAction(final boolean fireAction) {
		heldAction = ShortcutConfig.getAction(modeManager.getMode(), shortcut);

		if (heldAction != null) {
			actionHandler.fireAction(heldAction);
		}
	}

	@Override
	public void keyPressed(final KeyEvent e) {
		Logger.debug("Pressed " + KeyEvent.getKeyText(e.getKeyCode()));

		try {
			int keyCode = e.getKeyCode();
			if (keyCode == KeyEvent.VK_UNDEFINED) {
				return;
			}

			if (keyCode == VK_CONTROL) {
				shortcut.ctrl = true;
				replaceHeldAction(true);
				return;
			}
			if (keyCode == VK_SHIFT) {
				shortcut.shift = true;
				replaceHeldAction(true);
				return;
			}
			if (keyCode == VK_ALT) {
				shortcut.alt = true;
				replaceHeldAction(true);
				return;
			}
			if (keyCode == VK_META) {
				shortcut.command = true;
				replaceHeldAction(true);
				return;
			}
			if (keyCode == VK_INSERT) {
				shortcut.insert = true;
				replaceHeldAction(true);
				return;
			}
			if (keyCode == KeyEvent.VK_SCROLL_LOCK) {
				setScrollLock();
				return;
			}

			if (keyCode == KeyEvent.VK_ADD) {
				keyCode = KeyEvent.VK_PLUS;
			}
			if (keyCode == KeyEvent.VK_SUBTRACT) {
				keyCode = KeyEvent.VK_MINUS;
			}

			if (!SecretsConfig.optionsUnlocked
					&& keyCode == secretOptionsUnlockSequence[secretOptionsUnlockSequencePosition]) {
				secretOptionsUnlockSequencePosition++;
				if (secretOptionsUnlockSequencePosition == secretOptionsUnlockSequence.length) {
					SecretsConfig.optionsUnlocked = true;
				}
			}

			shortcut.key = keyCode;
			replaceHeldAction(true);
			e.consume();
		} catch (final Exception ex) {
			Logger.error("Exception on key pressed " + KeyEvent.getKeyText(e.getKeyCode()), ex);
		}
	}

	@Override
	public void keyReleased(final KeyEvent e) {
		Logger.debug("Released " + KeyEvent.getKeyText(e.getKeyCode()));

		try {
			int keyCode = e.getKeyCode();
			if (keyCode == KeyEvent.VK_ADD) {
				keyCode = KeyEvent.VK_PLUS;
			}
			if (keyCode == KeyEvent.VK_SUBTRACT) {
				keyCode = KeyEvent.VK_MINUS;
			}

			switch (keyCode) {
				case VK_CONTROL:
					shortcut.ctrl = false;
					replaceHeldAction(false);
					break;
				case VK_SHIFT:
					shortcut.shift = false;
					replaceHeldAction(false);
					break;
				case VK_ALT:
					shortcut.alt = false;
					replaceHeldAction(false);
					break;
				case VK_META:
					shortcut.command = false;
					replaceHeldAction(false);
					break;
				case VK_INSERT:
					shortcut.insert = false;
					replaceHeldAction(false);
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
