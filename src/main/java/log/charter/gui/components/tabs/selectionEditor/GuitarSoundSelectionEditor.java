package log.charter.gui.components.tabs.selectionEditor;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static log.charter.gui.components.simple.TextInputWithValidation.generateForInteger;
import static log.charter.gui.components.tabs.selectionEditor.CurrentSelectionEditor.getSingleValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JCheckBox;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.config.values.InstrumentConfig;
import log.charter.data.song.Arrangement;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.enums.BassPickingTechnique;
import log.charter.data.song.enums.HOPO;
import log.charter.data.song.enums.Harmonic;
import log.charter.data.song.enums.Mute;
import log.charter.data.song.notes.Chord;
import log.charter.data.song.notes.ChordNote;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.notes.CommonNote;
import log.charter.data.song.notes.GuitarSound;
import log.charter.data.song.notes.Note;
import log.charter.data.song.notes.NoteInterface;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.simple.TextInputWithValidation;
import log.charter.gui.components.simple.ToggleButtonGroupInRow;
import log.charter.gui.components.tabs.chordEditor.ChordTemplatesEditorTab;
import log.charter.gui.components.tabs.selectionEditor.bends.SelectionBendEditor;
import log.charter.gui.components.tabs.selectionEditor.chords.ChordTemplateEditor;
import log.charter.gui.components.tabs.selectionEditor.simpleComponents.BasicCheckboxInput;
import log.charter.gui.components.utils.RowedPosition;
import log.charter.gui.components.utils.validators.IntegerValueValidator;
import log.charter.services.data.ChartItemsHandler;
import log.charter.services.data.GuitarSoundsStatusesHandler;
import log.charter.services.data.fixers.ArrangementFixer;
import log.charter.services.data.fixers.DuplicatedChordTemplatesRemover;
import log.charter.services.data.fixers.UnusedChordTemplatesRemover;
import log.charter.services.data.selection.ISelectionAccessor;
import log.charter.services.data.selection.Selection;
import log.charter.services.data.selection.SelectionManager;
import log.charter.services.mouseAndKeyboard.KeyboardHandler;
import log.charter.util.collections.Pair;

public class GuitarSoundSelectionEditor extends ChordTemplateEditor {
	private ArrangementFixer arrangementFixer;
	private ChartData chartData;
	private CharterFrame charterFrame;
	private ChartItemsHandler chartItemsHandler;
	private ChordTemplatesEditorTab chordTemplatesEditorTab;
	private GuitarSoundsStatusesHandler guitarSoundsStatusesHandler;
	private KeyboardHandler keyboardHandler;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	private final CurrentSelectionEditor parent;

	private ChordTemplate chordTemplate = new ChordTemplate();

	private ToggleButtonGroupInRow<Mute> mute;
	private ToggleButtonGroupInRow<HOPO> hopo;
	private ToggleButtonGroupInRow<BassPickingTechnique> bassPickingTechnique;
	private ToggleButtonGroupInRow<Harmonic> harmonic;
	private FieldWithLabel<TextInputWithValidation> slideFret;
	private FieldWithLabel<JCheckBox> unpitchedSlide;
	private FieldWithLabel<JCheckBox> vibrato;
	private FieldWithLabel<JCheckBox> tremolo;
	private FieldWithLabel<JCheckBox> accent;
	private FieldWithLabel<JCheckBox> linkNext;
	private FieldWithLabel<JCheckBox> splitIntoNotes;
	private FieldWithLabel<JCheckBox> onlyBox;
	private FieldWithLabel<JCheckBox> ignore;
	private FieldWithLabel<JCheckBox> passOtherNotes;

	private SelectionBendEditor selectionBendEditor;

	public GuitarSoundSelectionEditor(final CurrentSelectionEditor parent) {
		super(parent);

		this.parent = parent;
	}

	private void addMuteInputs(final CurrentSelectionEditor parent, final RowedPosition position) {
		mute = new ToggleButtonGroupInRow<>(parent, position, 65, Label.MUTE, //
				makeChangeForCommonNotes(NoteInterface::mute), //
				asList(new Pair<>(Mute.NONE, Label.MUTE_NONE), //
						new Pair<>(Mute.PALM, Label.MUTE_PALM), //
						new Pair<>(Mute.FULL, Label.MUTE_FULL)));
	}

	private void addHOPOInputs(final CurrentSelectionEditor parent, final RowedPosition position) {
		hopo = new ToggleButtonGroupInRow<>(parent, position, 49, Label.HOPO, //
				makeChangeForCommonNotes(NoteInterface::hopo), //
				asList(new Pair<>(HOPO.NONE, Label.HOPO_NONE), //
						new Pair<>(HOPO.HAMMER_ON, Label.HOPO_HAMMER_ON), //
						new Pair<>(HOPO.PULL_OFF, Label.HOPO_PULL_OFF), //
						new Pair<>(HOPO.TAP, Label.HOPO_TAP)));
	}

	private void addBassPickingTechniqueInputs(final CurrentSelectionEditor parent, final RowedPosition position) {
		bassPickingTechnique = new ToggleButtonGroupInRow<>(parent, position, 65, Label.BASS_PICKING_TECHNIQUE, //
				makeChangeForCommonNotes(NoteInterface::bassPicking), //
				asList(new Pair<>(BassPickingTechnique.NONE, Label.BASS_PICKING_NONE), //
						new Pair<>(BassPickingTechnique.SLAP, Label.BASS_PICKING_SLAP), //
						new Pair<>(BassPickingTechnique.POP, Label.BASS_PICKING_POP)));
	}

	private void addHarmonicInputs(final CurrentSelectionEditor parent, final RowedPosition position) {
		harmonic = new ToggleButtonGroupInRow<>(parent, position, 65, Label.HARMONIC, //
				makeChangeForCommonNotes(NoteInterface::harmonic), //
				asList(new Pair<>(Harmonic.NONE, Label.HARMONIC_NONE), //
						new Pair<>(Harmonic.NORMAL, Label.HARMONIC_NORMAL), //
						new Pair<>(Harmonic.PINCH, Label.HARMONIC_PINCH)));
	}

	private void addSlideFretInput(final CurrentSelectionEditor parent, final RowedPosition position) {
		final TextInputWithValidation slideFretInput = generateForInteger(null, 30, //
				new IntegerValueValidator(1, InstrumentConfig.frets, true), this::changeSlideFret, false);
		slideFret = new FieldWithLabel<>(Label.SLIDE_PANE_FRET, 60, 30, 20, slideFretInput, LabelPosition.LEFT);
		parent.add(slideFret, position);
	}

	private void addUnpitchedSlideInput(final CurrentSelectionEditor parent, final RowedPosition position) {
		final JCheckBox unpitchedSlideInput = new JCheckBox();
		unpitchedSlideInput.setFocusable(false);
		final Consumer<Boolean> onInputValueChange = makeChangeForCommonNotes(
				(final CommonNote note, final Boolean value) -> note.unpitchedSlide(value));
		unpitchedSlideInput.addActionListener(a -> onInputValueChange.accept(unpitchedSlideInput.isSelected()));
		unpitchedSlide = new FieldWithLabel<>(Label.SLIDE_PANE_UNPITCHED, 80, 20, 20, unpitchedSlideInput,
				LabelPosition.RIGHT_CLOSE);

		parent.add(unpitchedSlide, position);
	}

	private void addChordTemplateEditorParts(final int x) {
		addChordNameSuggestionButton(x + 100, 0);
		addChordNameInput(x + 20, 1);
		addChordTemplateEditor(x, 3);
	}

	private void addBendEditor(final int x) {
		selectionBendEditor = new SelectionBendEditor(parent, chartData, guitarSoundsStatusesHandler, selectionManager,
				undoSystem);
		selectionBendEditor.setLocation(x, parent.sizes.getY(2));
		parent.add(selectionBendEditor);
	}

	public void addTo(final CurrentSelectionEditor selectionEditor) {
		super.init(chartData, charterFrame, keyboardHandler, () -> chordTemplate, this::templateEdited);

		parent.addStringChangeOperation(this::updateStringSelectionDependentValues);
		final RowedPosition position = new RowedPosition(20, selectionEditor.sizes);

		addMuteInputs(selectionEditor, position);
		position.newRows(2);

		addHOPOInputs(selectionEditor, position);
		position.newRows(2);

		addBassPickingTechniqueInputs(selectionEditor, position);
		position.newRows(2);

		addHarmonicInputs(selectionEditor, position);
		position.newRows(2);

		addSlideFretInput(selectionEditor, position);
		addUnpitchedSlideInput(selectionEditor, position);
		position.newRow();

		final int firstCheckboxRowWidth = 50;
		final int secondCheckboxRowWidth = 70;

		vibrato = BasicCheckboxInput.addField(parent, Label.VIBRATO, position, firstCheckboxRowWidth,
				makeChangeForCommonNotes(NoteInterface::vibrato));
		tremolo = BasicCheckboxInput.addField(parent, Label.TREMOLO, position, secondCheckboxRowWidth,
				makeChangeForCommonNotes(NoteInterface::tremolo));
		position.newRow();

		accent = BasicCheckboxInput.addField(parent, Label.ACCENT, position, firstCheckboxRowWidth,
				makeChangeForGuitarSounds(GuitarSound::accent));
		linkNext = BasicCheckboxInput.addField(parent, Label.LINK_NEXT, position, secondCheckboxRowWidth,
				this::changeLinkNext);
		position.newRow();

		splitIntoNotes = BasicCheckboxInput.addField(parent, Label.SPLIT_INTO_NOTES, position, firstCheckboxRowWidth,
				makeChangeForChords(Chord::splitIntoNotes));
		onlyBox = BasicCheckboxInput.addField(parent, Label.ONLY_BOX, position, secondCheckboxRowWidth,
				makeChangeForChords(Chord::forceNoNotes));
		position.newRow();

		ignore = BasicCheckboxInput.addField(parent, Label.IGNORE, position, firstCheckboxRowWidth,
				makeChangeForGuitarSounds(GuitarSound::ignore));
		passOtherNotes = BasicCheckboxInput.addField(parent, Label.PASS_NOTES, position, secondCheckboxRowWidth,
				makeChangeForGuitarSounds(GuitarSound::passOtherNotes));

		addChordTemplateEditorParts(275);
		addBendEditor(900);

		hideFields();
	}

	private void refreshBendEditorStrings() {
		final List<ChordOrNote> sounds = selectionManager.getSelectedElements(PositionType.GUITAR_NOTE);
		if (sounds.size() == 1) {
			selectionBendEditor.enableAndSelectStrings(sounds.get(0));
		}
	}

	private void changeSelectedToChords() {
		final Arrangement arrangement = chartData.currentArrangement();
		final int templateId = arrangement.getChordTemplateIdWithSave(chordTemplate);

		chartItemsHandler.mapSounds(sound -> sound.asChord(templateId, chordTemplate));

		refreshBendEditorStrings();
		chordTemplatesEditorTab.refreshTemplates();
	}

	private void changeSelectedChordsToNotes() {
		clearChordName();

		final Arrangement arrangement = chartData.currentArrangement();
		final List<ChordTemplate> chordTemplates = arrangement.chordTemplates;

		chartItemsHandler.mapSounds(sound -> sound.asNote(chordTemplates));

		refreshBendEditorStrings();
	}

	private <T, V> Consumer<T> makeChange(final Function<Stream<ChordOrNote>, Stream<V>> filterMapper,
			final BiConsumer<V, T> onChange) {
		return t -> {
			undoSystem.addUndo();

			final Stream<ChordOrNote> stream = selectionManager
					.<ChordOrNote>getSelectedElements(PositionType.GUITAR_NOTE).stream();

			filterMapper.apply(stream)//
					.forEach(v -> onChange.accept(v, t));
		};
	}

	private <T> Consumer<T> makeChangeForChords(final BiConsumer<Chord, T> onChange) {
		return makeChange(stream -> stream//
				.filter(sound -> sound.isChord())//
				.map(sound -> sound.chord()), //
				onChange);
	}

	private <T> Consumer<T> makeChangeForGuitarSounds(final BiConsumer<GuitarSound, T> onChange) {
		return makeChange(stream -> stream//
				.map(sound -> sound.asGuitarSound()), //
				onChange);
	}

	@SuppressWarnings("unchecked")
	private <T> Consumer<T> makeChangeForCommonNotes(final BiConsumer<CommonNote, T> onChange) {
		return makeChange(stream -> stream//
				.flatMap(sound -> (Stream<CommonNote>) sound.notes())//
				.filter(note -> parent.isEdited(note.string())), //

				onChange);
	}

	private void changeLinkNext(final boolean newLinkNext) {
		undoSystem.addUndo();

		final List<ChordOrNote> sounds = chartData.currentArrangementLevel().sounds;
		final List<Selection<ChordOrNote>> selected = selectionManager
				.<ChordOrNote>getSelected(PositionType.GUITAR_NOTE);

		for (final Selection<ChordOrNote> selection : selected) {
			if (selection.selectable.isNote()) {
				if (parent.isEdited(selection.selectable.note().string)) {
					selection.selectable.note().linkNext = newLinkNext;
				}
			} else {
				selection.selectable.chord().chordNotes.forEach((string, chordNote) -> {
					if (parent.isEdited(string)) {
						chordNote.linkNext = newLinkNext;
					}
				});
			}

			final int nextId = selection.id + 1;
			if (nextId < sounds.size()) {
				final ChordOrNote nextSound = sounds.get(nextId);
				if (nextSound.isChord()) {
					nextSound.chord().splitIntoNotes = true;
				}
			}

			arrangementFixer.fixSoundLength(selection.id, sounds);
		}

		if (selected.size() == 1) {
			selectionBendEditor.onChangeSelection(selected.stream().findAny().get().selectable);
		}

		guitarSoundsStatusesHandler
				.updateLinkedNotes(selected.stream().map(s -> s.id).collect(Collectors.toCollection(ArrayList::new)));
	}

	private void cleanSlide() {
		final List<Selection<ChordOrNote>> selected = selectionManager
				.<ChordOrNote>getSelected(PositionType.GUITAR_NOTE);

		for (final Selection<ChordOrNote> selection : selected) {
			if (selection.selectable.isNote()) {
				final Note note = selection.selectable.note();
				note.slideTo = null;
			} else {
				selection.selectable.chord().chordNotes.values().forEach(n -> n.slideTo = null);
			}
		}

		guitarSoundsStatusesHandler
				.updateLinkedNotes(selected.stream().map(s -> s.id).collect(Collectors.toCollection(ArrayList::new)));
	}

	private void setSlide(final int newSlideFret) {
		final List<ChordTemplate> chordTemplates = chartData.currentArrangement().chordTemplates;
		final List<Selection<ChordOrNote>> selected = selectionManager
				.<ChordOrNote>getSelected(PositionType.GUITAR_NOTE);

		for (final Selection<ChordOrNote> selection : selected) {
			if (selection.selectable.isNote()) {
				final Note note = selection.selectable.note();
				note.slideTo = newSlideFret;
			} else {
				final Chord chord = selection.selectable.chord();
				final ChordTemplate chordTemplate = chordTemplates.get(chord.templateId());
				final int minFret = chordTemplate.frets.entrySet().stream()//
						.filter(fret -> parent.isEdited(fret.getKey()) && fret.getValue() > 0)//
						.map(fret -> fret.getValue())//
						.collect(Collectors.minBy(Integer::compare)).orElse(1);
				final int fretDifference = minFret - newSlideFret;
				for (final Entry<Integer, ChordNote> chordNoteEntry : chord.chordNotes.entrySet()) {
					if (!parent.isEdited(chordNoteEntry.getKey())) {
						continue;
					}

					final int fret = chordTemplate.frets.get(chordNoteEntry.getKey());
					if (fret == 0) {
						continue;
					}

					chordNoteEntry.getValue().slideTo = min(InstrumentConfig.frets, max(1, fret - fretDifference));
				}
			}
		}

		guitarSoundsStatusesHandler
				.updateLinkedNotes(selected.stream().map(s -> s.id).collect(Collectors.toCollection(ArrayList::new)));
	}

	private void changeSlideFret(final Integer newSlideFret) {
		undoSystem.addUndo();

		if (newSlideFret == null) {
			cleanSlide();
			return;
		}

		setSlide(newSlideFret);
	}

	private void templateEdited() {
		if (chordTemplate.frets.isEmpty()) {
			return;
		}

		undoSystem.addUndo();

		final Arrangement arrangement = chartData.currentArrangement();

		if (chordTemplate.frets.size() == 0) {
			final ISelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
					.accessor(PositionType.GUITAR_NOTE);
			final List<Integer> selected = selectionAccessor.getSelected()//
					.stream()//
					.map(selection -> selection.id)//
					.collect(Collectors.toList());

			selectionManager.clear();
			chartItemsHandler.delete(PositionType.GUITAR_NOTE, selected);
			return;
		}

		List<Selection<ChordOrNote>> selected;
		if (chordTemplate.frets.size() == 1) {
			changeSelectedChordsToNotes();

			selected = selectionManager.getSelected(PositionType.GUITAR_NOTE);

			for (final Entry<Integer, Integer> entry : chordTemplate.frets.entrySet()) {
				for (final Selection<ChordOrNote> selection : selected) {
					selection.selectable.note().string = entry.getKey();
					selection.selectable.note().fret = entry.getValue();
				}
			}

			if (selected.size() == 1) {
				selectionBendEditor.enableAndSelectStrings(selected.get(0).selectable);
			}
		} else {
			final int templateId = arrangement.getChordTemplateIdWithSave(chordTemplate);
			selected = selectionManager.getSelected(PositionType.GUITAR_NOTE);

			for (final Selection<ChordOrNote> selection : selected) {
				if (selection.selectable.isChord()) {
					selection.selectable.chord().updateTemplate(templateId, chordTemplate);
				}
			}

			changeSelectedToChords();
		}

		guitarSoundsStatusesHandler
				.updateLinkedNotes(selected.stream().map(s -> s.id).collect(Collectors.toCollection(ArrayList::new)));

		DuplicatedChordTemplatesRemover.remove(arrangement);
		UnusedChordTemplatesRemover.remove(arrangement);
		chordTemplatesEditorTab.refreshTemplates();
	}

	@Override
	public void showFields() {
		super.showFields();

		mute.setVisible(true);
		hopo.setVisible(true);
		bassPickingTechnique.setVisible(true);
		harmonic.setVisible(true);
		accent.setVisible(true);
		linkNext.setVisible(true);
		splitIntoNotes.setVisible(true);
		onlyBox.setVisible(true);
		ignore.setVisible(true);
		passOtherNotes.setVisible(true);
		slideFret.setVisible(true);
		unpitchedSlide.setVisible(true);
		vibrato.setVisible(true);
		tremolo.setVisible(true);
	}

	@Override
	public void hideFields() {
		super.hideFields();

		mute.setVisible(false);
		hopo.setVisible(false);
		bassPickingTechnique.setVisible(false);
		harmonic.setVisible(false);
		accent.setVisible(false);
		linkNext.setVisible(false);
		splitIntoNotes.setVisible(false);
		onlyBox.setVisible(false);
		ignore.setVisible(false);
		passOtherNotes.setVisible(false);
		slideFret.setVisible(false);
		unpitchedSlide.setVisible(false);
		vibrato.setVisible(false);
		tremolo.setVisible(false);

		selectionBendEditor.setVisible(false);
	}

	private <T> T getValueFromSelectedStrings(final Function<Note, T> noteValueGetter,
			final Function<ChordNote, T> chordNoteValueGetter, final T defaultValue,
			final Collection<ChordOrNote> selected) {
		T value = null;

		for (final ChordOrNote sound : selected) {
			if (sound.isNote()) {
				if (!parent.isEdited(sound.note().string)) {
					continue;
				}

				final T newValue = noteValueGetter.apply(sound.note());
				if (value != null && value != newValue) {
					return defaultValue;
				}
				value = newValue;
			} else {
				final Chord chord = sound.chord();
				for (final Entry<Integer, ChordNote> chordNote : chord.chordNotes.entrySet()) {
					if (!parent.isEdited(chordNote.getKey())) {
						continue;
					}

					final T newValue = chordNoteValueGetter.apply(chordNote.getValue());
					if (value != null && value != newValue) {
						return defaultValue;
					}
					value = newValue;
				}
			}
		}

		return value == null ? defaultValue : value;
	}

	private void setFretsOnSelectionChange(final Collection<ChordOrNote> selected) {
		final Integer templateId = getSingleValue(selected,
				sound -> sound.isChord() ? sound.chord().templateId() : null, null);
		if (templateId != null) {
			chordTemplate = new ChordTemplate(chartData.currentArrangement().chordTemplates.get(templateId));
			setCurrentValuesInInputs();
			return;
		}

		final Integer fretValue = getSingleValue(selected, sound -> sound.isNote() ? sound.note().fret : null, null);
		final Integer stringValue = getSingleValue(selected, sound -> sound.isNote() ? sound.note().string : null,
				null);

		chordTemplate = new ChordTemplate();
		if (stringValue != null && fretValue != null) {
			chordTemplate.frets.put(stringValue, fretValue);
		}
		setCurrentValuesInInputs();
	}

	private void updateStringSelectionDependentValues() {
		final List<ChordOrNote> selected = selectionManager.getSelectedElements(PositionType.GUITAR_NOTE);

		mute.setSelected(getValueFromSelectedStrings(n -> n.mute, n -> n.mute, Mute.NONE, selected));
		hopo.setSelected(getValueFromSelectedStrings(n -> n.hopo, n -> n.hopo, HOPO.NONE, selected));
		harmonic.setSelected(getValueFromSelectedStrings(n -> n.harmonic, n -> n.harmonic, Harmonic.NONE, selected));
		linkNext.field.setSelected(getValueFromSelectedStrings(n -> n.linkNext, n -> n.linkNext, false, selected));
		vibrato.field.setSelected(getValueFromSelectedStrings(n -> n.vibrato, n -> n.vibrato, false, selected));
		tremolo.field.setSelected(getValueFromSelectedStrings(n -> n.tremolo, n -> n.tremolo, false, selected));

		final List<CommonNote> selectedNotesWithoutOpenStrings = selected.stream()//
				.flatMap(sound -> sound.notesWithFrets(chartData.currentChordTemplates()))//
				.filter(n -> n.fret() > 0 && parent.isEdited(n.string()))//
				.collect(Collectors.toList());

		final Pair<Integer, Boolean> slideValue = getSingleValue(selectedNotesWithoutOpenStrings,
				note -> new Pair<>(note.slideTo(), note.unpitchedSlide()), new Pair<>(null, false));

		slideFret.field.setTextWithoutEvent(slideValue.a == null ? "" : (slideValue.a + ""));
		unpitchedSlide.field.setSelected(slideValue.b);
	}

	public void selectionChanged(final ISelectionAccessor<ChordOrNote> selectedChordOrNotesAccessor,
			final boolean stringsCouldChange) {
		final List<ChordOrNote> selected = selectionManager.getSelectedElements(PositionType.GUITAR_NOTE);
		setFretsOnSelectionChange(selected);

		updateStringSelectionDependentValues();

		bassPickingTechnique.setSelected(getSingleValue(selected,
				sound -> sound.isNote() ? sound.note().bassPicking : null, BassPickingTechnique.NONE));

		final boolean accentValue = getSingleValue(selected, sound -> sound.asGuitarSound().accent, false);
		accent.field.setSelected(accentValue);

		final boolean splitIntoNotesValue = getSingleValue(selected,
				sound -> sound.isChord() ? sound.chord().splitIntoNotes : false, false);
		splitIntoNotes.field.setSelected(splitIntoNotesValue);

		final boolean forceNoNotesValue = getSingleValue(selected,
				sound -> sound.isChord() ? sound.chord().forceNoNotes : false, false);
		onlyBox.field.setSelected(forceNoNotesValue);

		final boolean ignoreValue = getSingleValue(selected, sound -> sound.asGuitarSound().ignore, false);
		ignore.field.setSelected(ignoreValue);

		final boolean passOtherNotesValue = getSingleValue(selected, sound -> sound.asGuitarSound().passOtherNotes,
				false);
		passOtherNotes.field.setSelected(passOtherNotesValue);

		if (selected.size() == 1) {
			selectionBendEditor.onChangeSelection(selected.get(0));
			selectionBendEditor.setVisible(true);
		} else {
			selectionBendEditor.setVisible(false);
		}
	}
}
