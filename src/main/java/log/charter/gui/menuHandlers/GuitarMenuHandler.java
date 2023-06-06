package log.charter.gui.menuHandlers;

import static log.charter.song.notes.IPosition.findFirstIdAfterEqual;
import static log.charter.song.notes.IPosition.findLastIdBeforeEqual;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

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
import log.charter.gui.CharterFrame;
import log.charter.gui.components.SpecialMenuItem;
import log.charter.gui.handlers.KeyboardHandler;
import log.charter.song.ChordTemplate;
import log.charter.song.Level;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.IPosition;
import log.charter.util.CollectionUtils.ArrayList2;

class GuitarMenuHandler extends CharterMenuHandler {
	private ChartData data;
	private CharterFrame frame;
	private KeyboardHandler keyboardHandler;
	private ModeManager modeManager;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	public void init(final ChartData data, final CharterFrame frame, final KeyboardHandler keyboardHandler,
			final ModeManager modeManager, final SelectionManager selectionManager, final UndoSystem undoSystem) {
		this.data = data;
		this.frame = frame;
		this.keyboardHandler = keyboardHandler;
		this.modeManager = modeManager;
		this.selectionManager = selectionManager;
		this.undoSystem = undoSystem;
	}

	@Override
	boolean isApplicable() {
		return !data.isEmpty && modeManager.editMode == EditMode.GUITAR;
	}

	@Override
	JMenu prepareMenu() {
		final JMenu menu = new JMenu(Label.GUITAR_MENU.label());

		menu.add(new SpecialMenuItem(Label.GUITAR_MENU_STRING_UP, "Up", keyboardHandler::moveNotesUpKeepFrets));
		menu.add(new SpecialMenuItem(Label.GUITAR_MENU_STRING_DOWN, "Down", keyboardHandler::moveNotesDownKeepFrets));
		menu.add(new SpecialMenuItem(Label.GUITAR_MENU_STRING_UP_KEEP_FRETS, "Ctrl-Up", keyboardHandler::moveNotesUp));
		menu.add(new SpecialMenuItem(Label.GUITAR_MENU_STRING_DOWN_KEEP_FRETS, "Ctrl-Down",
				keyboardHandler::moveNotesDown));

		menu.addSeparator();
		menu.add(new SpecialMenuItem(Label.GUITAR_MENU_TOGGLE_MUTES, "M", keyboardHandler::toggleMute));
		menu.add(new SpecialMenuItem(Label.GUITAR_MENU_TOGGLE_HOPO, "H", keyboardHandler::toggleHOPO));
		menu.add(new SpecialMenuItem(Label.GUITAR_MENU_TOGGLE_HARMONIC, "O", keyboardHandler::toggleHarmonic));
		menu.add(new SpecialMenuItem(Label.GUITAR_MENU_SET_SLIDE, "S", keyboardHandler::editSlide));
		menu.add(new SpecialMenuItem(Label.GUITAR_MENU_TOGGLE_ACCENT, "A", keyboardHandler::toggleAccent));
		menu.add(new SpecialMenuItem(Label.GUITAR_MENU_TOGGLE_VIBRATO, "V", keyboardHandler::toggleVibrato));
		menu.add(new SpecialMenuItem(Label.GUITAR_MENU_TOGGLE_TREMOLO, "T", keyboardHandler::toggleTremolo));
		menu.add(new SpecialMenuItem(Label.GUITAR_MENU_TOGGLE_LINK_NEXT, "L", keyboardHandler::toggleLinkNext));

		menu.addSeparator();
		final JMenuItem noteOptions = new SpecialMenuItem(Label.GUITAR_MENU_EDIT_NOTE, "W", keyboardHandler::editNote);
		noteOptions.setToolTipText(Label.GUITAR_MENU_EDIT_NOTE_TOOLTIP.label());
		menu.add(noteOptions);
		final JMenuItem chordOptions = new SpecialMenuItem(Label.GUITAR_MENU_EDIT_AS_CHORD, "Q",
				keyboardHandler::editNoteAsChord);
		chordOptions.setToolTipText(Label.GUITAR_MENU_EDIT_AS_CHORD_TOOLTIP.label());
		menu.add(chordOptions);
		final JMenuItem singleNoteOptions = new SpecialMenuItem(Label.GUITAR_MENU_EDIT_AS_SINGLE_NOTE, "E",
				keyboardHandler::editNoteAsSingleNote);
		singleNoteOptions.setToolTipText(Label.GUITAR_MENU_EDIT_AS_SINGLE_NOTE_TOOLTIP.label());
		menu.add(singleNoteOptions);
		menu.add(new SpecialMenuItem(Label.GUITAR_MENU_EDIT_BEND, "B", keyboardHandler::editBend));

		menu.addSeparator();
		menu.add(new SpecialMenuItem(Label.GUITAR_MENU_MARK_HAND_SHAPE, "Shift-H", keyboardHandler::markHandShape));
		menu.add(new SpecialMenuItem(Label.GUITAR_MENU_EDIT_HAND_SHAPE, "Ctrl-H", keyboardHandler::editHandShape));

		menu.addSeparator();
		menu.add(new SpecialMenuItem(Label.GUITAR_MENU_AUTOCREATE_FHP, null, this::addFHP));

		menu.addSeparator();
		menu.add(new SpecialMenuItem(Label.GUITAR_MENU_FULL_SCREEN_PREVIEW, "F11", frame::switchFullscreenPreview));

		return menu;
	}

	private void addFHP() {
		undoSystem.addUndo();

		final Level level = data.getCurrentArrangementLevel();
		ArrayList2<ChordOrNote> sounds = level.chordsAndNotes;
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
