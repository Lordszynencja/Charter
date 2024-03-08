package log.charter.gui.menuHandlers;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import log.charter.data.config.Localization.Label;
import log.charter.gui.components.simple.SpecialMenuItem;
import log.charter.gui.handlers.Action;
import log.charter.gui.handlers.ActionHandler;
import log.charter.gui.handlers.mouseAndKeyboard.Shortcut;
import log.charter.gui.handlers.mouseAndKeyboard.ShortcutConfig;

abstract class CharterMenuHandler {
	private static void setDefaultColors(final JComponent component) {
		component.setBackground(CharterMenuBar.backgroundColor.color());
	}

	protected static JMenu createMenu(final Label label) {
		final JMenu menu = new JMenu(label.label());
		setDefaultColors(menu);
		return menu;
	}

	protected static JMenuItem createItem(final Label label, final Runnable onAction) {
		return createItem(label.label(), onAction);
	}

	protected static JMenuItem createItem(final String label, final Runnable onAction) {
		final JMenuItem item = new JMenuItem(label);
		item.addActionListener(e -> onAction.run());
		setDefaultColors(item);
		return item;
	}

	protected ActionHandler actionHandler;

	protected void init(final ActionHandler actionHandler) {
		this.actionHandler = actionHandler;
	}

	protected JMenuItem createItem(final Action action) {
		final Label label = action.label;
		final Shortcut shortcut = ShortcutConfig.getShortcut(action);
		final String shortcutName = shortcut == null ? null : shortcut.name("-");
		final JMenuItem item = new SpecialMenuItem(label, shortcutName, () -> actionHandler.fireAction(action));
		setDefaultColors(item);
		return item;
	}

	abstract boolean isApplicable();

	abstract JMenu prepareMenu();

}
