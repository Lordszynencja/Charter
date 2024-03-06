package log.charter.gui.menuHandlers;

import java.awt.Dimension;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

import log.charter.data.ArrangementFixer;
import log.charter.data.ChartData;
import log.charter.data.copySystem.CopyManager;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.CharterFrame;
import log.charter.gui.chartPanelDrawers.common.waveform.WaveFormDrawer;
import log.charter.gui.components.toolbar.ChartToolbar;
import log.charter.gui.handlers.ActionHandler;
import log.charter.gui.handlers.AudioHandler;
import log.charter.gui.handlers.SongFileHandler;
import log.charter.gui.handlers.data.ChartTimeHandler;
import log.charter.gui.handlers.data.ProjectAudioHandler;
import log.charter.gui.utils.Framer;
import log.charter.util.CollectionUtils.ArrayList2;

public class CharterMenuBar extends JMenuBar {
	private static final long serialVersionUID = -5784270027920161709L;

	public static final ColorLabel backgroundColor = ColorLabel.BASE_BG_2;

	private final ArrangementMenuHandler arrangementMenuHandler = new ArrangementMenuHandler();
	private final EditMenuHandler editMenuHandler = new EditMenuHandler();
	private final FileMenuHandler fileMenuHandler = new FileMenuHandler();
	private final GuitarMenuHandler guitarMenuHandler = new GuitarMenuHandler();
	private final InfoMenuHandler infoMenuHandler = new InfoMenuHandler();
	private final MusicMenuHandler musicMenuHandler = new MusicMenuHandler();
	private final NotesMenuHandler notesMenuHandler = new NotesMenuHandler();
	private final VocalsMenuHandler vocalsMenuHandler = new VocalsMenuHandler();

	private final ArrayList2<CharterMenuHandler> menus = new ArrayList2<>(//
			fileMenuHandler, //
			editMenuHandler, //
			musicMenuHandler, //
			arrangementMenuHandler, //
			vocalsMenuHandler, //
			notesMenuHandler, //
			guitarMenuHandler, //
			infoMenuHandler);

	public void init(final ActionHandler actionHandler, final ArrangementFixer arrangementFixer,
			final AudioHandler audioHandler, final ChartTimeHandler chartTimeHandler, final ChartToolbar chartToolbar,
			final CopyManager copyManager, final ChartData data, final CharterFrame frame, final Framer framer,
			final ModeManager modeManager, final ProjectAudioHandler projectAudioHandler,
			final SelectionManager selectionManager, final SongFileHandler songFileHandler, final UndoSystem undoSystem,
			final WaveFormDrawer waveFormDrawer) {
		arrangementMenuHandler.init(actionHandler, data, frame, this, modeManager, selectionManager);
		editMenuHandler.init(actionHandler, chartTimeHandler, data, frame, modeManager, projectAudioHandler,
				undoSystem);
		fileMenuHandler.init(actionHandler, arrangementFixer, chartTimeHandler, data, frame, framer, this, modeManager,
				songFileHandler);
		guitarMenuHandler.init(actionHandler, data, modeManager, selectionManager, undoSystem);
		infoMenuHandler.init(frame, this);
		musicMenuHandler.init(actionHandler, modeManager);
		notesMenuHandler.init(actionHandler, modeManager);
		vocalsMenuHandler.init(actionHandler, modeManager);

		final Dimension size = new Dimension(100, 20);
		setMinimumSize(size);
		setSize(size);
		setMaximumSize(size);
		setPreferredSize(size);

		setBackground(backgroundColor.color());

		refreshMenus();

		frame.setJMenuBar(this);
	}

	public void refreshMenus() {
		final List<JMenu> menusToAdd = menus.stream()//
				.filter(menu -> menu.isApplicable())//
				.map(menu -> menu.prepareMenu())//
				.collect(Collectors.toList());

		removeAll();
		menusToAdd.forEach(this::add);
		validate();
	}
}
