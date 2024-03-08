package log.charter.gui.components.utils;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.managers.ModeManager;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.main.CharterMain;
import log.charter.song.Arrangement;

public class TitleUpdater {
	private ChartData chartData;
	private CharterFrame charterFrame;
	private ModeManager modeManager;
	private UndoSystem undoSystem;

	private String getSongData() {
		return chartData.songChart.artistName() + " - " + chartData.songChart.title();
	}

	private String getArrangementTitlePart() {
		final int number = chartData.currentArrangement + 1;
		final Arrangement arrangement = chartData.getCurrentArrangement();
		final String arrangementTypeName = arrangement.getTypeNameLabel();
		final String tuning = arrangement.getTuningName("%s - %s");

		return "[%d] %s (%s)".formatted(number, arrangementTypeName, tuning);
	}

	private String modeInfo() {
		return switch (modeManager.getMode()) {
			case GUITAR -> getSongData() + " : " + getArrangementTitlePart();
			case TEMPO_MAP -> getSongData() + " : Tempo map";
			case VOCALS -> getSongData() + " : Vocals";
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
	}
}
