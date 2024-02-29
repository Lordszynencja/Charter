package log.charter.gui.handlers.data;

import log.charter.data.ChartData;
import log.charter.data.managers.selection.Selection;
import log.charter.data.managers.selection.SelectionAccessor;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.panes.songEdits.VocalPane;
import log.charter.song.vocals.Vocal;
import log.charter.util.CollectionUtils.ArrayList2;

public class VocalsHandler {
	private ChartData data;
	private CharterFrame frame;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	public void init(final ChartData data, final CharterFrame frame, final SelectionManager selectionManager,
			final UndoSystem undoSystem) {
		this.data = data;
		this.frame = frame;
		this.selectionManager = selectionManager;
		this.undoSystem = undoSystem;
	}

	public void editVocals() {
		final SelectionAccessor<Vocal> selectedAccessor = selectionManager.getSelectedAccessor(PositionType.VOCAL);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		final ArrayList2<Selection<Vocal>> selectedVocals = selectedAccessor.getSortedSelected();
		final Selection<Vocal> firstSelectedVocal = selectedVocals.remove(0);
		new VocalPane(firstSelectedVocal.id, firstSelectedVocal.selectable, data, frame, selectionManager, undoSystem,
				selectedVocals);
	}

	public void toggleWordPart() {
		final SelectionAccessor<Vocal> selectedAccessor = selectionManager.getSelectedAccessor(PositionType.VOCAL);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		undoSystem.addUndo();

		for (final Selection<Vocal> vocalSelection : selectedAccessor.getSelectedSet()) {
			vocalSelection.selectable.setPhraseEnd(false);
			vocalSelection.selectable.setWordPart(!vocalSelection.selectable.isWordPart());
		}

		frame.selectionChanged(false);
	}

	public void togglePhraseEnd() {
		final SelectionAccessor<Vocal> selectedAccessor = selectionManager.getSelectedAccessor(PositionType.VOCAL);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		undoSystem.addUndo();

		for (final Selection<Vocal> selectedVocal : selectedAccessor.getSelectedSet()) {
			selectedVocal.selectable.setWordPart(false);
			selectedVocal.selectable.setPhraseEnd(!selectedVocal.selectable.isPhraseEnd());
		}

		frame.selectionChanged(false);
	}
}
