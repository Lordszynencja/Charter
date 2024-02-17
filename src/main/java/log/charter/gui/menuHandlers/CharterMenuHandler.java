package log.charter.gui.menuHandlers;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import log.charter.data.config.Localization.Label;

abstract class CharterMenuHandler {
	protected static JMenuItem createItem(final Label label, final Runnable onAction) {
		return createItem(label.label(), onAction);
	}

	protected static JMenuItem createItem(final String label, final Runnable onAction) {
		final JMenuItem item = new JMenuItem(label);
		item.addActionListener(e -> onAction.run());
		return item;
	}

	abstract boolean isApplicable();

	abstract JMenu prepareMenu();

}
