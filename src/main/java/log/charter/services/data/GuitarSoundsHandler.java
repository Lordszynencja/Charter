package log.charter.services.data;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.data.config.Config.frets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.song.Arrangement;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.configs.Tuning;
import log.charter.data.song.notes.Chord;
import log.charter.data.song.notes.ChordNote;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.notes.Note;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.components.tabs.selectionEditor.CurrentSelectionEditor;
import log.charter.services.data.selection.Selection;
import log.charter.services.data.selection.SelectionManager;
import log.charter.util.chordRecognition.ChordNameSuggester;
import log.charter.util.collections.ArrayList2;
import log.charter.util.collections.Pair;
import log.charter.util.data.IntRange;

public class GuitarSoundsHandler {
	private ChartData chartData;
	private CurrentSelectionEditor currentSelectionEditor;
	private GuitarSoundsStatusesHandler guitarSoundsStatusesHandler;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	private boolean stringsInRange(final List<Selection<ChordOrNote>> selected, final IntRange stringRange) {
		return !selected.stream()//
				.flatMap(selection -> selection.selectable.notes())//
				.anyMatch(note -> !stringRange.inRange(note.string()));
	}

	private List<Selection<ChordOrNote>> getSelectedSoundsWithoutStringAboveBelow(final IntRange stringRange) {
		final List<Selection<ChordOrNote>> selected = selectionManager.<ChordOrNote>accessor(PositionType.GUITAR_NOTE)
				.getSelected();
		if (!stringsInRange(selected, stringRange)) {
			return new ArrayList<>();
		}

		return selected;
	}

	private Map<Integer, Integer> getStringDifferencesEmpty(final IntRange stringRange) {
		final Map<Integer, Integer> stringDifferences = new HashMap<>();
		for (int i = stringRange.min; i <= stringRange.max; i++) {
			stringDifferences.put(i, 0);
		}

		return stringDifferences;
	}

	private Map<Integer, Integer> getStringDifferences(final Tuning tuning, final IntRange stringRange,
			final int stringChange) {
		final Map<Integer, Integer> stringDifferences = new HashMap<>();
		for (int i = stringRange.min; i <= stringRange.max; i++) {
			stringDifferences.put(i, tuning.getStringOffset(i) - tuning.getStringOffset(i + stringChange));
		}

		return stringDifferences;
	}

	private void moveChordStrings(final IntRange stringRange, final Map<Integer, ChordNote> chordNotes,
			final int stringChange) {
		final Map<Integer, ChordNote> temporaryChordNotes = new HashMap<>();
		for (int string = stringRange.min; string <= stringRange.max; string++) {
			final ChordNote movedChordNote = chordNotes.get(string);
			if (movedChordNote != null) {
				final int newString = string + stringChange;
				temporaryChordNotes.put(newString, movedChordNote);
			}
		}

		chordNotes.clear();
		chordNotes.putAll(temporaryChordNotes);
	}

	private ChordTemplate moveChordTemplateStrings(final IntRange stringRange,
			final Map<Integer, Integer> stringDifferences, final ChordTemplate chordTemplate, final int stringChange) {
		final ChordTemplate newChordTemplate = new ChordTemplate();
		newChordTemplate.chordName = "";
		newChordTemplate.arpeggio = chordTemplate.arpeggio;

		for (int string = stringRange.min; string <= stringRange.max; string++) {
			Integer fret = chordTemplate.frets.get(string);
			if (fret == null) {
				continue;
			}

			fret = fret + stringDifferences.get(string);
			if (fret < 0 || fret > Config.frets) {
				return null;
			}
			final int newString = string + stringChange;

			newChordTemplate.frets.put(newString, fret);
			newChordTemplate.fingers.put(newString, chordTemplate.fingers.get(string));
		}

		return newChordTemplate;
	}

	private void moveStrings(final Map<Integer, Integer> stringDifferences, final IntRange stringRange,
			final List<Selection<ChordOrNote>> selected, final int stringChange) {
		undoSystem.addUndo();

		final Arrangement arrangement = chartData.currentArrangement();
		final Map<Integer, Integer> movedChordTemplates = new HashMap<>();
		final List<ChordTemplate> chordTemplates = arrangement.chordTemplates;
		final List<ChordOrNote> sounds = chartData.currentSounds();
		final Set<Integer> idsDone = new HashSet<>();

		for (final Selection<ChordOrNote> selection : selected) {
			if (idsDone.contains(selection.id)) {
				continue;
			}

			final ChordOrNote sound = selection.selectable;
			if (sound.isNote()) {
				final int newFret = sound.note().fret + stringDifferences.get(sound.note().string);
				if (newFret >= 0 && newFret <= Config.frets) {
					Note currentNote = sound.note();
					currentNote.fret = newFret;

					final int stringFrom = currentNote.string;
					int id = selection.id;
					while (currentNote != null) {
						currentNote.string += stringChange;
						idsDone.add(id);

						if (!currentNote.linkNext()) {
							break;
						}

						final Pair<Integer, ChordOrNote> nextSoundWithId = ChordOrNote
								.findNextSoundWithIdOnString(stringFrom, id + 1, sounds);
						if (nextSoundWithId == null || nextSoundWithId.b.isChord()) {
							break;
						}

						currentNote = nextSoundWithId.b.note();
						id = nextSoundWithId.a;
					}
				}

				continue;
			}

			if (movedChordTemplates.containsKey(sound.chord().templateId())) {
				moveChordStrings(stringRange, sound.chord().chordNotes, stringChange);
				final int newTemplateId = movedChordTemplates.get(sound.chord().templateId());
				final ChordTemplate chordTemplate = chordTemplates.get(newTemplateId);
				sound.chord().updateTemplate(newTemplateId, chordTemplate);
				continue;
			}

			final ChordTemplate oldTemplate = chordTemplates.get(sound.chord().templateId());
			final ChordTemplate newChordTemplate = moveChordTemplateStrings(stringRange, stringDifferences, oldTemplate,
					stringChange);
			if (newChordTemplate == null) {
				continue;
			}

			moveChordStrings(stringRange, sound.chord().chordNotes, stringChange);
			final int newTemplateId = arrangement.getChordTemplateIdWithSave(newChordTemplate);
			movedChordTemplates.put(sound.chord().templateId(), newTemplateId);
			sound.chord().updateTemplate(newTemplateId, newChordTemplate);
			idsDone.add(selection.id);
		}

		guitarSoundsStatusesHandler
				.updateLinkedNotes(selected.stream().map(s -> s.id).collect(Collectors.toCollection(ArrayList::new)));

		currentSelectionEditor.selectionChanged(true);
	}

	public void moveStringsWithoutFretChange(final int stringChange) {
		final int strings = chartData.currentStrings();
		final IntRange stringRange = new IntRange(max(0, -stringChange), strings - 1 - max(0, stringChange));
		final List<Selection<ChordOrNote>> selected = getSelectedSoundsWithoutStringAboveBelow(stringRange);
		if (selected.isEmpty()) {
			return;
		}

		final Map<Integer, Integer> stringDifferences = getStringDifferencesEmpty(stringRange);
		moveStrings(stringDifferences, stringRange, selected, stringChange);
	}

	public void moveStringsWithFretChange(final int stringChange) {
		final int strings = chartData.currentStrings();
		final IntRange stringRange = new IntRange(max(0, -stringChange), strings - 1 - max(0, stringChange));
		final List<Selection<ChordOrNote>> selected = getSelectedSoundsWithoutStringAboveBelow(stringRange);
		if (selected.isEmpty()) {
			return;
		}

		final Map<Integer, Integer> stringDifferences = getStringDifferences(chartData.currentArrangement().tuning,
				stringRange, stringChange);
		moveStrings(stringDifferences, stringRange, selected, stringChange);
	}

	private void setChordName(final ChordTemplate template) {
		if (template.chordName == null || template.chordName.isBlank()) {
			return;
		}

		final ArrayList2<String> suggestedNames = ChordNameSuggester
				.suggestChordNames(chartData.currentArrangement().tuning, template.frets);

		if (!suggestedNames.isEmpty()) {
			template.chordName = suggestedNames.get(0);
		} else {
			template.chordName = "";
		}
	}

	private boolean validFretChange(final List<Selection<ChordOrNote>> selected, final int fretChange) {
		final IntRange fretRange = new IntRange(max(0, -fretChange), Config.frets - max(0, fretChange));

		for (final Selection<ChordOrNote> selection : selected) {
			final ChordOrNote sound = selection.selectable;
			if (sound.isNote()) {
				if (!fretRange.inRange(sound.note().fret)) {
					return false;
				}
				continue;
			}

			final Chord chord = sound.chord();
			final ChordTemplate template = chartData.currentArrangement().chordTemplates.get(chord.templateId());
			for (final int fret : template.frets.values()) {
				if (!fretRange.inRange(fret)) {
					return false;
				}
			}
		}

		return true;
	}

	private ChordTemplate moveTemplateFrets(final ChordTemplate template, final int fretChange) {
		final ChordTemplate newTemplate = new ChordTemplate();

		newTemplate.fingers.putAll(template.fingers);
		for (final Entry<Integer, Integer> stringFret : template.frets.entrySet()) {
			final int string = stringFret.getKey();
			final int oldFret = stringFret.getValue();
			final int newFret = max(0, min(Config.frets, oldFret + fretChange));

			newTemplate.frets.put(string, newFret);
			if (newFret == 0) {
				newTemplate.fingers.remove(string);
			}
		}

		setChordName(newTemplate);

		return newTemplate;
	}

	private void moveFret(final ChordOrNote sound, final int fretChange) {
		if (sound.isNote()) {
			sound.note().fret += fretChange;
			return;
		}

		final Chord chord = sound.chord();
		final ChordTemplate oldTemplate = chartData.currentArrangement().chordTemplates.get(chord.templateId());
		final ChordTemplate newTemplate = moveTemplateFrets(oldTemplate, fretChange);

		final int newTemplateId = chartData.currentArrangement().getChordTemplateIdWithSave(newTemplate);
		chord.updateTemplate(newTemplateId, newTemplate);
	}

	public void moveFret(final int fretChange) {
		final List<Selection<ChordOrNote>> selected = selectionManager.<ChordOrNote>accessor(PositionType.GUITAR_NOTE)
				.getSelected();
		if (selected.isEmpty()) {
			return;
		}

		if (!validFretChange(selected, fretChange)) {
			return;
		}

		undoSystem.addUndo();
		selected.forEach(selection -> moveFret(selection.selectable, fretChange));
		guitarSoundsStatusesHandler
				.updateLinkedNotes(selected.stream().map(s -> s.id).collect(Collectors.toCollection(ArrayList::new)));

		currentSelectionEditor.selectionChanged(false);
	}

	public void setFret(final int fret) {
		final List<Selection<ChordOrNote>> selected = selectionManager.<ChordOrNote>accessor(PositionType.GUITAR_NOTE)
				.getSelected();
		if (selected.isEmpty()) {
			return;
		}

		undoSystem.addUndo();

		for (final Selection<ChordOrNote> selection : selected) {
			final ChordOrNote sound = selection.selectable;
			if (sound.isNote()) {
				sound.note().fret = fret;
				continue;
			}

			final Chord chord = sound.chord();
			final ChordTemplate oldTemplate = chartData.currentArrangement().chordTemplates.get(chord.templateId());
			final int fretChange = fret - oldTemplate.getLowestFret();
			if (fretChange == 0 || oldTemplate.getHighestFret() + fretChange > frets) {
				continue;
			}

			final ChordTemplate newTemplate = moveTemplateFrets(oldTemplate, fretChange);

			final int newTemplateId = chartData.currentArrangement().getChordTemplateIdWithSave(newTemplate);
			chord.updateTemplate(newTemplateId, newTemplate);
		}

		guitarSoundsStatusesHandler
				.updateLinkedNotes(selected.stream().map(s -> s.id).collect(Collectors.toCollection(ArrayList::new)));

		currentSelectionEditor.selectionChanged(false);
	}
}
