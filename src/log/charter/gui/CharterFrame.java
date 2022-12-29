package log.charter.gui;

import java.awt.Dimension;
import java.awt.Insets;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalScrollBarUI;

import log.charter.data.ArrangementFixer;
import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.data.managers.HighlightManager;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.ChartPanelColors.ColorLabel;
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
import log.charter.gui.lookAndFeel.CharterTheme;
import log.charter.gui.menuHandlers.CharterMenuBar;
import log.charter.io.Logger;
import log.charter.main.LogCharterRSMain;

public class CharterFrame extends JFrame {
	private static final long serialVersionUID = 3603305480386377813L;

	private final CharterMenuBar charterMenuBar = new CharterMenuBar();
	private final ChartPanel chartPanel = new ChartPanel();
	private final JScrollBar scrollBar = createScrollBar();
	private final JLabel helpLabel = createHelp();

	private final ArrangementFixer arrangementFixer = new ArrangementFixer();
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
		try {
			final InputStream stream = this.getClass().getResourceAsStream("/rs_charter_icon.png");
			setIconImage(ImageIO.read(stream));
		} catch (final IOException e) {
			Logger.error("Couldn't load icon", e);
		}

		CharterTheme.install(this);

		setLayout(null);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setLocationByPlatform(true);
		setSize(Config.windowWidth, Config.windowHeight);
		setLocation(Config.windowPosX, Config.windowPosY);

		arrangementFixer.init(data);
		audioDrawer.init(data, chartPanel);
		audioHandler.init(data, this, keyboardHandler);
		beatsDrawer.init(data, chartPanel, mouseButtonPressReleaseHandler, selectionManager);
		data.init(audioHandler, charterMenuBar, modeManager, scrollBar, selectionManager, undoSystem);
		keyboardHandler.init(audioHandler, data, this, modeManager, mouseHandler, selectionManager);
		highlightManager.init(data, modeManager, selectionManager);
		modeManager.init(data, this, highlightManager, keyboardHandler, selectionManager, undoSystem);
		mouseButtonPressReleaseHandler.init(highlightManager);
		mouseHandler.init(audioHandler, data, keyboardHandler, modeManager, mouseButtonPressReleaseHandler,
				selectionManager);
		songFileHandler.init(arrangementFixer, data, this, charterMenuBar, undoSystem);
		selectionManager.init(data, modeManager, mouseButtonPressReleaseHandler);
		undoSystem.init(data, modeManager, selectionManager);

		charterMenuBar.init(audioDrawer, audioHandler, data, this, keyboardHandler, modeManager, selectionManager,
				songFileHandler, undoSystem);
		chartPanel.init(audioDrawer, beatsDrawer, data, highlightManager, keyboardHandler, modeManager,
				mouseButtonPressReleaseHandler, mouseHandler, selectionManager);

		final Insets insets = getInsets();
		final int widthDifference = insets.left + insets.right;

		add(chartPanel, 0, Config.windowWidth - widthDifference, DrawerUtils.HEIGHT);
		add(scrollBar, DrawerUtils.HEIGHT, Config.windowWidth - widthDifference, 20);
		add(helpLabel, DrawerUtils.HEIGHT + 20, Config.windowWidth - widthDifference,
				Config.windowHeight - DrawerUtils.HEIGHT - 20);

		addComponentListener(new CharterFrameComponentListener(this, chartPanel, helpLabel, scrollBar));
		addKeyListener(keyboardHandler);
		addWindowFocusListener(new CharterFrameWindowFocusListener(this));
		addWindowListener(new CharterFrameWindowListener(this));

		setGuitarHelp();

		validate();
		setVisible(true);
		setFocusable(true);

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
		help.setBackground(ColorLabel.BASE_BG_2.color());
		help.setForeground(ColorLabel.BASE_DARK_TEXT.color());
		help.setOpaque(true);

		return help;
	}

	private JScrollBar createScrollBar() {
		final JScrollBar scrollBar = new JScrollBar(JScrollBar.HORIZONTAL, 0, 0, 0, 1);
		scrollBar.addAdjustmentListener(e -> {
			data.setNextTime(e.getValue());
		});
		scrollBar.setBackground(ColorLabel.BASE_BG_4.color());

		scrollBar.setUI(new MetalScrollBarUI() {
			@Override
			protected JButton createDecreaseButton(final int orientation) {
				return createZeroButton();
			}

			@Override
			protected JButton createIncreaseButton(final int orientation) {
				return createZeroButton();
			}

			private JButton createZeroButton() {
				final JButton jbutton = new JButton();
				jbutton.setPreferredSize(new Dimension(0, 0));
				jbutton.setMinimumSize(new Dimension(0, 0));
				jbutton.setMaximumSize(new Dimension(0, 0));
				return jbutton;
			}

			@Override
			protected void configureScrollBarColors() {
				UIManager.put("ScrollBar.highlight", ColorLabel.BASE_BG_1.color());
				UIManager.put("ScrollBar.shadow", ColorLabel.BASE_BG_3.color());
				UIManager.put("ScrollBar.thumb", ColorLabel.BASE_BG_3.color());
				UIManager.put("ScrollBar.thumbShadow", ColorLabel.BASE_BG_2.color());
				UIManager.put("ScrollBar.thumbHighlight", ColorLabel.BASE_BG_4.color());

				super.configureScrollBarColors();
			}

		});

		return scrollBar;
	}

	public void setNextTime(final int t) {
		scrollBar.setValue(t);
		data.setNextTime(t);
	}

	public void setGuitarHelp() {
		helpLabel.setText("<html>TEST BUILD<br>"//
				+ "</html>");
	}

	public void setLyricsHelp() {
		helpLabel.setText("<html>TEST BUILD</html>");
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
