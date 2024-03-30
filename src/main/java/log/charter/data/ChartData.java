package log.charter.data;

import static log.charter.data.config.Config.maxStrings;

import java.io.File;
import java.util.List;

import log.charter.data.config.Config;
import log.charter.data.song.Anchor;
import log.charter.data.song.Arrangement;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.EventPoint;
import log.charter.data.song.HandShape;
import log.charter.data.song.Level;
import log.charter.data.song.SongChart;
import log.charter.data.song.ToneChange;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.vocals.Vocals;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.menuHandlers.CharterMenuBar;
import log.charter.services.data.selection.SelectionManager;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;

public class ChartData {
	public String path = Config.lastDir;
	public String projectFileName = "project.rscp";
	public boolean isEmpty = true;
	public SongChart songChart = new SongChart();

	public int currentArrangement = 0;
	public int currentLevel = 0;

	private CharterFrame charterFrame;
	private CharterMenuBar charterMenuBar;
	private ModeManager modeManager;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	public void setNewSong(final File songFolder, final SongChart song, final String projectFileName) {
		setSong(songFolder.getAbsolutePath(), song, projectFileName, EditMode.TEMPO_MAP, 0, 0);
	}

	public void setSong(final String dir, final SongChart song, final String projectFileName, final EditMode editMode,
			final int arrangement, final int level) {
		currentArrangement = arrangement;
		isEmpty = false;

		songChart = song;

		selectionManager.clear();
		currentLevel = level;
		modeManager.setMode(editMode);

		charterMenuBar.refreshMenus();
		charterFrame.updateSizes();

		path = dir;
		this.projectFileName = projectFileName;
		Config.lastDir = path;
		Config.lastPath = new File(path, projectFileName).getAbsolutePath();
		Config.markChanged();

		selectionManager.clear();
		undoSystem.clear();
	}

	public ImmutableBeatsMap beats() {
		return songChart.beatsMap.immutable;
	}

	public int currentStrings() {
		if (modeManager.getMode() != EditMode.GUITAR) {
			return maxStrings;
		}

		return currentArrangement().tuning.strings();
	}

	public Vocals currentVocals() {
		return songChart.vocals;
	}

	public Arrangement currentArrangement() {
		if (songChart == null || currentArrangement < 0 || currentArrangement >= songChart.arrangements.size()) {
			return new Arrangement();
		}

		return songChart.arrangements.get(currentArrangement);
	}

	public String getCurrentArrangementName() {
		return currentArrangement().getTypeNameLabel(currentArrangement);
	}

	public List<EventPoint> currentEventPoints() {
		return currentArrangement().eventPoints;
	}

	public List<ToneChange> currentToneChanges() {
		return currentArrangement().toneChanges;
	}

	public List<ChordTemplate> currentChordTemplates() {
		return currentArrangement().chordTemplates;
	}

	public Level currentArrangementLevel() {
		return currentArrangement().getLevel(currentLevel);
	}

	public List<Anchor> currentAnchors() {
		return currentArrangementLevel().anchors;
	}

	public List<ChordOrNote> currentSounds() {
		return currentArrangementLevel().sounds;
	}

	public List<HandShape> currentHandShapes() {
		return currentArrangementLevel().handShapes;
	}

}
