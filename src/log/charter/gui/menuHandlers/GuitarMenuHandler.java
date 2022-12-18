package log.charter.gui.menuHandlers;

import javax.swing.JMenu;

import log.charter.data.ChartData;
import log.charter.data.EditMode;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.selection.ChordOrNote;
import log.charter.data.managers.selection.Selection;
import log.charter.data.managers.selection.SelectionAccessor;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.song.Chord;
import log.charter.song.Note;

class GuitarMenuHandler extends CharterMenuHandler {
	private ChartData data;
	private ModeManager modeManager;
	private SelectionManager selectionManager;

	public void init(final ChartData data, final ModeManager modeManager, final SelectionManager selectionManager) {
		this.data = data;
		this.modeManager = modeManager;
		this.selectionManager = selectionManager;
	}

	@Override
	boolean isApplicable() {
		return !data.isEmpty && modeManager.editMode == EditMode.GUITAR;
	}

	@Override
	JMenu prepareMenu() {
		final JMenu menu = new JMenu("Guitar");

		menu.add(createItem("toggle HO/PO", button('H'), this::toggleHOPO));
		menu.add(createItem("toggle mutes", button('P'), this::toggleMute));

		return menu;
	}

	private void toggleHOPO() {
		final SelectionAccessor<ChordOrNote> selectedAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		for (final Selection<ChordOrNote> selectedChordOrNote : selectedAccessor.getSelectedSet()) {
			if (selectedChordOrNote.selectable.note == null) {
				continue;
			}

			final Note note = selectedChordOrNote.selectable.note;
			if (note.hammerOn) {
				note.hammerOn = false;
				note.pullOff = true;
			} else if (note.pullOff) {
				note.pullOff = false;
				note.hopo = false;
			} else {
				note.hammerOn = true;
				note.hopo = true;
			}
		}
	}

	private void toggleMute() {
		final SelectionAccessor<ChordOrNote> selectedAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		for (final Selection<ChordOrNote> selectedChordOrNote : selectedAccessor.getSelectedSet()) {
			if (selectedChordOrNote.selectable.chord != null) {
				final Chord chord = selectedChordOrNote.selectable.chord;
				if (chord.palmMute) {
					chord.palmMute = false;
					chord.fretHandMute = true;
				} else if (chord.fretHandMute) {
					chord.fretHandMute = false;
				} else {
					chord.palmMute = true;
				}

				continue;
			}

			final Note note = selectedChordOrNote.selectable.note;
			if (note.palmMute) {
				note.palmMute = false;
				note.mute = true;
			} else if (note.mute) {
				note.mute = false;
			} else {
				note.palmMute = true;
			}
		}
	}
}
