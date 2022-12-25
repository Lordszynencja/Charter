package log.charter.gui.menuHandlers;

import java.util.function.Consumer;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.data.managers.selection.ChordOrNote;
import log.charter.data.managers.selection.Selection;
import log.charter.data.managers.selection.SelectionAccessor;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.panes.ChordOptionsPane;
import log.charter.gui.panes.HandShapePane;
import log.charter.gui.panes.NoteOptionsPane;
import log.charter.gui.panes.SlidePane;
import log.charter.song.HandShape;
import log.charter.song.enums.HOPO;
import log.charter.song.enums.Mute;
import log.charter.song.notes.Chord;
import log.charter.song.notes.Note;
import log.charter.util.CollectionUtils.ArrayList2;

class GuitarMenuHandler extends CharterMenuHandler {
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
		return !data.isEmpty && modeManager.editMode == EditMode.GUITAR;
	}

	@Override
	JMenu prepareMenu() {
		final JMenu menu = new JMenu(Label.GUITAR_MENU.label());

		menu.add(createItem(Label.GUITAR_MENU_TOGGLE_MUTES, button('P'), this::toggleMute));
		menu.add(createItem(Label.GUITAR_MENU_TOGGLE_HOPO, button('H'), this::toggleHOPO));
		menu.add(createItem(Label.GUITAR_MENU_SET_SLIDE, button('S'), this::setSlide));
		menu.add(createItem(Label.GUITAR_MENU_TOGGLE_LINK_NEXT, button('L'), this::toggleLinkNext));

		menu.addSeparator();
		final JMenuItem noteOptions = createItem(Label.GUITAR_MENU_NOTE_OPTIONS, button('N'), this::noteOptions);
		noteOptions.setToolTipText(Label.GUITAR_MENU_NOTE_OPTIONS_TOOLTIP.label());
		menu.add(noteOptions);

		final JMenuItem chordOptions = createItem(Label.GUITAR_MENU_CHORD_OPTIONS, button('Q'), this::chordOptions);
		chordOptions.setToolTipText(Label.GUITAR_MENU_CHORD_OPTIONS_TOOLTIP.label());
		menu.add(chordOptions);

		final JMenuItem singleNoteOptions = createItem(Label.GUITAR_MENU_SINGLE_NOTE_OPTIONS, button('E'),
				this::singleNoteOptions);
		singleNoteOptions.setToolTipText(Label.GUITAR_MENU_SINGLE_NOTE_OPTIONS_TOOLTIP.label());
		menu.add(singleNoteOptions);

		menu.addSeparator();
		menu.add(createItem(Label.GUITAR_MENU_HAND_SHAPE_OPTIONS, button('U'), this::handShapeOptions));

		return menu;
	}

	private void singleToggleOnAllSelectedNotes(final Consumer<Chord> chordHandler, final Consumer<Note> noteHandler) {
		final SelectionAccessor<ChordOrNote> selectedAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		undoSystem.addUndo();

		for (final Selection<ChordOrNote> selectedChordOrNote : selectedAccessor.getSelectedSet()) {
			if (selectedChordOrNote.selectable.chord != null) {
				chordHandler.accept(selectedChordOrNote.selectable.chord);
			} else {
				noteHandler.accept(selectedChordOrNote.selectable.note);
			}
		}
	}

	private void toggleMute() {
		singleToggleOnAllSelectedNotes(chord -> {
			if (chord.mute == Mute.PALM) {
				chord.mute = Mute.STRING;
			} else if (chord.mute == Mute.STRING) {
				chord.mute = null;
			} else {
				chord.mute = Mute.PALM;
			}
		}, note -> {
			if (note.mute == Mute.PALM) {
				note.mute = Mute.STRING;
			} else if (note.mute == Mute.STRING) {
				note.mute = null;
			} else {
				note.mute = Mute.PALM;
			}
		});
	}

	private void toggleHOPO() {
		singleToggleOnAllSelectedNotes(chord -> {
		}, note -> {
			if (note.hopo == HOPO.HAMMER_ON) {
				note.hopo = HOPO.PULL_OFF;
			} else if (note.hopo == HOPO.PULL_OFF) {
				note.hopo = HOPO.NONE;
			} else {
				note.hopo = HOPO.HAMMER_ON;
			}
		});
	}

	private void toggleLinkNext() {
		singleToggleOnAllSelectedNotes(chord -> {
			chord.linkNext = !chord.linkNext;
		}, note -> {
			note.linkNext = !note.linkNext;
		});
	}

	private void setSlide() {
		final SelectionAccessor<ChordOrNote> selectedAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		new SlidePane(frame, undoSystem, selectedAccessor.getSortedSelected().get(0).selectable);
	}

	private ArrayList2<ChordOrNote> getSelectedNotes() {
		final SelectionAccessor<ChordOrNote> selectedAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);

		return selectedAccessor.getSortedSelected().map(selection -> selection.selectable);
	}

	private void openChordOptionsPopup(final ArrayList2<ChordOrNote> selected) {
		new ChordOptionsPane(data, frame, undoSystem, selected);
	}

	private void openSingleNoteOptionsPopup(final ArrayList2<ChordOrNote> selected) {
		new NoteOptionsPane(data, frame, undoSystem, selected);
	}

	private void noteOptions() {
		final ArrayList2<ChordOrNote> selected = getSelectedNotes();
		if (selected.isEmpty()) {
			return;
		}

		if (selected.get(0).isChord()) {
			openChordOptionsPopup(selected);
		} else {
			openSingleNoteOptionsPopup(selected);
		}
	}

	private void chordOptions() {
		final ArrayList2<ChordOrNote> selected = getSelectedNotes();
		if (selected.isEmpty()) {
			return;
		}

		openChordOptionsPopup(selected);
	}

	private void singleNoteOptions() {
		final ArrayList2<ChordOrNote> selected = getSelectedNotes();
		if (selected.isEmpty()) {
			return;
		}

		openSingleNoteOptionsPopup(selected);
	}

	private void handShapeOptions() {
		final SelectionAccessor<HandShape> selectedAccessor = selectionManager
				.getSelectedAccessor(PositionType.HAND_SHAPE);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		new HandShapePane(data, frame, selectedAccessor.getSortedSelected().get(0).selectable);
	}
}
