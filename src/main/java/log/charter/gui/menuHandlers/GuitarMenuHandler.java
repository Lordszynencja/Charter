package log.charter.gui.menuHandlers;

import static java.lang.Math.min;
import static log.charter.song.notes.IPosition.findFirstIdAfter;
import static log.charter.song.notes.IPosition.findLastIdBefore;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

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
import log.charter.gui.handlers.KeyboardHandler;
import log.charter.gui.panes.ChordBendPane;
import log.charter.gui.panes.ChordOptionsPane;
import log.charter.gui.panes.HandShapePane;
import log.charter.gui.panes.NoteBendPane;
import log.charter.gui.panes.NoteOptionsPane;
import log.charter.gui.panes.SlidePane;
import log.charter.song.ArrangementChart;
import log.charter.song.ChordTemplate;
import log.charter.song.HandShape;
import log.charter.song.enums.HOPO;
import log.charter.song.enums.Harmonic;
import log.charter.song.enums.Mute;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.GuitarSound;
import log.charter.song.notes.Note;
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

		menu.add(createItem(Label.GUITAR_MENU_SET_FRET, ctrl('F'), this::setFret));
		menu.add(createItem(Label.GUITAR_MENU_STRING_UP, button(KeyEvent.VK_UP), this::moveNotesUpKeepFrets));
		menu.add(createItem(Label.GUITAR_MENU_STRING_DOWN, button(KeyEvent.VK_DOWN), this::moveNotesDownKeepFrets));
		menu.add(createItem(Label.GUITAR_MENU_STRING_UP_KEEP_FRETS, ctrl(KeyEvent.VK_UP), this::moveNotesUp));
		menu.add(createItem(Label.GUITAR_MENU_STRING_DOWN_KEEP_FRETS, ctrl(KeyEvent.VK_DOWN), this::moveNotesDown));

		menu.addSeparator();
		menu.add(createItem(Label.GUITAR_MENU_TOGGLE_MUTES, button('M'), this::toggleMute));
		menu.add(createItem(Label.GUITAR_MENU_TOGGLE_HOPO, button('H'), this::toggleHOPO));
		menu.add(createItem(Label.GUITAR_MENU_TOGGLE_HARMONIC, button('O'), this::toggleHarmonic));
		menu.add(createItem(Label.GUITAR_MENU_SET_SLIDE, button('S'), this::setSlide));
		menu.add(createItem(Label.GUITAR_MENU_TOGGLE_ACCENT, button('A'), this::toggleAccent));
		menu.add(createItem(Label.GUITAR_MENU_TOGGLE_VIBRATO, button('V'), this::toggleVibrato));
		menu.add(createItem(Label.GUITAR_MENU_TOGGLE_TREMOLO, button('T'), this::toggleTremolo));
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
		menu.add(createItem(Label.GUITAR_MENU_BEND_SETTINGS, button('B'), this::openBendEditor));

		menu.addSeparator();
		menu.add(createItem(Label.GUITAR_MENU_MARK_HAND_SHAPE, shift('H'), this::markHandShape));
		menu.add(createItem(Label.GUITAR_MENU_HAND_SHAPE_OPTIONS, ctrl('H'), this::handShapeOptions));

		return menu;
	}

	private <T> void singleToggleOnAllSelectedNotesWithBaseValueNote(final Function<Note, T> baseValueGetter,
			final BiConsumer<Note, T> handler) {
		final SelectionAccessor<ChordOrNote> selectedAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		final ArrayList2<Selection<ChordOrNote>> selected = selectedAccessor.getSortedSelected();
		selected.removeIf(selection -> selection.selectable.isChord());
		final T baseValue = baseValueGetter.apply(selected.get(0).selectable.note);

		undoSystem.addUndo();
		selected.forEach(selectedValue -> handler.accept(selectedValue.selectable.note, baseValue));
	}

	private <T> void singleToggleOnAllSelectedNotesWithBaseValue(final Function<GuitarSound, T> baseValueGetter,
			final BiConsumer<GuitarSound, T> handler) {
		final SelectionAccessor<ChordOrNote> selectedAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		final ArrayList2<Selection<ChordOrNote>> selected = selectedAccessor.getSortedSelected();
		final T baseValue = baseValueGetter.apply(selected.get(0).selectable.asGuitarSound());

		undoSystem.addUndo();
		selected.forEach(selectedValue -> handler.accept(selectedValue.selectable.asGuitarSound(), baseValue));
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

	private boolean containsString(final ArrayList2<Selection<ChordOrNote>> selectedSounds, final int string) {
		final ArrayList2<ChordTemplate> chordTemplates = data.getCurrentArrangement().chordTemplates;

		for (final Selection<ChordOrNote> selection : selectedSounds) {
			final ChordOrNote sound = selection.selectable;
			if (sound.isNote()) {
				if (sound.note.string == string) {
					return true;
				}
				continue;
			}

			if (chordTemplates.get(sound.chord.chordId).frets.get(string) != null) {
				return true;
			}
		}

		return false;
	}

	private void moveNotesUp() {
		final ArrayList2<Selection<ChordOrNote>> selectedSounds = selectionManager
				.<ChordOrNote>getSelectedAccessor(PositionType.GUITAR_NOTE).getSortedSelected();
		if (selectedSounds.isEmpty()) {
			return;
		}

		final int topString = data.currentStrings() - 1;
		if (containsString(selectedSounds, topString)) {
			return;
		}

		final ArrangementChart arrangement = data.getCurrentArrangement();
		final Map<Integer, Integer> stringDifferences = new HashMap<>();
		for (int i = 0; i <= topString - 1; i++) {
			stringDifferences.put(i, arrangement.tuning.getStringOffset(i) - arrangement.tuning.getStringOffset(i + 1));
		}

		undoSystem.addUndo();

		final Map<Integer, Integer> movedChordTemplates = new HashMap<>();
		final ArrayList2<ChordTemplate> chordTemplates = arrangement.chordTemplates;

		for (final Selection<ChordOrNote> selection : selectedSounds) {
			final ChordOrNote sound = selection.selectable;
			if (sound.isNote()) {
				final int newFret = sound.note.fret + stringDifferences.get(sound.note.string);
				if (newFret >= 0 && newFret <= Config.frets) {
					sound.note.fret = newFret;
					sound.note.string++;
				}
				continue;
			}

			if (movedChordTemplates.containsKey(sound.chord.chordId)) {
				sound.chord.chordId = movedChordTemplates.get(sound.chord.chordId);
				continue;
			}

			final ChordTemplate newChordTemplate = new ChordTemplate(chordTemplates.get(sound.chord.chordId));
			newChordTemplate.chordName = "";
			boolean wrongFret = false;
			for (int string = topString - 1; string >= 0; string--) {
				Integer fret = newChordTemplate.frets.remove(string);
				if (fret == null) {
					continue;
				}

				fret = fret + stringDifferences.get(string);
				if (fret < 0 || fret > Config.frets) {
					wrongFret = true;
					break;
				}

				newChordTemplate.frets.put(string + 1, fret);
				newChordTemplate.fingers.put(string + 1, newChordTemplate.fingers.remove(string));
			}

			if (wrongFret) {
				continue;
			}

			final int newTemplateId = arrangement.getChordTemplateIdWithSave(newChordTemplate);
			movedChordTemplates.put(sound.chord.chordId, newTemplateId);
			sound.chord.chordId = newTemplateId;
		}
	}

	private void moveNotesDown() {
		final ArrayList2<Selection<ChordOrNote>> selectedSounds = selectionManager
				.<ChordOrNote>getSelectedAccessor(PositionType.GUITAR_NOTE).getSortedSelected();
		if (selectedSounds.isEmpty()) {
			return;
		}

		final int topString = data.currentStrings() - 1;
		if (containsString(selectedSounds, 0)) {
			return;
		}

		final ArrangementChart arrangement = data.getCurrentArrangement();
		final Map<Integer, Integer> stringDifferences = new HashMap<>();
		for (int i = 1; i <= topString; i++) {
			stringDifferences.put(i, arrangement.tuning.getStringOffset(i) - arrangement.tuning.getStringOffset(i - 1));
		}

		undoSystem.addUndo();

		final Map<Integer, Integer> movedChordTemplates = new HashMap<>();
		final ArrayList2<ChordTemplate> chordTemplates = arrangement.chordTemplates;

		for (final Selection<ChordOrNote> selection : selectedSounds) {
			final ChordOrNote sound = selection.selectable;
			if (sound.isNote()) {
				final int newFret = sound.note.fret + stringDifferences.get(sound.note.string);
				if (newFret >= 0 && newFret <= Config.frets) {
					sound.note.fret = newFret;
					sound.note.string--;
				}
				continue;
			}

			if (movedChordTemplates.containsKey(sound.chord.chordId)) {
				sound.chord.chordId = movedChordTemplates.get(sound.chord.chordId);
				continue;
			}

			final ChordTemplate newChordTemplate = new ChordTemplate(chordTemplates.get(sound.chord.chordId));
			newChordTemplate.chordName = "";
			boolean wrongFret = false;
			for (int string = 1; string <= topString; string++) {
				Integer fret = newChordTemplate.frets.remove(string);
				if (fret == null) {
					continue;
				}

				fret = fret + stringDifferences.get(string);
				if (fret < 0 || fret > Config.frets) {
					wrongFret = true;
					break;
				}

				newChordTemplate.frets.put(string - 1, fret);
				newChordTemplate.fingers.put(string - 1, newChordTemplate.fingers.remove(string));
			}

			if (wrongFret) {
				continue;
			}

			final int newTemplateId = arrangement.getChordTemplateIdWithSave(newChordTemplate);
			movedChordTemplates.put(sound.chord.chordId, newTemplateId);
			sound.chord.chordId = newTemplateId;
		}
	}

	private void moveNotesUpKeepFrets() {
		final ArrayList2<Selection<ChordOrNote>> selectedSounds = selectionManager
				.<ChordOrNote>getSelectedAccessor(PositionType.GUITAR_NOTE).getSortedSelected();
		if (selectedSounds.isEmpty()) {
			return;
		}

		final int topString = data.currentStrings() - 1;
		if (containsString(selectedSounds, topString)) {
			return;
		}

		undoSystem.addUndo();

		final Map<Integer, Integer> movedChordTemplates = new HashMap<>();
		final ArrangementChart arrangement = data.getCurrentArrangement();
		final ArrayList2<ChordTemplate> chordTemplates = arrangement.chordTemplates;

		for (final Selection<ChordOrNote> selection : selectedSounds) {
			final ChordOrNote sound = selection.selectable;
			if (sound.isNote()) {
				sound.note.string++;
				continue;
			}

			if (movedChordTemplates.containsKey(sound.chord.chordId)) {
				sound.chord.chordId = movedChordTemplates.get(sound.chord.chordId);
				continue;
			}

			final ChordTemplate newChordTemplate = new ChordTemplate(chordTemplates.get(sound.chord.chordId));
			newChordTemplate.chordName = "";
			for (int string = topString - 1; string >= 0; string--) {
				final Integer fret = newChordTemplate.frets.remove(string);
				if (fret == null) {
					continue;
				}

				newChordTemplate.frets.put(string + 1, fret);
				newChordTemplate.fingers.put(string + 1, newChordTemplate.fingers.remove(string));
			}

			final int newTemplateId = arrangement.getChordTemplateIdWithSave(newChordTemplate);
			movedChordTemplates.put(sound.chord.chordId, newTemplateId);
			sound.chord.chordId = newTemplateId;
		}
	}

	private void moveNotesDownKeepFrets() {
		final ArrayList2<Selection<ChordOrNote>> selectedSounds = selectionManager
				.<ChordOrNote>getSelectedAccessor(PositionType.GUITAR_NOTE).getSortedSelected();
		if (selectedSounds.isEmpty()) {
			return;
		}

		final int topString = data.currentStrings() - 1;
		if (containsString(selectedSounds, 0)) {
			return;
		}

		undoSystem.addUndo();

		final Map<Integer, Integer> movedChordTemplates = new HashMap<>();
		final ArrangementChart arrangement = data.getCurrentArrangement();
		final ArrayList2<ChordTemplate> chordTemplates = arrangement.chordTemplates;

		for (final Selection<ChordOrNote> selection : selectedSounds) {
			final ChordOrNote sound = selection.selectable;
			if (sound.isNote()) {
				sound.note.string--;
				continue;
			}

			if (movedChordTemplates.containsKey(sound.chord.chordId)) {
				sound.chord.chordId = movedChordTemplates.get(sound.chord.chordId);
				continue;
			}

			final ChordTemplate newChordTemplate = new ChordTemplate(chordTemplates.get(sound.chord.chordId));
			newChordTemplate.chordName = "";
			for (int string = 1; string <= topString; string++) {
				final Integer fret = newChordTemplate.frets.remove(string);
				if (fret == null) {
					continue;
				}

				newChordTemplate.frets.put(string - 1, fret);
				newChordTemplate.fingers.put(string - 1, newChordTemplate.fingers.remove(string));
			}

			final int newTemplateId = arrangement.getChordTemplateIdWithSave(newChordTemplate);
			movedChordTemplates.put(sound.chord.chordId, newTemplateId);
			sound.chord.chordId = newTemplateId;
		}
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

	private void toggleAccent() {
		this.singleToggleOnAllSelectedNotesWithBaseValue(sound -> !sound.accent,
				(sound, accent) -> sound.accent = accent);
	}

	private void toggleVibrato() {
		this.singleToggleOnAllSelectedNotesWithBaseValueNote(note -> note.vibrato == null ? 80 : null,
				(note, vibrato) -> note.vibrato = vibrato);
	}

	private void toggleTremolo() {
		this.singleToggleOnAllSelectedNotesWithBaseValue(sound -> !sound.tremolo,
				(sound, tremolo) -> sound.tremolo = tremolo);
	}

	private void toggleLinkNext() {
		this.singleToggleOnAllSelectedNotesWithBaseValue(sound -> !sound.linkNext,
				(sound, linkNext) -> sound.linkNext = linkNext);
	}

	private void openBendEditor() {
		final SelectionAccessor<ChordOrNote> selectedAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		final ChordOrNote sound = selectedAccessor.getSortedSelected().get(0).selectable;
		if (sound.length() < 10) {
			return;
		}

		if (sound.isChord()) {
			new ChordBendPane(data.songChart.beatsMap, frame, undoSystem, sound.chord,
					data.getCurrentArrangement().chordTemplates.get(sound.chord.chordId));
		} else if (sound.isNote()) {
			new NoteBendPane(data.songChart.beatsMap, frame, undoSystem, sound.note);
		}
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

	private void setFret() {
		Integer fret = null;
		do {
			final String fretString = frame.showInputDialog("Fret:", "");
			if (fretString == null || fretString.isEmpty()) {
				return;
			}
			try {
				fret = Integer.valueOf(fretString);
			} catch (final NumberFormatException e) {
			}
		} while (fret == null);

		keyboardHandler.setFret(fret);
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
			endPosition = data.songChart.beatsMap.getPositionWithAddedGrid(endPosition, 1, 8);
		} else {
			endPosition = min(handShapes.get(firstIdAfter).position() - Config.minNoteDistance,
					data.songChart.beatsMap.getPositionWithAddedGrid(endPosition, 1, 8));
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
