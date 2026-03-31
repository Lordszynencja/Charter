package log.charter.services.data;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.util.CollectionUtils.firstAfterEqual;
import static log.charter.util.CollectionUtils.lastBeforeEqual;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import log.charter.data.ChartData;
import log.charter.data.ChordTemplateFingerSetter;
import log.charter.data.config.values.InstrumentConfig;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.FHP;
import log.charter.data.song.HandShape;
import log.charter.data.song.notes.Chord;
import log.charter.data.song.notes.ChordNote;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.notes.Note;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.components.tabs.chordEditor.ChordTemplatesEditorTab;
import log.charter.gui.components.tabs.selectionEditor.CurrentSelectionEditor;
import log.charter.services.data.selection.Selection;
import log.charter.services.data.selection.SelectionManager;
import log.charter.util.CollectionUtils;
import log.charter.util.chordRecognition.ChordNameSuggester;
import log.charter.util.data.IntRange;

public class GuitarSoundsHandler {
	private ChartData chartData;
	private ChordTemplatesEditorTab chordTemplatesEditorTab;
	private CurrentSelectionEditor currentSelectionEditor;
	private GuitarSoundsStatusesHandler guitarSoundsStatusesHandler;
	private SelectionManager selectionManager;
	private StringsChanger stringsChanger;
	private UndoSystem undoSystem;

	private boolean soundsStringsInRange(final List<Selection<ChordOrNote>> selected, final IntRange stringRange) {
		return !selected.stream()//
				.flatMap(selection -> selection.selectable.notes())//
				.anyMatch(note -> !stringRange.inRange(note.string()));
	}

	private List<Selection<ChordOrNote>> getSelectedSoundsWithoutStringAboveBelow(final IntRange stringRange) {
		final List<Selection<ChordOrNote>> selected = selectionManager.<ChordOrNote>accessor(PositionType.GUITAR_NOTE)
				.getSelected();
		if (!soundsStringsInRange(selected, stringRange)) {
			return new ArrayList<>();
		}

		return selected;
	}

	private boolean handShapesStringsInRange(final List<Selection<HandShape>> selected, final IntRange stringRange) {
		return !selected.stream()//
				.flatMap(selection -> chartData.currentChordTemplates().get(selection.selectable.templateId).frets
						.keySet().stream())//
				.anyMatch(string -> !stringRange.inRange(string));
	}

	private List<Selection<HandShape>> getSelectedHandShapesWithoutStringAboveBelow(final IntRange stringRange) {
		final List<Selection<HandShape>> selected = selectionManager.<HandShape>accessor(PositionType.HAND_SHAPE)
				.getSelected();
		if (!handShapesStringsInRange(selected, stringRange)) {
			return new ArrayList<>();
		}

		return selected;
	}

	public void moveStringsWithoutFretChange(final int stringChange) {
		final int strings = chartData.currentStrings();
		final IntRange stringRange = new IntRange(max(0, -stringChange), strings - 1 - max(0, stringChange));
		final List<Selection<ChordOrNote>> selectedSounds = getSelectedSoundsWithoutStringAboveBelow(stringRange);
		if (!selectedSounds.isEmpty()) {
			stringsChanger.new Action(chartData.currentArrangement().tuning, stringRange, stringChange, false)
					.moveStrings(selectedSounds);
			return;
		}

		final List<Selection<HandShape>> selectedHandShapes = getSelectedHandShapesWithoutStringAboveBelow(stringRange);
		if (!selectedHandShapes.isEmpty()) {
			stringsChanger.new Action(chartData.currentArrangement().tuning, stringRange, stringChange, false)
					.moveHandShapesStrings(selectedHandShapes);
		}
	}

	public void moveStringsWithFretChange(final int stringChange) {
		final int strings = chartData.currentStrings();
		final IntRange stringRange = new IntRange(max(0, -stringChange), strings - 1 - max(0, stringChange));
		final List<Selection<ChordOrNote>> selectedSounds = getSelectedSoundsWithoutStringAboveBelow(stringRange);
		if (!selectedSounds.isEmpty()) {
			stringsChanger.new Action(chartData.currentArrangement().tuning, stringRange, stringChange, true)
					.moveStrings(selectedSounds);
			return;
		}

		final List<Selection<HandShape>> selectedHandShapes = getSelectedHandShapesWithoutStringAboveBelow(stringRange);
		if (!selectedHandShapes.isEmpty()) {
			stringsChanger.new Action(chartData.currentArrangement().tuning, stringRange, stringChange, true)
					.moveHandShapesStrings(selectedHandShapes);
		}
	}

	private void updateChordName(final ChordTemplate oldTemplate, final ChordTemplate template, final int change) {
		if (change % 12 == 0) {
			template.chordName = oldTemplate.chordName;
			return;
		}

		if (oldTemplate.chordName == null || oldTemplate.chordName.isBlank()) {
			return;
		}

		final String toneNameRegex = "([A-G](#|b)?){1}";
		final String regex = toneNameRegex + "([^/]*)?(/" + toneNameRegex + ")?";

		final Matcher matcher = Pattern.compile(regex).matcher(oldTemplate.chordName);
		if (!matcher.find()) {
			return;
		}

		final String tone = matcher.group(1);
		final String suffixes = matcher.group(3);
		final String slashTone = matcher.group(5);

		final StringBuilder b = new StringBuilder(ChordNameSuggester.changeToneName(tone, change));
		if (suffixes != null) {
			b.append(suffixes);
		}
		if (slashTone != null) {
			b.append("/" + ChordNameSuggester.changeToneName(slashTone, change));
		}

		template.chordName = b.toString();
	}

	private void setChordName(final ChordTemplate template) {
		final List<String> suggestedNames = ChordNameSuggester.suggestChordNames(chartData.currentArrangement().tuning,
				template.frets);
		if (!suggestedNames.isEmpty()) {
			template.chordName = suggestedNames.get(0);
		}
	}

	private boolean validFHPsFretChange(final List<Selection<FHP>> selected, final int fretChange) {
		final IntRange fretRange = new IntRange(max(1, -fretChange + 1), InstrumentConfig.frets - max(0, fretChange));

		for (final Selection<FHP> selection : selected) {
			final FHP fhp = selection.selectable;
			if (!fretRange.inRange(fhp.fret) || !fretRange.inRange(fhp.topFret())) {
				return false;
			}
		}

		return true;
	}

	private boolean validSoundsFretChange(final List<Selection<ChordOrNote>> selected, final int fretChange) {
		final IntRange fretRange = new IntRange(max(0, -fretChange), InstrumentConfig.frets - max(0, fretChange));

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

	private boolean validHandShapesFretChange(final List<Selection<HandShape>> selected, final int fretChange) {
		final IntRange fretRange = new IntRange(max(0, -fretChange), InstrumentConfig.frets - max(0, fretChange));

		for (final Selection<HandShape> selection : selected) {
			final HandShape handShape = selection.selectable;

			final ChordTemplate template = chartData.currentArrangement().chordTemplates.get(handShape.templateId);
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
			final int newFret = max(0, min(InstrumentConfig.frets, oldFret + fretChange));

			newTemplate.frets.put(string, newFret);
		}

		updateChordName(template, newTemplate, fretChange);
		ChordTemplateFingerSetter.setSuggestedFingers(newTemplate);

		return newTemplate;
	}

	private ChordTemplate moveTemplateFretsSimple(final ChordTemplate template, final int fret,
			final Set<Integer> editedStrings) {
		final ChordTemplate newTemplate = new ChordTemplate();

		for (final Entry<Integer, Integer> stringFret : template.frets.entrySet()) {
			final int string = stringFret.getKey();
			final int newFret = editedStrings.contains(string) ? fret : stringFret.getValue();
			newTemplate.frets.put(string, newFret);
		}

		if (template.frets.size() > 2) {
			setChordName(newTemplate);
		}
		ChordTemplateFingerSetter.setSuggestedFingers(newTemplate);

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
		chordTemplatesEditorTab.refreshTemplates();
	}

	private void moveFret(final HandShape handShape, final int fretChange) {
		final ChordTemplate oldTemplate = chartData.currentArrangement().chordTemplates.get(handShape.templateId);
		final ChordTemplate newTemplate = moveTemplateFrets(oldTemplate, fretChange);

		final int newTemplateId = chartData.currentArrangement().getChordTemplateIdWithSave(newTemplate);
		handShape.templateId = newTemplateId;
		chordTemplatesEditorTab.refreshTemplates();
	}

	public void moveFHPsFret(final List<Selection<FHP>> selected, final int fretChange) {
		if (!validFHPsFretChange(selected, fretChange)) {
			return;
		}

		undoSystem.addUndo();

		selected.forEach(selection -> selection.selectable.fret += fretChange);

		currentSelectionEditor.selectionChanged(false);
	}

	public void moveSoundsFret(final List<Selection<ChordOrNote>> selected, final int fretChange) {
		if (!validSoundsFretChange(selected, fretChange)) {
			return;
		}

		undoSystem.addUndo();

		selected.forEach(selection -> moveFret(selection.selectable, fretChange));
		guitarSoundsStatusesHandler
				.updateLinkedNotes(selected.stream().map(s -> s.id).collect(Collectors.toCollection(ArrayList::new)));

		currentSelectionEditor.selectionChanged(false);
	}

	public void moveHandShapesFret(final List<Selection<HandShape>> selected, final int fretChange) {
		if (!validHandShapesFretChange(selected, fretChange)) {
			return;
		}

		undoSystem.addUndo();

		selected.forEach(selection -> moveFret(selection.selectable, fretChange));
		guitarSoundsStatusesHandler
				.updateLinkedNotes(selected.stream().map(s -> s.id).collect(Collectors.toCollection(ArrayList::new)));

		currentSelectionEditor.selectionChanged(false);
	}

	public void moveFret(final int fretChange) {
		final List<Selection<FHP>> selectedFHPs = selectionManager.<FHP>accessor(PositionType.FHP).getSelected();
		if (!selectedFHPs.isEmpty()) {
			moveFHPsFret(selectedFHPs, fretChange);
			return;
		}
		final List<Selection<ChordOrNote>> selectedSounds = selectionManager
				.<ChordOrNote>accessor(PositionType.GUITAR_NOTE).getSelected();
		if (!selectedSounds.isEmpty()) {
			moveSoundsFret(selectedSounds, fretChange);
			return;
		}

		final List<Selection<HandShape>> selectedHandShapes = selectionManager
				.<HandShape>accessor(PositionType.HAND_SHAPE).getSelected();
		if (!selectedHandShapes.isEmpty()) {
			moveHandShapesFret(selectedHandShapes, fretChange);
			return;
		}
	}

	private void setFretForFHPs(final int fret) {
		final List<Selection<FHP>> selected = selectionManager.getSelected(PositionType.FHP);
		if (selected.isEmpty()) {
			return;
		}

		undoSystem.addUndo();

		for (final Selection<FHP> fhpSelection : selected) {
			fhpSelection.selectable.fret = fret;
		}
	}

	private void setSecondFretForFHPs(final int fret) {
		final List<Selection<FHP>> selected = selectionManager.getSelected(PositionType.FHP);
		if (selected.isEmpty()) {
			return;
		}

		undoSystem.addUndo();

		for (final Selection<FHP> fhpSelection : selected) {
			if (fret >= fhpSelection.selectable.fret + 3) {
				fhpSelection.selectable.width = fret - fhpSelection.selectable.fret + 1;
			}
		}
	}

	private void setFretForSounds(final int fret) {
		final List<Selection<ChordOrNote>> selected = selectionManager.getSelected(PositionType.GUITAR_NOTE);
		if (selected.isEmpty()) {
			return;
		}

		final Set<Integer> editedStrings = currentSelectionEditor.getEditedStrings();
		final boolean setFretsSimple = !IntStream.range(0, chartData.currentStrings())
				.allMatch(editedStrings::contains);

		undoSystem.addUndo();

		for (final Selection<ChordOrNote> selection : selected) {
			final ChordOrNote sound = selection.selectable;
			if (sound.isNote()) {
				if (editedStrings.contains(sound.note().string)) {
					sound.note().fret = fret;
				}
				continue;
			}

			final Chord chord = sound.chord();
			final ChordTemplate oldTemplate = chartData.currentArrangement().chordTemplates.get(chord.templateId());
			final ChordTemplate newTemplate;
			if (setFretsSimple) {
				newTemplate = moveTemplateFretsSimple(oldTemplate, fret, editedStrings);
			} else {
				final int fretChange = fret - oldTemplate.getLowestFret();
				if (fretChange == 0 || oldTemplate.getHighestFret() + fretChange > InstrumentConfig.frets) {
					continue;
				}

				newTemplate = moveTemplateFrets(oldTemplate, fretChange);
			}

			final int newTemplateId = chartData.currentArrangement().getChordTemplateIdWithSave(newTemplate);
			chord.updateTemplate(newTemplateId, newTemplate);
		}

		guitarSoundsStatusesHandler
				.updateLinkedNotes(selected.stream().map(s -> s.id).collect(Collectors.toCollection(ArrayList::new)));

		currentSelectionEditor.selectionChanged(false);
		chordTemplatesEditorTab.refreshTemplates();
	}

	private void setSlideFretForNote(final Set<Integer> editedStrings, final Note note, final int fret) {
		if (!editedStrings.contains(note.string)) {
			return;
		}

		if (note.fret == fret) {
			note.unpitchedSlide = false;
			note.slideTo = null;
		} else {
			if (note.slideTo == null) {
				note.unpitchedSlide = !note.linkNext;
			}
			note.slideTo = fret;
		}
	}

	private void setSlideFretForChord(final Set<Integer> editedStrings, final Chord chord,
			final ChordTemplate chordTemplate, final int fret) {
		final int minFret = chordTemplate.frets.entrySet().stream()//
				.filter(f -> editedStrings.contains(f.getKey()) && f.getValue() > 0)//
				.map(f -> f.getValue())//
				.collect(Collectors.minBy(Integer::compare)).orElse(1);
		final int fretDifference = minFret - fret;

		for (final Entry<Integer, ChordNote> chordNoteEntry : chord.chordNotes.entrySet()) {
			if (!editedStrings.contains(chordNoteEntry.getKey())) {
				continue;
			}

			final ChordNote chordNote = chordNoteEntry.getValue();
			final int noteFret = chordTemplate.frets.get(chordNoteEntry.getKey());
			final int slideTo = min(InstrumentConfig.frets, max(1, noteFret - fretDifference));
			if (noteFret == 0 || slideTo == noteFret) {
				chordNote.slideTo = null;
				chordNote.unpitchedSlide = false;
				continue;
			}

			chordNote.unpitchedSlide = !chordNote.linkNext;
			chordNote.slideTo = slideTo;
		}
	}

	private void setSlideFret(final int newSlideFret) {
		final List<Selection<ChordOrNote>> selected = selectionManager.getSelected(PositionType.GUITAR_NOTE);
		if (selected.isEmpty()) {
			return;
		}

		final Set<Integer> editedStrings = currentSelectionEditor.getEditedStrings();

		undoSystem.addUndo();

		final List<ChordTemplate> chordTemplates = chartData.currentArrangement().chordTemplates;

		for (final Selection<ChordOrNote> selection : selected) {
			if (selection.selectable.isNote()) {
				setSlideFretForNote(editedStrings, selection.selectable.note(), newSlideFret);
				continue;
			}

			final Chord chord = selection.selectable.chord();
			final ChordTemplate chordTemplate = chordTemplates.get(chord.templateId());
			setSlideFretForChord(editedStrings, chord, chordTemplate, newSlideFret);
		}

		guitarSoundsStatusesHandler
				.updateLinkedNotes(selected.stream().map(s -> s.id).collect(Collectors.toCollection(ArrayList::new)));

		currentSelectionEditor.selectionChanged(false);
	}

	private void setFretForHandShapes(final int fret) {
		final List<Selection<HandShape>> selected = selectionManager.getSelected(PositionType.HAND_SHAPE);
		if (selected.isEmpty()) {
			return;
		}

		final Set<Integer> editedStrings = currentSelectionEditor.getEditedStrings();
		final boolean setFretsSimple = !IntStream.range(0, chartData.currentStrings())
				.allMatch(editedStrings::contains);

		undoSystem.addUndo();

		for (final Selection<HandShape> selection : selected) {
			final HandShape handShape = selection.selectable;

			final ChordTemplate oldTemplate = chartData.currentArrangement().chordTemplates.get(handShape.templateId);
			final ChordTemplate newTemplate;
			if (setFretsSimple) {
				newTemplate = moveTemplateFretsSimple(oldTemplate, fret, editedStrings);
			} else {
				final int fretChange = fret - oldTemplate.getLowestFret();
				if (fretChange == 0 || oldTemplate.getHighestFret() + fretChange > InstrumentConfig.frets) {
					continue;
				}

				newTemplate = moveTemplateFrets(oldTemplate, fretChange);
			}

			final int newTemplateId = chartData.currentArrangement().getChordTemplateIdWithSave(newTemplate);
			handShape.templateId = newTemplateId;
		}

		currentSelectionEditor.selectionChanged(false);
		chordTemplatesEditorTab.refreshTemplates();
	}

	public void setFret(final int fret, final int typingNumber) {
		switch (selectionManager.selectedType()) {
			case FHP:
				if (typingNumber % 2 == 0) {
					setFretForFHPs(fret);
				} else {
					setSecondFretForFHPs(fret);
				}
				break;
			case GUITAR_NOTE:
				if (typingNumber % 2 == 0) {
					setFretForSounds(fret);
				} else {
					setSlideFret(fret);
				}
				break;
			case HAND_SHAPE:
				setFretForHandShapes(fret);
				break;
			default:
				break;
		}
	}

	private ChordTemplate setFingerOnTemplate(final Set<Integer> editedStrings, final ChordTemplate template,
			final int finger) {
		final ChordTemplate newTemplate = new ChordTemplate(template);

		for (final Entry<Integer, Integer> fret : template.frets.entrySet()) {
			if (!editedStrings.contains(fret.getKey()) || fret.getValue() == null || fret.getValue() == 0) {
				continue;
			}

			newTemplate.fingers.put(fret.getKey(), finger);
		}

		return newTemplate;
	}

	private void setFingerOnSounds(final int finger) {
		final List<Selection<ChordOrNote>> selected = selectionManager.getSelected(PositionType.GUITAR_NOTE);
		if (selected.isEmpty()) {
			return;
		}

		final Set<Integer> editedStrings = currentSelectionEditor.getEditedStrings();
		undoSystem.addUndo();

		for (final Selection<ChordOrNote> selection : selected) {
			final ChordOrNote sound = selection.selectable;
			if (sound.isNote()) {
				continue;
			}

			final Chord chord = sound.chord();
			final ChordTemplate oldTemplate = chartData.currentArrangement().chordTemplates.get(chord.templateId());
			final ChordTemplate newTemplate = setFingerOnTemplate(editedStrings, oldTemplate, finger);
			final int newTemplateId = chartData.currentArrangement().getChordTemplateIdWithSave(newTemplate);
			chord.updateTemplate(newTemplateId, newTemplate);
		}

		guitarSoundsStatusesHandler
				.updateLinkedNotes(selected.stream().map(s -> s.id).collect(Collectors.toCollection(ArrayList::new)));
		currentSelectionEditor.selectionChanged(false);
		chordTemplatesEditorTab.refreshTemplates();
	}

	private void setFingerForHandShapes(final int finger) {
		final List<Selection<HandShape>> selected = selectionManager.getSelected(PositionType.HAND_SHAPE);
		if (selected.isEmpty()) {
			return;
		}

		final Set<Integer> editedStrings = currentSelectionEditor.getEditedStrings();

		undoSystem.addUndo();

		for (final Selection<HandShape> selection : selected) {
			final HandShape handShape = selection.selectable;
			final ChordTemplate oldTemplate = chartData.currentArrangement().chordTemplates.get(handShape.templateId);
			final ChordTemplate newTemplate = setFingerOnTemplate(editedStrings, oldTemplate, finger);
			handShape.templateId = chartData.currentArrangement().getChordTemplateIdWithSave(newTemplate);
		}

		currentSelectionEditor.selectionChanged(false);
		chordTemplatesEditorTab.refreshTemplates();
	}

	public void setFinger(final int finger) {
		switch (selectionManager.selectedType()) {
			case GUITAR_NOTE:
				setFingerOnSounds(finger);
				break;
			case HAND_SHAPE:
				setFingerForHandShapes(finger);
				break;
			default:
				break;
		}
	}

	private void setHandShapeTemplateOnChordsForSounds() {
		final List<Selection<ChordOrNote>> selected = selectionManager.getSelected(PositionType.GUITAR_NOTE);
		final List<Chord> chords = selected.stream().filter(s -> s.selectable.isChord()).map(s -> s.selectable.chord())
				.collect(Collectors.toList());
		if (chords.isEmpty()) {
			return;
		}

		undoSystem.addUndo();

		final List<HandShape> handShapes = chartData.currentHandShapes();
		final List<ChordTemplate> chordTemplates = chartData.currentChordTemplates();

		for (final Chord chord : chords) {
			final HandShape handShape = CollectionUtils.lastBeforeEqual(handShapes, chord).find();
			if (handShape == null || handShape.endPosition().compareTo(chord) < 0) {
				continue;
			}

			final ChordTemplate chordTemplate = chordTemplates.get(handShape.templateId);
			chord.updateTemplate(handShape.templateId, chordTemplate);
		}

		guitarSoundsStatusesHandler
				.updateLinkedNotes(selected.stream().map(s -> s.id).collect(Collectors.toCollection(ArrayList::new)));
		currentSelectionEditor.selectionChanged(true);
	}

	public void setHandShapeTemplateOnChordsForHandShapes() {
		final List<Selection<HandShape>> selected = selectionManager.getSelected(PositionType.HAND_SHAPE);
		if (selected.isEmpty()) {
			return;
		}

		final List<ChordOrNote> sounds = chartData.currentSounds();
		final List<Integer> changedSoundIds = new ArrayList<>();
		final List<ChordTemplate> chordTemplates = chartData.currentChordTemplates();

		undoSystem.addUndo();

		for (final Selection<HandShape> selection : selected) {
			final HandShape handShape = selection.selectable;
			final ChordTemplate template = chordTemplates.get(handShape.templateId);

			final Integer from = firstAfterEqual(sounds, handShape).findId();
			final Integer to = lastBeforeEqual(sounds, handShape.endPosition()).findId();
			if (from == null || to == null) {
				return;
			}

			for (int i = from; i <= to; i++) {
				final ChordOrNote sound = sounds.get(i);
				if (!sound.isChord()) {
					continue;
				}

				changedSoundIds.add(i);
				sound.chord().updateTemplate(handShape.templateId, template);
			}
		}

		guitarSoundsStatusesHandler.updateLinkedNotes(changedSoundIds);
		currentSelectionEditor.selectionChanged(true);
	}

	public void setHandShapeTemplateOnChords() {
		switch (selectionManager.selectedType()) {
			case GUITAR_NOTE:
				setHandShapeTemplateOnChordsForSounds();
				break;
			case HAND_SHAPE:
				setHandShapeTemplateOnChordsForHandShapes();
				break;
			default:
				break;
		}
	}
}
