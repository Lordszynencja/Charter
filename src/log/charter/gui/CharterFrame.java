package log.charter.gui;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;

import log.charter.data.ChartData;
import log.charter.data.Config;
import log.charter.data.UndoSystem;
import log.charter.gui.chartPanelDrawers.common.DrawerUtils;
import log.charter.gui.handlers.AudioHandler;
import log.charter.gui.handlers.ChartPanelMouseListener;
import log.charter.gui.handlers.CharterFrameComponentListener;
import log.charter.gui.handlers.CharterFrameWindowFocusListener;
import log.charter.gui.handlers.CharterFrameWindowListener;
import log.charter.gui.handlers.SongFileHandler;
import log.charter.main.LogCharterRSMain;

public class CharterFrame extends JFrame {
	private static final long serialVersionUID = 3603305480386377813L;

	private final ChartPanel chartPanel = new ChartPanel();
	private final CharterMenuBar menuBar = new CharterMenuBar();
	private final JScrollBar scrollBar = createScrollBar();
	private final JLabel helpLabel = createHelp();

	private final ChartPanelMouseListener chartPanelMouseListener = new ChartPanelMouseListener();

	private final ChartData data = new ChartData();
	private final AudioHandler audioHandler = new AudioHandler();
	private final ChartKeyboardHandler chartKeyboardHandler = new ChartKeyboardHandler();
	private final HighlightManager highlightManager = new HighlightManager();
	private final SongFileHandler songFileHandler = new SongFileHandler();
	private final SelectionManager selectionManager = new SelectionManager();
	private final UndoSystem undoSystem = new UndoSystem();

	private final Framer framer = new Framer(this::frame);

	public CharterFrame() {
		super(LogCharterRSMain.TITLE + " : No project");
		setLayout(null);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setLocationByPlatform(true);
		setSize(Config.windowWidth, Config.windowHeight);
		setLocation(Config.windowPosX, Config.windowPosY);

		chartPanelMouseListener.init(audioHandler, data, chartKeyboardHandler, selectionManager);

		audioHandler.init(data, this, chartKeyboardHandler);
		data.init(this, menuBar, undoSystem);
		chartKeyboardHandler.init(audioHandler, data, this, selectionManager);
		chartPanel.init(chartPanelMouseListener, audioHandler, data, chartKeyboardHandler, highlightManager,
				selectionManager);
		highlightManager.init(data, chartPanelMouseListener, selectionManager);
		menuBar.init(audioHandler, chartKeyboardHandler, this, data, songFileHandler);
		songFileHandler.init(data, this);
		selectionManager.init(data);
		undoSystem.init(data);

		add(chartPanel, 0, Config.windowWidth, DrawerUtils.HEIGHT);
		add(scrollBar, DrawerUtils.HEIGHT, Config.windowWidth, 20);
		add(helpLabel, DrawerUtils.HEIGHT + 20, Config.windowWidth, 300);

		addComponentListener(new CharterFrameComponentListener(chartPanel, scrollBar));
		addKeyListener(chartKeyboardHandler);
		addWindowFocusListener(new CharterFrameWindowFocusListener(chartKeyboardHandler));
		addWindowListener(new CharterFrameWindowListener(chartKeyboardHandler));

		setGuitarHelp();

		validate();
		setVisible(true);

		framer.start();
	}

	private void frame() {
		audioHandler.frame();
		chartKeyboardHandler.moveFromArrowKeys();
		updateTitle();

		data.time = (int) data.nextTime;

		repaint();
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

	private int getMusicLength() {
		return data.music.msLength();
	}

	private JScrollBar createScrollBar() {
		final JScrollBar scrollBar = new JScrollBar(JScrollBar.HORIZONTAL, 0, 1, 0, 2000);
		scrollBar.addAdjustmentListener(e -> {
			final int length = getMusicLength();
			final double nextTime = 1.0 * length * e.getValue() / scrollBar.getMaximum();
			data.setNextTime((int) nextTime);
		});

		return scrollBar;
	}

	public void setNextTime(final int t) {
		final int songLength = getMusicLength();
		final int value = (int) Math.round(songLength == 0 ? 0 : 1.0 * t * scrollBar.getMaximum() / songLength);
		scrollBar.setValue(value);
		data.setNextTime(t);
	}

	public void setGuitarHelp() {
		helpLabel.setText("<html>G, 1-9 → set grid size<br>"//
				+ "1-9 when mouse is on beat → set beats in measure<br>"//
				+ "Left press above tempo section → add/edit/remove song section<br>"//
				+ "</html>");
	}

	public void setLyricsHelp() {
		helpLabel.setText("<html>G → toggle grid<br>"//
				+ "G, 1-9 → set grid size<br>"//
				+ "1-9 when mouse is on beat → set beats in measure<br>"//
				+ "Left press above tempo section → add/edit/remove song section<br>"//
				+ "Ctrl + L → place vocal line (vocals editing)<br>"//
				+ "L → edit vocal note (vocals editing)<br>"//
				+ "W → toggle note is word part (vocals editing)<br></html>");
	}

	public boolean checkChanged() {
		if (data.changed) {
			final int result = JOptionPane.showConfirmDialog(this, "You have unsaved changes. Do you want to save?",
					"Unsaved changes", JOptionPane.YES_NO_CANCEL_OPTION);

			if (result == JOptionPane.YES_OPTION) {
				songFileHandler.save();
				return true;
			}
			if (result == JOptionPane.NO_OPTION) {
				return true;
			}

			return false;
		}

		return true;
	}

	public void showPopup(final String msg) {
		JOptionPane.showMessageDialog(this, msg);
	}

	public String showInputDialog(final String msg, final String value) {
		return JOptionPane.showInputDialog(this, msg, value);
	}

	private void updateTitle() {
		String title;
		if (data.isEmpty) {
			title = LogCharterRSMain.TITLE + " : No project";
		} else {
			title = LogCharterRSMain.TITLE + " : " + data.songChart.artistName + " - " + data.songChart.title + " : "//
					+ data.editMode.name//
					+ (data.changed ? "*" : "");
		}

		if (title.equals(getTitle())) {
			return;
		}

		setTitle(title);
	}
}
