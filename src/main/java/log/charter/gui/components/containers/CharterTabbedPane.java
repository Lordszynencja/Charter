package log.charter.gui.components.containers;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTabbedPane;

import log.charter.gui.lookAndFeel.CharterTabbedPaneUI;

public class CharterTabbedPane extends JTabbedPane {
	private static final long serialVersionUID = 7754083325093561588L;

	public static class Tab {
		public final String name;
		public final Component component;

		public Icon icon = null;
		public String tip = null;

		public Tab(final String name, final Component component) {
			this.name = name;
			this.component = component;
		}

		public Tab icon(final Icon value) {
			icon = value;
			return this;
		}

		public Tab tip(final String value) {
			tip = value;
			return this;
		}
	}

	public CharterTabbedPane(final Tab... tabs) {
		super();
		setUI(new CharterTabbedPaneUI());

		for (final Tab tab : tabs) {
			this.addTab(tab.name, tab.icon, tab.component, tab.tip);
		}
	}
}
