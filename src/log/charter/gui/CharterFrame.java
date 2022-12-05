package log.charter.gui;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollBar;

import log.charter.data.Config;
import log.charter.gui.handlers.CharterFrameComponentListener;
import log.charter.gui.handlers.CharterFrameMouseWheelListener;
import log.charter.gui.handlers.CharterFrameWindowFocusListener;
import log.charter.gui.handlers.CharterFrameWindowListener;
import log.charter.main.LogCharterMain;

public class CharterFrame extends JFrame {
	private static final long serialVersionUID = 3603305480386377813L;

	public final ChartEventsHandler handler;
	public final ChartPanel chartPanel;
	public final CharterMenuBar menuBar;
	public final JScrollBar scrollBar;
	public final JLabel helpLabel;

	public CharterFrame() {
		super(LogCharterMain.TITLE);
		setLayout(null);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setLocationByPlatform(true);
		setVisible(true);
		setSize(Config.windowWidth, Config.windowHeight);
		setLocation(Config.windowPosX, Config.windowPosY);

		handler = new ChartEventsHandler(this);
		chartPanel = new ChartPanel(handler);
		add(chartPanel, 0, Config.windowWidth, ChartPanel.HEIGHT);
		menuBar = new CharterMenuBar(handler);
		setJMenuBar(menuBar);
		scrollBar = createScrollBar();
		add(scrollBar, ChartPanel.HEIGHT, Config.windowWidth, 20);
		helpLabel = createHelp();
		add(helpLabel, ChartPanel.HEIGHT + 20, Config.windowWidth, 300);
		setGuitarHelp();

		addKeyListener(handler);
		addMouseWheelListener(new CharterFrameMouseWheelListener(handler));
		addWindowFocusListener(new CharterFrameWindowFocusListener(handler));
		addWindowListener(new CharterFrameWindowListener(handler));
		addComponentListener(new CharterFrameComponentListener(this));
		validate();
	}

	private void add(final JComponent component, final int y, final int w, final int h) {
		component.setBounds(0, y, w, h);
		final Dimension size = new Dimension(w, h);
		component.setMinimumSize(size);
		component.setPreferredSize(size);
		component.setMaximumSize(size);
		component.validate();

		add(component);
	}

	private JLabel createHelp() {
		final JLabel help = new JLabel();
		help.setVerticalAlignment(JLabel.TOP);
		return help;
	}

	private JScrollBar createScrollBar() {
		final JScrollBar scrollBar = new JScrollBar(JScrollBar.HORIZONTAL, 0, 1, 0, 10000);
		scrollBar.addAdjustmentListener(e -> {
			final double length = handler.data.music.msLength();
			handler.setNextTimeWithoutScrolling((length * e.getValue()) / scrollBar.getMaximum());
		});

		return scrollBar;
	}

	public void setGuitarHelp() {
		helpLabel.setText("<html>G, 1-9 → set grid size<br>"//
				+ "1-9 when mouse is on beat → set beats in measure<br>"//
				+ "Left press above tempo section → add/edit/remove song section<br>"//
				+ "</html>");
	}

	public void setDrumsHelp() {
		helpLabel.setText("<html>G → toggle grid<br>"//
				+ "G, 1-9 → set grid size<br>"//
				+ "1-9 when mouse is on beat → set beats in measure<br>"//
				+ "Left press above tempo section → add/edit/remove song section<br>"//
				+ "Ctrl + W → toggle Star Power section<br>"//
				+ "Ctrl + P → toggle Solo section<br>"//
				+ "Ctrl + K → toggle Drum Roll section<br>"//
				+ "Ctrl + L → toggle Special Drum Roll section<br>"//
				+ "Ctrl + E → toggle expert+ bass<br>"//
				+ "Ctrl + Y → toggle yellow tom<br>"//
				+ "Ctrl + B → toggle blue tom<br>"//
				+ "Ctrl + G → toggle green tom<br>"//
				+ "Ctrl + H → set auto-HOPO for selected notes (type max distance since previous note in ms to make it HOPO)<br>"//
				+ "</html>");
	}

	public void setKeysHelp() {
		helpLabel.setText("<html>G → toggle grid<br>"//
				+ "G, 1-9 → set grid size<br>"//
				+ "1-9 when mouse is on beat → set beats in measure<br>"//
				+ "Left press above tempo section → add/edit/remove song section<br>"//
				+ "Ctrl + W → toggle Star Power section<br>"//
				+ "Ctrl + Y → toggle Solo section<br>"//
				+ "U → toggle selected notes crazy<br>"//
				+ "</html>");
	}

	public void setLyricsHelp() {
		helpLabel.setText("<html>G → toggle grid<br>"//
				+ "G, 1-9 → set grid size<br>"//
				+ "1-9 when mouse is on beat → set beats in measure<br>"//
				+ "Left press above tempo section → add/edit/remove song section<br>"//
				+ "Ctrl + W → toggle Star Power section<br>"//
				+ "Ctrl + L → place vocal line (vocals editing)<br>"//
				+ "L → edit vocal note (vocals editing)<br>"//
				+ "T → toggle note toneless (vocals editing)<br>"//
				+ "Q → toggle note connected (vocals editing)<br>"//
				+ "W → toggle note is word part (vocals editing)<br></html>");
	}
}
