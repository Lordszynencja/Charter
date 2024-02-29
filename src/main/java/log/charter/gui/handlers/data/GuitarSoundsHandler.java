package log.charter.gui.handlers.data;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.HashMap;
import java.util.Map;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.managers.selection.Selection;
import log.charter.data.managers.selection.SelectionAccessor;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.song.Arrangement;
import log.charter.song.ChordTemplate;
import log.charter.song.notes.Chord;
import log.charter.song.notes.ChordNote;
import log.charter.song.notes.ChordOrNote;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.chordRecognition.ChordNameSuggester;

public class GuitarSoundsHandler {

	private ChartData data;
	private CharterFrame frame;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	public void init(final ChartData data, final CharterFrame frame, final SelectionManager selectionManager,
			final UndoSystem undoSystem) {
		this.data = data;
		this.frame = frame;
		this.selectionManager = selectionManager;
		this.undoSystem = undoSystem;
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

			if (chordTemplates.get(sound.chord.templateId()).frets.get(string) != null) {
				return true;
			}
		}

		return false;
	}

	private void moveChordNotesUp(final int strings, final Map<Integer, ChordNote> chordNotes) {
		chordNotes.remove(strings - 1);
		for (int string = strings - 2; string >= 0; string--) {
			final ChordNote movedChordNote = chordNotes.remove(string);
			if (movedChordNote != null) {
				chordNotes.put(string + 1, movedChordNote);
			}
		}
	}

	private void moveChordNotesDown(final int strings, final Map<Integer, ChordNote> chordNotes) {
		chordNotes.remove(0);
		for (int string = 1; string < strings; string++) {
			final ChordNote movedChordNote = chordNotes.remove(string);
			if (movedChordNote != null) {
				chordNotes.put(string - 1, movedChordNote);
			}
		}
	}

	public void moveNotesUpKeepFrets() {
		final ArrayList2<Selection<ChordOrNote>> selectedSounds = selectionManager
				.<ChordOrNote>getSelectedAccessor(PositionType.GUITAR_NOTE).getSortedSelected();
		if (selectedSounds.isEmpty()) {
			return;
		}

		final int strings = data.currentStrings();
		if (containsString(selectedSounds, strings - 1)) {
			return;
		}

		undoSystem.addUndo();

		final Map<Integer, Integer> movedChordTemplates = new HashMap<>();
		final Arrangement arrangement = data.getCurrentArrangement();
		final ArrayList2<ChordTemplate> chordTemplates = arrangement.chordTemplates;

		for (final Selection<ChordOrNote> selection : selectedSounds) {
			final ChordOrNote sound = selection.selectable;
			if (sound.isNote()) {
				sound.note.string++;
				continue;
			}

			if (movedChordTemplates.containsKey(sound.chord.templateId())) {
				moveChordNotesUp(strings, sound.chord.chordNotes);
				final int newTemplateId = movedChordTemplates.get(sound.chord.templateId());
				final ChordTemplate chordTemplate = chordTemplates.get(newTemplateId);
				sound.chord.updateTemplate(newTemplateId, chordTemplate);
				continue;
			}

			final ChordTemplate newChordTemplate = new ChordTemplate(chordTemplates.get(sound.chord.templateId()));
			newChordTemplate.chordName = "";
			for (int string = strings - 2; string >= 0; string--) {
				final Integer fret = newChordTemplate.frets.remove(string);
				if (fret == null) {
					continue;
				}

				newChordTemplate.frets.put(string + 1, fret);
				newChordTemplate.fingers.put(string + 1, newChordTemplate.fingers.remove(string));
			}

			moveChordNotesUp(strings, sound.chord.chordNotes);
			final int newTemplateId = arrangement.getChordTemplateIdWithSave(newChordTemplate);
			movedChordTemplates.put(sound.chord.templateId(), newTemplateId);
			sound.chord.updateTemplate(newTemplateId, newChordTemplate);
		}

		frame.selectionChanged(true);
	}

	public void moveNotesDownKeepFrets() {
		final ArrayList2<Selection<ChordOrNote>> selectedSounds = selectionManager
				.<ChordOrNote>getSelectedAccessor(PositionType.GUITAR_NOTE).getSortedSelected();
		if (selectedSounds.isEmpty()) {
			return;
		}

		if (containsString(selectedSounds, 0)) {
			return;
		}

		undoSystem.addUndo();

		final int strings = data.currentStrings();
		final Map<Integer, Integer> movedChordTemplates = new HashMap<>();
		final Arrangement arrangement = data.getCurrentArrangement();
		final ArrayList2<ChordTemplate> chordTemplates = arrangement.chordTemplates;

		for (final Selection<ChordOrNote> selection : selectedSounds) {
			final ChordOrNote sound = selection.selectable;
			if (sound.isNote()) {
				sound.note.string--;
				continue;
			}

			if (movedChordTemplates.containsKey(sound.chord.templateId())) {
				moveChordNotesDown(strings, sound.chord.chordNotes);
				final int newTemplateId = movedChordTemplates.get(sound.chord.templateId());
				final ChordTemplate chordTemplate = chordTemplates.get(newTemplateId);
				sound.chord.updateTemplate(newTemplateId, chordTemplate);
				continue;
			}

			final ChordTemplate newChordTemplate = new ChordTemplate(chordTemplates.get(sound.chord.templateId()));
			newChordTemplate.chordName = "";
			for (int string = 1; string < strings; string++) {
				final Integer fret = newChordTemplate.frets.remove(string);
				if (fret == null) {
					continue;
				}

				newChordTemplate.frets.put(string - 1, fret);
				newChordTemplate.fingers.put(string - 1, newChordTemplate.fingers.remove(string));
			}

			moveChordNotesDown(strings, sound.chord.chordNotes);
			final int newTemplateId = arrangement.getChordTemplateIdWithSave(newChordTemplate);
			movedChordTemplates.put(sound.chord.templateId(), newTemplateId);
			sound.chord.updateTemplate(newTemplateId, newChordTemplate);
		}

		frame.selectionChanged(true);
	}

	public void moveNotesUp() {
		final ArrayList2<Selection<ChordOrNote>> selectedSounds = selectionManager
				.<ChordOrNote>getSelectedAccessor(PositionType.GUITAR_NOTE).getSortedSelected();
		if (selectedSounds.isEmpty()) {
			return;
		}

		final int strings = data.currentStrings();
		if (containsString(selectedSounds, strings - 1)) {
			return;
		}

		final Arrangement arrangement = data.getCurrentArrangement();
		final Map<Integer, Integer> stringDifferences = new HashMap<>();
		for (int i = 0; i < strings; i++) {
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

			if (movedChordTemplates.containsKey(sound.chord.templateId())) {
				moveChordNotesUp(strings, sound.chord.chordNotes);
				final int newTemplateId = movedChordTemplates.get(sound.chord.templateId());
				final ChordTemplate chordTemplate = chordTemplates.get(newTemplateId);
				sound.chord.updateTemplate(newTemplateId, chordTemplate);
				continue;
			}

			final ChordTemplate newChordTemplate = new ChordTemplate(chordTemplates.get(sound.chord.templateId()));
			newChordTemplate.chordName = "";
			boolean wrongFret = false;
			for (int string = strings - 2; string >= 0; string--) {
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

			moveChordNotesUp(strings, sound.chord.chordNotes);
			final int newTemplateId = arrangement.getChordTemplateIdWithSave(newChordTemplate);
			movedChordTemplates.put(sound.chord.templateId(), newTemplateId);
			sound.chord.updateTemplate(newTemplateId, newChordTemplate);
		}

		frame.selectionChanged(true);
	}

	public void moveNotesDown() {
		final ArrayList2<Selection<ChordOrNote>> selectedSounds = selectionManager
				.<ChordOrNote>getSelectedAccessor(PositionType.GUITAR_NOTE).getSortedSelected();
		if (selectedSounds.isEmpty()) {
			return;
		}

		if (containsString(selectedSounds, 0)) {
			return;
		}

		final int strings = data.currentStrings();
		final Arrangement arrangement = data.getCurrentArrangement();
		final Map<Integer, Integer> stringDifferences = new HashMap<>();
		for (int i = 1; i < strings; i++) {
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

			if (movedChordTemplates.containsKey(sound.chord.templateId())) {
				moveChordNotesDown(strings, sound.chord.chordNotes);
				final int newTemplateId = movedChordTemplates.get(sound.chord.templateId());
				final ChordTemplate chordTemplate = chordTemplates.get(newTemplateId);
				sound.chord.updateTemplate(newTemplateId, chordTemplate);

				continue;
			}

			final ChordTemplate newChordTemplate = new ChordTemplate(chordTemplates.get(sound.chord.templateId()));
			newChordTemplate.chordName = "";
			boolean wrongFret = false;
			for (int string = 1; string < strings; string++) {
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

			moveChordNotesDown(strings, sound.chord.chordNotes);
			final int newTemplateId = arrangement.getChordTemplateIdWithSave(newChordTemplate);
			movedChordTemplates.put(sound.chord.templateId(), newTemplateId);
			sound.chord.updateTemplate(newTemplateId, newChordTemplate);
		}

		frame.selectionChanged(true);
	}

	private void setChordName(final ChordTemplate template) {
		if (template.chordName == null || template.chordName.isBlank()) {
			return;
		}

		final ArrayList2<String> suggestedNames = ChordNameSuggester
				.suggestChordNames(data.getCurrentArrangement().tuning, template.frets);

		if (!suggestedNames.isEmpty()) {
			template.chordName = suggestedNames.get(0);
		} else {
			template.chordName = "";
		}
	}

	public void setFret(final int fret) {
		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		if (!selectionAccessor.isSelected()) {
			return;
		}

		undoSystem.addUndo();

		final ArrayList2<Selection<ChordOrNote>> selected = selectionAccessor.getSortedSelected();
		for (final Selection<ChordOrNote> selection : selected) {
			if (selection.selectable.isChord()) {
				final Chord chord = selection.selectable.chord;
				final ChordTemplate newTemplate = new ChordTemplate(
						data.getCurrentArrangement().chordTemplates.get(chord.templateId()));
				int fretChange = 0;
				for (int i = 0; i < data.currentStrings(); i++) {
					if (newTemplate.frets.get(i) != null) {
						fretChange = fret - newTemplate.frets.get(i);
						break;
					}
				}
				if (fretChange == 0) {
					continue;
				}

				for (final int string : newTemplate.frets.keySet()) {
					final int oldFret = newTemplate.frets.get(string);
					final int newFret = max(0, min(Config.frets, oldFret + fretChange));

					newTemplate.frets.put(string, newFret);
					if (newFret == 0) {
						newTemplate.fingers.remove(string);
					}
				}

				setChordName(newTemplate);
				final int newTemplateId = data.getCurrentArrangement().getChordTemplateIdWithSave(newTemplate);
				chord.updateTemplate(newTemplateId, newTemplate);
			} else {
				selection.selectable.note.fret = fret;
			}
		}

		frame.selectionChanged(false);
	}
}
