package log.charter.services.data;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.data.config.Config.frets;
import static log.charter.util.CollectionUtils.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.song.Arrangement;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.notes.Chord;
import log.charter.data.song.notes.ChordNote;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.components.tabs.selectionEditor.CurrentSelectionEditor;
import log.charter.services.data.selection.Selection;
import log.charter.services.data.selection.SelectionManager;
import log.charter.util.chordRecognition.ChordNameSuggester;
import log.charter.util.collections.ArrayList2;
import log.charter.util.data.IntRange;

public class GuitarSoundsHandler {
	private ChartData chartData;
	private CurrentSelectionEditor currentSelectionEditor;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	private boolean stringsInRange(final List<ChordOrNote> sounds, final IntRange stringRange) {
		return !sounds.stream()//
				.flatMap(ChordOrNote::notes)//
				.anyMatch(note -> !stringRange.inRange(note.string()));
	}

	private List<ChordOrNote> getSelectedSounds() {
		final List<Selection<ChordOrNote>> selectedSounds = selectionManager
				.<ChordOrNote>accessor(PositionType.GUITAR_NOTE).getSelected();
		if (selectedSounds.isEmpty()) {
			return new ArrayList<>();
		}

		return map(selectedSounds, selection -> selection.selectable);
	}

	private List<ChordOrNote> getSelectedSoundsWithoutStringAboveBelow(final IntRange stringRange) {
		final List<ChordOrNote> sounds = getSelectedSounds();
		if (!stringsInRange(sounds, stringRange)) {
			return new ArrayList<>();
		}

		return sounds;
	}

	private Map<Integer, Integer> getStringDifferencesEmpty(final IntRange stringRange) {
		final Map<Integer, Integer> stringDifferences = new HashMap<>();
		for (int i = stringRange.min; i <= stringRange.max; i++) {
			stringDifferences.put(i, 0);
		}

		return stringDifferences;
	}

	private Map<Integer, Integer> getStringDifferences(final Arrangement arrangement, final IntRange stringRange,
			final int stringChange) {
		final Map<Integer, Integer> stringDifferences = new HashMap<>();
		for (int i = stringRange.min; i <= stringRange.max; i++) {
			stringDifferences.put(i,
					arrangement.tuning.getStringOffset(i) - arrangement.tuning.getStringOffset(i + stringChange));
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

	private void moveStrings(final Arrangement arrangement, final Map<Integer, Integer> stringDifferences,
			final IntRange stringRange, final List<ChordOrNote> sounds, final int stringChange) {
		undoSystem.addUndo();

		final Map<Integer, Integer> movedChordTemplates = new HashMap<>();
		final List<ChordTemplate> chordTemplates = arrangement.chordTemplates;

		for (final ChordOrNote sound : sounds) {
			if (sound.isNote()) {
				final int newFret = sound.note().fret + stringDifferences.get(sound.note().string);
				if (newFret >= 0 && newFret <= Config.frets) {
					sound.note().fret = newFret;
					sound.note().string += stringChange;
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
		}

		currentSelectionEditor.selectionChanged(true);
	}

	public void moveStringsWithoutFretChange(final int stringChange) {
		final int strings = chartData.currentStrings();
		final IntRange stringRange = new IntRange(max(0, -stringChange), strings - 1 - max(0, stringChange));
		final List<ChordOrNote> sounds = getSelectedSoundsWithoutStringAboveBelow(stringRange);
		if (sounds.isEmpty()) {
			return;
		}

		final Arrangement arrangement = chartData.currentArrangement();
		final Map<Integer, Integer> stringDifferences = getStringDifferencesEmpty(stringRange);
		moveStrings(arrangement, stringDifferences, stringRange, sounds, stringChange);
	}

	public void moveStringsWithFretChange(final int stringChange) {
		final int strings = chartData.currentStrings();
		final IntRange stringRange = new IntRange(max(0, -stringChange), strings - 1 - max(0, stringChange));
		final List<ChordOrNote> sounds = getSelectedSoundsWithoutStringAboveBelow(stringRange);
		if (sounds.isEmpty()) {
			return;
		}

		final Arrangement arrangement = chartData.currentArrangement();
		final Map<Integer, Integer> stringDifferences = getStringDifferences(arrangement, stringRange, stringChange);
		moveStrings(arrangement, stringDifferences, stringRange, sounds, stringChange);
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

	private boolean validFretChange(final List<ChordOrNote> sounds, final int fretChange) {
		final IntRange fretRange = new IntRange(max(0, -fretChange), Config.frets - max(0, fretChange));

		for (final ChordOrNote sound : sounds) {
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
		final List<ChordOrNote> sounds = getSelectedSounds();
		if (sounds.isEmpty()) {
			return;
		}

		if (!validFretChange(sounds, fretChange)) {
			return;
		}

		undoSystem.addUndo();
		sounds.forEach(sound -> moveFret(sound, fretChange));
		currentSelectionEditor.selectionChanged(false);
	}

	public void setFret(final int fret) {
		final List<ChordOrNote> sounds = getSelectedSounds();
		if (sounds.isEmpty()) {
			return;
		}

		undoSystem.addUndo();

		for (final ChordOrNote sound : sounds) {
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

		currentSelectionEditor.selectionChanged(false);
	}
}
