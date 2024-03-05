package log.charter.gui.components.containers;

import java.awt.Component;

import javax.swing.JScrollPane;

import log.charter.gui.lookAndFeel.CharterScrollBarUI;

public class CharterScrollPane extends JScrollPane {
	private static final long serialVersionUID = 7947218119497728703L;

	public CharterScrollPane(final Component view) {
		super(view);

		getVerticalScrollBar().setUI(new CharterScrollBarUI());
		getHorizontalScrollBar().setUI(new CharterScrollBarUI());
	}

	public CharterScrollPane(final Component view, final int vsbPolicy, final int hsbPolicy) {
		super(view, vsbPolicy, hsbPolicy);
	}
}
