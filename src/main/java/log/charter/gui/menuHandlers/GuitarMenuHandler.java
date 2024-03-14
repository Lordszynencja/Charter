package log.charter.gui.menuHandlers;

import static log.charter.util.CollectionUtils.firstAfterEqual;
import static log.charter.util.CollectionUtils.lastBeforeEqual;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.Level;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.position.IVirtualConstantPosition;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.services.Action;
import log.charter.services.ArrangementFretHandPositionsCreator;
import log.charter.services.CharterContext.Initiable;
import log.charter.services.data.selection.ISelectionAccessor;
import log.charter.services.data.selection.Selection;
import log.charter.services.data.selection.SelectionManager;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;

class GuitarMenuHandler extends CharterMenuHandler implements Initiable {
	private ChartData chartData;
	private ModeManager modeManager;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	@Override
	public void init() {
		super.init(actionHandler);
	}

	@Override
	boolean isApplicable() {
		return modeManager.getMode() == EditMode.GUITAR;
	}

	@Override
	JMenu prepareMenu() {
		final JMenu menu = createMenu(Label.GUITAR_MENU);
		menu.add(createItem(Action.MOVE_STRING_UP));
		menu.add(createItem(Action.MOVE_STRING_DOWN));
		menu.add(createItem(Action.MOVE_STRING_UP_SIMPLE));
		menu.add(createItem(Action.MOVE_STRING_DOWN_SIMPLE));
		menu.add(createItem(Action.MOVE_FRET_UP));
		menu.add(createItem(Action.MOVE_FRET_DOWN));
		menu.addSeparator();
		final JMenu noteFretOperationsSubMenu = createMenu(Label.NOTE_FRET_OPERATIONS);
		noteFretOperationsSubMenu.add(createItem(Action.FRET_0));
		noteFretOperationsSubMenu.add(createItem(Action.FRET_1));
		noteFretOperationsSubMenu.add(createItem(Action.FRET_2));
		noteFretOperationsSubMenu.add(createItem(Action.FRET_3));
		noteFretOperationsSubMenu.add(createItem(Action.FRET_4));
		noteFretOperationsSubMenu.add(createItem(Action.FRET_5));
		noteFretOperationsSubMenu.add(createItem(Action.FRET_6));
		noteFretOperationsSubMenu.add(createItem(Action.FRET_7));
		noteFretOperationsSubMenu.add(createItem(Action.FRET_8));
		noteFretOperationsSubMenu.add(createItem(Action.FRET_9));
		menu.add(noteFretOperationsSubMenu);

		menu.addSeparator();
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

		menu.addSeparator();
		menu.add(createItem(Action.MARK_HAND_SHAPE));

		menu.addSeparator();
		menu.add(createItem(Label.GUITAR_MENU_AUTOCREATE_FHP, this::addFHP));

		menu.addSeparator();
		menu.add(createItem(Action.TOGGLE_PREVIEW_WINDOW));
		menu.add(createItem(Action.TOGGLE_BORDERLESS_PREVIEW_WINDOW));

		return menu;
	}

	private <T extends IVirtualConstantPosition> List<ChordOrNote> handleSelection(
			final ISelectionAccessor<T> selectionAccessor, final List<ChordOrNote> sounds) {
		final List<Selection<T>> selected = selectionAccessor.getSortedSelected();

		final Integer fromId = firstAfterEqual(sounds, selected.get(0).selectable.positionAsPosition(chartData.beats()))
				.findId();
		final Integer toId = lastBeforeEqual(sounds,
				selected.get(selected.size() - 1).selectable.positionAsPosition(chartData.beats())).findId();

		if (fromId == null || toId == null) {
			return new ArrayList<>();
		}

		final List<ChordOrNote> selectedSounds = new ArrayList<>();
		for (int i = fromId; i <= toId; i++) {
			selectedSounds.add(sounds.get(i));
		}

		return selectedSounds;
	}

	private <T extends IVirtualConstantPosition> void addFHP() {
		undoSystem.addUndo();

		final Level level = chartData.currentArrangementLevel();
		List<ChordOrNote> sounds = level.sounds;
		for (final PositionType positionType : PositionType.values()) {
			if (positionType == PositionType.NONE) {
				continue;
			}

			final ISelectionAccessor<T> selectionAccessor = selectionManager.accessor(positionType);
			if (selectionAccessor.isSelected()) {
				sounds = handleSelection(selectionAccessor, sounds);
				break;
			}
		}

		final List<ChordTemplate> chordTemplates = chartData.currentArrangement().chordTemplates;

		ArrangementFretHandPositionsCreator.createFretHandPositions(chartData.beats(), chordTemplates, sounds,
				level.anchors);
	}
}
