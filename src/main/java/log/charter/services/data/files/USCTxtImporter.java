package log.charter.services.data.files;

import static java.lang.Math.max;

import java.io.File;
import java.util.function.Consumer;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.vocals.Vocal;
import log.charter.data.song.vocals.Vocal.VocalFlag;
import log.charter.data.song.vocals.VocalPath;
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
		private Vocal currentVocal = null;
		private int lastBeatPosition = -1;
		private Consumer<String> textAdder;

		private Consumer<String> selectTextAdder(final String[] lines) {
			boolean spacesBeforeWords = false;
			boolean spacesAfterWords = false;
			boolean joinsBeforeSyllable = false;

			for (final String line : lines) {
				if (!line.matches("[:\\\\*FRG] [0-9]* [0-9]* -?[0-9]* .*")) {
					continue;
				}

				final String text = line.split(" ", 5)[4];
				if (text.startsWith(" ")) {
					spacesBeforeWords = true;
					break;
				}
				if (text.endsWith(" ")) {
					spacesAfterWords = true;
					break;
				}
				if (text.startsWith("-") || text.startsWith("~") || text.startsWith("+") || text.startsWith("=")) {
					joinsBeforeSyllable = true;
					break;
				}
				if (text.endsWith("-") || text.endsWith("~") || text.endsWith("+") || text.endsWith("=")) {
					break;
				}
			}

			if (spacesBeforeWords) {
				return s -> {
					if (s.startsWith(" ")) {
						currentVocal.text(s.substring(1));
					} else {
						if (lastVocal != null) {
							lastVocal.flag(VocalFlag.WORD_PART);
						}
						currentVocal.text(s);
					}
				};
			}
			if (spacesAfterWords) {
				return s -> {
					if (s.endsWith(" ")) {
						currentVocal.text(s.substring(0, s.length() - 1));
					} else {
						currentVocal.flag(VocalFlag.WORD_PART);
						currentVocal.text(s);
					}
				};
			}
			if (joinsBeforeSyllable) {
				return s -> {
					if (s.startsWith("-") || s.startsWith("~") || s.startsWith("+") || s.startsWith("=")) {
						currentVocal.text(s.substring(1));
						if (lastVocal != null) {
							lastVocal.flag(VocalFlag.WORD_PART);
						}
					} else {
						currentVocal.text(s);
					}
				};
			}
			return s -> {
				if (s.endsWith("-") || s.endsWith("~") || s.endsWith("+") || s.endsWith("=")) {
					currentVocal.text(s.substring(0, s.length() - 1));
					currentVocal.flag(VocalFlag.WORD_PART);
				} else {
					currentVocal.text(s);
				}
			};
		}

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

			final int startTime = (int) (startPosition + beatToTime(beatId));
			final int endTime = (int) (startTime + beatToTime(length));
			currentVocal = new Vocal();
			currentVocal.position(FractionalPosition.fromTime(chartData.beats(), startTime));
			currentVocal.endPosition(FractionalPosition.fromTime(chartData.beats(), endTime));

			final String text = tokens[4];
			textAdder.accept(text);

			chartData.currentVocals().vocals.add(currentVocal);

			lastVocal = currentVocal;
		}

		private void endLine() {
			if (lastVocal != null) {
				lastVocal.flag(VocalFlag.PHRASE_END);
			}

			lastVocal = null;
		}

		public void importUSCFile(final String data) {
			final String[] lines = data.split("\r\n|\r|\n");
			textAdder = selectTextAdder(lines);

			for (final String line : lines) {
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
				if (line.matches("[:\\\\*FRG] [0-9]* [0-9]* -?[0-9]* .*")) {
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
		chartData.addVocals(new VocalPath());

		new FileImporter().importUSCFile(RW.read(file));
		arrangementFixer.fixLengths(chartData.currentVocals().vocals);

		ComponentUtils.showPopup(charterFrame, Label.USC_IMPORTED_SUCCESSFULLY);
	}
}
