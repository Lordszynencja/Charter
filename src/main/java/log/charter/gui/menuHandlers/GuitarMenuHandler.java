package log.charter.gui.menuHandlers;

import static log.charter.song.notes.IConstantPosition.findFirstIdAfterEqual;
import static log.charter.song.notes.IConstantPosition.findLastIdBeforeEqual;

import javax.swing.JMenu;

import log.charter.data.ArrangementFretHandPositionsCreator;
import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.data.managers.selection.Selection;
import log.charter.data.managers.selection.SelectionAccessor;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.handlers.mouseAndKeyboard.Action;
import log.charter.gui.handlers.mouseAndKeyboard.KeyboardHandler;
import log.charter.song.ChordTemplate;
import log.charter.song.Level;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.IPosition;
import log.charter.util.CollectionUtils.ArrayList2;

class GuitarMenuHandler extends CharterMenuHandler {
	private ChartData data;
	private KeyboardHandler keyboardHandler;
	private ModeManager modeManager;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	public void init(final ChartData data, final KeyboardHandler keyboardHandler, final ModeManager modeManager,
			final SelectionManager selectionManager, final UndoSystem undoSystem) {
		this.data = data;
		this.keyboardHandler = keyboardHandler;
		this.modeManager = modeManager;
		this.selectionManager = selectionManager;
		this.undoSystem = undoSystem;
	}

	@Override
	boolean isApplicable() {
		return modeManager.getMode() == EditMode.GUITAR;
	}

	@Override
	JMenu prepareMenu() {
		final JMenu menu = new JMenu(Label.GUITAR_MENU.label());
		menu.add(createItem(keyboardHandler, Action.MOVE_STRING_UP));
		menu.add(createItem(keyboardHandler, Action.MOVE_STRING_DOWN));
		menu.add(createItem(keyboardHandler, Action.MOVE_STRING_UP_SIMPLE));
		menu.add(createItem(keyboardHandler, Action.MOVE_STRING_DOWN_SIMPLE));
		menu.add(createItem(keyboardHandler, Action.MOVE_FRET_UP));
		menu.add(createItem(keyboardHandler, Action.MOVE_FRET_DOWN));

		menu.addSeparator();
		final JMenu noteStatusOperationsSubMenu = new JMenu(Label.NOTE_STATUS_OPERATIONS.label());
		noteStatusOperationsSubMenu.add(createItem(keyboardHandler, Action.TOGGLE_MUTE));
		noteStatusOperationsSubMenu.add(createItem(keyboardHandler, Action.TOGGLE_MUTE_INDEPENDENTLY));
		noteStatusOperationsSubMenu.add(createItem(keyboardHandler, Action.TOGGLE_HOPO));
		noteStatusOperationsSubMenu.add(createItem(keyboardHandler, Action.TOGGLE_HOPO_INDEPENDENTLY));
		noteStatusOperationsSubMenu.add(createItem(keyboardHandler, Action.TOGGLE_HARMONIC));
		noteStatusOperationsSubMenu.add(createItem(keyboardHandler, Action.TOGGLE_HARMONIC_INDEPENDENTLY));
		noteStatusOperationsSubMenu.add(createItem(keyboardHandler, Action.TOGGLE_ACCENT));
		noteStatusOperationsSubMenu.add(createItem(keyboardHandler, Action.TOGGLE_ACCENT_INDEPENDENTLY));
		noteStatusOperationsSubMenu.add(createItem(keyboardHandler, Action.TOGGLE_VIBRATO));
		noteStatusOperationsSubMenu.add(createItem(keyboardHandler, Action.TOGGLE_VIBRATO_INDEPENDENTLY));
		noteStatusOperationsSubMenu.add(createItem(keyboardHandler, Action.TOGGLE_TREMOLO));
		noteStatusOperationsSubMenu.add(createItem(keyboardHandler, Action.TOGGLE_TREMOLO_INDEPENDENTLY));
		noteStatusOperationsSubMenu.add(createItem(keyboardHandler, Action.TOGGLE_LINK_NEXT));
		noteStatusOperationsSubMenu.add(createItem(keyboardHandler, Action.TOGGLE_LINK_NEXT_INDEPENDENTLY));
		menu.add(noteStatusOperationsSubMenu);

		menu.addSeparator();
		menu.add(createItem(keyboardHandler, Action.MARK_HAND_SHAPE));

		menu.addSeparator();
		menu.add(createItem(Label.GUITAR_MENU_AUTOCREATE_FHP, this::addFHP));

		menu.addSeparator();
		menu.add(createItem(keyboardHandler, Action.TOGGLE_PREVIEW_WINDOW));
		menu.add(createItem(keyboardHandler, Action.TOGGLE_BORDERLESS_PREVIEW_WINDOW));

		return menu;
	}

	private void addFHP() {
		undoSystem.addUndo();

		final Level level = data.getCurrentArrangementLevel();
		ArrayList2<ChordOrNote> sounds = level.sounds;
		for (final PositionType positionType : PositionType.values()) {
			if (positionType == PositionType.NONE) {
				continue;
			}

			final SelectionAccessor<IPosition> selectionAccessor = selectionManager.getSelectedAccessor(positionType);
			if (selectionAccessor.isSelected()) {
				final ArrayList2<Selection<IPosition>> selected = selectionAccessor.getSortedSelected();
				final int fromId = findFirstIdAfterEqual(sounds, selected.get(0).selectable.position());
				final int toId = findLastIdBeforeEqual(sounds, selected.getLast().selectable.position());
				final ArrayList2<ChordOrNote> selectedSounds = new ArrayList2<>();
				for (int i = fromId; i <= toId; i++) {
					selectedSounds.add(sounds.get(i));
				}
				sounds = selectedSounds;
				break;
			}
		}

		final ArrayList2<ChordTemplate> chordTemplates = data.getCurrentArrangement().chordTemplates;
		selectionManager.getSelectedAccessor(PositionType.GUITAR_NOTE);

		ArrangementFretHandPositionsCreator.createFretHandPositions(chordTemplates, sounds, level.anchors);
	}
}
