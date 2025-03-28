package log.charter.gui.menuHandlers;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.simple.SpecialMenuItem;
import log.charter.gui.components.utils.ComponentUtils;
import log.charter.io.Logger;
import log.charter.services.Action;
import log.charter.services.ActionHandler;
import log.charter.services.mouseAndKeyboard.Shortcut;
import log.charter.services.mouseAndKeyboard.ShortcutConfig;

abstract class CharterMenuHandler {
	private static void setDefaultColors(final JComponent component) {
		component.setBackground(CharterMenuBar.backgroundColor.color());
	}

	protected static JMenu createMenu(final Label label) {
		final JMenu menu = new JMenu(label.label());
		setDefaultColors(menu);
		return menu;
	}

	protected ActionHandler actionHandler;
	protected CharterFrame charterFrame;

	protected JMenuItem createItem(final Label label, final Runnable onAction) {
		return createItem(label.label(), onAction);
	}

	protected JMenuItem createItem(final String label, final Runnable onAction) {
		final JMenuItem item = new JMenuItem(label);
		item.addActionListener(e -> {
			try {
				onAction.run();
			} catch (final Throwable t) {
				Logger.error("Couldn't do action " + label, t);
				ComponentUtils.showPopup(charterFrame, Label.ERROR, t.getLocalizedMessage());
			}
		});
		setDefaultColors(item);
		return item;
	}

	protected JMenuItem createItem(final Action action, final Label label) {
		final Shortcut shortcut = ShortcutConfig.shortcuts.get(action);
		final String shortcutName = shortcut == null ? null : shortcut.name("-");
		final JMenuItem item = new SpecialMenuItem(label, shortcutName, () -> actionHandler.fireAction(action));
		setDefaultColors(item);
		return item;
	}

	protected JMenuItem createItem(final Action action) {
		return createItem(action, action.label);
	}

	protected JMenuItem createDisabledItem(final Action action) {
		final JMenuItem item = createItem(action);
		item.setEnabled(false);
		return item;
	}

	abstract boolean isApplicable();

	abstract JMenu prepareMenu();

}
