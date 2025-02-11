package log.charter.services.data.files;

import static java.lang.Math.max;

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

public class USCTxtImporter {
	public static boolean isUSCFile(final String data) {
		for (final String line : data.split("\r\n|\r|\n")) {
			if (line.startsWith("#")//
					|| line.matches("- [0-9]*")//
					|| line.matches("[:\\\\*FRG] [0-9]* [0-9]* -?[0-9]* .*")//
					|| line.matches("P(1|2)")//
					|| line.matches("E")//
					|| line.isBlank()) {
				continue;
			}

			return false;
		}

		return true;
	}

	private class FileImporter {
		private double bpm = 120;
		private double startPosition = chartData.beats().get(0).position();
		private Vocal lastVocal = null;
		private int lastBeatPosition = -1;

		private void readBpm(final String line) {
			bpm = Double.valueOf(line.substring(5));
		}

		private void readGap(final String line) {
			startPosition = max(startPosition, Integer.valueOf(line.substring(5)));
		}

		private double beatToTime(final int beat) {
			return beat * 15_000 / bpm;
		}

		private void addNextSyllable(final String line) {
			final String[] tokens = line.split(" ", 5);
			final int beatId = Integer.valueOf(tokens[1]);
			if (lastBeatPosition >= beatId) {
				return;
			}

			lastBeatPosition = beatId;
			final int length = Integer.valueOf(tokens[2]);
			String text = tokens[4];
			if (text.startsWith("-") && lastVocal != null) {
				lastVocal.flag(VocalFlag.WORD_PART);
				text = text.substring(1);
			}

			final int startTime = (int) (startPosition + beatToTime(beatId));
			final int endTime = (int) (startTime + beatToTime(length));
			final Vocal newVocal = new Vocal();
			newVocal.position(FractionalPosition.fromTime(chartData.beats(), startTime));
			newVocal.endPosition(FractionalPosition.fromTime(chartData.beats(), endTime));
			newVocal.text(text);

			chartData.currentVocals().vocals.add(newVocal);
			lastVocal = newVocal;
		}

		private void endLine() {
			if (lastVocal != null) {
				lastVocal.flag(VocalFlag.PHRASE_END);
			}

			lastVocal = null;
		}

		public void importUSCFile(final String data) {
			for (final String line : data.split("\r\n|\r|\n")) {
				if (line.startsWith("#")) {
					if (line.matches("#BPM:[0-9\\.]*")) {
						readBpm(line);
					}
					if (line.matches("#GAP:[0-9]*")) {
						readGap(line);
					}

					continue;
				}
				if (line.matches("P(1|2)")) {
					continue;
				}
				if (line.matches("[:\\\\*FRG] [0-9]* [0-9]* [0-9]* .*")) {
					addNextSyllable(line);
					continue;
				}
				if (line.matches("- [0-9]*")) {
					endLine();
					continue;
				}
				if (line.equals("E")) {
					endLine();
					return;
				}
			}
		}
	}

	private ArrangementFixer arrangementFixer;
	private ChartData chartData;
	private CharterFrame charterFrame;
	private ModeManager modeManager;
	private UndoSystem undoSystem;

	public void importUSCFile(final File file) {
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

		new FileImporter().importUSCFile(RW.read(file));
		arrangementFixer.fixLengths(chartData.songChart.vocals.vocals);

		ComponentUtils.showPopup(charterFrame, Label.USC_IMPORTED_SUCCESSFULLY);
	}
}
