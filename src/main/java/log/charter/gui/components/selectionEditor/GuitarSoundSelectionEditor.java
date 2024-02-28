package log.charter.gui.components.selectionEditor;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static log.charter.data.ArrangementFixer.fixSoundLength;
import static log.charter.data.config.Config.frets;
import static log.charter.data.config.Config.maxStrings;
import static log.charter.gui.ChartPanelColors.getStringBasedColor;
import static log.charter.gui.components.TextInputWithValidation.ValueValidator.createIntValidator;
import static log.charter.gui.components.selectionEditor.CurrentSelectionEditor.getSingleValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import log.charter.gui.components.FieldWithLabel;
import log.charter.gui.components.FieldWithLabel.LabelPosition;
import log.charter.gui.components.TextInputWithValidation;
import log.charter.gui.components.ToggleButtonGroupInRow;
import log.charter.gui.components.selectionEditor.subEditors.SelectionBendEditor;
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
import log.charter.song.notes.Note;
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

	private ToggleButtonGroupInRow<Mute> mute; // changed
	private ToggleButtonGroupInRow<HOPO> hopo; // changed
	private ToggleButtonGroupInRow<BassPickingTechnique> bassPickingTechnique; // changed
	private ToggleButtonGroupInRow<Harmonic> harmonic; // changed
	private FieldWithLabel<JCheckBox> accent;
	private FieldWithLabel<JCheckBox> linkNext;
	private FieldWithLabel<JCheckBox> splitIntoNotes;
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

	private void addChordStringsSelection(final CurrentSelectionEditor parent, final AtomicInteger row) {
		strings = new ArrayList<>();

		for (int i = 0; i < maxStrings; i++) {
			final int string = i;
			final JCheckBox stringCheckbox = new JCheckBox((string + 1) + "");
			stringCheckbox.setForeground(getStringBasedColor(StringColorLabelType.NOTE, string, maxStrings));
			stringCheckbox.addActionListener(e -> updateStringSelectionDependentValues());
			parent.add(stringCheckbox, 20 + 40 * i, parent.getY(row.get()), 40, 20);

			strings.add(stringCheckbox);
		}

		row.getAndIncrement();
	}

	private void addStringFretInputs(final CurrentSelectionEditor parent, final AtomicInteger row) {
		final TextInputWithValidation stringInput = new TextInputWithValidation(null, 30,
				createIntValidator(1, 1, false), (final Integer val) -> changeString(val - 1), false);
		string = new FieldWithLabel<>(Label.STRING, 40, 30, 20, stringInput, LabelPosition.LEFT_CLOSE);
		string.setLocation(20, parent.getY(row.get()));
		parent.add(string);

		final TextInputWithValidation fretInput = new TextInputWithValidation(null, 30,
				createIntValidator(0, frets, false), (final Integer val) -> changeFret(val), false);
		fret = new FieldWithLabel<>(Label.FRET, 40, 30, 20, fretInput, LabelPosition.LEFT_CLOSE);
		fret.setLocation(110, parent.getY(row.getAndIncrement()));
		parent.add(fret);
	}

	private void addMuteInputs(final CurrentSelectionEditor parent, final AtomicInteger row) {
		mute = new ToggleButtonGroupInRow<>(parent, 20, row, 65, 270, Label.MUTE, this::changeMute, //
				asList(new Pair<>(Mute.NONE, Label.MUTE_NONE), // moved to start
						new Pair<>(Mute.FULL, Label.MUTE_FULL), //
						new Pair<>(Mute.PALM, Label.MUTE_PALM)));
	}

	private void addHOPOInputs(final CurrentSelectionEditor parent, final AtomicInteger row) {
		hopo = new ToggleButtonGroupInRow<>(parent, 20, row, 65, 270, Label.HOPO, this::changeHOPO, //
				asList(new Pair<>(HOPO.NONE, Label.HOPO_NONE), // moved to start
						new Pair<>(HOPO.HAMMER_ON, Label.HOPO_HAMMER_ON), //
						new Pair<>(HOPO.PULL_OFF, Label.HOPO_PULL_OFF), //
						new Pair<>(HOPO.TAP, Label.HOPO_TAP)));
	}

	private void addBassPickingTechniqueInputs(final CurrentSelectionEditor parent, final AtomicInteger row) {
		bassPickingTechnique = new ToggleButtonGroupInRow<>(parent, 20, row, 65, 270, Label.BASS_PICKING_TECHNIQUE,
				this::changeBassPickingTechnique, //
				asList(new Pair<>(BassPickingTechnique.NONE, Label.BASS_PICKING_NONE), // moved to start
						new Pair<>(BassPickingTechnique.POP, Label.BASS_PICKING_POP), //
						new Pair<>(BassPickingTechnique.SLAP, Label.BASS_PICKING_SLAP)));
	}

	private void addHarmonicInputs(final CurrentSelectionEditor parent, final AtomicInteger row) {
		harmonic = new ToggleButtonGroupInRow<>(parent, 20, row, 65, 270, Label.HARMONIC, this::changeHarmonic, //
				asList(new Pair<>(Harmonic.NONE, Label.HARMONIC_NONE), // moved to start
						new Pair<>(Harmonic.NORMAL, Label.HARMONIC_NORMAL), //
						new Pair<>(Harmonic.PINCH, Label.HARMONIC_PINCH)));
	}

	private void addAccentLinkNextInputs(final CurrentSelectionEditor parent, final AtomicInteger row) {
		int x = 20;
		final JCheckBox accentInput = new JCheckBox();
		accentInput.addActionListener(a -> changeAccent(accentInput.isSelected()));
		accent = new FieldWithLabel<>(Label.ACCENT, 50, 20, 20, accentInput, LabelPosition.LEFT_CLOSE);
		accent.setLocation(x, parent.getY(row.get()));
		parent.add(accent);

		x += 90;
		final JCheckBox linkNextInput = new JCheckBox();
		linkNextInput.addActionListener(a -> changeLinkNext(linkNextInput.isSelected()));
		linkNext = new FieldWithLabel<>(Label.LINK_NEXT, 60, 20, 20, linkNextInput, LabelPosition.LEFT_CLOSE);
		linkNext.setLocation(x, parent.getY(row.getAndIncrement()));
		parent.add(linkNext);
	}

	private void addSplitIntoNotesIgnoreInputs(final CurrentSelectionEditor parent, final AtomicInteger row) {
		int x = 20;
		final JCheckBox splitIntoNotesInput = new JCheckBox();
		splitIntoNotesInput.addActionListener(a -> changeSplitIntoNotes(splitIntoNotesInput.isSelected()));
		splitIntoNotes = new FieldWithLabel<>(Label.SPLIT_INTO_NOTES, 50, 20, 20, splitIntoNotesInput,
				LabelPosition.LEFT_CLOSE);
		splitIntoNotes.setLocation(x, parent.getY(row.get()));
		parent.add(splitIntoNotes);

		x += 90;
		final JCheckBox ignoreInput = new JCheckBox();
		ignoreInput.addActionListener(a -> changeIgnore(ignoreInput.isSelected()));
		ignore = new FieldWithLabel<>(Label.IGNORE_NOTE, 60, 20, 20, ignoreInput, LabelPosition.LEFT_CLOSE);
		ignore.setLocation(x, parent.getY(row.getAndIncrement()));
		parent.add(ignore);
	}

	private void addPassOtherNotesInputs(final CurrentSelectionEditor parent, final AtomicInteger row) {
		final JCheckBox passOtherNotesInput = new JCheckBox();
		passOtherNotesInput.addActionListener(a -> changePassOtherNotes(passOtherNotesInput.isSelected()));
		passOtherNotes = new FieldWithLabel<>(Label.PASS_OTHER_NOTES, 120, 20, 20, passOtherNotesInput,
				LabelPosition.LEFT_CLOSE);
		passOtherNotes.setLocation(50, parent.getY(row.getAndIncrement()));
		parent.add(passOtherNotes);
	}

	private void addSlideInputs(final CurrentSelectionEditor parent, final AtomicInteger row) {
		final TextInputWithValidation slideFretInput = new TextInputWithValidation(null, 30,
				createIntValidator(1, frets, true), (final Integer val) -> changeSlideFret(val), false);
		slideFret = new FieldWithLabel<>(Label.SLIDE_PANE_FRET, 60, 30, 20, slideFretInput, LabelPosition.LEFT);
		slideFret.setLocation(20, parent.getY(row.get()));
		parent.add(slideFret);

		final JCheckBox unpitchedSlideInput = new JCheckBox();
		unpitchedSlideInput.addActionListener(a -> changeUnpitchedSlide(unpitchedSlideInput.isSelected()));
		unpitchedSlide = new FieldWithLabel<>(Label.SLIDE_PANE_UNPITCHED, 80, 20, 20, unpitchedSlideInput,
				LabelPosition.RIGHT_CLOSE);
		unpitchedSlide.setLocation(115, parent.getY(row.getAndIncrement()));
		parent.add(unpitchedSlide);
	}

	private void addVibratoTremoloInput(final CurrentSelectionEditor parent, final AtomicInteger row) {
		final JCheckBox vibratoInput = new JCheckBox();
		vibratoInput.addActionListener(a -> changeVibrato(vibratoInput.isSelected()));
		vibrato = new FieldWithLabel<>(Label.VIBRATO, 60, 30, 20, vibratoInput, LabelPosition.LEFT_CLOSE);
		vibrato.setLocation(20, parent.getY(row.get()));
		parent.add(vibrato);

		final JCheckBox tremoloInput = new JCheckBox();
		tremoloInput.addActionListener(a -> changeTremolo(tremoloInput.isSelected()));
		tremolo = new FieldWithLabel<>(Label.TREMOLO, 50, 20, 20, tremoloInput, LabelPosition.LEFT_CLOSE);
		tremolo.setLocation(120, parent.getY(row.getAndIncrement()));
		parent.add(tremolo);
	}

	private void addChordTemplateEditorParts(final int x) {
		addChordNameSuggestionButton(x + 100, 0);
		addChordNameInput(x + 100, 1);
		addChordTemplateEditor(x, 3);
	}

	private void addBendEditor(final int x) {
		selectionBendEditor = new SelectionBendEditor(parent, data, selectionManager, undoSystem);
		selectionBendEditor.setLocation(x, parent.rowHeight * 2);
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

		final AtomicInteger row = new AtomicInteger();

		addChordStringsSelection(selectionEditor, row);
		addStringFretInputs(selectionEditor, row);
		addMuteInputs(selectionEditor, row);
		addHOPOInputs(selectionEditor, row);
		addBassPickingTechniqueInputs(selectionEditor, row);
		addHarmonicInputs(selectionEditor, row);
		addAccentLinkNextInputs(selectionEditor, row);
		addSplitIntoNotesIgnoreInputs(selectionEditor, row);
		addPassOtherNotesInputs(selectionEditor, row);
		addSlideInputs(selectionEditor, row);
		addVibratoTremoloInput(selectionEditor, row);
		addChordTemplateEditorParts(380);
		addBendEditor(1000);

		hideFields();
	}

	private void changeToChordsOnly() {
		string.field.setTextWithoutEvent("");
		fret.field.setTextWithoutEvent("");

		final Arrangement arrangement = data.getCurrentArrangement();
		final int templateId = arrangement.getChordTemplateIdWithSave(chordTemplate);
		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		final HashSet2<Selection<ChordOrNote>> selected = selectionAccessor.getSelectedSet();

		for (final Selection<ChordOrNote> selection : selected) {
			if (selection.selectable.isChord()) {
				continue;
			}

			selection.selectable.turnToChord(templateId, arrangement.chordTemplates.get(templateId));
		}

		if (selected.size() == 1) {
			selectionBendEditor.enableAndSelectStrings(selected.stream().findAny().get().selectable);
		}
	}

	private void changeSelectedChordsToNotes() {
		final Arrangement arrangement = data.getCurrentArrangement();
		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		final HashSet2<Selection<ChordOrNote>> selected = selectionAccessor.getSelectedSet();

		clearChordName();

		for (final Selection<ChordOrNote> selection : selected) {
			if (selection.selectable.isNote()) {
				continue;
			}

			final ChordTemplate template = arrangement.chordTemplates.get(selection.selectable.chord.templateId());
			selection.selectable.turnToNote(template);
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
			selection.selectable.note.string = newString;
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
			selection.selectable.note.fret = newFret;
		}

		setCurrentValuesInInputs();
	}

	private void changeValueForSelected(final Consumer<Note> noteValueSetter,
			final Consumer<ChordNote> chordNoteValueSetter) {
		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		for (final Selection<ChordOrNote> selection : selectionAccessor.getSelectedSet()) {
			if (selection.selectable.isNote()) {
				if (isSelected(selection.selectable.note.string)) {
					noteValueSetter.accept(selection.selectable.note);
				}
			} else {
				selection.selectable.chord.chordNotes.forEach((string, chordNote) -> {
					if (isSelected(string)) {
						chordNoteValueSetter.accept(chordNote);
					}
				});
			}
		}
	}

	private void changeMute(final Mute newMute) {
		undoSystem.addUndo();
		changeValueForSelected(n -> n.mute = newMute, n -> n.mute = newMute);
	}

	private void changeHOPO(final HOPO newHOPO) {
		undoSystem.addUndo();
		changeValueForSelected(n -> n.hopo = newHOPO, n -> n.hopo = newHOPO);
	}

	private void changeBassPickingTechnique(final BassPickingTechnique newBassPickingTechnique) {
		undoSystem.addUndo();
		changeSelectedChordsToNotes();
		changeValueForSelected(n -> n.bassPicking = newBassPickingTechnique, n -> {});
	}

	private void changeHarmonic(final Harmonic newHarmonic) {
		undoSystem.addUndo();
		changeValueForSelected(n -> n.harmonic = newHarmonic, n -> n.harmonic = newHarmonic);
	}

	private void changeAccent(final boolean newAccent) {
		undoSystem.addUndo();

		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		for (final Selection<ChordOrNote> selection : selectionAccessor.getSelectedSet()) {
			selection.selectable.asGuitarSound().accent = newAccent;
		}
	}

	private void changeLinkNext(final boolean newLinkNext) {
		undoSystem.addUndo();

		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		final ArrayList2<ChordOrNote> sounds = data.getCurrentArrangementLevel().sounds;
		final HashSet2<Selection<ChordOrNote>> selected = selectionAccessor.getSelectedSet();
		for (final Selection<ChordOrNote> selection : selected) {
			if (selection.selectable.isNote()) {
				if (isSelected(selection.selectable.note.string)) {
					selection.selectable.note.linkNext = newLinkNext;
				}
			} else {
				selection.selectable.chord.chordNotes.forEach((string, chordNote) -> {
					if (isSelected(string)) {
						chordNote.linkNext = newLinkNext;
					}
				});
			}

			final int nextId = selection.id + 1;
			if (nextId < sounds.size()) {
				final ChordOrNote nextSound = sounds.get(nextId);
				if (nextSound.isChord()) {
					nextSound.chord.splitIntoNotes = true;
				}
			}

			fixSoundLength(selection.id, sounds);
		}

		if (selected.size() == 1) {
			selectionBendEditor.onChangeSelection(selected.stream().findAny().get().selectable);
		}
	}

	private void changeSplitIntoNotes(final boolean newSplitIntoNotes) {
		undoSystem.addUndo();

		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		for (final Selection<ChordOrNote> selection : selectionAccessor.getSelectedSet()) {
			if (!selection.selectable.isChord()) {
				continue;
			}

			selection.selectable.chord.splitIntoNotes = newSplitIntoNotes;
		}
	}

	private void changeIgnore(final boolean newIgnore) {
		undoSystem.addUndo();

		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		for (final Selection<ChordOrNote> selection : selectionAccessor.getSelectedSet()) {
			selection.selectable.asGuitarSound().ignore = newIgnore;
		}
	}

	private void changePassOtherNotes(final boolean newPassOtherNotes) {
		undoSystem.addUndo();

		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		for (final Selection<ChordOrNote> selection : selectionAccessor.getSelectedSet()) {
			selection.selectable.asGuitarSound().passOtherNotes = newPassOtherNotes;
		}
	}

	private void cleanSlide() {
		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		for (final Selection<ChordOrNote> selection : selectionAccessor.getSelectedSet()) {
			if (selection.selectable.isNote()) {
				final Note note = selection.selectable.note;
				note.slideTo = null;

				if (note.linkNext) {
					final ChordOrNote nextSound = ChordOrNote.findNextSoundOnString(note.string, selection.id + 1,
							data.getCurrentArrangementLevel().sounds);
					if (nextSound != null && nextSound.isNote()) {
						nextSound.note.fret = note.fret;
					}
				}
			} else {
				selection.selectable.chord.chordNotes.values().forEach(n -> n.slideTo = null);
			}
		}
	}

	private void setSlide(final int newSlideFret) {
		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		final ArrayList2<ChordTemplate> chordTemplates = data.getCurrentArrangement().chordTemplates;

		for (final Selection<ChordOrNote> selection : selectionAccessor.getSelectedSet()) {
			if (selection.selectable.isNote()) {
				final Note note = selection.selectable.note;
				note.slideTo = newSlideFret;

				if (note.linkNext) {
					final ChordOrNote nextSound = ChordOrNote.findNextSoundOnString(note.string, selection.id + 1,
							data.getCurrentArrangementLevel().sounds);
					if (nextSound != null && nextSound.isNote()) {
						nextSound.note.fret = note.slideTo;
					}
				}
			} else {
				final Chord chord = selection.selectable.chord;
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

	private void changeVibrato(final boolean newVibrato) {
		undoSystem.addUndo();
		changeValueForSelected(n -> n.vibrato = newVibrato, n -> n.vibrato = newVibrato);
	}

	private void changeTremolo(final boolean newTremolo) {
		undoSystem.addUndo();
		changeValueForSelected(n -> n.tremolo = newTremolo, n -> n.tremolo = newTremolo);
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
					selection.selectable.note.string = entry.getKey();
					selection.selectable.note.fret = entry.getValue();
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
					selection.selectable.chord.updateTemplate(templateId, chordTemplate);
				}
			}

			changeToChordsOnly();
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
				if (!isSelected(selection.selectable.note.string)) {
					continue;
				}

				final T newValue = noteValueGetter.apply(selection.selectable.note);
				if (value != null && value != newValue) {
					return defaultValue;
				}
				value = newValue;
			} else {
				final Chord chord = selection.selectable.chord;
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
				return selection.selectable.note.slideTo;
			}

			Integer slideTo = null;
			for (final Entry<Integer, ChordNote> chordNote : selection.selectable.chord.chordNotes.entrySet()) {
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
				selection -> selection.selectable.isChord() ? selection.selectable.chord.templateId() : null, null);
		if (templateId != null) {
			chordTemplate = new ChordTemplate(data.getCurrentArrangement().chordTemplates.get(templateId));
			setCurrentValuesInInputs();
			fret.field.setTextWithoutEvent("");
			string.field.setTextWithoutEvent("");
			return;
		}

		final Integer fretValue = getSingleValue(selected,
				selection -> selection.selectable.isNote() ? selection.selectable.note.fret : null, null);
		final Integer stringValue = getSingleValue(selected,
				selection -> selection.selectable.isNote() ? selection.selectable.note.string : null, null);

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
				selection -> selection.selectable.isNote() ? selection.selectable.note.bassPicking : null,
				BassPickingTechnique.NONE));

		final boolean accentValue = getSingleValue(selected, selection -> selection.selectable.asGuitarSound().accent,
				false);
		accent.field.setSelected(accentValue);

		final boolean splitIntoNotesValue = getSingleValue(selected,
				selection -> selection.selectable.isChord() ? selection.selectable.chord.splitIntoNotes : false, false);
		splitIntoNotes.field.setSelected(splitIntoNotesValue);

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
