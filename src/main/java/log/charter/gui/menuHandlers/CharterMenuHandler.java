package log.charter.gui.menuHandlers;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import log.charter.data.config.Localization.Label;
import log.charter.gui.components.SpecialMenuItem;
import log.charter.gui.handlers.mouseAndKeyboard.Action;
import log.charter.gui.handlers.mouseAndKeyboard.KeyboardHandler;
import log.charter.gui.handlers.mouseAndKeyboard.ShortcutConfig;

abstract class CharterMenuHandler {
	protected static JMenuItem createItem(final Label label, final Runnable onAction) {
		return createItem(label.label(), onAction);
	}

	protected static JMenuItem createItem(final String label, final Runnable onAction) {
		final JMenuItem item = new JMenuItem(label);
		item.addActionListener(e -> onAction.run());
		return item;
	}

	protected static JMenuItem createItem(final KeyboardHandler keyboardHandler, final Action action) {
		final Label label = action.label;
		final String shortcutName = ShortcutConfig.getShortcut(action).name("-");
		return new SpecialMenuItem(label, shortcutName, () -> keyboardHandler.fireAction(action));
	}

	abstract boolean isApplicable();

	abstract JMenu prepareMenu();

}
