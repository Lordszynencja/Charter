package log.charter.gui;

import static log.charter.data.config.Config.windowExtendedState;
import static log.charter.data.config.Config.windowHeight;
import static log.charter.data.config.Config.windowWidth;
import static log.charter.gui.components.utils.ComponentUtils.askYesNoCancel;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import log.charter.data.ArrangementFixer;
import log.charter.data.ArrangementValidator;
import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.data.copySystem.CopyManager;
import log.charter.data.managers.HighlightManager;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.RepeatManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.chartPanelDrawers.common.BeatsDrawer;
import log.charter.gui.chartPanelDrawers.common.DrawerUtils;
import log.charter.gui.chartPanelDrawers.common.waveform.WaveFormDrawer;
import log.charter.gui.components.containers.CharterScrollPane;
import log.charter.gui.components.preview3D.Preview3DFrame;
import log.charter.gui.components.preview3D.Preview3DPanel;
import log.charter.gui.components.selectionEditor.CurrentSelectionEditor;
import log.charter.gui.components.simple.ChartMap;
import log.charter.gui.components.toolbar.ChartToolbar;
import log.charter.gui.components.utils.ComponentUtils.ConfirmAnswer;
import log.charter.gui.handlers.ActionHandler;
import log.charter.gui.handlers.AudioHandler;
import log.charter.gui.handlers.CharterFrameComponentListener;
import log.charter.gui.handlers.CharterFrameWindowFocusListener;
import log.charter.gui.handlers.CharterFrameWindowListener;
import log.charter.gui.handlers.SongFileHandler;
import log.charter.gui.handlers.data.ChartItemsHandler;
import log.charter.gui.handlers.data.ChartTimeHandler;
import log.charter.gui.handlers.data.ProjectAudioHandler;
import log.charter.gui.handlers.mouseAndKeyboard.KeyboardHandler;
import log.charter.gui.handlers.mouseAndKeyboard.MouseButtonPressReleaseHandler;
import log.charter.gui.handlers.mouseAndKeyboard.MouseHandler;
import log.charter.gui.lookAndFeel.CharterTabbedPaneUI;
import log.charter.gui.lookAndFeel.CharterTheme;
import log.charter.gui.menuHandlers.CharterMenuBar;
import log.charter.io.Logger;
import log.charter.main.CharterMain;
import log.charter.song.Arrangement;
import log.charter.sound.StretchedFileLoader;
import net.sf.image4j.codec.ico.ICODecoder;

public class CharterFrame extends JFrame {
	private static final long serialVersionUID = 3603305480386377813L;

	private final CharterMenuBar charterMenuBar = new CharterMenuBar();
	private final ChartToolbar chartToolbar = new ChartToolbar();
	private final ChartPanel chartPanel = new ChartPanel();
	private final CurrentSelectionEditor currentSelectionEditor = new CurrentSelectionEditor();
	private final ChartMap chartMap = new ChartMap();
	private final JLabel helpLabel = createHelp();
	private final Preview3DPanel preview3DPanel = new Preview3DPanel();
	private final JTabbedPane tabs = createTabs();

	private final Preview3DFrame windowedPreviewFrame = new Preview3DFrame();
	private final Preview3DPanel windowedPreview3DPanel = new Preview3DPanel();

	private final ActionHandler actionHandler = new ActionHandler();
	private final ArrangementFixer arrangementFixer = new ArrangementFixer();
	private final ArrangementValidator arrangementValidator = new ArrangementValidator();
	private final AudioHandler audioHandler = new AudioHandler();
	private final BeatsDrawer beatsDrawer = new BeatsDrawer();
	private final ChartItemsHandler chartItemsHandler = new ChartItemsHandler();
	private final ChartTimeHandler chartTimeHandler = new ChartTimeHandler();
	private final CopyManager copyManager = new CopyManager();
	private final ChartData data = new ChartData();
	private final HighlightManager highlightManager = new HighlightManager();
	private final KeyboardHandler keyboardHandler = new KeyboardHandler();
	private final ModeManager modeManager = new ModeManager();
	private final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler = new MouseButtonPressReleaseHandler();
	private final MouseHandler mouseHandler = new MouseHandler();
	private final ProjectAudioHandler projectAudioHandler = new ProjectAudioHandler();
	private final RepeatManager repeatManager = new RepeatManager();
	private final SongFileHandler songFileHandler = new SongFileHandler();
	private final SelectionManager selectionManager = new SelectionManager();
	private final UndoSystem undoSystem = new UndoSystem();
	private final WaveFormDrawer waveFormDrawer = new WaveFormDrawer();

	private final Framer framer = new Framer(this::frame);
	private final Thread audioFramer = new Thread(() -> {
		try {
			while (true) {
				audioFrame();
				Thread.sleep(0, 100_000);
			}
		} catch (final InterruptedException e) {
			Logger.error("error in audio framer", e);
		}
	});

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

		setSize(Config.windowWidth, Config.windowHeight);
		setLocation(Config.windowPosX, Config.windowPosY);
		setExtendedState(windowExtendedState);

		actionHandler.init(audioHandler, arrangementFixer, data, this, chartItemsHandler, chartTimeHandler,
				chartToolbar, copyManager, modeManager, mouseHandler, repeatManager, selectionManager, songFileHandler,
				undoSystem, waveFormDrawer);
		arrangementFixer.init(chartTimeHandler, data);
		arrangementValidator.init(chartTimeHandler, data, this, modeManager);
		audioHandler.init(chartTimeHandler, chartToolbar, data, this, modeManager, projectAudioHandler, repeatManager);
		beatsDrawer.init(data, chartPanel, repeatManager, selectionManager);
		chartItemsHandler.init(arrangementFixer, data, modeManager, selectionManager, undoSystem);
		chartTimeHandler.init(data, modeManager, projectAudioHandler);
		copyManager.init(chartTimeHandler, data, this, modeManager, selectionManager, undoSystem);
		data.init(this, audioHandler, charterMenuBar, modeManager, selectionManager, undoSystem);
		keyboardHandler.init(actionHandler, chartTimeHandler, framer, modeManager);
		highlightManager.init(chartTimeHandler, data, modeManager, selectionManager);
		modeManager.init(audioHandler, charterMenuBar, chartTimeHandler, chartToolbar, currentSelectionEditor, data,
				this, highlightManager, keyboardHandler, selectionManager, undoSystem);
		mouseButtonPressReleaseHandler.init(highlightManager);
		mouseHandler.init(arrangementFixer, chartTimeHandler, data, this, keyboardHandler, modeManager,
				mouseButtonPressReleaseHandler, selectionManager, undoSystem);
		projectAudioHandler.init(waveFormDrawer);
		repeatManager.init(audioHandler, chartTimeHandler, chartToolbar);
		songFileHandler.init(arrangementFixer, arrangementValidator, audioHandler, chartTimeHandler, data, this,
				charterMenuBar, modeManager, projectAudioHandler, undoSystem);
		selectionManager.init(chartTimeHandler, data, this, modeManager, mouseButtonPressReleaseHandler);
		undoSystem.init(chartTimeHandler, data, modeManager, selectionManager);
		waveFormDrawer.init(chartPanel, chartToolbar, modeManager, projectAudioHandler);

		charterMenuBar.init(actionHandler, arrangementFixer, audioHandler, chartTimeHandler, chartToolbar, copyManager,
				data, this, framer, modeManager, projectAudioHandler, selectionManager, songFileHandler, undoSystem,
				waveFormDrawer);
		chartToolbar.init(audioHandler, keyboardHandler, modeManager, repeatManager, waveFormDrawer);
		chartPanel.init(beatsDrawer, chartTimeHandler, data, highlightManager, keyboardHandler, modeManager,
				mouseButtonPressReleaseHandler, mouseHandler, selectionManager, waveFormDrawer);
		chartMap.init(chartTimeHandler, chartPanel, data, this, modeManager);
		currentSelectionEditor.init(arrangementFixer, data, this, chartItemsHandler, keyboardHandler, selectionManager,
				undoSystem);
		preview3DPanel.init(chartTimeHandler, data, keyboardHandler, modeManager, repeatManager);

		windowedPreview3DPanel.init(chartTimeHandler, data, keyboardHandler, modeManager, repeatManager);
		windowedPreviewFrame.init(this, keyboardHandler, windowedPreview3DPanel);

		add(chartToolbar);
		add(chartPanel);
		add(chartMap);
		add(tabs);
		resizeComponents();

		addComponentListener(new CharterFrameComponentListener(this));
		addKeyListener(keyboardHandler);
		addWindowFocusListener(new CharterFrameWindowFocusListener(keyboardHandler));
		addWindowListener(new CharterFrameWindowListener(this));

		validate();
		setVisible(true);
		setFocusable(true);

		framer.start();
		audioFramer.start();
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
		windowExtendedState = getExtendedState();
		Config.markChanged();

		resizeComponents();
	}

	private void resizeComponent(final AtomicInteger y, final Component c, final int width, final int height) {
		changeComponentBounds(c, 0, y.getAndAdd(height), width, height);
	}

	private void resizeComponents() {
		final Insets insets = getInsets();
		final int width = windowWidth - insets.left - insets.right;
		final int height = windowHeight - insets.top - insets.bottom - charterMenuBar.getHeight();

		final AtomicInteger y = new AtomicInteger(0);
		resizeComponent(y, chartToolbar, width, chartToolbar.getHeight());
		resizeComponent(y, chartPanel, width, DrawerUtils.editAreaHeight);
		resizeComponent(y, chartMap, width, DrawerUtils.chartMapHeight);
		changeComponentBounds(tabs, 0, y.get(), width, height - y.get());
	}

	public CharterFrame(final String title, final String path) {
		this(title);

		songFileHandler.open(path);
	}

	public void switchWindowedPreview() {
		windowedPreviewFrame.setVisible(!windowedPreviewFrame.isVisible());
	}

	public void switchBorderlessWindowedPreview() {
		if (windowedPreviewFrame.isUndecorated()) {
			windowedPreviewFrame.setWindowed();
		} else {
			windowedPreviewFrame.setBorderlessFullScreen();
		}
	}

	private void frame() {
		try {
			actionHandler.frame();
			keyboardHandler.frame();
			repeatManager.frame();
			updateTitle();

			chartTimeHandler.frame();

			if (windowedPreviewFrame.isShowing()) {
				windowedPreviewFrame.repaint();
				windowedPreview3DPanel.repaint();
			}
			if (preview3DPanel.isShowing()) {
				preview3DPanel.repaint();
			}

			repaint();
		} catch (final Exception e) {
			Logger.error("Exception in frame()", e);
		}
	}

	private void audioFrame() {
		try {
			audioHandler.frame();
		} catch (final Exception e) {
			Logger.error("Exception in audioFrame()", e);
		}
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
		tabs.setUI(new CharterTabbedPaneUI());

		tabs.addTab("Quick Edit", new CharterScrollPane(currentSelectionEditor));
		tabs.addTab("Help", helpLabel);
		tabs.addTab("Text", new CharterScrollPane(textArea));
		tabs.addTab("3D Preview", preview3DPanel);

		return tabs;
	}

	public boolean checkChanged() {
		if (undoSystem.isSaved()) {
			return true;
		}

		final ConfirmAnswer answer = askYesNoCancel(this, Label.UNSAVED_CHANGES_POPUP, Label.UNSAVED_CHANGES_MESSAGE);

		switch (answer) {
			case YES:
				songFileHandler.save();
			case NO:
				return true;
			default:
				return false;
		}
	}

	public void showPopup(final String msg) {
		JOptionPane.showMessageDialog(this, msg);
	}

	public String showInputDialog(final String msg, final String value) {
		return JOptionPane.showInputDialog(this, msg, value);
	}

	public void selectionChanged(final boolean stringsCouldChange) {
		currentSelectionEditor.selectionChanged(stringsCouldChange);
	}

	private void updateTitle() {
		final String title = makeTitle();
		if (title.equals(getTitle())) {
			return;
		}

		setTitle(title);
		repaint();
	}

	public void updateEditAreaSizes() {
		final EditMode editMode = modeManager.getMode();

		final Arrangement arrangement = data.getCurrentArrangement();
		final boolean bass = arrangement.isBass();
		final int strings = arrangement.tuning.strings();

		DrawerUtils.updateEditAreaSizes(editMode, bass, strings);
		resize();
	}

	private String getArrangementTitlePart() {
		final int number = data.currentArrangement + 1;
		final Arrangement arrangement = data.getCurrentArrangement();
		final String arrangementTypeName = arrangement.getTypeNameLabel();
		final String tuning = arrangement.getTuningName("%s - %s");

		return "[%d] %s (%s)".formatted(number, arrangementTypeName, tuning);
	}

	private String makeTitle() {
		if (data.isEmpty) {
			return CharterMain.TITLE + " : " + Label.NO_PROJECT.label();
		}

		String title = CharterMain.TITLE + " : " + data.songChart.artistName() + " - " + data.songChart.title() + " : ";

		switch (modeManager.getMode()) {
			case GUITAR:
				title += getArrangementTitlePart();
				break;
			case TEMPO_MAP:
				title += "Tempo map";
				break;
			case VOCALS:
				title += "Vocals";
				break;
			default:
				title += "Surprise mode! (contact dev for fix)";
				break;
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

		boolean restorePreviewWindow = false;
		if (windowedPreviewFrame.isVisible() && windowedPreviewFrame.isFocused()) {
			restorePreviewWindow = true;
			windowedPreviewFrame.dispose();
		}

		final int result = JOptionPane.showConfirmDialog(this, Label.EXIT_MESSAGE.label(), Label.EXIT_POPUP.label(),
				JOptionPane.YES_NO_OPTION);

		if (JOptionPane.YES_OPTION == result) {
			if (!checkChanged()) {
				return;
			}

			dispose();
			StretchedFileLoader.stopAllProcesses();
			System.exit(0);
			return;
		}

		if (restorePreviewWindow) {
			windowedPreviewFrame.setVisible(true);
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

	public void reloadTextures() {
		preview3DPanel.reloadTextures();
		windowedPreview3DPanel.reloadTextures();
	}
}
