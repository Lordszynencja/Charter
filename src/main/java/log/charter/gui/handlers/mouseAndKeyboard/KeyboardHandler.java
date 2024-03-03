package log.charter.gui.handlers.mouseAndKeyboard;

import static java.awt.event.KeyEvent.VK_0;
import static java.awt.event.KeyEvent.VK_1;
import static java.awt.event.KeyEvent.VK_2;
import static java.awt.event.KeyEvent.VK_3;
import static java.awt.event.KeyEvent.VK_4;
import static java.awt.event.KeyEvent.VK_5;
import static java.awt.event.KeyEvent.VK_6;
import static java.awt.event.KeyEvent.VK_7;
import static java.awt.event.KeyEvent.VK_8;
import static java.awt.event.KeyEvent.VK_9;
import static java.awt.event.KeyEvent.VK_ALT;
import static java.awt.event.KeyEvent.VK_CONTROL;
import static java.awt.event.KeyEvent.VK_NUMPAD0;
import static java.awt.event.KeyEvent.VK_NUMPAD1;
import static java.awt.event.KeyEvent.VK_NUMPAD2;
import static java.awt.event.KeyEvent.VK_NUMPAD3;
import static java.awt.event.KeyEvent.VK_NUMPAD4;
import static java.awt.event.KeyEvent.VK_NUMPAD5;
import static java.awt.event.KeyEvent.VK_NUMPAD6;
import static java.awt.event.KeyEvent.VK_NUMPAD7;
import static java.awt.event.KeyEvent.VK_NUMPAD8;
import static java.awt.event.KeyEvent.VK_NUMPAD9;
import static java.awt.event.KeyEvent.VK_SHIFT;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.gui.Framer;
import log.charter.gui.handlers.Action;
import log.charter.gui.handlers.ActionHandler;
import log.charter.gui.handlers.data.ChartTimeHandler;

public class KeyboardHandler implements KeyListener {
	private ActionHandler actionHandler;
	private ChartTimeHandler chartTimeHandler;
	private Framer framer;
	private ModeManager modeManager;

	private boolean ctrl = false;
	private boolean alt = false;
	private boolean shift = false;

	private int heldNonModifierKey = -1;
	private Action heldAction = null;

	public void init(final ActionHandler actionHandler, final ChartTimeHandler chartTimeHandler, final Framer framer,
			final ModeManager modeManager) {
		this.actionHandler = actionHandler;
		this.chartTimeHandler = chartTimeHandler;
		this.framer = framer;
		this.modeManager = modeManager;
	}

	public void clearKeys() {
		ctrl = false;
		alt = false;
		shift = false;
		heldNonModifierKey = -1;
		heldAction = null;
	}

	public void frame() {
		if (heldAction == null) {
			return;
		}

		double speed;
		switch (heldAction) {
			case FAST_BACKWARD:
				speed = -framer.frameLength * 32;
				break;
			case FAST_FORWARD:
				speed = framer.frameLength * 32;
				break;
			case MOVE_BACKWARD:
				speed = -framer.frameLength * 4;
				break;
			case MOVE_FORWARD:
				speed = framer.frameLength * 4;
				break;
			case SLOW_BACKWARD:
				speed = -framer.frameLength;
				break;
			case SLOW_FORWARD:
				speed = framer.frameLength;
				break;
			default:
				return;
		}

		chartTimeHandler.setNextTime(chartTimeHandler.time() + speed);
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

	private int getKeyNumber(final int code) {
		return switch (code) {
			case VK_0 -> 0;
			case VK_NUMPAD0 -> 0;
			case VK_1 -> 1;
			case VK_NUMPAD1 -> 1;
			case VK_2 -> 2;
			case VK_NUMPAD2 -> 2;
			case VK_3 -> 3;
			case VK_NUMPAD3 -> 3;
			case VK_4 -> 4;
			case VK_NUMPAD4 -> 4;
			case VK_5 -> 5;
			case VK_NUMPAD5 -> 5;
			case VK_6 -> 6;
			case VK_NUMPAD6 -> 6;
			case VK_7 -> 7;
			case VK_NUMPAD7 -> 7;
			case VK_8 -> 8;
			case VK_NUMPAD8 -> 8;
			case VK_9 -> 0;
			case VK_NUMPAD9 -> 0;
			default -> -1;
		};
	}

	private static final String markBookmarkActionName = Action.MARK_BOOKMARK_0.name().substring(0,
			Action.MARK_BOOKMARK_0.name().length() - 1);
	private static final String moveToBookmarkActionName = Action.MOVE_TO_BOOKMARK_0.name().substring(0,
			Action.MOVE_TO_BOOKMARK_0.name().length() - 1);
	private static final String fretActionName = Action.MOVE_TO_BOOKMARK_0.name().substring(0,
			Action.MOVE_TO_BOOKMARK_0.name().length() - 1);

	private void tryKeyNumber(final int keyCode) {
		if (modeManager.getMode() == EditMode.EMPTY) {
			return;
		}

		final int number = getKeyNumber(keyCode);
		if (number < 0 || number > 9) {
			return;
		}

		String actionName;
		if (ctrl) {
			actionName = markBookmarkActionName;
		} else if (shift) {
			actionName = moveToBookmarkActionName;
		} else if (modeManager.getMode() == EditMode.GUITAR) {
			actionName = fretActionName;
		} else {
			return;
		}

		actionHandler.fireAction(Action.valueOf(actionName + number));
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
			return;
		}

		tryKeyNumber(keyCode);
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
