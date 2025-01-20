package log.charter.services.data;

import static java.util.Arrays.asList;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import log.charter.data.ChartData;
import log.charter.data.song.BendValue;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.enums.HOPO;
import log.charter.data.song.enums.Harmonic;
import log.charter.data.song.enums.Mute;
import log.charter.data.song.notes.Chord;
import log.charter.data.song.notes.ChordNote;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.notes.CommonNote;
import log.charter.data.song.notes.CommonNoteWithFret;
import log.charter.data.song.notes.GuitarSound;
import log.charter.data.song.notes.Note;
import log.charter.data.song.notes.NoteInterface;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.components.tabs.chordEditor.ChordTemplatesEditorTab;
import log.charter.gui.components.tabs.selectionEditor.CurrentSelectionEditor;
import log.charter.services.data.fixers.ArrangementFixer;
import log.charter.services.data.selection.ISelectionAccessor;
import log.charter.services.data.selection.Selection;
import log.charter.services.data.selection.SelectionManager;
import log.charter.util.collections.Pair;

public class GuitarSoundsStatusesHandler {
	private static Map<Mute, Mute> mutesCycleMap = getCycleMap(asList(Mute.NONE, Mute.PALM, Mute.FULL));
	private static Map<HOPO, HOPO> hoposCycleMap = getCycleMap(
			asList(HOPO.NONE, HOPO.HAMMER_ON, HOPO.PULL_OFF, HOPO.TAP));
	private static Map<Harmonic, Harmonic> harmonicsCycleMap = getCycleMap(
			asList(Harmonic.NONE, Harmonic.NORMAL, Harmonic.PINCH));
	private static Map<Boolean, Boolean> booleanCycleMap = getCycleMap(asList(false, true));

	private static <T> T getNextCyclicalValue(final List<T> values, final T value) {
		return values.get((values.indexOf(value) + 1) % values.size());
	}

	private static <T> Map<T, T> getCycleMap(final List<T> values) {
		final Map<T, T> map = new HashMap<>();
		for (final T value : values) {
			map.put(value, getNextCyclicalValue(values, value));
		}

		return map;
	}

	private ArrangementFixer arrangementFixer;
	private ChartData chartData;
	private ChordTemplatesEditorTab chordTemplatesEditorTab;
	private CurrentSelectionEditor currentSelectionEditor;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	public <T> void singleToggleOnAllSelectedNotesWithBaseValue(final Function<ChordOrNote, T> baseValueGetter,
			final BiConsumer<ChordOrNote, T> handler) {
		final ISelectionAccessor<ChordOrNote> selectedAccessor = selectionManager.accessor(PositionType.GUITAR_NOTE);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		final List<Selection<ChordOrNote>> selected = selectedAccessor.getSelected();
		final T baseValue = baseValueGetter.apply(selected.get(0).selectable);

		undoSystem.addUndo();
		selected.forEach(selectedValue -> handler.accept(selectedValue.selectable, baseValue));

		currentSelectionEditor.selectionChanged(false);
	}

	private <T> T getNewValueNotes(final Map<T, T> cycleMap, final ChordOrNote sound,
			final Function<CommonNote, T> getter, final T defaultValue) {
		return cycleMap.get(sound.notes()//
				.findFirst()//
				.map(getter)//
				.orElse(defaultValue));
	}

	public <T> void cyclicalToggleNotes(final Map<T, T> cycleMap, final Function<CommonNote, T> getter,
			final BiConsumer<CommonNote, T> setter, final T defaultValue) {
		final ISelectionAccessor<ChordOrNote> selectedAccessor = selectionManager.accessor(PositionType.GUITAR_NOTE);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		final List<Selection<ChordOrNote>> selected = selectedAccessor.getSelected();
		final T valueToSet = getNewValueNotes(cycleMap, selected.get(0).selectable, getter, defaultValue);

		undoSystem.addUndo();
		selected.forEach(selectedValue -> selectedValue.selectable.notes()//
				.forEach(note -> setter.accept(note, valueToSet)));
		currentSelectionEditor.selectionChanged(false);
	}

	public <T> void independentCyclicalToggleNotes(final Map<T, T> cycleMap, final Function<CommonNote, T> getter,
			final BiConsumer<CommonNote, T> setter) {
		final ISelectionAccessor<ChordOrNote> selectedAccessor = selectionManager.accessor(PositionType.GUITAR_NOTE);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		final List<Selection<ChordOrNote>> selected = selectedAccessor.getSelected();

		undoSystem.addUndo();

		selected.forEach(selectedValue -> selectedValue.selectable.notes()//
				.forEach(note -> setter.accept(note, cycleMap.get(getter.apply(note)))));
		currentSelectionEditor.selectionChanged(false);
	}

	public <T> void cyclicalToggleSound(final Map<T, T> cycleMap, final Function<GuitarSound, T> getter,
			final BiConsumer<GuitarSound, T> setter) {
		final ISelectionAccessor<ChordOrNote> selectedAccessor = selectionManager.accessor(PositionType.GUITAR_NOTE);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		final List<Selection<ChordOrNote>> selected = selectedAccessor.getSelected();
		final T valueToSet = cycleMap.get(getter.apply(selected.get(0).selectable.asGuitarSound()));

		undoSystem.addUndo();
		selected.forEach(selectedValue -> setter.accept(selectedValue.selectable.asGuitarSound(), valueToSet));
		currentSelectionEditor.selectionChanged(false);
	}

	public <T> void independentCyclicalToggleSound(final Map<T, T> cycleMap, final Function<GuitarSound, T> getter,
			final BiConsumer<GuitarSound, T> setter) {
		final ISelectionAccessor<ChordOrNote> selectedAccessor = selectionManager.accessor(PositionType.GUITAR_NOTE);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		final List<Selection<ChordOrNote>> selected = selectedAccessor.getSelected();

		undoSystem.addUndo();
		selected.forEach(selectedValue -> {
			final GuitarSound sound = selectedValue.selectable.asGuitarSound();
			setter.accept(sound, cycleMap.get(getter.apply(sound)));
		});
		currentSelectionEditor.selectionChanged(false);
	}

	public void toggleMute() {
		cyclicalToggleNotes(mutesCycleMap, CommonNote::mute, CommonNote::mute, Mute.NONE);
	}

	public void toggleMuteIndependently() {
		independentCyclicalToggleNotes(mutesCycleMap, CommonNote::mute, CommonNote::mute);
	}

	public void toggleHOPO() {
		cyclicalToggleNotes(hoposCycleMap, CommonNote::hopo, CommonNote::hopo, HOPO.NONE);
	}

	public void toggleHOPOIndependently() {
		independentCyclicalToggleNotes(hoposCycleMap, CommonNote::hopo, CommonNote::hopo);
	}

	public void toggleHarmonic() {
		cyclicalToggleNotes(harmonicsCycleMap, CommonNote::harmonic, CommonNote::harmonic, Harmonic.NONE);
	}

	public void toggleHarmonicIndependently() {
		independentCyclicalToggleNotes(harmonicsCycleMap, CommonNote::harmonic, CommonNote::harmonic);
	}

	public void toggleAccent() {
		cyclicalToggleSound(booleanCycleMap, sound -> sound.accent, (sound, accent) -> sound.accent = accent);
	}

	public void toggleAccentIndependently() {
		independentCyclicalToggleSound(booleanCycleMap, sound -> sound.accent,
				(sound, accent) -> sound.accent = accent);
	}

	public void toggleVibrato() {
		cyclicalToggleNotes(booleanCycleMap, CommonNote::vibrato, CommonNote::vibrato, false);
	}

	public void toggleVibratoIndependently() {
		independentCyclicalToggleNotes(booleanCycleMap, CommonNote::vibrato, CommonNote::vibrato);
	}

	public void toggleTremolo() {
		cyclicalToggleNotes(booleanCycleMap, CommonNote::tremolo, CommonNote::tremolo, false);
	}

	public void toggleTremoloIndependently() {
		independentCyclicalToggleNotes(booleanCycleMap, CommonNote::tremolo, CommonNote::tremolo);
	}

	private void updateLinkedNotesFrets(final CommonNoteWithFret note, final Note nextNote) {
		nextNote.fret = note.slideTo() == null ? note.fret() : note.slideTo();
	}

	private void updateLinkedNotesFrets(final CommonNoteWithFret note, final Chord nextChord) {
		final int fret = note.slideTo() == null ? note.fret() : note.slideTo();
		final int string = note.string();
		final ChordTemplate chordTemplate = new ChordTemplate(
				chartData.currentChordTemplates().get(nextChord.templateId()));
		if (chordTemplate.frets.get(string) == fret) {
			return;
		}

		chordTemplate.frets.put(string, fret);
		nextChord.updateTemplate(chartData.currentArrangement().getChordTemplateIdWithSave(chordTemplate),
				chordTemplate);
		chordTemplatesEditorTab.refreshTemplates();
	}

	private void updateLinkedNotesBends(final CommonNote note, final NoteInterface nextNote) {
		if (note.bendValues().isEmpty()) {
			return;
		}

		final BigDecimal lastBendValue = note.bendValues().get(note.bendValues().size() - 1).bendValue;
		if (nextNote.bendValues().isEmpty() && lastBendValue.compareTo(BigDecimal.ZERO) != 0) {
			nextNote.bendValues().add(new BendValue(nextNote.position(), lastBendValue));
		} else {
			final BendValue firstBend = nextNote.bendValues().get(0);
			if (firstBend.compareTo(nextNote) != 0) {
				nextNote.bendValues().add(new BendValue(new FractionalPosition(), lastBendValue));
			} else if (firstBend.bendValue.compareTo(lastBendValue) != 0) {
				firstBend.bendValue = lastBendValue;
			}
		}
	}

	private void updateLinkedNote(final List<ChordOrNote> sounds, final int id) {
		final ChordOrNote sound = sounds.get(id);
		sound.notesWithFrets(chartData.currentChordTemplates()).forEach(n -> {
			CommonNoteWithFret currentSound = n;
			int currentId = id;
			while (currentSound != null && currentSound.linkNext()) {
				final Pair<Integer, ChordOrNote> nextSound = ChordOrNote.findNextSoundWithIdOnString(n.string(),
						currentId + 1, sounds);
				if (nextSound == null) {
					break;
				}

				if (nextSound.b.isNote()) {
					final Note nextNote = nextSound.b.note();
					updateLinkedNotesFrets(currentSound, nextNote);
					updateLinkedNotesBends(currentSound, nextNote);
					currentSound = new CommonNoteWithFret(nextNote);
				} else {
					final Chord nextChord = nextSound.b.chord();
					final ChordNote nextNote = nextChord.chordNotes.get(n.string());
					updateLinkedNotesFrets(currentSound, nextChord);
					updateLinkedNotesBends(currentSound, nextNote);

					final int fret = chartData.currentChordTemplates().get(nextChord.templateId()).frets
							.get(n.string());
					currentSound = new CommonNoteWithFret(nextChord, n.string(), fret);
				}

				currentId = nextSound.a;
			}

			final ChordOrNote previousSound = ChordOrNote.findPreviousSoundOnString(n.string(), id - 1, sounds);
			if (previousSound != null && previousSound.linkNext(n.string())) {
				if (previousSound.isNote()) {
					final Note previousNote = previousSound.note();
					if (previousNote.fret == n.fret()) {
						previousNote.slideTo = null;
					} else {
						previousNote.slideTo = n.fret();
					}

					previousNote.unpitchedSlide = false;
				} else {
					final Chord previousChord = previousSound.chord();
					final ChordNote previousNote = previousChord.chordNotes.get(n.string());
					final ChordTemplate chordTemplate = chartData.currentChordTemplates()
							.get(previousChord.templateId());
					if (chordTemplate.frets.get(n.string()) == n.fret()) {
						previousNote.slideTo = null;
					} else {
						previousNote.slideTo = n.fret();
					}
				}
			}
		});
	}

	public void updateLinkedNote(final int id) {
		updateLinkedNote(chartData.currentSounds(), id);
	}

	public void updateLinkedNotes(final List<Integer> ids) {
		ids.sort(null);
		final List<ChordOrNote> sounds = chartData.currentSounds();

		for (final int id : ids) {
			updateLinkedNote(sounds, id);
		}
	}

	public void toggleLinkNext() {
		final ISelectionAccessor<ChordOrNote> selectedAccessor = selectionManager.accessor(PositionType.GUITAR_NOTE);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		cyclicalToggleNotes(booleanCycleMap, CommonNote::linkNext, CommonNote::linkNext, false);

		final List<Selection<ChordOrNote>> selected = selectedAccessor.getSelected();
		arrangementFixer.fixNoteLengths(chartData.currentArrangementLevel().sounds, selected.get(0).id,
				selected.get(selected.size() - 1).id);

		updateLinkedNotes(selected.stream().map(s -> s.id).collect(Collectors.toCollection(ArrayList::new)));

		currentSelectionEditor.selectionChanged(false);
	}

	public void toggleLinkNextIndependently() {
		final ISelectionAccessor<ChordOrNote> selectedAccessor = selectionManager.accessor(PositionType.GUITAR_NOTE);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		independentCyclicalToggleNotes(booleanCycleMap, CommonNote::linkNext, CommonNote::linkNext);

		final List<Selection<ChordOrNote>> selected = selectedAccessor.getSelected();
		arrangementFixer.fixNoteLengths(chartData.currentArrangementLevel().sounds, selected.get(0).id,
				selected.get(selected.size() - 1).id);
		updateLinkedNotes(selected.stream().map(s -> s.id).collect(Collectors.toCollection(ArrayList::new)));

		currentSelectionEditor.selectionChanged(false);
	}
}
