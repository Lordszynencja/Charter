package log.charter.services.data;

import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.song.vocals.Vocal;
import log.charter.data.song.vocals.Vocal.VocalFlag;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.tabs.selectionEditor.CurrentSelectionEditor;
import log.charter.gui.panes.songEdits.VocalPane;
import log.charter.services.data.selection.ISelectionAccessor;
import log.charter.services.data.selection.Selection;
import log.charter.services.data.selection.SelectionManager;

public class VocalsHandler {
	private ChartData chartData;
	private CharterFrame charterFrame;
	private CurrentSelectionEditor currentSelectionEditor;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	public void editVocals() {
		final ISelectionAccessor<Vocal> selectedAccessor = selectionManager.accessor(PositionType.VOCAL);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		final List<Selection<Vocal>> selectedVocals = selectedAccessor.getSelected();
		final Selection<Vocal> firstSelectedVocal = selectedVocals.remove(0);
		new VocalPane(firstSelectedVocal.id, firstSelectedVocal.selectable, chartData, charterFrame, selectionManager,
				undoSystem, selectedVocals);
	}

	private void toggle(final VocalFlag flag) {
		final ISelectionAccessor<Vocal> selectedAccessor = selectionManager.accessor(PositionType.VOCAL);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		undoSystem.addUndo();

		for (final Selection<Vocal> selection : selectedAccessor.getSelected2()) {
			final Vocal vocal = selection.selectable;
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
}
