package log.charter.services.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import log.charter.data.ChartData;
import log.charter.data.ChordTemplateFingerSetter;
import log.charter.data.config.values.InstrumentConfig;
import log.charter.data.song.Arrangement;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.configs.Tuning;
import log.charter.data.song.notes.Chord;
import log.charter.data.song.notes.ChordNote;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.notes.CommonNoteWithFret;
import log.charter.data.song.notes.Note;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.components.tabs.chordEditor.ChordTemplatesEditorTab;
import log.charter.gui.components.tabs.selectionEditor.CurrentSelectionEditor;
import log.charter.services.data.selection.Selection;
import log.charter.util.collections.Pair;
import log.charter.util.data.IntRange;

public class StringsChanger {
	private static Map<Integer, Integer> getStringDifferencesEmpty(final IntRange stringRange) {
		final Map<Integer, Integer> stringDifferences = new HashMap<>();
		for (int i = stringRange.min; i <= stringRange.max; i++) {
			stringDifferences.put(i, 0);
		}

		return stringDifferences;
	}

	private static Map<Integer, Integer> getStringDifferences(final Tuning tuning, final IntRange stringRange,
			final int stringChange) {
		final Map<Integer, Integer> stringDifferences = new HashMap<>();
		for (int i = stringRange.min; i <= stringRange.max; i++) {
			stringDifferences.put(i, tuning.getStringOffset(i) - tuning.getStringOffset(i + stringChange));
		}

		return stringDifferences;
	}

	private ChartData chartData;
	private ChordTemplatesEditorTab chordTemplatesEditorTab;
	private CurrentSelectionEditor currentSelectionEditor;
	private GuitarSoundsStatusesHandler guitarSoundsStatusesHandler;
	private UndoSystem undoSystem;

	public class Action {
		private final IntRange stringRange;
		private final int stringChange;
		private final Map<Integer, Integer> stringDifferences;
		private final Arrangement arrangement = chartData.currentArrangement();

		private final Set<Integer> idsVisited = new HashSet<>();

		final Map<Integer, Integer> movedChordTemplates = new HashMap<>();
		final List<ChordTemplate> chordTemplates = arrangement.chordTemplates;

		public Action(final Tuning tuning, final IntRange stringRange, final int stringChange,
				final boolean fretChange) {
			this.stringRange = stringRange;
			this.stringChange = stringChange;
			stringDifferences = fretChange ? getStringDifferences(tuning, stringRange, stringChange)
					: getStringDifferencesEmpty(stringRange);
		}

		private void getSoundWithLinked(final ChordOrNote sound, final int id, final List<ChordOrNote> linkedSounds) {
			if (idsVisited.contains(id)) {
				return;
			}

			linkedSounds.add(sound);
			idsVisited.add(id);

			sound.notes().forEach(note -> {
				final int string = note.string();
				final Pair<Integer, ChordOrNote> previousSound = ChordOrNote.findPreviousSoundWithIdOnString(string,
						id - 1, chartData.currentSounds());

				if (previousSound != null && previousSound.b.getString(string).get().linkNext()) {
					getSoundWithLinked(previousSound.b, previousSound.a, linkedSounds);
				}

				if (note.linkNext()) {
					final Pair<Integer, ChordOrNote> nextSound = ChordOrNote.findNextSoundWithIdOnString(string, id + 1,
							chartData.currentSounds());
					if (nextSound != null) {
						getSoundWithLinked(nextSound.b, nextSound.a, linkedSounds);
					}
				}
			});
		}

		private List<ChordOrNote> getSoundWithLinked(final ChordOrNote sound, final int id) {
			final List<ChordOrNote> list = new ArrayList<>();
			getSoundWithLinked(sound, id, list);
			return list;
		}

		private boolean invalidStringChangeForNote(final CommonNoteWithFret note) {
			if (note.string() < stringRange.min || note.string() > stringRange.max) {
				return true;
			}

			final int stringDifference = stringDifferences.get(note.string());
			final int newFret = note.fret() + stringDifference;
			if (newFret < 0 || newFret > InstrumentConfig.frets) {
				return true;
			}

			if (note.slideTo() != null) {
				final int newSlideTo = note.slideTo() + stringDifference;
				if (newSlideTo < 0 || newSlideTo > InstrumentConfig.frets) {
					return true;
				}
			}

			return false;
		}

		private boolean validateStringChange(final List<ChordOrNote> sounds) {
			for (final ChordOrNote sound : sounds) {
				if (sound.notesWithFrets(chordTemplates).anyMatch(this::invalidStringChangeForNote)) {
					return false;
				}
			}

			return true;
		}

		private void moveNote(final Note note) {
			final int stringDifference = stringDifferences.get(note.string);

			note.string += stringChange;
			note.fret += stringDifference;

			if (note.slideTo != null) {
				note.slideTo += stringDifference;
			}
		}

		private void moveChordNoteStrings(final Map<Integer, ChordNote> chordNotes) {
			final Map<Integer, ChordNote> temporaryChordNotes = new HashMap<>();
			for (int string = stringRange.min; string <= stringRange.max; string++) {
				final ChordNote movedChordNote = chordNotes.get(string);
				if (movedChordNote != null) {
					final int newString = string + stringChange;
					if (movedChordNote.slideTo != null) {
						movedChordNote.slideTo += stringDifferences.get(string);
					}

					temporaryChordNotes.put(newString, movedChordNote);
				}
			}

			chordNotes.clear();
			chordNotes.putAll(temporaryChordNotes);
		}

		private int[] getShape(final ChordTemplate chordTemplate) {
			final ArrayList<Entry<Integer, Integer>> frets = new ArrayList<>(chordTemplate.frets.entrySet());
			frets.sort((a, b) -> a.getKey().compareTo(b.getKey()));
			final int[] shape = new int[frets.size() - 1];

			for (int i = 0; i < shape.length; i++) {
				shape[i] = frets.get(i + 1).getValue() - frets.get(i).getValue();
			}

			return shape;
		}

		private boolean chordShapeChanged(final ChordTemplate chordTemplate, final ChordTemplate newChordTemplate) {
			return chordTemplate.frets.containsValue(0) //
					|| newChordTemplate.frets.containsValue(0)//
					|| Arrays.compare(getShape(chordTemplate), getShape(newChordTemplate)) != 0;
		}

		private ChordTemplate moveChordTemplateStrings(final ChordTemplate chordTemplate) {
			final ChordTemplate newChordTemplate = new ChordTemplate();
			newChordTemplate.chordName = chordTemplate.chordName;
			newChordTemplate.arpeggio = chordTemplate.arpeggio;

			for (final Entry<Integer, Integer> templateNote : chordTemplate.frets.entrySet()) {
				if (templateNote.getValue() == null) {
					continue;
				}
				final int string = templateNote.getKey();
				int fret = templateNote.getValue();

				fret += stringDifferences.get(string);
				final int newString = string + stringChange;

				newChordTemplate.frets.put(newString, fret);
				newChordTemplate.fingers.put(newString, chordTemplate.fingers.get(string));
			}

			if (chordShapeChanged(chordTemplate, newChordTemplate)) {
				ChordTemplateFingerSetter.setSuggestedFingers(newChordTemplate);
			}

			return newChordTemplate;
		}

		private void moveChord(final Chord chord) {
			if (movedChordTemplates.containsKey(chord.templateId())) {
				moveChordNoteStrings(chord.chordNotes);
				final int newTemplateId = movedChordTemplates.get(chord.templateId());
				final ChordTemplate chordTemplate = chordTemplates.get(newTemplateId);
				chord.updateTemplate(newTemplateId, chordTemplate);
				return;
			}

			final ChordTemplate oldTemplate = chordTemplates.get(chord.templateId());
			final ChordTemplate newChordTemplate = moveChordTemplateStrings(oldTemplate);

			moveChordNoteStrings(chord.chordNotes);
			final int newTemplateId = arrangement.getChordTemplateIdWithSave(newChordTemplate);
			movedChordTemplates.put(chord.templateId(), newTemplateId);
			chord.updateTemplate(newTemplateId, newChordTemplate);
		}

		public void moveStrings(final List<Selection<ChordOrNote>> selected) {
			undoSystem.addUndo();

			for (final Selection<ChordOrNote> selection : selected) {
				if (idsVisited.contains(selection.id)) {
					continue;
				}

				final List<ChordOrNote> group = getSoundWithLinked(selection.selectable, selection.id);
				if (!validateStringChange(group)) {
					continue;
				}

				for (final ChordOrNote sound : group) {
					if (sound.isNote()) {
						moveNote(sound.note());
					} else {
						moveChord(sound.chord());
					}
				}
			}

			guitarSoundsStatusesHandler.updateLinkedNotes(
					selected.stream().map(s -> s.id).collect(Collectors.toCollection(ArrayList::new)));
			currentSelectionEditor.selectionChanged(true);
			chordTemplatesEditorTab.refreshTemplates();
		}
	}
}
