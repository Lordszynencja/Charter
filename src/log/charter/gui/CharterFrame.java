package log.charter.gui;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.data.managers.HighlightManager;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.chartPanelDrawers.common.AudioDrawer;
import log.charter.gui.chartPanelDrawers.common.BeatsDrawer;
import log.charter.gui.chartPanelDrawers.common.DrawerUtils;
import log.charter.gui.handlers.AudioHandler;
import log.charter.gui.handlers.CharterFrameComponentListener;
import log.charter.gui.handlers.CharterFrameWindowFocusListener;
import log.charter.gui.handlers.CharterFrameWindowListener;
import log.charter.gui.handlers.KeyboardHandler;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler;
import log.charter.gui.handlers.MouseHandler;
import log.charter.gui.handlers.SongFileHandler;
import log.charter.gui.menuHandlers.CharterMenuBar;
import log.charter.main.LogCharterRSMain;

public class CharterFrame extends JFrame {
	private static final long serialVersionUID = 3603305480386377813L;

	private final CharterMenuBar charterMenuBar = new CharterMenuBar();
	private final ChartPanel chartPanel = new ChartPanel();
	private final JScrollBar scrollBar = createScrollBar();
	private final JLabel helpLabel = createHelp();

	private final AudioDrawer audioDrawer = new AudioDrawer();
	private final AudioHandler audioHandler = new AudioHandler();
	private final BeatsDrawer beatsDrawer = new BeatsDrawer();
	private final ChartData data = new ChartData();
	private final HighlightManager highlightManager = new HighlightManager();
	private final KeyboardHandler keyboardHandler = new KeyboardHandler();
	private final ModeManager modeManager = new ModeManager();
	private final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler = new MouseButtonPressReleaseHandler();
	private final MouseHandler mouseHandler = new MouseHandler();
	private final SongFileHandler songFileHandler = new SongFileHandler();
	private final SelectionManager selectionManager = new SelectionManager();
	private final UndoSystem undoSystem = new UndoSystem();

	private final Framer framer = new Framer(this::frame);

	public CharterFrame() {
		super(LogCharterRSMain.TITLE + " : " + Label.NO_PROJECT.label());
		setLayout(null);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setLocationByPlatform(true);
		setSize(Config.windowWidth, Config.windowHeight);
		setLocation(Config.windowPosX, Config.windowPosY);

		audioDrawer.init(data, chartPanel);
		audioHandler.init(data, this, keyboardHandler);
		beatsDrawer.init(data, chartPanel, mouseButtonPressReleaseHandler, selectionManager);
		data.init(audioHandler, charterMenuBar, modeManager, selectionManager, undoSystem);
		keyboardHandler.init(audioHandler, data, this, modeManager, mouseHandler);
		highlightManager.init(data, modeManager, selectionManager);
		modeManager.init(data, this, keyboardHandler, selectionManager, undoSystem);
		mouseButtonPressReleaseHandler.init(highlightManager);
		mouseHandler.init(audioHandler, data, keyboardHandler, modeManager, mouseButtonPressReleaseHandler,
				selectionManager);
		songFileHandler.init(data, this, undoSystem);
		selectionManager.init(data, modeManager, mouseButtonPressReleaseHandler);
		undoSystem.init(data, modeManager, selectionManager);

		charterMenuBar.init(audioDrawer, audioHandler, data, this, keyboardHandler, modeManager, selectionManager,
				songFileHandler, undoSystem);
		chartPanel.init(audioDrawer, beatsDrawer, data, highlightManager, keyboardHandler, modeManager,
				mouseButtonPressReleaseHandler, mouseHandler, selectionManager);

		add(chartPanel, 0, Config.windowWidth, DrawerUtils.HEIGHT);
		add(scrollBar, DrawerUtils.HEIGHT, Config.windowWidth, 20);
		add(helpLabel, DrawerUtils.HEIGHT + 20, Config.windowWidth, 300);

		addComponentListener(new CharterFrameComponentListener(chartPanel, scrollBar));
		addKeyListener(keyboardHandler);
		addWindowFocusListener(new CharterFrameWindowFocusListener(this));
		addWindowListener(new CharterFrameWindowListener(this));

		setGuitarHelp();

		validate();
		setVisible(true);

		framer.start();
	}

	private void frame() {
		audioHandler.frame();
		keyboardHandler.moveFromArrowKeys();
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
		if (undoSystem.isSaved()) {
			return true;
		}

		final int result = JOptionPane.showConfirmDialog(this, Label.UNSAVED_CHANGES_MESSAGE.label(),
				Label.UNSAVED_CHANGES_POPUP.label(), JOptionPane.YES_NO_CANCEL_OPTION);

		if (result == JOptionPane.YES_OPTION) {
			songFileHandler.save();
			return true;
		}

		if (result == JOptionPane.NO_OPTION) {
			return true;
		}

		return false;
	}

	public void showPopup(final String msg) {
		JOptionPane.showMessageDialog(this, msg);
	}

	public String showInputDialog(final String msg, final String value) {
		return JOptionPane.showInputDialog(this, msg, value);
	}

	private void updateTitle() {
		final String title = makeTitle();
		if (title.equals(getTitle())) {
			return;
		}

		setTitle(title);
	}

	private String makeTitle() {
		if (data.isEmpty) {
			return LogCharterRSMain.TITLE + " : " + Label.NO_PROJECT.label();
		}
		return LogCharterRSMain.TITLE + " : " + data.songChart.artistName + " - " + data.songChart.title + " : "//
				+ data.getCurrentArrangement().getTypeNameLabel()//
				+ (undoSystem.isSaved() ? "" : "*");
	}

	public void cancelAllActions() {
		audioHandler.stopMusic();
		keyboardHandler.clearKeys();
	}

	public void exit() {
		audioHandler.stopMusic();
		if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this, Label.EXIT_MESSAGE.label(),
				Label.EXIT_POPUP.label(), JOptionPane.YES_NO_OPTION)) {
			if (!checkChanged()) {
				return;
			}

			dispose();
			System.exit(0);
		}
	}

}
