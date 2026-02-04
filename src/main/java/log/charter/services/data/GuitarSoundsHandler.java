package log.charter.services.data;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import log.charter.data.ChartData;
import log.charter.data.config.values.InstrumentConfig;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.FHP;
import log.charter.data.song.notes.Chord;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.components.tabs.chordEditor.ChordTemplatesEditorTab;
import log.charter.gui.components.tabs.selectionEditor.CurrentSelectionEditor;
import log.charter.services.data.selection.Selection;
import log.charter.services.data.selection.SelectionManager;
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

	public void moveStringsWithoutFretChange(final int stringChange) {
		final int strings = chartData.currentStrings();
		final IntRange stringRange = new IntRange(max(0, -stringChange), strings - 1 - max(0, stringChange));
		final List<Selection<ChordOrNote>> selected = getSelectedSoundsWithoutStringAboveBelow(stringRange);
		if (selected.isEmpty()) {
			return;
		}

		stringsChanger.new Action(chartData.currentArrangement().tuning, stringRange, stringChange, false)
				.moveStrings(selected);
	}

	public void moveStringsWithFretChange(final int stringChange) {
		final int strings = chartData.currentStrings();
		final IntRange stringRange = new IntRange(max(0, -stringChange), strings - 1 - max(0, stringChange));
		final List<Selection<ChordOrNote>> selected = getSelectedSoundsWithoutStringAboveBelow(stringRange);
		if (selected.isEmpty()) {
			return;
		}
		stringsChanger.new Action(chartData.currentArrangement().tuning, stringRange, stringChange, true)
				.moveStrings(selected);
	}

	private void setChordName(final ChordTemplate template) {
		if (template.chordName == null || template.chordName.isBlank()) {
			return;
		}

		final List<String> suggestedNames = ChordNameSuggester.suggestChordNames(chartData.currentArrangement().tuning,
				template.frets);

		if (!suggestedNames.isEmpty()) {
			template.chordName = suggestedNames.get(0);
		} else {
			template.chordName = "";
		}
	}

	private boolean validFretChange(final List<Selection<ChordOrNote>> selected, final int fretChange) {
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

	private ChordTemplate moveTemplateFrets(final ChordTemplate template, final int fretChange) {
		final ChordTemplate newTemplate = new ChordTemplate();

		newTemplate.fingers.putAll(template.fingers);
		for (final Entry<Integer, Integer> stringFret : template.frets.entrySet()) {
			final int string = stringFret.getKey();
			final int oldFret = stringFret.getValue();
			final int newFret = max(0, min(InstrumentConfig.frets, oldFret + fretChange));

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
		chordTemplatesEditorTab.refreshTemplates();
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

	private void setFretForSounds(final int fret) {
		final List<Selection<ChordOrNote>> selected = selectionManager.getSelected(PositionType.GUITAR_NOTE);
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
			if (fretChange == 0 || oldTemplate.getHighestFret() + fretChange > InstrumentConfig.frets) {
				continue;
			}

			final ChordTemplate newTemplate = moveTemplateFrets(oldTemplate, fretChange);

			final int newTemplateId = chartData.currentArrangement().getChordTemplateIdWithSave(newTemplate);
			chord.updateTemplate(newTemplateId, newTemplate);
		}

		guitarSoundsStatusesHandler
				.updateLinkedNotes(selected.stream().map(s -> s.id).collect(Collectors.toCollection(ArrayList::new)));

		currentSelectionEditor.selectionChanged(false);
		chordTemplatesEditorTab.refreshTemplates();
	}

	public void setFret(final int fret) {
		switch (selectionManager.selectedType()) {
			case FHP:
				setFretForFHPs(fret);
				break;
			case GUITAR_NOTE:
				setFretForSounds(fret);
				break;
			default:
				break;
		}
	}
}
