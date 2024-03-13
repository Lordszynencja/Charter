package log.charter.services.data;

import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.song.vocals.Vocal;
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

		final List<Selection<Vocal>> selectedVocals = selectedAccessor.getSortedSelected();
		final Selection<Vocal> firstSelectedVocal = selectedVocals.remove(0);
		new VocalPane(firstSelectedVocal.id, firstSelectedVocal.selectable, chartData, charterFrame, selectionManager,
				undoSystem, selectedVocals);
	}

	public void toggleWordPart() {
		final ISelectionAccessor<Vocal> selectedAccessor = selectionManager.accessor(PositionType.VOCAL);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		undoSystem.addUndo();

		for (final Selection<Vocal> vocalSelection : selectedAccessor.getSelectedSet()) {
			vocalSelection.selectable.setPhraseEnd(false);
			vocalSelection.selectable.setWordPart(!vocalSelection.selectable.isWordPart());
		}

		currentSelectionEditor.selectionChanged(false);
	}

	public void togglePhraseEnd() {
		final ISelectionAccessor<Vocal> selectedAccessor = selectionManager.accessor(PositionType.VOCAL);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		undoSystem.addUndo();

		for (final Selection<Vocal> selectedVocal : selectedAccessor.getSelectedSet()) {
			selectedVocal.selectable.setWordPart(false);
			selectedVocal.selectable.setPhraseEnd(!selectedVocal.selectable.isPhraseEnd());
		}

		currentSelectionEditor.selectionChanged(false);
	}
}
