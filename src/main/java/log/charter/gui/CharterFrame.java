package log.charter.gui;

import static java.util.Arrays.asList;
import static log.charter.data.config.Config.windowExtendedState;
import static log.charter.data.config.Config.windowHeight;
import static log.charter.data.config.Config.windowWidth;
import static log.charter.gui.components.utils.ComponentUtils.askYesNo;
import static log.charter.gui.components.utils.ComponentUtils.askYesNoCancel;
import static log.charter.gui.components.utils.ComponentUtils.setComponentBoundsWithValidateRepaint;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.swing.JFrame;
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
import log.charter.gui.components.containers.CharterTabbedPane;
import log.charter.gui.components.containers.CharterTabbedPane.Tab;
import log.charter.gui.components.preview3D.Preview3DPanel;
import log.charter.gui.components.simple.ChartMap;
import log.charter.gui.components.tabs.HelpTab;
import log.charter.gui.components.tabs.selectionEditor.CurrentSelectionEditor;
import log.charter.gui.components.toolbar.ChartToolbar;
import log.charter.gui.components.utils.ComponentUtils.ConfirmAnswer;
import log.charter.gui.components.utils.TitleUpdater;
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
import log.charter.gui.handlers.windows.WindowedPreviewHandler;
import log.charter.gui.lookAndFeel.CharterTheme;
import log.charter.gui.menuHandlers.CharterMenuBar;
import log.charter.gui.utils.AudioFramer;
import log.charter.gui.utils.Framer;
import log.charter.io.Logger;
import log.charter.song.Arrangement;
import log.charter.sound.StretchedFileLoader;
import log.charter.util.CollectionUtils.Pair;
import net.sf.image4j.codec.ico.ICODecoder;

public class CharterFrame extends JFrame {
	private static final long serialVersionUID = 3603305480386377813L;

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
	private final TitleUpdater titleUpdater = new TitleUpdater();
	private final UndoSystem undoSystem = new UndoSystem();
	private final WaveFormDrawer waveFormDrawer = new WaveFormDrawer();
	private final WindowedPreviewHandler windowedPreviewHandler = new WindowedPreviewHandler();

	private final CharterMenuBar charterMenuBar = new CharterMenuBar();
	private final ChartToolbar chartToolbar = new ChartToolbar();
	private final ChartPanel chartPanel = new ChartPanel();
	private final ChartMap chartMap = new ChartMap();
	private final CurrentSelectionEditor currentSelectionEditor = new CurrentSelectionEditor();
	private final HelpTab helpTab = new HelpTab();
	private final JTextArea textArea = createTextArea();
	private final Preview3DPanel preview3DPanel = new Preview3DPanel();
	private final CharterTabbedPane tabs = new CharterTabbedPane(//
			new Tab("Quick Edit", new CharterScrollPane(currentSelectionEditor)), //
			new Tab("Help", helpTab), //
			new Tab("Text", new CharterScrollPane(textArea)), //
			new Tab("3D Preview", preview3DPanel));

	private final AudioFramer audioFramer = new AudioFramer();
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

		setSize(Config.windowWidth, Config.windowHeight);
		setLocation(Config.windowPosX, Config.windowPosY);
		setExtendedState(windowExtendedState);

		actionHandler.init(audioHandler, arrangementFixer, data, this, chartItemsHandler, chartTimeHandler,
				chartToolbar, copyManager, currentSelectionEditor, modeManager, mouseHandler, repeatManager,
				selectionManager, songFileHandler, undoSystem, waveFormDrawer);
		arrangementFixer.init(chartTimeHandler, data);
		arrangementValidator.init(chartTimeHandler, data, this, modeManager);
		audioFramer.init(audioHandler);
		audioHandler.init(chartTimeHandler, chartToolbar, data, this, modeManager, projectAudioHandler, repeatManager);
		beatsDrawer.init(data, chartPanel, repeatManager, selectionManager);
		chartItemsHandler.init(arrangementFixer, data, modeManager, selectionManager, undoSystem);
		chartTimeHandler.init(data, keyboardHandler, modeManager, projectAudioHandler);
		copyManager.init(chartTimeHandler, data, this, modeManager, selectionManager, undoSystem);
		data.init(this, audioHandler, charterMenuBar, modeManager, selectionManager, undoSystem);
		keyboardHandler.init(actionHandler, modeManager);
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
		selectionManager.init(data, chartTimeHandler, currentSelectionEditor, modeManager,
				mouseButtonPressReleaseHandler);
		titleUpdater.init(data, this, modeManager, undoSystem);
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
		preview3DPanel.init(data, chartTimeHandler, keyboardHandler, modeManager, repeatManager);
		windowedPreviewHandler.init(data, this, chartTimeHandler, keyboardHandler, modeManager, repeatManager);

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

		audioFramer.start();
		framer.start();
	}

	public void resize() {
		windowHeight = getHeight();
		windowWidth = getWidth();
		windowExtendedState = getExtendedState();
		Config.markChanged();

		resizeComponents();
	}

	private void resizeComponents() {
		final Insets insets = getInsets();
		final int width = windowWidth - insets.left - insets.right;
		final int height = windowHeight - insets.top - insets.bottom - charterMenuBar.getHeight();

		final List<Pair<Component, Integer>> componentHeights = asList(//
				new Pair<>(chartToolbar, chartToolbar.getHeight()), //
				new Pair<>(chartPanel, DrawerUtils.editAreaHeight), //
				new Pair<>(chartMap, DrawerUtils.chartMapHeight), //
				new Pair<>(tabs,
						height - chartToolbar.getHeight() - DrawerUtils.editAreaHeight - DrawerUtils.chartMapHeight));

		int y = 0;
		for (final Pair<Component, Integer> componentHeight : componentHeights) {
			setComponentBoundsWithValidateRepaint(componentHeight.a, 0, y, width, componentHeight.b);
			y += componentHeight.b;
		}
	}

	public CharterFrame(final String title, final String path) {
		this(title);

		songFileHandler.open(path);
	}

	public void switchWindowedPreview() {
		windowedPreviewHandler.switchWindowedPreview();
	}

	public void switchBorderlessWindowedPreview() {
		windowedPreviewHandler.switchBorderlessWindowedPreview();
	}

	private void frame(final double frameTime) {
		try {

			titleUpdater.updateTitle();
			repeatManager.frame();
			chartTimeHandler.frame(frameTime);

			windowedPreviewHandler.paintFrame();
			if (preview3DPanel.isShowing()) {
				preview3DPanel.repaint();
			}
			repaint();
		} catch (final Exception e) {
			Logger.error("Exception in frame()", e);
		}
	}

	private JTextArea createTextArea() {
		final JTextArea textArea = new JTextArea(1000, 1000);
		textArea.setBackground(ColorLabel.BASE_BG_2.color());
		textArea.setForeground(ColorLabel.BASE_TEXT.color());
		textArea.setCaretColor(ColorLabel.BASE_TEXT.color());

		return textArea;
	}

	public void updateEditAreaSizes() {
		final EditMode editMode = modeManager.getMode();
		final Arrangement arrangement = data.getCurrentArrangement();
		final boolean bass = arrangement.isBass();
		final int strings = arrangement.tuning.strings();

		DrawerUtils.updateEditAreaSizes(editMode, bass, strings);
		resize();
	}

	public boolean askToSaveChanged() {
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

	public void exit() {
		audioHandler.stopMusic();

		final boolean restorePreviewWindow = windowedPreviewHandler.temporaryDispose();

		final ConfirmAnswer areYouSure = askYesNo(this, Label.EXIT_POPUP, Label.EXIT_MESSAGE);

		if (areYouSure != ConfirmAnswer.YES) {
			if (restorePreviewWindow) {
				windowedPreviewHandler.restore();
			}

			return;
		}
		if (!askToSaveChanged()) {
			return;
		}

		audioFramer.stop();
		framer.stop();
		StretchedFileLoader.stopAllProcesses();
		dispose();
		System.exit(0);
	}

	@Override
	public void paint(final Graphics g) {
		super.paint(g);
		helpTab.addFrameTime();
	}

	public void reloadTextures() {
		preview3DPanel.reloadTextures();
		windowedPreviewHandler.reloadTextures();
	}
}
