package log.charter.services.data;

import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPosition;
import log.charter.data.song.position.virtual.IVirtualConstantPosition;
import log.charter.data.song.vocals.Vocal;
import log.charter.data.song.vocals.Vocal.VocalFlag;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.tabs.TextTab;
import log.charter.gui.components.tabs.selectionEditor.CurrentSelectionEditor;
import log.charter.gui.panes.songEdits.VocalPane;
import log.charter.services.data.fixers.ArrangementFixer;
import log.charter.services.data.selection.Selection;
import log.charter.services.data.selection.SelectionManager;

public class VocalsHandler {
	private ArrangementFixer arrangementFixer;
	private ChartData chartData;
	private CharterFrame charterFrame;
	private ChartTimeHandler chartTimeHandler;
	private CurrentSelectionEditor currentSelectionEditor;
	private SelectionManager selectionManager;
	private TextTab textTab;
	private UndoSystem undoSystem;

	public void editVocals() {
		final List<Selection<Vocal>> selected = selectionManager.getSelected(PositionType.VOCAL);
		if (selected.isEmpty()) {
			return;
		}

		final Selection<Vocal> firstSelectedVocal = selected.remove(0);
		new VocalPane(firstSelectedVocal.id, firstSelectedVocal.selectable, chartData, charterFrame, selectionManager,
				undoSystem, selected);
	}

	private void toggle(final VocalFlag flag) {
		final List<Vocal> selected = selectionManager.getSelectedElements(PositionType.VOCAL);
		if (selected.isEmpty()) {
			return;
		}

		undoSystem.addUndo();

		for (final Vocal vocal : selected) {
			vocal.flag(vocal.flag() == flag ? VocalFlag.NONE : flag);
		}

		currentSelectionEditor.selectionChanged(false);
	}

	public void toggleWordPart() {
		toggle(VocalFlag.WORD_PART);
	}

	public void togglePhraseEnd() {
		toggle(VocalFlag.PHRASE_END);
	}

	private class LyricsFromTextPlacer {
		private String remainingText = "";
		private String line = null;
		private String word = "";
		private boolean moreThanOneWord;
		private String syllable = "";
		private boolean moreThanOneSyllable;

		private void getLine() {
			final String text = textTab.getText().strip();
			if (text.isBlank()) {
				return;
			}

			final String[] lines = text.split("\r\n|\r|\n", 2);
			line = lines[0].strip();
			if (lines.length == 2) {
				remainingText = lines[1];
			}
		}

		private void getWord() {
			final String[] words = line.split(" +", 2);
			word = words[0];
			if (words.length == 2) {
				moreThanOneWord = true;
				remainingText = words[1] + "\n" + remainingText;
			}
		}

		private void getSyllable() {
			syllable = "";
			while (word.startsWith("-") || word.startsWith("+")) {
				syllable += word.substring(0, 1);
				word = word.substring(1);
			}

			if (!word.isBlank()) {
				final String[] syllables = word.split("[-+]", 2);
				syllable += syllables[0];
				if (syllables.length == 2) {
					moreThanOneSyllable = true;
					remainingText = syllables[1] + (moreThanOneWord ? " " : "\n") + remainingText;
				}
			}
		}

		private void addVocal() {
			final FractionalPosition currentTime = chartTimeHandler.timeFractional();
			IVirtualConstantPosition vocalPosition = chartData.beats().getPositionFromGridClosestTo(currentTime);
			VocalFlag flag = VocalFlag.NONE;

			if (moreThanOneSyllable) {
				flag = VocalFlag.WORD_PART;
			} else if (!moreThanOneWord) {
				flag = VocalFlag.PHRASE_END;
			}

			for (final Vocal vocal : chartData.currentVocals().vocals) {
				if (vocal.position().equals(vocalPosition)) {
					vocalPosition = chartData.beats().addGrid(vocalPosition, 1).toFraction(chartData.beats());
				}
			}
			final IVirtualConstantPosition vocalEndPosition = chartData.beats().getMinEndPositionAfter(vocalPosition)
					.toFraction(chartData.beats());

			final FractionalPosition start = vocalPosition.toFraction(chartData.beats()).position();
			final FractionalPosition end = vocalEndPosition.toFraction(chartData.beats()).position();
			final Vocal vocal = new Vocal(start, end, syllable, flag);

			chartData.currentVocals().vocals.add(vocal);
			chartData.currentVocals().vocals.sort(IConstantFractionalPosition::compareTo);
		}

		public void placeSyllable() {
			getLine();
			if (line == null) {
				return;
			}
			getWord();
			if (word.isBlank()) {
				return;
			}
			getSyllable();
			if (syllable.isBlank()) {
				return;
			}

			undoSystem.addUndo();
			addVocal();
			arrangementFixer.fixLengths(chartData.currentVocals().vocals);
			textTab.setText(remainingText);
		}
	}

	public void placeLyricFromText() {
		new LyricsFromTextPlacer().placeSyllable();
	}
}
