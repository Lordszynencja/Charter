package log.charter.gui.components.utils;

import log.charter.CharterMain;
import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.Arrangement;
import log.charter.data.song.vocals.VocalPath;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.services.editModes.ModeManager;

public class TitleUpdater {
	private ChartData chartData;
	private CharterFrame charterFrame;
	private ModeManager modeManager;
	private UndoSystem undoSystem;

	private String getSongData() {
		return chartData.songChart.artistName() + " - " + chartData.songChart.title();
	}

	private String getVocalPathTitlePart() {
		final VocalPath vocalPath = chartData.currentVocals();
		return vocalPath.getName(chartData.currentVocals);
	}

	private String getArrangementTitlePart() {
		final Arrangement arrangement = chartData.currentArrangement();
		final String arrangementName = arrangement.getTypeNameLabel(chartData.currentArrangement);
		final String tuning = arrangement.getTuningName("%s - %s");

		return "%s (%s)".formatted(arrangementName, tuning);
	}

	private String modeInfo() {
		return switch (modeManager.getMode()) {
			case TEMPO_MAP -> getSongData() + " : Tempo map";
			case VOCALS -> getSongData() + " : " + getVocalPathTitlePart();
			case GUITAR -> getSongData() + " : " + getArrangementTitlePart();
			case EMPTY -> Label.NO_PROJECT.label();
			default -> "Surprise mode! (contact dev for fix)";
		};
	}

	private String addUnsavedStatus(final String title) {
		if (undoSystem.isSaved()) {
			return title;
		}

		return title + "*";
	}

	public void updateTitle() {
		String title = "%s : %s".formatted(CharterMain.TITLE, modeInfo());
		title = addUnsavedStatus(title);

		if (title.equals(charterFrame.getTitle())) {
			return;
		}

		charterFrame.setTitle(title);
		charterFrame.validate();
	}

}
