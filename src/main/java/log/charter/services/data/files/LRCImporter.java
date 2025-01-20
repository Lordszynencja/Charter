package log.charter.services.data.files;

import java.io.File;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.vocals.Vocal;
import log.charter.data.song.vocals.Vocal.VocalFlag;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.utils.ComponentUtils;
import log.charter.gui.components.utils.ComponentUtils.ConfirmAnswer;
import log.charter.services.data.fixers.ArrangementFixer;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;
import log.charter.util.RW;

public class LRCImporter {
	private class FileImporter {
		private int offset = 0;

		private int parseTime(final String timeString) {
			final String[] parts = timeString.split("[:.]");
			final int minutes = Integer.valueOf(parts[0]);
			final int seconds = Integer.valueOf(parts[1]) + minutes * 60;
			final int centiseconds = Integer.valueOf(parts[2]) + seconds * 100;

			return centiseconds * 10;
		}

		private void addVocal(final int start, final int end, final String text, final boolean phraseEnd) {
			if (offset + start < chartData.beats().get(0).position()) {
				offset = chartData.beats().get(0).position();
			}

			if (text.isBlank()) {
				return;
			}

			final Vocal newVocal = new Vocal(FractionalPosition.fromTime(chartData.beats(), offset + start), //
					FractionalPosition.fromTime(chartData.beats(), offset + end), //
					text.strip(), //
					phraseEnd ? VocalFlag.PHRASE_END : VocalFlag.NONE);

			chartData.currentVocals().vocals.add(newVocal);
		}

		private void addLine(final String line) {
			final String[] parts = line.split("\\]", 2);
			int vocalTime = parseTime(parts[0].substring(1));

			final String[] words = parts[1].strip().split(" ");
			String text = "";

			for (final String word : words) {
				if (word.matches("<[0-9]*:[0-9]*\\.[0-9]*>")) {
					final int nextVocalTime = parseTime(word.replaceAll("[<>]", ""));
					addVocal(vocalTime, nextVocalTime - 1, text, false);
					vocalTime = nextVocalTime;
					text = "";
				} else {
					text += " " + word;
				}
			}

			if (!text.isBlank()) {
				addVocal(vocalTime, vocalTime + 3000, text, true);
			}
		}

		public void importLRCFile(final String data) {
			for (final String line : data.split("\r\n|\r|\n")) {
				if (line.matches("\\[[a-zA-Z#]:.*\\]") || line.isBlank()) {
					continue;
				}

				if (line.matches("\\[[0-9]*:[0-9]*\\.[0-9]*\\].*")) {
					addLine(line);
					continue;
				}
			}
		}
	}

	private ArrangementFixer arrangementFixer;
	private ChartData chartData;
	private CharterFrame charterFrame;
	private ModeManager modeManager;
	private UndoSystem undoSystem;

	public void importLRCFile(final File file) {
		if (!chartData.currentVocals().vocals.isEmpty()) {
			final ConfirmAnswer answer = ComponentUtils.askYesNo(charterFrame, Label.VOCALS_EXIST,
					Label.VOCALS_EXIST_REPLACE_QUESTION);

			if (answer != ConfirmAnswer.YES) {
				return;
			}
		}

		modeManager.setMode(EditMode.VOCALS);
		undoSystem.addUndo();
		chartData.currentVocals().vocals.clear();

		new FileImporter().importLRCFile(RW.read(file));
		arrangementFixer.fixLengths(chartData.songChart.vocals.vocals);

		ComponentUtils.showPopup(charterFrame, Label.LRC_IMPORTED_SUCCESSFULLY);
	}
}
