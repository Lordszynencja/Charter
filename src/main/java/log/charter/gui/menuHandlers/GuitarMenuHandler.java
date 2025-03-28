package log.charter.gui.menuHandlers;

import static log.charter.util.CollectionUtils.getFromTo;

import java.util.List;

import javax.swing.JMenu;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.position.fractional.IConstantFractionalPosition;
import log.charter.data.song.position.virtual.IVirtualConstantPosition;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.services.Action;
import log.charter.services.ArrangementFretHandPositionsCreator;
import log.charter.services.data.selection.ISelectionAccessor;
import log.charter.services.data.selection.SelectionManager;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;

class GuitarMenuHandler extends CharterMenuHandler {
	private ChartData chartData;
	private ModeManager modeManager;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	@Override
	boolean isApplicable() {
		return modeManager.getMode() == EditMode.GUITAR;
	}

	private void addStringChangeItems(final JMenu menu) {
		menu.add(createItem(Action.MOVE_STRING_UP));
		menu.add(createItem(Action.MOVE_STRING_DOWN));
		menu.add(createItem(Action.MOVE_STRING_UP_SIMPLE));
		menu.add(createItem(Action.MOVE_STRING_DOWN_SIMPLE));
	}

	private void addFretChangeItems(final JMenu menu) {
		menu.add(createItem(Action.MOVE_FRET_UP));
		menu.add(createItem(Action.MOVE_FRET_DOWN));
		menu.add(createItem(Action.MOVE_FRET_UP_OCTAVE));
		menu.add(createItem(Action.MOVE_FRET_DOWN_OCTAVE));
		final JMenu noteFretOperationsSubMenu = createMenu(Label.NOTE_FRET_OPERATIONS);
		for (int i = 0; i < 10; i++) {
			noteFretOperationsSubMenu.add(createItem(Action.valueOf("NUMBER_" + i)));
		}
		menu.add(noteFretOperationsSubMenu);
	}

	private void addNoteStatusOperations(final JMenu menu) {
		final JMenu noteStatusOperationsSubMenu = createMenu(Label.NOTE_STATUS_OPERATIONS);
		noteStatusOperationsSubMenu.add(createItem(Action.TOGGLE_MUTE));
		noteStatusOperationsSubMenu.add(createItem(Action.TOGGLE_MUTE_INDEPENDENTLY));
		noteStatusOperationsSubMenu.add(createItem(Action.TOGGLE_HOPO));
		noteStatusOperationsSubMenu.add(createItem(Action.TOGGLE_HOPO_INDEPENDENTLY));
		noteStatusOperationsSubMenu.add(createItem(Action.TOGGLE_HARMONIC));
		noteStatusOperationsSubMenu.add(createItem(Action.TOGGLE_HARMONIC_INDEPENDENTLY));
		noteStatusOperationsSubMenu.add(createItem(Action.TOGGLE_ACCENT));
		noteStatusOperationsSubMenu.add(createItem(Action.TOGGLE_ACCENT_INDEPENDENTLY));
		noteStatusOperationsSubMenu.add(createItem(Action.TOGGLE_VIBRATO));
		noteStatusOperationsSubMenu.add(createItem(Action.TOGGLE_VIBRATO_INDEPENDENTLY));
		noteStatusOperationsSubMenu.add(createItem(Action.TOGGLE_TREMOLO));
		noteStatusOperationsSubMenu.add(createItem(Action.TOGGLE_TREMOLO_INDEPENDENTLY));
		noteStatusOperationsSubMenu.add(createItem(Action.TOGGLE_LINK_NEXT));
		noteStatusOperationsSubMenu.add(createItem(Action.TOGGLE_LINK_NEXT_INDEPENDENTLY));
		menu.add(noteStatusOperationsSubMenu);
	}

	@Override
	JMenu prepareMenu() {
		final JMenu menu = createMenu(Label.GUITAR_MENU);
		addStringChangeItems(menu);

		menu.addSeparator();
		addFretChangeItems(menu);

		menu.addSeparator();
		addNoteStatusOperations(menu);

		menu.addSeparator();
		menu.add(createItem(Action.MARK_HAND_SHAPE));

		menu.addSeparator();
		menu.add(createItem(Label.GUITAR_MENU_AUTOCREATE_FHP, this::addFHP));

		menu.addSeparator();
		menu.add(createItem(Action.TOGGLE_PREVIEW_WINDOW));
		menu.add(createItem(Action.TOGGLE_BORDERLESS_PREVIEW_WINDOW));

		return menu;
	}

	private <T extends IVirtualConstantPosition> void addFHP() {
		final ISelectionAccessor<IVirtualConstantPosition> selectedAccessor = selectionManager.selectedAccessor();
		final List<IVirtualConstantPosition> selectedElements = selectedAccessor.getSelectedElements();
		if (selectedElements.isEmpty()) {
			return;
		}

		final IConstantFractionalPosition from = selectedElements.get(0).toFraction(chartData.beats());
		final IConstantFractionalPosition to = selectedElements.get(selectedElements.size() - 1)
				.toFraction(chartData.beats());
		final List<ChordOrNote> soundsToAddFHPFor = getFromTo(chartData.currentSounds(), from, to);
		if (soundsToAddFHPFor.isEmpty()) {
			return;
		}

		undoSystem.addUndo();
		ArrangementFretHandPositionsCreator.createFHPs(chartData.beats(), chartData.currentChordTemplates(),
				soundsToAddFHPFor, chartData.currentFHPs());
	}
}
