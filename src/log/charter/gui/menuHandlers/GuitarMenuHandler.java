package log.charter.gui.menuHandlers;

import static java.lang.Math.min;
import static log.charter.song.notes.IPosition.findFirstIdAfter;
import static log.charter.song.notes.IPosition.findLastIdBefore;

import java.util.function.Consumer;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
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
import log.charter.song.ChordTemplate;
import log.charter.song.HandShape;
import log.charter.song.enums.HOPO;
import log.charter.song.enums.Harmonic;
import log.charter.song.enums.Mute;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.GuitarSound;
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
		menu.add(createItem(Label.GUITAR_MENU_TOGGLE_HARMONIC, button('O'), this::toggleHarmonic));
		menu.add(createItem(Label.GUITAR_MENU_SET_SLIDE, button('S'), this::setSlide));
		menu.add(createItem(Label.GUITAR_MENU_TOGGLE_LINK_NEXT, button('L'), this::toggleLinkNext));

		menu.addSeparator();
		final JMenuItem noteOptions = createItem(Label.GUITAR_MENU_NOTE_OPTIONS, button('W'), this::noteOptions);
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
		menu.add(createItem(Label.GUITAR_MENU_HAND_SHAPE_OPTIONS, button('T'), this::handShapeOptions));
		menu.add(createItem(Label.GUITAR_MENU_MARK_HAND_SHAPE, ctrl('T'), this::markHandShape));

		return menu;
	}

	private void singleToggleOnAllSelectedNotes(final Consumer<GuitarSound> handler) {
		final SelectionAccessor<ChordOrNote> selectedAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		undoSystem.addUndo();
		selectedAccessor.getSelectedSet().forEach(selected -> handler.accept(selected.selectable.asGuitarSound()));
	}

	private void toggleMute() {
		singleToggleOnAllSelectedNotes(sound -> {
			if (sound.mute == Mute.NONE) {
				sound.mute = Mute.PALM;
			} else if (sound.mute == Mute.PALM) {
				sound.mute = Mute.STRING;
			} else if (sound.mute == Mute.STRING) {
				sound.mute = Mute.NONE;
			}
		});
	}

	private void toggleHOPO() {
		singleToggleOnAllSelectedNotes(sound -> {
			if (sound.hopo == HOPO.NONE) {
				sound.hopo = HOPO.HAMMER_ON;
			} else if (sound.hopo == HOPO.HAMMER_ON) {
				sound.hopo = HOPO.PULL_OFF;
			} else if (sound.hopo == HOPO.PULL_OFF) {
				sound.hopo = HOPO.TAP;
			} else if (sound.hopo == HOPO.TAP) {
				sound.hopo = HOPO.NONE;
			}
		});
	}

	private void toggleHarmonic() {
		singleToggleOnAllSelectedNotes(sound -> {
			if (sound.harmonic == Harmonic.NONE) {
				sound.harmonic = Harmonic.NORMAL;
			} else if (sound.harmonic == Harmonic.NORMAL) {
				sound.harmonic = Harmonic.PINCH;
			} else if (sound.harmonic == Harmonic.PINCH) {
				sound.harmonic = Harmonic.NONE;
			}
		});
	}

	private void toggleLinkNext() {
		singleToggleOnAllSelectedNotes(sound -> {
			sound.linkNext = !sound.linkNext;
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

		undoSystem.addUndo();

		new HandShapePane(data, frame, selectedAccessor.getSortedSelected().get(0).selectable, () -> {
			undoSystem.undo();
			undoSystem.removeRedo();
		});
	}

	private void markHandShape() {
		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		if (!selectionAccessor.isSelected()) {
			return;
		}

		undoSystem.addUndo();

		final ArrayList2<HandShape> handShapes = data.getCurrentArrangementLevel().handShapes;
		final ArrayList2<Selection<ChordOrNote>> selected = selectionAccessor.getSortedSelected();
		final int position = selected.get(0).selectable.position();
		int endPosition = selected.getLast().selectable.endPosition();
		final int firstIdAfter = findFirstIdAfter(handShapes, endPosition);
		if (firstIdAfter == -1) {
			endPosition = data.songChart.beatsMap.getPositionWithAddedGrid(endPosition, 1, 16);
		} else {
			endPosition = min(handShapes.get(firstIdAfter).position() - Config.minNoteDistance,
					data.songChart.beatsMap.getPositionWithAddedGrid(endPosition, 1, 16));
		}

		int deleteFromId = findLastIdBefore(handShapes, position);
		if (deleteFromId == -1) {
			deleteFromId = 0;
		}
		if (handShapes.size() > deleteFromId && handShapes.get(deleteFromId).endPosition() < position) {
			deleteFromId++;
		}
		final int deleteToId = firstIdAfter == -1 ? handShapes.size() - 1 : firstIdAfter - 1;
		for (int i = deleteToId; i >= deleteFromId; i--) {
			handShapes.remove(i);
		}

		ChordTemplate chordTemplate = new ChordTemplate();
		if (selected.get(0).selectable.isChord()) {
			chordTemplate = data.getCurrentArrangement().chordTemplates.get(selected.get(0).selectable.chord.chordId);
		}

		final HandShape handShape = new HandShape(position, endPosition - position);
		handShape.chordId = data.getCurrentArrangement().getChordTemplateIdWithSave(chordTemplate);

		handShapes.add(handShape);
		handShapes.sort(null);
		new HandShapePane(data, frame, handShape, () -> {
			undoSystem.undo();
			undoSystem.removeRedo();
		});
	}
}
