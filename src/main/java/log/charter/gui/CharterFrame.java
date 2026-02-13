package log.charter.gui;

import static java.util.Arrays.asList;
import static log.charter.data.config.SystemType.MAC;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.dnd.DropTarget;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.JFrame;

import log.charter.CharterMain;
import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.data.config.SystemType;
import log.charter.data.config.values.WindowStateConfig;
import log.charter.data.song.Arrangement;
import log.charter.gui.chartPanelDrawers.common.DrawerUtils;
import log.charter.gui.components.containers.CharterScrollPane;
import log.charter.gui.components.containers.CharterTabbedPane;
import log.charter.gui.components.containers.CharterTabbedPane.Tab;
import log.charter.gui.components.preview3D.Preview3DPanel;
import log.charter.gui.components.simple.ChartMap;
import log.charter.gui.components.tabs.HelpTab;
import log.charter.gui.components.tabs.TextTab;
import log.charter.gui.components.tabs.chordEditor.ChordTemplatesEditorTab;
import log.charter.gui.components.tabs.errorsTab.ErrorsTab;
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
	public enum TabType {
		QUICK_EDIT, CHORD_TEMPLATES, ERRORS, PREVIEW_3D, TEXT, HELP
	}

	private static final long serialVersionUID = 3603305480386377813L;

	private ChartData chartData;
	private CharterContext charterContext;
	private ChordTemplatesEditorTab chordTemplatesEditorTab;
	private CurrentSelectionEditor currentSelectionEditor;
	private ErrorsTab errorsTab;
	private FileDropHandler fileDropHandler;
	private HelpTab helpTab;
	private KeyboardHandler keyboardHandler;
	private ModeManager modeManager;
	private TextTab textTab;

	private final Preview3DPanel preview3DPanel = SystemType.not(MAC) ? new Preview3DPanel() : null;

	private CharterMenuBar charterMenuBar;
	private ChartToolbar chartToolbar;
	private ChartPanel chartPanel;
	private ChartMap chartMap;
	private CharterTabbedPane tabs;

	private final Map<TabType, Integer> tabPositions = new HashMap<>();

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

	private List<TabType> getTabsOrder() {
		if (SystemType.is(MAC)) {
			return asList(TabType.QUICK_EDIT, TabType.CHORD_TEMPLATES, TabType.ERRORS, TabType.TEXT, TabType.HELP);
		} else {
			return asList(TabType.QUICK_EDIT, TabType.CHORD_TEMPLATES, TabType.ERRORS, TabType.PREVIEW_3D, TabType.TEXT,
					TabType.HELP);
		}
	}

	private Function<TabType, Tab> makeTabGenerator(final Tab errorsParentTab) {
		return type -> switch (type) {
			case CHORD_TEMPLATES ->
				new Tab(Label.TAB_CHORD_TEMPLATES_EDITOR, new CharterScrollPane(chordTemplatesEditorTab));
			case ERRORS -> errorsParentTab;
			case HELP -> new Tab(Label.TAB_HELP, helpTab);
			case PREVIEW_3D -> new Tab(Label.TAB_3D_PREVIEW, preview3DPanel);
			case QUICK_EDIT -> new Tab(Label.TAB_QUICK_EDIT, new CharterScrollPane(currentSelectionEditor));
			case TEXT -> new Tab(Label.TAB_TEXT, textTab);
			default -> throw new IllegalArgumentException("Unknown tab to make: " + type.name());
		};
	}

	@Override
	public void init() {
		if (SystemType.not(MAC)) {
			charterContext.initObject(preview3DPanel);
		}

		preview3DPanel.setPanelName("Tab preview");

		setSize(WindowStateConfig.width, WindowStateConfig.height);
		setLocation(WindowStateConfig.x, WindowStateConfig.y);
		setExtendedState(WindowStateConfig.extendedState);

		final Tab errorsParentTab = new Tab(Label.TAB_ERRORS, errorsTab);

		final List<TabType> tabsOrder = getTabsOrder();
		tabs = new CharterTabbedPane(
				tabsOrder.stream().map(makeTabGenerator(errorsParentTab)).collect(Collectors.toList()));
		for (int i = 0; i < tabsOrder.size(); i++) {
			tabPositions.put(tabsOrder.get(i), i);
		}
		errorsTab.setTab(errorsParentTab);

		add(chartToolbar);
		add(chartPanel);
		add(chartMap);
		add(tabs);

		addComponentListener(new CharterFrameComponentListener(this));
		addKeyListener(keyboardHandler);
		addWindowFocusListener(new CharterFrameWindowFocusListener(keyboardHandler));
		addWindowListener(new CharterFrameWindowListener(charterContext));
		setDropTarget(new DropTarget(this, fileDropHandler));

		setFocusTraversalKeysEnabled(false);
	}

	public void finishInitAndShow() {
		resizeComponents();

		validate();
		setVisible(true);
		setFocusable(true);
	}

	private void resizeComponents() {
		final Insets insets = getInsets();
		final int width = WindowStateConfig.width - insets.left - insets.right;
		final int height = WindowStateConfig.height - insets.top - insets.bottom - charterMenuBar.getHeight();

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

	public void resize() {
		WindowStateConfig.height = getHeight();
		WindowStateConfig.width = getWidth();
		WindowStateConfig.extendedState = getExtendedState();
		Config.markChanged();

		resizeComponents();
		validate();
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
		if (SystemType.is(MAC)) {
			return;
		}

		preview3DPanel.reloadTextures();
	}

	public void setTab(final TabType tab) {
		final Integer position = tabPositions.get(tab);
		if (position == null) {
			return;
		}

		tabs.setSelectedIndex(position);
	}

	@Override
	public void repaint() {
		if (preview3DPanel != null && preview3DPanel.isShowing()) {
			if (preview3DPanel.painted) {
				super.repaint();
			}
			preview3DPanel.repaint();
		} else {
			super.repaint();
		}
	}

	@Override
	public void paint(final Graphics g) {
		try {
			super.paint(g);
		} catch (final Exception e) {
			Logger.error("Error in CharterFrame.paint", e);
		}
	}
}
