package log.charter.gui.components.containers;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JTabbedPane;

import log.charter.data.config.Localization.Label;
import log.charter.gui.lookAndFeel.CharterTabbedPaneUI;

public class CharterTabbedPane extends JTabbedPane implements ComponentListener {
	private static final long serialVersionUID = 7754083325093561588L;

	public static class Tab {
		public final String name;
		public final Component component;

		public Icon icon = null;
		public String tip = null;
		public Color textColorOverride = null;

		public Tab(final Label label, final Component component) {
			this(label.label(), component);
		}

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

		public void setTextColorOverride(final Color override) {
			textColorOverride = override;
		}

		public void clearTextColorOVerride() {
			textColorOverride = null;
		}
	}

	public final List<Tab> tabs;

	public CharterTabbedPane(final List<Tab> tabs) {
		super();
		setUI(new CharterTabbedPaneUI(this));

		this.tabs = tabs;

		for (final Tab tab : tabs) {
			this.addTab(tab.name, tab.icon, tab.component, tab.tip);
		}

		addComponentListener(this);
	}

	@Override
	public Icon getIconAt(final int index) {
		return tabs.get(index).icon;
	}

	@Override
	public void componentResized(final ComponentEvent e) {
		final Dimension newTabSize = new Dimension(getWidth() - 25, getHeight() - 40);

		for (int i = 0; i < getTabCount(); i++) {
			final Component component = getComponentAt(i);
			component.setPreferredSize(newTabSize);
			component.setSize(newTabSize);
		}
	}

	@Override
	public void componentMoved(final ComponentEvent e) {
	}

	@Override
	public void componentShown(final ComponentEvent e) {
	}

	@Override
	public void componentHidden(final ComponentEvent e) {
	}
}
