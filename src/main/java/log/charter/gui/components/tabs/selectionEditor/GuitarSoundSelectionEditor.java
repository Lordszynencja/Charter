package log.charter.gui.components.tabs.selectionEditor;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static log.charter.data.config.ChartPanelColors.getStringBasedColor;
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
import log.charter.data.config.ChartPanelColors.StringColorLabelType;
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
import log.charter.gui.components.tabs.selectionEditor.simpleComponents.FretInput;
import log.charter.gui.components.tabs.selectionEditor.simpleComponents.StringInput;
import log.charter.gui.components.utils.RowedPosition;
import log.charter.gui.components.utils.validators.IntegerValueValidator;
import log.charter.gui.lookAndFeel.CharterCheckBox;
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

	private ChordTemplate chordTemplate = new ChordTemplate();

	private int lastStringsAmount = 0;
	private List<JCheckBox> strings;
	private FieldWithLabel<TextInputWithValidation> string;
	private FieldWithLabel<TextInputWithValidation> fret;

	private ToggleButtonGroupInRow<Mute> mute;
	private ToggleButtonGroupInRow<HOPO> hopo;
	private ToggleButtonGroupInRow<BassPickingTechnique> bassPickingTechnique;
	private ToggleButtonGroupInRow<Harmonic> harmonic;
	private FieldWithLabel<JCheckBox> accent;
	private FieldWithLabel<JCheckBox> linkNext;
	private FieldWithLabel<JCheckBox> splitIntoNotes;
	private FieldWithLabel<JCheckBox> forceNoNotes;
	private FieldWithLabel<JCheckBox> ignore;
	private FieldWithLabel<JCheckBox> passOtherNotes;
	private FieldWithLabel<TextInputWithValidation> slideFret;
	private FieldWithLabel<JCheckBox> unpitchedSlide;
	private FieldWithLabel<JCheckBox> vibrato;
	private FieldWithLabel<JCheckBox> tremolo;

	private SelectionBendEditor selectionBendEditor;

	public GuitarSoundSelectionEditor(final CurrentSelectionEditor parent) {
		super(parent);
	}

	private boolean isSelected(final int string) {
		return strings.get(string).isSelected();
	}

	private void addChordStringsSelection(final CurrentSelectionEditor parent, final RowedPosition position) {
		strings = new ArrayList<>();

		for (int i = 0; i < InstrumentConfig.maxStrings; i++) {
			final int string = i;
			final JCheckBox stringCheckbox = new JCheckBox((string + 1) + "");
			stringCheckbox.setFocusable(false);
			stringCheckbox.addActionListener(e -> updateStringSelectionDependentValues());
			parent.addWithSettingSize(stringCheckbox, position.getAndAddX(40), position.y(), 40, 20);
			strings.add(stringCheckbox);
		}
	}

	private void addMuteInputs(final CurrentSelectionEditor parent, final RowedPosition position) {
		mute = new ToggleButtonGroupInRow<>(parent, position, 65, Label.MUTE, //
				makeChangeForCommonNotes(NoteInterface::mute), //
				asList(new Pair<>(Mute.NONE, Label.MUTE_NONE), //
						new Pair<>(Mute.FULL, Label.MUTE_FULL), //
						new Pair<>(Mute.PALM, Label.MUTE_PALM)));
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
						new Pair<>(BassPickingTechnique.POP, Label.BASS_PICKING_POP), //
						new Pair<>(BassPickingTechnique.SLAP, Label.BASS_PICKING_SLAP)));
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

		final RowedPosition position = new RowedPosition(20, selectionEditor.sizes);
		addChordStringsSelection(selectionEditor, position);
		position.newRow();

		string = StringInput.addField(selectionEditor, position, this::changeString);
		string.field.setEnabled(false);
		fret = FretInput.addField(selectionEditor, position, this::changeFret);
		fret.field.setEnabled(false);
		position.newRow();

		addMuteInputs(selectionEditor, position);
		position.newRows(2);

		addHOPOInputs(selectionEditor, position);
		position.newRows(2);

		addBassPickingTechniqueInputs(selectionEditor, position);
		position.newRows(2);

		addHarmonicInputs(selectionEditor, position);
		position.newRows(2);

		accent = BasicCheckboxInput.addField(parent, Label.ACCENT, position,
				makeChangeForGuitarSounds(GuitarSound::accent));
		linkNext = BasicCheckboxInput.addField(parent, Label.LINK_NEXT, position, this::changeLinkNext);
		position.newRow();

		splitIntoNotes = BasicCheckboxInput.addField(parent, Label.SPLIT_INTO_NOTES, position,
				makeChangeForChords(Chord::splitIntoNotes));
		forceNoNotes = BasicCheckboxInput.addField(parent, Label.FORCE_NO_NOTES, position,
				makeChangeForChords(Chord::forceNoNotes));
		position.newRow();

		ignore = BasicCheckboxInput.addField(parent, Label.IGNORE, position,
				makeChangeForGuitarSounds(GuitarSound::ignore));
		passOtherNotes = BasicCheckboxInput.addField(parent, Label.PASS_OTHER_NOTES, position, 120,
				makeChangeForGuitarSounds(GuitarSound::passOtherNotes));
		position.newRow();

		addSlideFretInput(selectionEditor, position);
		addUnpitchedSlideInput(selectionEditor, position);
		position.newRow();

		vibrato = BasicCheckboxInput.addField(parent, Label.VIBRATO, position,
				makeChangeForCommonNotes(NoteInterface::vibrato));
		tremolo = BasicCheckboxInput.addField(parent, Label.TREMOLO, position,
				makeChangeForCommonNotes(NoteInterface::tremolo));

		addChordTemplateEditorParts(380);
		addBendEditor(1000);

		hideFields();
	}

	private void refreshBendEditorStrings() {
		final List<ChordOrNote> sounds = selectionManager.getSelectedElements(PositionType.GUITAR_NOTE);
		if (sounds.size() == 1) {
			selectionBendEditor.enableAndSelectStrings(sounds.get(0));
		}
	}

	private void changeSelectedToChords() {
		string.field.setTextWithoutEvent("");
		fret.field.setTextWithoutEvent("");

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

	private void changeString(final int newString) {
		undoSystem.addUndo();
		changeSelectedChordsToNotes();

		chordTemplate = new ChordTemplate();
		final String fretValue = fret.field.getText();
		if (fretValue != null && !fretValue.isBlank()) {
			chordTemplate.frets.put(newString, Integer.valueOf(fretValue));
		}

		final List<ChordOrNote> sounds = selectionManager.getSelectedElements(PositionType.GUITAR_NOTE);
		for (final ChordOrNote sound : sounds) {
			sound.note().string = newString;
		}

		if (sounds.size() == 1) {
			selectionBendEditor.enableAndSelectStrings(sounds.get(0));
		}

		setCurrentValuesInInputs();
	}

	private void changeFret(final int newFret) {
		undoSystem.addUndo();
		changeSelectedChordsToNotes();

		chordTemplate = new ChordTemplate();
		final String stringValue = string.field.getText();
		if (stringValue != null) {
			chordTemplate.frets.put(Integer.valueOf(stringValue) - 1, newFret);
		}

		final List<Selection<ChordOrNote>> selected = selectionManager.getSelected(PositionType.GUITAR_NOTE);
		for (final Selection<ChordOrNote> selection : selected) {
			selection.selectable.note().fret = newFret;
		}

		guitarSoundsStatusesHandler
				.updateLinkedNotes(selected.stream().map(s -> s.id).collect(Collectors.toCollection(ArrayList::new)));

		setCurrentValuesInInputs();
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
				.filter(note -> isSelected(note.string())), //

				onChange);
	}

	private void changeLinkNext(final boolean newLinkNext) {
		undoSystem.addUndo();

		final List<ChordOrNote> sounds = chartData.currentArrangementLevel().sounds;
		final List<Selection<ChordOrNote>> selected = selectionManager
				.<ChordOrNote>getSelected(PositionType.GUITAR_NOTE);

		for (final Selection<ChordOrNote> selection : selected) {
			if (selection.selectable.isNote()) {
				if (isSelected(selection.selectable.note().string)) {
					selection.selectable.note().linkNext = newLinkNext;
				}
			} else {
				selection.selectable.chord().chordNotes.forEach((string, chordNote) -> {
					if (isSelected(string)) {
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
						.filter(fret -> isSelected(fret.getKey()) && fret.getValue() > 0)//
						.map(fret -> fret.getValue())//
						.collect(Collectors.minBy(Integer::compare)).orElse(1);
				final int fretDifference = minFret - newSlideFret;
				for (final Entry<Integer, ChordNote> chordNoteEntry : chord.chordNotes.entrySet()) {
					if (!isSelected(chordNoteEntry.getKey())) {
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
				string.field.setTextWithoutEvent(entry.getKey() + "");
				fret.field.setTextWithoutEvent(entry.getValue() + "");
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

		for (int string = 0; string < chartData.currentStrings(); string++) {
			strings.get(string).setVisible(true);
		}
		string.setVisible(true);
		fret.setVisible(true);
		mute.setVisible(true);
		hopo.setVisible(true);
		bassPickingTechnique.setVisible(true);
		harmonic.setVisible(true);
		accent.setVisible(true);
		linkNext.setVisible(true);
		splitIntoNotes.setVisible(true);
		forceNoNotes.setVisible(true);
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

		strings.forEach(s -> s.setVisible(false));
		string.setVisible(false);
		fret.setVisible(false);
		mute.setVisible(false);
		hopo.setVisible(false);
		bassPickingTechnique.setVisible(false);
		harmonic.setVisible(false);
		accent.setVisible(false);
		linkNext.setVisible(false);
		splitIntoNotes.setVisible(false);
		forceNoNotes.setVisible(false);
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
				if (!isSelected(sound.note().string)) {
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
					if (!isSelected(chordNote.getKey())) {
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

	private void updateStringSelectionDependentValues() {
		final List<ChordOrNote> selected = selectionManager.getSelectedElements(PositionType.GUITAR_NOTE);

		mute.setSelected(getValueFromSelectedStrings(n -> n.mute, n -> n.mute, Mute.NONE, selected));
		hopo.setSelected(getValueFromSelectedStrings(n -> n.hopo, n -> n.hopo, HOPO.NONE, selected));
		harmonic.setSelected(getValueFromSelectedStrings(n -> n.harmonic, n -> n.harmonic, Harmonic.NONE, selected));
		linkNext.field.setSelected(getValueFromSelectedStrings(n -> n.linkNext, n -> n.linkNext, false, selected));
		unpitchedSlide.field.setSelected(
				getValueFromSelectedStrings(n -> n.unpitchedSlide, n -> n.unpitchedSlide, false, selected));
		vibrato.field.setSelected(getValueFromSelectedStrings(n -> n.vibrato, n -> n.vibrato, false, selected));
		tremolo.field.setSelected(getValueFromSelectedStrings(n -> n.tremolo, n -> n.tremolo, false, selected));

		final Integer slideFretValue = getSingleValue(selected, sound -> {
			if (sound.isNote()) {
				return sound.note().slideTo;
			}

			Integer slideTo = null;
			for (final Entry<Integer, ChordNote> chordNote : sound.chord().chordNotes.entrySet()) {
				if (!isSelected(chordNote.getKey()) || chordNote.getValue().slideTo == null) {
					continue;
				}

				slideTo = slideTo == null ? chordNote.getValue().slideTo//
						: min(slideTo, chordNote.getValue().slideTo);
			}

			return slideTo;
		}, null);

		slideFret.field.setTextWithoutEvent(slideFretValue == null ? "" : (slideFretValue + ""));
	}

	private void setFretsOnSelectionChange(final Collection<ChordOrNote> selected) {
		final Integer templateId = getSingleValue(selected,
				sound -> sound.isChord() ? sound.chord().templateId() : null, null);
		if (templateId != null) {
			chordTemplate = new ChordTemplate(chartData.currentArrangement().chordTemplates.get(templateId));
			setCurrentValuesInInputs();
			fret.field.setTextWithoutEvent("");
			string.field.setTextWithoutEvent("");
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

		fret.field.setTextWithoutEvent(fretValue == null ? "" : (fretValue + ""));
		string.field.setTextWithoutEvent(stringValue == null ? "" : ((stringValue + 1) + ""));
	}

	private void changeAvailableStrings() {
		if (chartData.currentStrings() == lastStringsAmount) {
			return;
		}

		lastStringsAmount = chartData.currentStrings();
		for (int string = 0; string < lastStringsAmount; string++) {
			strings.get(string).setIcon(new CharterCheckBox.CheckBoxIcon(
					getStringBasedColor(StringColorLabelType.NOTE, string, lastStringsAmount)));
		}
	}

	public void selectionChanged(final ISelectionAccessor<ChordOrNote> selectedChordOrNotesAccessor,
			final boolean stringsCouldChange) {
		changeAvailableStrings();

		if (stringsCouldChange) {
			strings.forEach(string -> string.setSelected(true));
		}

		final List<ChordOrNote> selected = selectionManager.getSelectedElements(PositionType.GUITAR_NOTE);
		setFretsOnSelectionChange(selected);
		string.field.setValidator(new IntegerValueValidator(1, chartData.currentStrings(), false));

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
		forceNoNotes.field.setSelected(forceNoNotesValue);

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

	public List<Integer> getSelectedStrings() {
		final List<Integer> selectedStrings = new ArrayList<>();
		for (int i = 0; i < strings.size(); i++) {
			if (strings.get(i).isSelected()) {
				selectedStrings.add(i);
			}
		}

		return selectedStrings;
	}
}
