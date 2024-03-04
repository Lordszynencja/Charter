package log.charter.gui.components.selectionEditor;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static log.charter.data.ArrangementFixer.fixSoundLength;
import static log.charter.data.config.Config.frets;
import static log.charter.data.config.Config.maxStrings;
import static log.charter.gui.ChartPanelColors.getStringBasedColor;
import static log.charter.gui.components.selectionEditor.CurrentSelectionEditor.getSingleValue;
import static log.charter.gui.components.simple.TextInputWithValidation.ValueValidator.createIntValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JCheckBox;

import log.charter.data.ArrangementFixer;
import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.managers.selection.Selection;
import log.charter.data.managers.selection.SelectionAccessor;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.ChartPanelColors.StringColorLabelType;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.selectionEditor.bends.SelectionBendEditor;
import log.charter.gui.components.selectionEditor.chords.ChordTemplateEditor;
import log.charter.gui.components.selectionEditor.components.BasicCheckboxInput;
import log.charter.gui.components.selectionEditor.components.FretInput;
import log.charter.gui.components.selectionEditor.components.StringInput;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.simple.TextInputWithValidation;
import log.charter.gui.components.simple.ToggleButtonGroupInRow;
import log.charter.gui.components.utils.RowedPosition;
import log.charter.gui.handlers.mouseAndKeyboard.KeyboardHandler;
import log.charter.song.Arrangement;
import log.charter.song.ChordTemplate;
import log.charter.song.enums.BassPickingTechnique;
import log.charter.song.enums.HOPO;
import log.charter.song.enums.Harmonic;
import log.charter.song.enums.Mute;
import log.charter.song.notes.Chord;
import log.charter.song.notes.ChordNote;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.GuitarSound;
import log.charter.song.notes.Note;
import log.charter.song.notes.NoteInterface;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashSet2;
import log.charter.util.CollectionUtils.Pair;

public class GuitarSoundSelectionEditor extends ChordTemplateEditor {
	private ArrangementFixer arrangementFixer;
	private ChartData data;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	private ChordTemplate chordTemplate = new ChordTemplate();

	private int lastStringsAmount = maxStrings;
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

		for (int i = 0; i < maxStrings; i++) {
			final int string = i;
			final JCheckBox stringCheckbox = new JCheckBox((string + 1) + "");
			stringCheckbox.setForeground(getStringBasedColor(StringColorLabelType.NOTE, string, maxStrings));
			stringCheckbox.addActionListener(e -> updateStringSelectionDependentValues());
			parent.add(stringCheckbox, position.getAndAddX(40), position.getY(), 40, 20);
			strings.add(stringCheckbox);
		}
	}

	private void addMuteInputs(final CurrentSelectionEditor parent, final RowedPosition position) {
		mute = new ToggleButtonGroupInRow<Mute>(parent, position, 65, Label.MUTE, //
				makeChangeForNoteInterfaces(NoteInterface::mute), //
				asList(new Pair<>(Mute.NONE, Label.MUTE_NONE), //
						new Pair<>(Mute.FULL, Label.MUTE_FULL), //
						new Pair<>(Mute.PALM, Label.MUTE_PALM)));
	}

	private void addHOPOInputs(final CurrentSelectionEditor parent, final RowedPosition position) {
		hopo = new ToggleButtonGroupInRow<>(parent, position, 65, Label.HOPO, //
				makeChangeForNoteInterfaces(NoteInterface::hopo), //
				asList(new Pair<>(HOPO.NONE, Label.HOPO_NONE), //
						new Pair<>(HOPO.HAMMER_ON, Label.HOPO_HAMMER_ON), //
						new Pair<>(HOPO.PULL_OFF, Label.HOPO_PULL_OFF), //
						new Pair<>(HOPO.TAP, Label.HOPO_TAP)));
	}

	private void addBassPickingTechniqueInputs(final CurrentSelectionEditor parent, final RowedPosition position) {
		bassPickingTechnique = new ToggleButtonGroupInRow<>(parent, position, 65, Label.BASS_PICKING_TECHNIQUE, //
				makeChangeForNoteInterfaces(NoteInterface::bassPicking), //
				asList(new Pair<>(BassPickingTechnique.NONE, Label.BASS_PICKING_NONE), //
						new Pair<>(BassPickingTechnique.POP, Label.BASS_PICKING_POP), //
						new Pair<>(BassPickingTechnique.SLAP, Label.BASS_PICKING_SLAP)));
	}

	private void addHarmonicInputs(final CurrentSelectionEditor parent, final RowedPosition position) {
		harmonic = new ToggleButtonGroupInRow<>(parent, position, 65, Label.HARMONIC, //
				makeChangeForNoteInterfaces(NoteInterface::harmonic), //
				asList(new Pair<>(Harmonic.NONE, Label.HARMONIC_NONE), //
						new Pair<>(Harmonic.NORMAL, Label.HARMONIC_NORMAL), //
						new Pair<>(Harmonic.PINCH, Label.HARMONIC_PINCH)));
	}

	private void addSlideFretInput(final CurrentSelectionEditor parent, final RowedPosition position) {
		final TextInputWithValidation slideFretInput = new TextInputWithValidation(null, 30,
				createIntValidator(1, frets, true), (final Integer val) -> changeSlideFret(val), false);
		slideFret = new FieldWithLabel<>(Label.SLIDE_PANE_FRET, 60, 30, 20, slideFretInput, LabelPosition.LEFT);
		parent.add(slideFret, position, 100);
	}

	private void addUnpitchedSlideInput(final CurrentSelectionEditor parent, final RowedPosition position) {
		final JCheckBox unpitchedSlideInput = new JCheckBox();
		unpitchedSlideInput.addActionListener(a -> changeUnpitchedSlide(unpitchedSlideInput.isSelected()));
		unpitchedSlide = new FieldWithLabel<>(Label.SLIDE_PANE_UNPITCHED, 80, 20, 20, unpitchedSlideInput,
				LabelPosition.RIGHT_CLOSE);
		parent.add(unpitchedSlide, position, 90);
	}

	private void addChordTemplateEditorParts(final int x) {
		addChordNameSuggestionButton(x + 100, 0);
		addChordNameInput(x + 100, 1);
		addChordTemplateEditor(x, 3);
	}

	private void addBendEditor(final int x) {
		selectionBendEditor = new SelectionBendEditor(parent, data, selectionManager, undoSystem);
		selectionBendEditor.setLocation(x, parent.sizes.getY(2));
		parent.add(selectionBendEditor);
	}

	public void init(final CurrentSelectionEditor selectionEditor, final ArrangementFixer arrangementFixer,
			final ChartData data, final CharterFrame frame, final KeyboardHandler keyboardHandler,
			final SelectionManager selectionManager, final UndoSystem undoSystem) {
		super.init(data, frame, keyboardHandler, () -> chordTemplate, this::templateEdited);

		this.arrangementFixer = arrangementFixer;
		this.data = data;
		this.selectionManager = selectionManager;
		this.undoSystem = undoSystem;

		final RowedPosition position = new RowedPosition(20, selectionEditor.sizes);
		addChordStringsSelection(selectionEditor, position);
		position.newRow();

		string = StringInput.addField(selectionEditor, position, this::changeString);
		fret = FretInput.addField(selectionEditor, position, this::changeFret);
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
				makeChangeForNoteInterfaces(NoteInterface::vibrato));
		tremolo = BasicCheckboxInput.addField(parent, Label.TREMOLO, position,
				makeChangeForNoteInterfaces(NoteInterface::tremolo));

		addChordTemplateEditorParts(380);
		addBendEditor(1000);

		hideFields();
	}

	private void changeSelectedToChords() {
		string.field.setTextWithoutEvent("");
		fret.field.setTextWithoutEvent("");

		final Arrangement arrangement = data.getCurrentArrangement();
		final ArrayList2<ChordOrNote> sounds = data.getCurrentArrangementLevel().sounds;
		final int templateId = arrangement.getChordTemplateIdWithSave(chordTemplate);
		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		final HashSet2<Selection<ChordOrNote>> selected = selectionAccessor.getSelectedSet();

		for (final Selection<ChordOrNote> selection : selected) {
			if (selection.selectable.isChord()) {
				continue;
			}

			final ChordTemplate template = arrangement.chordTemplates.get(templateId);
			sounds.set(selection.id, selection.selectable.asChord(templateId, template));
		}

		if (selected.size() == 1) {
			selectionBendEditor.enableAndSelectStrings(selected.stream().findAny().get().selectable);
		}
	}

	private void changeSelectedChordsToNotes() {
		final Arrangement arrangement = data.getCurrentArrangement();
		final ArrayList2<ChordOrNote> sounds = data.getCurrentArrangementLevel().sounds;
		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		final HashSet2<Selection<ChordOrNote>> selected = selectionAccessor.getSelectedSet();

		clearChordName();

		for (final Selection<ChordOrNote> selection : selected) {
			if (selection.selectable.isNote()) {
				continue;
			}

			final ChordTemplate template = arrangement.chordTemplates.get(selection.selectable.chord().templateId());
			sounds.set(selection.id, selection.selectable.asNote(template));
		}

		if (selected.size() == 1) {
			selectionBendEditor.enableAndSelectStrings(selected.stream().findAny().get().selectable);
		}
	}

	private void changeString(final int newString) {
		undoSystem.addUndo();
		changeSelectedChordsToNotes();

		chordTemplate = new ChordTemplate();
		final String fretValue = fret.field.getText();
		if (fretValue != null && !fretValue.isBlank()) {
			chordTemplate.frets.put(newString, Integer.valueOf(fretValue));
		}

		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		final HashSet2<Selection<ChordOrNote>> selected = selectionAccessor.getSelectedSet();
		for (final Selection<ChordOrNote> selection : selected) {
			selection.selectable.note().string = newString;
		}

		if (selected.size() == 1) {
			selectionBendEditor.enableAndSelectStrings(selected.stream().findAny().get().selectable);
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

		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		for (final Selection<ChordOrNote> selection : selectionAccessor.getSelectedSet()) {
			selection.selectable.note().fret = newFret;
		}

		setCurrentValuesInInputs();
	}

	private void changeValueForSelected(final Consumer<Note> noteValueSetter,
			final Consumer<ChordNote> chordNoteValueSetter) {
		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		for (final Selection<ChordOrNote> selection : selectionAccessor.getSelectedSet()) {
			if (selection.selectable.isNote()) {
				if (isSelected(selection.selectable.note().string)) {
					noteValueSetter.accept(selection.selectable.note());
				}
			} else {
				selection.selectable.chord().chordNotes.forEach((string, chordNote) -> {
					if (isSelected(string)) {
						chordNoteValueSetter.accept(chordNote);
					}
				});
			}
		}
	}

	private <T, V> Consumer<T> makeChange(final Function<Stream<Selection<ChordOrNote>>, Stream<V>> filterMapper,
			final BiConsumer<V, T> onChange) {
		return t -> {
			undoSystem.addUndo();

			final Stream<Selection<ChordOrNote>> stream = selectionManager
					.<ChordOrNote>getSelectedAccessor(PositionType.GUITAR_NOTE)//
					.getSelectedSet()//
					.stream();

			filterMapper.apply(stream)//
					.forEach(v -> onChange.accept(v, t));
		};
	}

	private <T> Consumer<T> makeChangeForChords(final BiConsumer<Chord, T> onChange) {
		return makeChange(stream -> stream//
				.filter(selection -> selection.selectable.isChord())//
				.map(selection -> selection.selectable.chord()), //
				onChange);
	}

	private <T> Consumer<T> makeChangeForGuitarSounds(final BiConsumer<GuitarSound, T> onChange) {
		return makeChange(stream -> stream//
				.map(selection -> selection.selectable.asGuitarSound()), //
				onChange);
	}

	private <T> Consumer<T> makeChangeForNoteInterfaces(final BiConsumer<NoteInterface, T> onChange) {
		return makeChange(stream -> stream//
				.flatMap(selection -> selection.selectable.noteInterfaces()), //
				onChange);
	}

	private void changeLinkNext(final boolean newLinkNext) {
		undoSystem.addUndo();

		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		final ArrayList2<ChordOrNote> sounds = data.getCurrentArrangementLevel().sounds;
		final HashSet2<Selection<ChordOrNote>> selected = selectionAccessor.getSelectedSet();
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

			fixSoundLength(selection.id, sounds);
		}

		if (selected.size() == 1) {
			selectionBendEditor.onChangeSelection(selected.stream().findAny().get().selectable);
		}
	}

	private void cleanSlide() {
		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		for (final Selection<ChordOrNote> selection : selectionAccessor.getSelectedSet()) {
			if (selection.selectable.isNote()) {
				final Note note = selection.selectable.note();
				note.slideTo = null;

				if (note.linkNext) {
					final ChordOrNote nextSound = ChordOrNote.findNextSoundOnString(note.string, selection.id + 1,
							data.getCurrentArrangementLevel().sounds);
					if (nextSound != null && nextSound.isNote()) {
						nextSound.note().fret = note.fret;
					}
				}
			} else {
				selection.selectable.chord().chordNotes.values().forEach(n -> n.slideTo = null);
			}
		}
	}

	private void setSlide(final int newSlideFret) {
		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		final ArrayList2<ChordTemplate> chordTemplates = data.getCurrentArrangement().chordTemplates;

		for (final Selection<ChordOrNote> selection : selectionAccessor.getSelectedSet()) {
			if (selection.selectable.isNote()) {
				final Note note = selection.selectable.note();
				note.slideTo = newSlideFret;

				if (note.linkNext) {
					final ChordOrNote nextSound = ChordOrNote.findNextSoundOnString(note.string, selection.id + 1,
							data.getCurrentArrangementLevel().sounds);
					if (nextSound != null && nextSound.isNote()) {
						nextSound.note().fret = note.slideTo;
					}
				}
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

					chordNoteEntry.getValue().slideTo = min(frets, max(1, fret - fretDifference));
				}
			}
		}
	}

	private void changeSlideFret(final Integer newSlideFret) {
		undoSystem.addUndo();

		if (newSlideFret == null) {
			cleanSlide();
			return;
		}

		setSlide(newSlideFret);
	}

	private void changeUnpitchedSlide(final boolean newUnpitchedSlide) {
		undoSystem.addUndo();
		changeValueForSelected(n -> n.unpitchedSlide = newUnpitchedSlide, n -> n.unpitchedSlide = newUnpitchedSlide);
	}

	private void templateEdited() {
		if (chordTemplate.frets.isEmpty()) {
			return;
		}

		undoSystem.addUndo();

		final Arrangement arrangement = data.getCurrentArrangement();

		if (chordTemplate.frets.size() == 1) {
			changeSelectedChordsToNotes();

			final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
					.getSelectedAccessor(PositionType.GUITAR_NOTE);
			final HashSet2<Selection<ChordOrNote>> selected = selectionAccessor.getSelectedSet();

			for (final Entry<Integer, Integer> entry : chordTemplate.frets.entrySet()) {
				string.field.setTextWithoutEvent(entry.getKey() + "");
				fret.field.setTextWithoutEvent(entry.getValue() + "");
				for (final Selection<ChordOrNote> selection : selected) {
					selection.selectable.note().string = entry.getKey();
					selection.selectable.note().fret = entry.getValue();
				}
			}

			if (selected.size() == 1) {
				selectionBendEditor.enableAndSelectStrings(selected.stream().findAny().get().selectable);
			}
		} else {
			final int templateId = arrangement.getChordTemplateIdWithSave(chordTemplate);
			final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
					.getSelectedAccessor(PositionType.GUITAR_NOTE);
			final HashSet2<Selection<ChordOrNote>> selected = selectionAccessor.getSelectedSet();

			for (final Selection<ChordOrNote> selection : selected) {
				if (selection.selectable.isChord()) {
					selection.selectable.chord().updateTemplate(templateId, chordTemplate);
				}
			}

			changeSelectedToChords();
		}

		arrangementFixer.fixDuplicatedChordTemplates(arrangement);
		arrangementFixer.removeUnusedChordTemplates(arrangement);
	}

	@Override
	public void showFields() {
		super.showFields();

		for (int string = 0; string < data.currentStrings(); string++) {
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
			final HashSet2<Selection<ChordOrNote>> selected) {
		T value = null;

		for (final Selection<ChordOrNote> selection : selected) {
			if (selection.selectable.isNote()) {
				if (!isSelected(selection.selectable.note().string)) {
					continue;
				}

				final T newValue = noteValueGetter.apply(selection.selectable.note());
				if (value != null && value != newValue) {
					return defaultValue;
				}
				value = newValue;
			} else {
				final Chord chord = selection.selectable.chord();
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
		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		final HashSet2<Selection<ChordOrNote>> selected = selectionAccessor.getSelectedSet();

		mute.setSelected(getValueFromSelectedStrings(n -> n.mute, n -> n.mute, Mute.NONE, selected));
		hopo.setSelected(getValueFromSelectedStrings(n -> n.hopo, n -> n.hopo, HOPO.NONE, selected));
		harmonic.setSelected(getValueFromSelectedStrings(n -> n.harmonic, n -> n.harmonic, Harmonic.NONE, selected));
		linkNext.field.setSelected(getValueFromSelectedStrings(n -> n.linkNext, n -> n.linkNext, false, selected));
		unpitchedSlide.field.setSelected(
				getValueFromSelectedStrings(n -> n.unpitchedSlide, n -> n.unpitchedSlide, false, selected));
		vibrato.field.setSelected(getValueFromSelectedStrings(n -> n.vibrato, n -> n.vibrato, false, selected));
		tremolo.field.setSelected(getValueFromSelectedStrings(n -> n.tremolo, n -> n.tremolo, false, selected));

		final Integer slideFretValue = getSingleValue(selected, selection -> {
			if (selection.selectable.isNote()) {
				return selection.selectable.note().slideTo;
			}

			Integer slideTo = null;
			for (final Entry<Integer, ChordNote> chordNote : selection.selectable.chord().chordNotes.entrySet()) {
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

	private void setFretsOnSelectionChange(final HashSet2<Selection<ChordOrNote>> selected) {
		final Integer templateId = getSingleValue(selected,
				selection -> selection.selectable.isChord() ? selection.selectable.chord().templateId() : null, null);
		if (templateId != null) {
			chordTemplate = new ChordTemplate(data.getCurrentArrangement().chordTemplates.get(templateId));
			setCurrentValuesInInputs();
			fret.field.setTextWithoutEvent("");
			string.field.setTextWithoutEvent("");
			return;
		}

		final Integer fretValue = getSingleValue(selected,
				selection -> selection.selectable.isNote() ? selection.selectable.note().fret : null, null);
		final Integer stringValue = getSingleValue(selected,
				selection -> selection.selectable.isNote() ? selection.selectable.note().string : null, null);

		chordTemplate = new ChordTemplate();
		if (stringValue != null && fretValue != null) {
			chordTemplate.frets.put(stringValue, fretValue);
		}
		setCurrentValuesInInputs();

		fret.field.setTextWithoutEvent(fretValue == null ? "" : (fretValue + ""));
		string.field.setTextWithoutEvent(stringValue == null ? "" : ((stringValue + 1) + ""));
	}

	private void changeAvailableStrings() {
		if (data.currentStrings() == lastStringsAmount) {
			return;
		}

		lastStringsAmount = data.currentStrings();
		for (int string = 0; string < lastStringsAmount; string++) {
			strings.get(string)
					.setForeground(getStringBasedColor(StringColorLabelType.NOTE, string, lastStringsAmount));
		}
	}

	public void selectionChanged(final SelectionAccessor<ChordOrNote> selectedChordOrNotesAccessor,
			final boolean stringsCouldChange) {
		changeAvailableStrings();

		if (stringsCouldChange) {
			strings.forEach(string -> string.setSelected(true));
		}

		final HashSet2<Selection<ChordOrNote>> selected = selectedChordOrNotesAccessor.getSelectedSet();
		setFretsOnSelectionChange(selected);
		string.field.setValidator(createIntValidator(1, data.currentStrings(), false));

		updateStringSelectionDependentValues();

		bassPickingTechnique.setSelected(getSingleValue(selected,
				selection -> selection.selectable.isNote() ? selection.selectable.note().bassPicking : null,
				BassPickingTechnique.NONE));

		final boolean accentValue = getSingleValue(selected, selection -> selection.selectable.asGuitarSound().accent,
				false);
		accent.field.setSelected(accentValue);

		final boolean splitIntoNotesValue = getSingleValue(selected,
				selection -> selection.selectable.isChord() ? selection.selectable.chord().splitIntoNotes : false,
				false);
		splitIntoNotes.field.setSelected(splitIntoNotesValue);

		final boolean forceNoNotesValue = getSingleValue(selected,
				selection -> selection.selectable.isChord() ? selection.selectable.chord().forceNoNotes : false, false);
		forceNoNotes.field.setSelected(forceNoNotesValue);

		final boolean ignoreValue = getSingleValue(selected, selection -> selection.selectable.asGuitarSound().ignore,
				false);
		ignore.field.setSelected(ignoreValue);

		final boolean passOtherNotesValue = getSingleValue(selected,
				selection -> selection.selectable.asGuitarSound().passOtherNotes, false);
		passOtherNotes.field.setSelected(passOtherNotesValue);

		if (selected.size() == 1) {
			selectionBendEditor.onChangeSelection(selected.stream().findAny().get().selectable);
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
