package log.charter.gui;

import static log.charter.data.config.Config.windowFullscreen;
import static log.charter.data.config.Config.windowHeight;
import static log.charter.data.config.Config.windowWidth;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.editAreaBottom;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalScrollBarUI;

import log.charter.data.ArrangementFixer;
import log.charter.data.ArrangementValidator;
import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.data.copySystem.CopyManager;
import log.charter.data.managers.HighlightManager;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.chartPanelDrawers.common.AudioDrawer;
import log.charter.gui.chartPanelDrawers.common.BeatsDrawer;
import log.charter.gui.components.preview3D.Preview3DPanel;
import log.charter.gui.components.selectionEditor.CurrentSelectionEditor;
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
import log.charter.sound.StretchedFileLoader;
import net.sf.image4j.codec.ico.ICODecoder;

public class CharterFrame extends JFrame {
	private static final long serialVersionUID = 3603305480386377813L;

	private final CharterMenuBar charterMenuBar = new CharterMenuBar();
	private final ChartPanel chartPanel = new ChartPanel();
	private final CurrentSelectionEditor currentSelectionEditor = new CurrentSelectionEditor();
	private final JScrollBar scrollBar = createScrollBar();
	private final JLabel helpLabel = createHelp();
	private final Preview3DPanel preview3DPanel = new Preview3DPanel();
	private final JTabbedPane tabs = createTabs();

	private final JFrame fullscreenPreviewFrame = new JFrame();
	private final Preview3DPanel fullscreenPreview3DPanel = new Preview3DPanel();

	private final ArrangementFixer arrangementFixer = new ArrangementFixer();
	private final ArrangementValidator arrangementValidator = new ArrangementValidator();
	private final AudioDrawer audioDrawer = new AudioDrawer();
	private final AudioHandler audioHandler = new AudioHandler();
	private final BeatsDrawer beatsDrawer = new BeatsDrawer();
	private final CopyManager copyManager = new CopyManager();
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

	public CharterFrame(final String title) {
		super(title);
		try {
			final InputStream stream = this.getClass().getResourceAsStream("/icon.ico");
			setIconImages(ICODecoder.read(stream));
		} catch (final IOException e) {
			Logger.error("Couldn't load icon", e);
		}

		CharterTheme.install(this);

		setLayout(null);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setLocationByPlatform(true);
		setSize(Config.windowWidth, Config.windowHeight);
		if (Config.windowFullscreen) {
			setExtendedState(JFrame.MAXIMIZED_BOTH);
		}
		setLocation(Config.windowPosX, Config.windowPosY);

		arrangementFixer.init(data);
		arrangementValidator.init(data, this);
		audioDrawer.init(data, chartPanel);
		audioHandler.init(data, this, modeManager);
		beatsDrawer.init(data, chartPanel, modeManager, mouseButtonPressReleaseHandler, selectionManager);
		copyManager.init(data, this, modeManager, selectionManager, undoSystem);
		data.init(audioHandler, charterMenuBar, modeManager, scrollBar, selectionManager, undoSystem);
		keyboardHandler.init(audioDrawer, audioHandler, copyManager, data, this, modeManager, mouseHandler,
				selectionManager, songFileHandler, undoSystem);
		highlightManager.init(data, modeManager, selectionManager);
		modeManager.init(data, this, highlightManager, keyboardHandler, selectionManager, undoSystem);
		mouseButtonPressReleaseHandler.init(highlightManager);
		mouseHandler.init(audioHandler, data, this, keyboardHandler, modeManager, mouseButtonPressReleaseHandler,
				selectionManager, undoSystem);
		songFileHandler.init(arrangementFixer, arrangementValidator, audioHandler, data, this, charterMenuBar,
				modeManager, undoSystem);
		selectionManager.init(data, this, modeManager, mouseButtonPressReleaseHandler);
		undoSystem.init(data, modeManager, selectionManager);

		charterMenuBar.init(audioDrawer, audioHandler, copyManager, data, this, keyboardHandler, modeManager,
				selectionManager, songFileHandler, undoSystem);
		chartPanel.init(audioDrawer, beatsDrawer, data, highlightManager, keyboardHandler, modeManager,
				mouseButtonPressReleaseHandler, mouseHandler, selectionManager);
		currentSelectionEditor.init(arrangementFixer, data, selectionManager, undoSystem);
		preview3DPanel.init(data, keyboardHandler, modeManager);

		fullscreenPreview3DPanel.init(data, keyboardHandler, modeManager);
		fullscreenPreviewFrame.addKeyListener(keyboardHandler);
		fullscreenPreviewFrame.addWindowFocusListener(new CharterFrameWindowFocusListener(this));
		fullscreenPreviewFrame.add(fullscreenPreview3DPanel);
		fullscreenPreviewFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		fullscreenPreviewFrame.setUndecorated(true);

		add(chartPanel);
		add(scrollBar);
		add(tabs);
		resizeComponents();

		addComponentListener(new CharterFrameComponentListener(this));
		addKeyListener(keyboardHandler);
		addWindowFocusListener(new CharterFrameWindowFocusListener(this));
		addWindowListener(new CharterFrameWindowListener(this));

		setGuitarHelp();

		validate();
		setVisible(true);
		setFocusable(true);

		framer.start();
	}

	private void changeComponentBounds(final Component c, final int x, final int y, final int w, final int h) {
		final Dimension newSize = new Dimension(w, h);

		c.setMinimumSize(newSize);
		c.setPreferredSize(newSize);
		c.setMaximumSize(newSize);
		c.setBounds(x, y, w, h);
		c.validate();
		c.repaint();
	}

	public void resize() {
		windowHeight = getHeight();
		windowWidth = getWidth();
		windowFullscreen = getExtendedState() == JFrame.MAXIMIZED_BOTH;
		Config.markChanged();

		resizeComponents();
	}

	private void resizeComponents() {
		final Insets insets = getInsets();
		final int width = windowWidth - insets.left - insets.right;
		final int height = windowHeight - insets.top - insets.bottom - charterMenuBar.getHeight();

		changeComponentBounds(chartPanel, 0, 0, width, editAreaBottom);

		final int scrollBarHeight = 20;
		changeComponentBounds(scrollBar, 0, editAreaBottom, width, scrollBarHeight);

		final int tabsY = editAreaBottom + scrollBarHeight;
		final int tabsHeight = height - tabsY;
		changeComponentBounds(tabs, 0, tabsY, width, tabsHeight);
	}

	public CharterFrame(final String title, final String path) {
		this(title);

		songFileHandler.open(path);
	}

	public void switchFullscreenPreview() {
		fullscreenPreviewFrame.setVisible(!fullscreenPreviewFrame.isVisible());
	}

	private void frame() {
		audioHandler.frame();
		keyboardHandler.frame();
		updateTitle();

		data.time = (int) data.nextTime;

		if (isFocused()) {
			repaint();
			preview3DPanel.repaint();
		}

		if (fullscreenPreviewFrame.isFocused()) {
			fullscreenPreviewFrame.repaint();
			fullscreenPreview3DPanel.repaint();
		}
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

	private JLabel createHelp() {
		final JLabel help = new JLabel();
		help.setVerticalAlignment(JLabel.TOP);
		help.setBackground(ColorLabel.BASE_BG_2.color());
		help.setForeground(ColorLabel.BASE_DARK_TEXT.color());
		help.setOpaque(true);
		help.setFocusable(false);

		return help;
	}

	private JTabbedPane createTabs() {
		final JTextArea textArea = new JTextArea(1000, 1000);
		textArea.setBackground(ColorLabel.BASE_BG_2.color());
		textArea.setForeground(ColorLabel.BASE_TEXT.color());
		textArea.setCaretColor(ColorLabel.BASE_TEXT.color());

		final JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
		tabs.addTab("Quick edit", new JScrollPane(currentSelectionEditor));
		tabs.addTab("Help", helpLabel);
		tabs.addTab("Text", new JScrollPane(textArea));
		tabs.addTab("3D Preview", preview3DPanel);

		return tabs;
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

	public void selectionChanged() {
		currentSelectionEditor.selectionChanged();
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

		String title = LogCharterRSMain.TITLE + " : " + data.songChart.artistName + " - " + data.songChart.title
				+ " : ";

		if (data.songChart.arrangements.isEmpty()) {
			return title;
		}

		if (modeManager.editMode == EditMode.GUITAR) {
			title += data.getCurrentArrangement().getTypeNameLabel();
		} else if (modeManager.editMode == EditMode.TEMPO_MAP) {
			title += "Tempo map";
		} else if (modeManager.editMode == EditMode.VOCALS) {
			title += "Vocals";
		}

		title += undoSystem.isSaved() ? "" : "*";

		return title;
	}

	public void cancelAllActions() {
		audioHandler.stopMusic();
		keyboardHandler.clearKeys();
	}

	public void exit() {
		audioHandler.stopMusic();

		fullscreenPreviewFrame.setVisible(false);
		fullscreenPreviewFrame.repaint();

		if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this, Label.EXIT_MESSAGE.label(),
				Label.EXIT_POPUP.label(), JOptionPane.YES_NO_OPTION)) {
			if (!checkChanged()) {
				return;
			}

			fullscreenPreviewFrame.dispose();
			dispose();
			StretchedFileLoader.stopAllProcesses();
			System.exit(0);
		}
	}

	private final List<Integer> frameTimes = new ArrayList<>();

	@Override
	public void paint(final Graphics g) {
		super.paint(g);

		final int t = (int) (System.nanoTime() / 1_000_000);
		frameTimes.add(t);

		frameTimes.removeIf(t0 -> t - t0 > 1000);
		helpLabel.setText("FPS: " + frameTimes.size());
	}
}
