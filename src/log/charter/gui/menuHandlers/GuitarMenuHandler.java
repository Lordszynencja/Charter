package log.charter.gui.menuHandlers;

import java.util.function.Consumer;

import javax.swing.JMenu;

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
import log.charter.gui.panes.SlidePane;
import log.charter.song.Chord;
import log.charter.song.Note;

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
			if (chord.palmMute) {
				chord.palmMute = false;
				chord.fretHandMute = true;
			} else if (chord.fretHandMute) {
				chord.fretHandMute = false;
			} else {
				chord.palmMute = true;
			}
		}, note -> {
			if (note.palmMute) {
				note.palmMute = false;
				note.fretHandMute = true;
			} else if (note.fretHandMute) {
				note.fretHandMute = false;
			} else {
				note.palmMute = true;
			}
		});
	}

	private void toggleHOPO() {
		singleToggleOnAllSelectedNotes(chord -> {
		}, note -> {
			if (note.hammerOn) {
				note.hopo = true;
				note.hammerOn = false;
				note.pullOff = true;
			} else if (note.pullOff) {
				note.hopo = false;
				note.hammerOn = false;
				note.pullOff = false;
			} else {
				note.hopo = true;
				note.hammerOn = true;
				note.pullOff = false;
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
}
