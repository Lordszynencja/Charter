package log.charter.gui;

import static java.util.Arrays.asList;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.dnd.DropTarget;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.swing.JFrame;

import log.charter.CharterMain;
import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.Arrangement;
import log.charter.gui.chartPanelDrawers.common.DrawerUtils;
import log.charter.gui.components.containers.CharterScrollPane;
import log.charter.gui.components.containers.CharterTabbedPane;
import log.charter.gui.components.containers.CharterTabbedPane.Tab;
import log.charter.gui.components.preview3D.Preview3DPanel;
import log.charter.gui.components.simple.ChartMap;
import log.charter.gui.components.tabs.HelpTab;
import log.charter.gui.components.tabs.TextTab;
import log.charter.gui.components.tabs.selectionEditor.CurrentSelectionEditor;
import log.charter.gui.components.toolbar.ChartToolbar;
import log.charter.gui.components.utils.ComponentUtils;
import log.charter.gui.lookAndFeel.CharterTheme;
import log.charter.gui.menuHandlers.CharterMenuBar;
import log.charter.io.Logger;
import log.charter.services.CharterContext;
import log.charter.services.CharterContext.Initiable;
import log.charter.services.CharterFrameComponentListener;
import log.charter.services.CharterFrameWindowFocusListener;
import log.charter.services.CharterFrameWindowListener;
import log.charter.services.data.files.FileDropHandler;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;
import log.charter.services.mouseAndKeyboard.KeyboardHandler;
import log.charter.util.collections.Pair;
import net.sf.image4j.codec.ico.ICODecoder;

public class CharterFrame extends JFrame implements Initiable {
	private static final long serialVersionUID = 3603305480386377813L;

	private ChartData chartData;
	private CharterContext charterContext;
	private CurrentSelectionEditor currentSelectionEditor;
	private FileDropHandler fileDropHandler;
	private HelpTab helpTab;
	private KeyboardHandler keyboardHandler;
	private ModeManager modeManager;
	private TextTab textTab;

	private final Preview3DPanel preview3DPanel = new Preview3DPanel();

	private CharterMenuBar charterMenuBar;
	private ChartToolbar chartToolbar;
	private ChartPanel chartPanel;
	private ChartMap chartMap;
	private CharterTabbedPane tabs;

	private boolean paintWaiting = false;

	public CharterFrame() {
		super(CharterMain.TITLE + " : " + Label.NO_PROJECT.label());
		try {
			final InputStream stream = this.getClass().getResourceAsStream("/icon.ico");
			setIconImages(ICODecoder.read(stream));
		} catch (final IOException e) {
			Logger.error("Couldn't load icon", e);
		}

		CharterTheme.install(this);

		setLayout(null);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	}

	@Override
	public void init() {
		charterContext.initObject(preview3DPanel);

		setSize(Config.windowWidth, Config.windowHeight);
		setLocation(Config.windowPosX, Config.windowPosY);
		setExtendedState(Config.windowExtendedState);

		tabs = new CharterTabbedPane(//
				new Tab("Quick Edit", new CharterScrollPane(currentSelectionEditor)), //
				new Tab("Help", helpTab), //
				new Tab("Text", textTab), //
				new Tab("3D Preview", preview3DPanel));

		add(chartToolbar);
		add(chartPanel);
		add(chartMap);
		add(tabs);

		addComponentListener(new CharterFrameComponentListener(this));
		addKeyListener(keyboardHandler);
		addWindowFocusListener(new CharterFrameWindowFocusListener(keyboardHandler));
		addWindowListener(new CharterFrameWindowListener(charterContext));
		setDropTarget(new DropTarget(this, fileDropHandler));
	}

	public void finishInitAndShow() {
		resizeComponents();

		validate();
		setVisible(true);
		setFocusable(true);
	}

	public void resize() {
		Config.windowHeight = getHeight();
		Config.windowWidth = getWidth();
		Config.windowExtendedState = getExtendedState();
		Config.markChanged();

		resizeComponents();
	}

	private void resizeComponents() {
		final Insets insets = getInsets();
		final int width = Config.windowWidth - insets.left - insets.right;
		final int height = Config.windowHeight - insets.top - insets.bottom - charterMenuBar.getHeight();

		final List<Pair<Component, Integer>> componentHeights = asList(//
				new Pair<>(chartToolbar, chartToolbar.getHeight()), //
				new Pair<>(chartPanel, DrawerUtils.editAreaHeight), //
				new Pair<>(chartMap, DrawerUtils.chartMapHeight), //
				new Pair<>(tabs,
						height - chartToolbar.getHeight() - DrawerUtils.editAreaHeight - DrawerUtils.chartMapHeight));

		int y = 0;
		for (final Pair<Component, Integer> componentHeight : componentHeights) {
			ComponentUtils.setComponentBoundsWithValidateRepaint(componentHeight.a, 0, y, width, componentHeight.b);
			y += componentHeight.b;
		}
	}

	public void updateSizes() {
		final EditMode editMode = modeManager.getMode();
		final Arrangement arrangement = chartData.currentArrangement();
		final boolean bass = arrangement.isBass();
		final int strings = arrangement.tuning.strings();

		DrawerUtils.updateEditAreaSizes(editMode, bass, strings);
		resize();
	}

	public void reloadTextures() {
		preview3DPanel.reloadTextures();
	}

	@Override
	public void repaint() {
		if (paintWaiting) {
			return;
		}

		paintWaiting = true;
		super.repaint();

		if (preview3DPanel.isShowing()) {
			preview3DPanel.repaint();
		}
	}

	@Override
	public void paint(final Graphics g) {
		super.paint(g);
		helpTab.addFrameTime();

		paintWaiting = false;
	}
}
