package log.charter.gui.handlers.data;

import static java.util.Arrays.asList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import log.charter.data.ArrangementFixer;
import log.charter.data.ChartData;
import log.charter.data.managers.selection.Selection;
import log.charter.data.managers.selection.SelectionAccessor;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.components.tabs.selectionEditor.CurrentSelectionEditor;
import log.charter.song.enums.HOPO;
import log.charter.song.enums.Harmonic;
import log.charter.song.enums.Mute;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.CommonNote;
import log.charter.song.notes.GuitarSound;
import log.charter.util.CollectionUtils.ArrayList2;

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
	private CurrentSelectionEditor currentSelectionEditor;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	public void init(final ChartData chartData, final CurrentSelectionEditor currentSelectionEditor,
			final SelectionManager selectionManager, final UndoSystem undoSystem) {
		this.chartData = chartData;
		this.currentSelectionEditor = currentSelectionEditor;
		this.selectionManager = selectionManager;
		this.undoSystem = undoSystem;
	}

	public <T> void singleToggleOnAllSelectedNotesWithBaseValue(final Function<ChordOrNote, T> baseValueGetter,
			final BiConsumer<ChordOrNote, T> handler) {
		final SelectionAccessor<ChordOrNote> selectedAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		final ArrayList2<Selection<ChordOrNote>> selected = selectedAccessor.getSortedSelected();
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
		final SelectionAccessor<ChordOrNote> selectedAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		final ArrayList2<Selection<ChordOrNote>> selected = selectedAccessor.getSortedSelected();
		final T valueToSet = getNewValueNotes(cycleMap, selected.get(0).selectable, getter, defaultValue);

		undoSystem.addUndo();
		selected.forEach(selectedValue -> selectedValue.selectable.notes()//
				.forEach(note -> setter.accept(note, valueToSet)));
		currentSelectionEditor.selectionChanged(false);
	}

	public <T> void independentCyclicalToggleNotes(final Map<T, T> cycleMap, final Function<CommonNote, T> getter,
			final BiConsumer<CommonNote, T> setter) {
		final SelectionAccessor<ChordOrNote> selectedAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		final ArrayList2<Selection<ChordOrNote>> selected = selectedAccessor.getSortedSelected();

		undoSystem.addUndo();

		selected.forEach(selectedValue -> selectedValue.selectable.notes()//
				.forEach(note -> setter.accept(note, cycleMap.get(getter.apply(note)))));
		currentSelectionEditor.selectionChanged(false);
	}

	public <T> void cyclicalToggleSound(final Map<T, T> cycleMap, final Function<GuitarSound, T> getter,
			final BiConsumer<GuitarSound, T> setter) {
		final SelectionAccessor<ChordOrNote> selectedAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		final ArrayList2<Selection<ChordOrNote>> selected = selectedAccessor.getSortedSelected();
		final T valueToSet = cycleMap.get(getter.apply(selected.get(0).selectable.asGuitarSound()));

		undoSystem.addUndo();
		selected.forEach(selectedValue -> setter.accept(selectedValue.selectable.asGuitarSound(), valueToSet));
		currentSelectionEditor.selectionChanged(false);
	}

	public <T> void independentCyclicalToggleSound(final Map<T, T> cycleMap, final Function<GuitarSound, T> getter,
			final BiConsumer<GuitarSound, T> setter) {
		final SelectionAccessor<ChordOrNote> selectedAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		final ArrayList2<Selection<ChordOrNote>> selected = selectedAccessor.getSortedSelected();

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

	public void toggleLinkNext() {
		final SelectionAccessor<ChordOrNote> selectedAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		cyclicalToggleNotes(booleanCycleMap, CommonNote::linkNext, CommonNote::linkNext, false);

		final ArrayList2<Selection<ChordOrNote>> selected = selectedAccessor.getSortedSelected();
		arrangementFixer.fixNoteLengths(chartData.getCurrentArrangementLevel().sounds, selected.get(0).id,
				selected.getLast().id);
	}

	public void toggleLinkNextIndependently() {
		final SelectionAccessor<ChordOrNote> selectedAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		if (!selectedAccessor.isSelected()) {
			return;
		}

		independentCyclicalToggleNotes(booleanCycleMap, CommonNote::linkNext, CommonNote::linkNext);

		final ArrayList2<Selection<ChordOrNote>> selected = selectedAccessor.getSortedSelected();
		arrangementFixer.fixNoteLengths(chartData.getCurrentArrangementLevel().sounds, selected.get(0).id,
				selected.getLast().id);
	}
}
