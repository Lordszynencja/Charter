package log.charter.gui.menuHandlers;

import javax.swing.JMenu;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.data.managers.selection.Selection;
import log.charter.data.managers.selection.SelectionAccessor;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.panes.VocalPane;
import log.charter.song.Vocal;
import log.charter.util.CollectionUtils.ArrayList2;

class VocalsMenuHandler extends CharterMenuHandler {

	private ChartData data;
	private CharterFrame frame;
	private ModeManager modeManager;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	public void init(final ChartData data, final CharterFrame frame, final ModeManager modeManager,
			final SelectionManager selectionManager, final UndoSystem undoSystem) {
		this.data = data;
		this.frame = frame;
		this.modeManager = modeManager;
		this.selectionManager = selectionManager;
		this.undoSystem = undoSystem;
	}

	@Override
	boolean isApplicable() {
		return !data.isEmpty && modeManager.editMode == EditMode.VOCALS;
	}

	@Override
	JMenu prepareMenu() {
		final JMenu menu = new JMenu(Label.VOCALS_MENU.label());

		menu.add(createItem(Label.VOCALS_MENU_EDIT_VOCALS, button('L'), this::editVocals));
		menu.add(createItem(Label.VOCALS_MENU_TOGGLE_WORD_PART, button('W'), this::toggleWordPart));
		menu.add(createItem(Label.VOCALS_MENU_TOGGLE_PHRASE_END, button('E'), this::togglePhraseEnd));

		return menu;
	}

	private void editVocals() {
		final SelectionAccessor<Vocal> selectedAccessor = selectionManager.getSelectedAccessor(PositionType.VOCAL);
		final ArrayList2<Selection<Vocal>> selectedVocals = selectedAccessor.getSortedSelected();
		final Selection<Vocal> firstSelectedVocal = selectedVocals.remove(0);
		new VocalPane(firstSelectedVocal.id, firstSelectedVocal.selectable, data, frame, selectionManager, undoSystem,
				selectedVocals);
	}

	private void toggleWordPart() {
		final SelectionAccessor<Vocal> selectedAccessor = selectionManager.getSelectedAccessor(PositionType.VOCAL);
		for (final Selection<Vocal> vocalSelection : selectedAccessor.getSelectedSet()) {
			vocalSelection.selectable.setWordPart(!vocalSelection.selectable.isWordPart());
		}
	}

	private void togglePhraseEnd() {
		final SelectionAccessor<Vocal> selectedAccessor = selectionManager.getSelectedAccessor(PositionType.VOCAL);
		final ArrayList2<Selection<Vocal>> selectedVocals = selectedAccessor.getSortedSelected();
		for (final Selection<Vocal> selectedVocal : selectedVocals) {
			selectedVocal.selectable.setPhraseEnd(!selectedVocal.selectable.isPhraseEnd());
		}
	}
}
