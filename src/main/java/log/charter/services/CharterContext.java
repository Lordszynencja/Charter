package log.charter.services;

import static log.charter.gui.components.utils.ComponentUtils.askYesNo;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.config.GraphicalConfig;
import log.charter.data.config.Localization.Label;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.ChartPanel;
import log.charter.gui.CharterFrame;
import log.charter.gui.chartPanelDrawers.common.BeatsDrawer;
import log.charter.gui.chartPanelDrawers.common.waveform.WaveFormDrawer;
import log.charter.gui.components.simple.ChartMap;
import log.charter.gui.components.tabs.HelpTab;
import log.charter.gui.components.tabs.TextTab;
import log.charter.gui.components.tabs.selectionEditor.CurrentSelectionEditor;
import log.charter.gui.components.toolbar.ChartToolbar;
import log.charter.gui.components.utils.ComponentUtils.ConfirmAnswer;
import log.charter.gui.components.utils.TitleUpdater;
import log.charter.gui.menuHandlers.CharterMenuBar;
import log.charter.io.Logger;
import log.charter.services.audio.AudioHandler;
import log.charter.services.audio.ClapsHandler;
import log.charter.services.audio.MetronomeHandler;
import log.charter.services.audio.MidiChartNotePlayer;
import log.charter.services.data.ChartItemsHandler;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.data.GuitarSoundsHandler;
import log.charter.services.data.GuitarSoundsStatusesHandler;
import log.charter.services.data.HandShapesHandler;
import log.charter.services.data.ProjectAudioHandler;
import log.charter.services.data.VocalsHandler;
import log.charter.services.data.copy.CopyManager;
import log.charter.services.data.files.ExistingProjectImporter;
import log.charter.services.data.files.FileDropHandler;
import log.charter.services.data.files.GP5FileImporter;
import log.charter.services.data.files.MidiImporter;
import log.charter.services.data.files.NewProjectCreator;
import log.charter.services.data.files.RSXMLImporter;
import log.charter.services.data.files.SongFileHandler;
import log.charter.services.data.files.SongFilesBackuper;
import log.charter.services.data.fixers.ArrangementFixer;
import log.charter.services.data.selection.SelectionManager;
import log.charter.services.data.validation.ArrangementValidator;
import log.charter.services.editModes.ModeManager;
import log.charter.services.mouseAndKeyboard.HighlightManager;
import log.charter.services.mouseAndKeyboard.KeyboardHandler;
import log.charter.services.mouseAndKeyboard.MouseButtonPressReleaseHandler;
import log.charter.services.mouseAndKeyboard.MouseHandler;
import log.charter.services.mouseAndKeyboard.ShortcutConfig;
import log.charter.services.utils.AudioFramer;
import log.charter.services.utils.Framer;
import log.charter.sound.StretchedFileLoader;

@SuppressWarnings("unused")
public class CharterContext {
	public interface Initiable {
		void init();
	}

	private final ActionHandler actionHandler = new ActionHandler();
	private final ArrangementFixer arrangementFixer = new ArrangementFixer();
	private final ArrangementValidator arrangementValidator = new ArrangementValidator();
	private final AudioHandler audioHandler = new AudioHandler();
	private final BeatsDrawer beatsDrawer = new BeatsDrawer();
	private final ChartData chartData = new ChartData();
	private final ChartItemsHandler chartItemsHandler = new ChartItemsHandler();
	private final ChartTimeHandler chartTimeHandler = new ChartTimeHandler();
	private final ClapsHandler clapsHandler = new ClapsHandler();
	private final CopyManager copyManager = new CopyManager();
	private final ExistingProjectImporter existingProjectImporter = new ExistingProjectImporter();
	private final FileDropHandler fileDropHandler = new FileDropHandler();
	private final GP5FileImporter gp5FileImporter = new GP5FileImporter();
	private final GuitarSoundsHandler guitarSoundsHandler = new GuitarSoundsHandler();
	private final GuitarSoundsStatusesHandler guitarSoundsStatusesHandler = new GuitarSoundsStatusesHandler();
	private final HandShapesHandler handShapesHandler = new HandShapesHandler();
	private final HighlightManager highlightManager = new HighlightManager();
	private final KeyboardHandler keyboardHandler = new KeyboardHandler();
	private final MetronomeHandler metronomeHandler = new MetronomeHandler();
	private final MidiChartNotePlayer midiChartNotePlayer = new MidiChartNotePlayer();
	private final MidiImporter midiImporter = new MidiImporter();
	private final ModeManager modeManager = new ModeManager();
	private final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler = new MouseButtonPressReleaseHandler();
	private final MouseHandler mouseHandler = new MouseHandler();
	private final NewProjectCreator newProjectCreator = new NewProjectCreator();
	private final ProjectAudioHandler projectAudioHandler = new ProjectAudioHandler();
	private final RepeatManager repeatManager = new RepeatManager();
	private final RSXMLImporter rsXMLImporter = new RSXMLImporter();
	private final SongFileHandler songFileHandler = new SongFileHandler();
	private final SongFilesBackuper songFilesBackuper = new SongFilesBackuper();
	private final SelectionManager selectionManager = new SelectionManager();
	private final TitleUpdater titleUpdater = new TitleUpdater();
	private final UndoSystem undoSystem = new UndoSystem();
	private final VocalsHandler vocalsHandler = new VocalsHandler();
	private final WaveFormDrawer waveFormDrawer = new WaveFormDrawer();
	private final WindowedPreviewHandler windowedPreviewHandler = new WindowedPreviewHandler();

	private final CharterFrame charterFrame = new CharterFrame();
	private final CharterMenuBar charterMenuBar = new CharterMenuBar();
	private final ChartToolbar chartToolbar = new ChartToolbar();
	private final ChartPanel chartPanel = new ChartPanel();
	private final ChartMap chartMap = new ChartMap();
	private final CurrentSelectionEditor currentSelectionEditor = new CurrentSelectionEditor();
	private final HelpTab helpTab = new HelpTab();
	private final TextTab textTab = new TextTab();

	private final AudioFramer audioFramer = new AudioFramer();
	private final Framer framer = new Framer(this::frame);

	final Map<String, Object> fields = getFieldsValues();

	private Map<String, Object> getFieldsValues() {
		final Map<String, Object> fields = new HashMap<>();
		fields.put("charterContext", this);

		for (final Field field : this.getClass().getDeclaredFields()) {
			try {
				fields.put(field.getName(), field.get(this));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				Logger.error("Couldn't get " + field.getName(), e);
				System.exit(0);
			}
		}

		return fields;
	}

	private void fillFieldsforObject(final Object o) {
		for (final Field field : o.getClass().getDeclaredFields()) {
			final Object fieldValue = fields.get(field.getName());
			if (fieldValue != null && fieldValue.getClass().equals(field.getType())) {
				try {
					if (!field.canAccess(o)) {
						field.setAccessible(true);
						field.set(o, fieldValue);
						field.setAccessible(false);
					} else {
						field.set(o, fieldValue);
					}
					Logger.debug("set field " + field.getName() + " of object " + o.getClass());
				} catch (IllegalArgumentException | IllegalAccessException e) {
					Logger.error("Couldn't set field %s of type %s".formatted(field.getName(), field.getType()), e);
					System.exit(0);
				}
			}
		}

	}

	public void initObject(final Object o) {
		try {
			fillFieldsforObject(o);

			if (Initiable.class.isAssignableFrom(o.getClass())) {
				final Initiable initiable = (Initiable) o;
				initiable.init();
				Logger.debug("initiated " + o.getClass().getSimpleName());
			}
		} catch (final IllegalArgumentException e) {
			Logger.error("Couldn't initiate " + o.getClass().getSimpleName(), e);
			System.exit(0);
		}
	}

	public void init() {
		for (final Field field : this.getClass().getDeclaredFields()) {
			try {
				initObject(field.get(this));
			} catch (final IllegalAccessException e) {
				Logger.error("Couldn't initiate " + field.getName(), e);
				System.exit(0);
			}
		}

		audioFramer.start();
		framer.start();

		new Thread(() -> {
			try {
				while (true) {
					Config.save();
					GraphicalConfig.save();
					ShortcutConfig.save();

					Thread.sleep(1000);
				}
			} catch (final InterruptedException e) {
				Logger.error("Error in config save thread", e);
			}
		}).start();

		charterFrame.finishInitAndShow();
	}

	public void openProject(final String path) {
		existingProjectImporter.open(path);
	}

	private void frame(final double frameTime) {
		try {
			titleUpdater.updateTitle();
			repeatManager.frame();
			chartTimeHandler.frame(frameTime);

			windowedPreviewHandler.paintFrame();
			charterFrame.repaint();
		} catch (final Exception e) {
			Logger.error("Exception in frame()", e);
		}
	}

	public void reloadTextures() {
		charterFrame.reloadTextures();
		windowedPreviewHandler.reloadTextures();
	}

	public void exit() {
		audioHandler.stopMusic();

		final boolean restorePreviewWindow = windowedPreviewHandler.temporaryDispose();

		final ConfirmAnswer areYouSure = askYesNo(charterFrame, Label.EXIT_POPUP, Label.EXIT_MESSAGE);

		if (areYouSure != ConfirmAnswer.YES) {
			if (restorePreviewWindow) {
				windowedPreviewHandler.restore();
			}

			return;
		}
		if (!songFileHandler.askToSaveChanged()) {
			return;
		}

		audioFramer.stop();
		framer.stop();
		StretchedFileLoader.clear();
		charterFrame.dispose();
		System.exit(0);
	}
}
