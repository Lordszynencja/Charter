package log.charter.gui.components.selectionEditor;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static log.charter.data.config.Config.frets;
import static log.charter.gui.components.TextInputWithValidation.ValueValidator.createIntValidator;
import static log.charter.gui.components.selectionEditor.CurrentSelectionEditor.getSingleValue;

import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
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
import log.charter.gui.components.FieldWithLabel;
import log.charter.gui.components.FieldWithLabel.LabelPosition;
import log.charter.gui.components.RadioButtonGroupInRow;
import log.charter.gui.components.TextInputWithValidation;
import log.charter.gui.components.selectionEditor.subEditors.SelectionBendEditor;
import log.charter.song.ArrangementChart;
import log.charter.song.ChordTemplate;
import log.charter.song.enums.BassPickingTechnique;
import log.charter.song.enums.HOPO;
import log.charter.song.enums.Harmonic;
import log.charter.song.enums.Mute;
import log.charter.song.notes.Chord;
import log.charter.song.notes.ChordNote;
import log.charter.song.notes.ChordOrNote;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashSet2;
import log.charter.util.CollectionUtils.Pair;

public class GuitarSoundSelectionEditor extends ChordTemplateEditor {
	private ArrangementFixer arrangementFixer;
	private ChartData data;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	private ChordTemplate chordTemplate = new ChordTemplate();
	private FieldWithLabel<TextInputWithValidation> string;
	private FieldWithLabel<TextInputWithValidation> fret;
	private RadioButtonGroupInRow<Mute> mute;
	private RadioButtonGroupInRow<HOPO> hopo;
	private RadioButtonGroupInRow<BassPickingTechnique> bassPickingTechnique;
	private RadioButtonGroupInRow<Harmonic> harmonic;
	private FieldWithLabel<JCheckBox> accent;
	private FieldWithLabel<JCheckBox> linkNext;
	private FieldWithLabel<JCheckBox> splitIntoNotes;
	private FieldWithLabel<JCheckBox> ignore;
	private FieldWithLabel<TextInputWithValidation> slideFret;
	private FieldWithLabel<JCheckBox> unpitchedSlide;
	private FieldWithLabel<JCheckBox> vibrato;
	private FieldWithLabel<JCheckBox> tremolo;

	private SelectionBendEditor selectionBendEditor;

	public GuitarSoundSelectionEditor(final CurrentSelectionEditor parent) {
		super(parent);
	}

	private void addStringInput(final CurrentSelectionEditor parent, final AtomicInteger row) {
		final TextInputWithValidation stringInput = new TextInputWithValidation(null, 30,
				createIntValidator(1, 1, false), (final Integer val) -> changeString(val - 1), false);
		string = new FieldWithLabel<>(Label.STRING, 60, 30, 20, stringInput, LabelPosition.LEFT);
		string.setLocation(20, parent.getY(row.getAndIncrement()));
		parent.add(string);
	}

	private void addFretInput(final CurrentSelectionEditor parent, final AtomicInteger row) {
		final TextInputWithValidation fretInput = new TextInputWithValidation(null, 30,
				createIntValidator(0, frets, false), (final Integer val) -> changeFret(val), false);
		fret = new FieldWithLabel<>(Label.FRET, 60, 30, 20, fretInput, LabelPosition.LEFT);
		fret.setLocation(20, parent.getY(row.getAndIncrement()));
		parent.add(fret);
	}

	private void addMuteInputs(final CurrentSelectionEditor parent, final AtomicInteger row) {
		mute = new RadioButtonGroupInRow<>(parent, 20, row, 65, 200, Label.MUTE, this::changeMute, //
				asList(new Pair<>(Mute.FULL, Label.MUTE_FULL), //
						new Pair<>(Mute.PALM, Label.MUTE_PALM), //
						new Pair<>(Mute.NONE, Label.MUTE_NONE)));
	}

	private void addHOPOInputs(final CurrentSelectionEditor parent, final AtomicInteger row) {
		hopo = new RadioButtonGroupInRow<>(parent, 20, row, 65, 270, Label.HOPO, this::changeHOPO, //
				asList(new Pair<>(HOPO.HAMMER_ON, Label.HOPO_HAMMER_ON), //
						new Pair<>(HOPO.PULL_OFF, Label.HOPO_PULL_OFF), //
						new Pair<>(HOPO.TAP, Label.HOPO_TAP), //
						new Pair<>(HOPO.NONE, Label.HOPO_NONE)));
	}

	private void addBassPickingTechniqueInputs(final CurrentSelectionEditor parent, final AtomicInteger row) {
		bassPickingTechnique = new RadioButtonGroupInRow<>(parent, 20, row, 65, 270, Label.BASS_PICKING_TECHNIQUE,
				this::changeBassPickingTechnique, //
				asList(new Pair<>(BassPickingTechnique.POP, Label.BASS_PICKING_POP), //
						new Pair<>(BassPickingTechnique.SLAP, Label.BASS_PICKING_SLAP), //
						new Pair<>(BassPickingTechnique.NONE, Label.BASS_PICKING_NONE)));
	}

	private void addHarmonicInputs(final CurrentSelectionEditor parent, final AtomicInteger row) {
		harmonic = new RadioButtonGroupInRow<>(parent, 20, row, 65, 270, Label.HARMONIC, this::changeHarmonic, //
				asList(new Pair<>(Harmonic.NORMAL, Label.HARMONIC_NORMAL), //
						new Pair<>(Harmonic.PINCH, Label.HARMONIC_PINCH), //
						new Pair<>(Harmonic.NONE, Label.HARMONIC_NONE)));
	}

	private void addAccentLinkNextSplitIntoNotesIgnoreInputs(final CurrentSelectionEditor parent,
			final AtomicInteger row) {
		int x = 10;

		final JCheckBox accentInput = new JCheckBox();
		accentInput.addActionListener(a -> changeAccent(accentInput.isSelected()));
		accent = new FieldWithLabel<JCheckBox>(Label.ACCENT, 50, 20, 20, accentInput, LabelPosition.LEFT_CLOSE);
		accent.setLocation(x, parent.getY(row.get()));
		parent.add(accent);

		x += 90;
		final JCheckBox linkNextInput = new JCheckBox();
		linkNextInput.addActionListener(a -> changeLinkNext(linkNextInput.isSelected()));
		linkNext = new FieldWithLabel<JCheckBox>(Label.LINK_NEXT, 60, 20, 20, linkNextInput, LabelPosition.LEFT_CLOSE);
		linkNext.setLocation(x, parent.getY(row.get()));
		parent.add(linkNext);

		x += 70;
		final JCheckBox splitIntoNotesInput = new JCheckBox();
		splitIntoNotesInput.addActionListener(a -> changeSplitIntoNotes(splitIntoNotesInput.isSelected()));
		splitIntoNotes = new FieldWithLabel<JCheckBox>(Label.SPLIT_INTO_NOTES, 60, 20, 20, splitIntoNotesInput,
				LabelPosition.LEFT_CLOSE);
		splitIntoNotes.setLocation(x, parent.getY(row.get()));
		parent.add(splitIntoNotes);

		x += 90;
		final JCheckBox ignoreInput = new JCheckBox();
		ignoreInput.addActionListener(a -> changeIgnore(ignoreInput.isSelected()));
		ignore = new FieldWithLabel<JCheckBox>(Label.IGNORE_NOTE, 60, 20, 20, ignoreInput, LabelPosition.LEFT_CLOSE);
		ignore.setLocation(x, parent.getY(row.getAndIncrement()));
		parent.add(ignore);
	}

	private void addSlideInputs(final CurrentSelectionEditor parent, final AtomicInteger row) {
		final TextInputWithValidation slideFretInput = new TextInputWithValidation(null, 30,
				createIntValidator(1, frets, true), (final Integer val) -> changeSlideFret(val), false);
		slideFret = new FieldWithLabel<>(Label.SLIDE_PANE_FRET, 60, 30, 20, slideFretInput, LabelPosition.LEFT);
		slideFret.setLocation(20, parent.getY(row.get()));
		parent.add(slideFret);

		final JCheckBox unpitchedSlideInput = new JCheckBox();
		unpitchedSlideInput.addActionListener(a -> changeUnpitchedSlide(unpitchedSlideInput.isSelected()));
		unpitchedSlide = new FieldWithLabel<JCheckBox>(Label.SLIDE_PANE_UNPITCHED, 80, 20, 20, unpitchedSlideInput,
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
		tremolo = new FieldWithLabel<JCheckBox>(Label.TREMOLO, 80, 20, 20, tremoloInput, LabelPosition.LEFT_CLOSE);
		tremolo.setLocation(100, parent.getY(row.getAndIncrement()));
		parent.add(tremolo);
	}

	private void addBendEditor() {
		selectionBendEditor = new SelectionBendEditor(parent, data, selectionManager, undoSystem);
		selectionBendEditor.setLocation(850, parent.rowHeight * 2);
		parent.add(selectionBendEditor);
	}

	public void init(final CurrentSelectionEditor selectionEditor, final ArrangementFixer arrangementFixer,
			final ChartData data, final SelectionManager selectionManager, final UndoSystem undoSystem) {
		super.init(data, () -> chordTemplate, this::templateEdited);

		this.arrangementFixer = arrangementFixer;
		this.data = data;
		this.selectionManager = selectionManager;
		this.undoSystem = undoSystem;

		final AtomicInteger row = new AtomicInteger();

		addStringInput(selectionEditor, row);
		addFretInput(selectionEditor, row);
		addMuteInputs(selectionEditor, row);
		addHOPOInputs(selectionEditor, row);
		addBassPickingTechniqueInputs(selectionEditor, row);
		addHarmonicInputs(selectionEditor, row);
		addAccentLinkNextSplitIntoNotesIgnoreInputs(selectionEditor, row);
		addSlideInputs(selectionEditor, row);
		addVibratoTremoloInput(selectionEditor, row);

		addChordNameSuggestionButton(400, 0);
		addChordNameInput(400, 1);
		addChordTemplateEditor(300, 3);

		addBendEditor();

		hideFields();
	}

	private void changeToChordsOnly() {
		string.field.setTextWithoutEvent("");
		fret.field.setTextWithoutEvent("");

		final ArrangementChart arrangement = data.getCurrentArrangement();
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
		final ArrangementChart arrangement = data.getCurrentArrangement();
		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		final HashSet2<Selection<ChordOrNote>> selected = selectionAccessor.getSelectedSet();

		chordNameInput.setTextWithoutUpdate("");
		chordNameInput.removePopup();

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

	private void changeMute(final Mute newMute) {
		undoSystem.addUndo();

		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		for (final Selection<ChordOrNote> selection : selectionAccessor.getSelectedSet()) {
			if (selection.selectable.isNote()) {
				selection.selectable.note.mute = newMute;
			} else {
				selection.selectable.chord.chordNotes.values().forEach(n -> n.mute = newMute);
			}
		}
	}

	private void changeHOPO(final HOPO newHOPO) {
		undoSystem.addUndo();

		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		for (final Selection<ChordOrNote> selection : selectionAccessor.getSelectedSet()) {
			if (selection.selectable.isNote()) {
				selection.selectable.note.hopo = newHOPO;
			} else {
				selection.selectable.chord.chordNotes.values().forEach(n -> n.hopo = newHOPO);
			}
		}
	}

	private void changeBassPickingTechnique(final BassPickingTechnique newBassPickingTechnique) {
		undoSystem.addUndo();
		changeSelectedChordsToNotes();

		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		for (final Selection<ChordOrNote> selection : selectionAccessor.getSelectedSet()) {
			selection.selectable.note.bassPicking = newBassPickingTechnique;
		}
	}

	private void changeHarmonic(final Harmonic newHarmonic) {
		undoSystem.addUndo();

		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		for (final Selection<ChordOrNote> selection : selectionAccessor.getSelectedSet()) {
			if (selection.selectable.isNote()) {
				selection.selectable.note.harmonic = newHarmonic;
			} else {
				selection.selectable.chord.chordNotes.values().forEach(n -> n.harmonic = newHarmonic);
			}
		}
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
		final ArrayList2<ChordOrNote> sounds = data.getCurrentArrangementLevel().chordsAndNotes;
		for (final Selection<ChordOrNote> selection : selectionAccessor.getSelectedSet()) {
			if (selection.selectable.isNote()) {
				selection.selectable.note.linkNext = newLinkNext;
			} else {
				selection.selectable.chord.chordNotes.values().forEach(n -> n.linkNext = newLinkNext);
			}

			final int nextId = selection.id + 1;
			if (nextId < sounds.size()) {
				final ChordOrNote nextSound = sounds.get(nextId);
				if (nextSound.isChord()) {
					nextSound.chord.splitIntoNotes = true;
				}
			}
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

	private void changeSlideFret(final Integer newSlideFret) {
		undoSystem.addUndo();

		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		final ArrayList2<ChordTemplate> chordTemplates = data.getCurrentArrangement().chordTemplates;

		for (final Selection<ChordOrNote> selection : selectionAccessor.getSelectedSet()) {
			if (selection.selectable.isNote()) {
				selection.selectable.note.slideTo = newSlideFret;
			} else {
				final Chord chord = selection.selectable.chord;
				final ChordTemplate chordTemplate = chordTemplates.get(chord.templateId());
				final int minFret = chordTemplate.frets.values().stream()//
						.filter(fret -> fret > 0)//
						.collect(Collectors.minBy(Integer::compare)).orElse(1);
				final int fretDifference = minFret - newSlideFret;
				for (final Entry<Integer, ChordNote> chordNoteEntry : chord.chordNotes.entrySet()) {
					final int fret = chordTemplate.frets.get(chordNoteEntry.getKey());
					if (fret == 0) {
						continue;
					}

					chordNoteEntry.getValue().slideTo = min(frets, max(1, fret - fretDifference));
				}
			}
		}
	}

	private void changeUnpitchedSlide(final boolean newUnpitchedSlide) {
		undoSystem.addUndo();

		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		for (final Selection<ChordOrNote> selection : selectionAccessor.getSelectedSet()) {
			if (selection.selectable.isNote()) {
				selection.selectable.note.unpitchedSlide = newUnpitchedSlide;
			} else {
				selection.selectable.chord.chordNotes.values().forEach(n -> n.unpitchedSlide = newUnpitchedSlide);
			}
		}
	}

	private void changeVibrato(final boolean newVibrato) {
		undoSystem.addUndo();

		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		for (final Selection<ChordOrNote> selection : selectionAccessor.getSelectedSet()) {
			if (selection.selectable.isNote()) {
				selection.selectable.note.vibrato = newVibrato;
			} else {
				selection.selectable.chord.chordNotes.values().forEach(n -> n.vibrato = newVibrato);
			}
		}
	}

	private void changeTremolo(final boolean newTremolo) {
		undoSystem.addUndo();

		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		for (final Selection<ChordOrNote> selection : selectionAccessor.getSelectedSet()) {
			if (selection.selectable.isNote()) {
				selection.selectable.note.tremolo = newTremolo;
			} else {
				selection.selectable.chord.chordNotes.values().forEach(n -> n.tremolo = newTremolo);
			}
		}
	}

	private void templateEdited() {
		if (chordTemplate.frets.isEmpty()) {
			return;
		}

		undoSystem.addUndo();

		final ArrangementChart arrangement = data.getCurrentArrangement();

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
		slideFret.setVisible(true);
		unpitchedSlide.setVisible(true);
		vibrato.setVisible(true);
		tremolo.setVisible(true);
	}

	@Override
	public void hideFields() {
		super.hideFields();

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
		slideFret.setVisible(false);
		unpitchedSlide.setVisible(false);
		vibrato.setVisible(false);
		tremolo.setVisible(false);

		selectionBendEditor.setVisible(false);
	}

	private void setFretsOnSelectionChange(final HashSet2<Selection<ChordOrNote>> selected) {
		final Integer templateId = getSingleValue(selected,
				selection -> selection.selectable.isChord() ? selection.selectable.chord.templateId() : null);
		if (templateId != null) {
			chordTemplate = new ChordTemplate(data.getCurrentArrangement().chordTemplates.get(templateId));
			setCurrentValuesInInputs();
			fret.field.setTextWithoutEvent("");
			string.field.setTextWithoutEvent("");
			return;
		}

		final Integer fretValue = getSingleValue(selected,
				selection -> selection.selectable.isNote() ? selection.selectable.note.fret : null);
		final Integer stringValue = getSingleValue(selected,
				selection -> selection.selectable.isNote() ? selection.selectable.note.string : null);

		chordTemplate = new ChordTemplate();
		if (stringValue != null && fretValue != null) {
			chordTemplate.frets.put(stringValue, fretValue);
		}
		setCurrentValuesInInputs();

		fret.field.setTextWithoutEvent(fretValue == null ? "" : (fretValue + ""));
		string.field.setTextWithoutEvent(stringValue == null ? "" : ((stringValue + 1) + ""));
	}

	public void selectionChanged(final SelectionAccessor<ChordOrNote> selectedChordOrNotesAccessor) {
		final HashSet2<Selection<ChordOrNote>> selected = selectedChordOrNotesAccessor.getSelectedSet();
		setFretsOnSelectionChange(selected);
		string.field.setValidator(createIntValidator(1, data.currentStrings(), false));

		mute.setSelected(getSingleValue(selected, selection -> {
			if (selection.selectable.isNote()) {
				return selection.selectable.note.mute;
			}

			final Mute mute = selection.selectable.chord.chordNotesValue(n -> n.mute, Mute.NONE);
			return mute == null ? Mute.NONE : mute;
		}));
		hopo.setSelected(getSingleValue(selected, selection -> {
			if (selection.selectable.isNote()) {
				return selection.selectable.note.hopo;
			}

			final HOPO hopo = selection.selectable.chord.chordNotesValue(n -> n.hopo, HOPO.NONE);
			return hopo == null ? HOPO.NONE : hopo;
		}));
		bassPickingTechnique.setSelected(getSingleValue(selected,
				selection -> selection.selectable.isNote() ? selection.selectable.note.bassPicking : null));
		harmonic.setSelected(getSingleValue(selected, selection -> {
			if (selection.selectable.isNote()) {
				return selection.selectable.note.harmonic;
			}

			final Harmonic harmonic = selection.selectable.chord.chordNotesValue(n -> n.harmonic, Harmonic.NONE);
			return harmonic == null ? Harmonic.NONE : harmonic;
		}));

		Boolean accentValue = getSingleValue(selected, selection -> selection.selectable.asGuitarSound().accent);
		if (accentValue == null) {
			accentValue = false;
		}
		accent.field.setSelected(accentValue);

		Boolean linkNextValue = getSingleValue(selected, selection -> selection.selectable.asGuitarSound().linkNext());
		if (linkNextValue == null) {
			linkNextValue = false;
		}
		linkNext.field.setSelected(linkNextValue);

		Boolean splitIntoNotesValue = getSingleValue(selected,
				selection -> selection.selectable.isChord() ? selection.selectable.chord.splitIntoNotes : false);
		if (splitIntoNotesValue == null) {
			splitIntoNotesValue = false;
		}
		splitIntoNotes.field.setSelected(splitIntoNotesValue);

		Boolean ignoreValue = getSingleValue(selected, selection -> selection.selectable.asGuitarSound().ignore);
		if (ignoreValue == null) {
			ignoreValue = false;
		}
		ignore.field.setSelected(ignoreValue);

		final Integer slideFretValue = getSingleValue(selected, selection -> {
			if (selection.selectable.isNote()) {
				return selection.selectable.note.slideTo;
			}

			Integer minSlideTo = null;
			for (final ChordNote chordNote : selection.selectable.chord.chordNotes.values()) {
				if (chordNote.slideTo == null) {
					continue;
				}

				if (minSlideTo == null) {
					minSlideTo = chordNote.slideTo;
				} else {
					minSlideTo = min(minSlideTo, chordNote.slideTo);
				}
			}

			return minSlideTo;
		});
		slideFret.field.setTextWithoutEvent(slideFretValue == null ? "" : (slideFretValue + ""));

		Boolean unpitchedSlideValue = getSingleValue(selected, selection -> {
			if (selection.selectable.isNote()) {
				return selection.selectable.note.unpitchedSlide;
			}

			return selection.selectable.chord.chordNotesValue(n -> n.unpitchedSlide, false);
		});
		if (unpitchedSlideValue == null) {
			unpitchedSlideValue = false;
		}
		unpitchedSlide.field.setSelected(unpitchedSlideValue);

		Boolean vibratoValue = getSingleValue(selected, selection -> {
			if (selection.selectable.isNote()) {
				return selection.selectable.note.vibrato;
			}

			return selection.selectable.chord.chordNotesValue(n -> n.vibrato, false);
		});
		if (vibratoValue == null) {
			vibratoValue = false;
		}
		vibrato.field.setSelected(vibratoValue);

		Boolean tremoloValue = getSingleValue(selected, selection -> {
			if (selection.selectable.isNote()) {
				return selection.selectable.note.tremolo;
			}

			return selection.selectable.chord.chordNotesValue(n -> n.tremolo, false);
		});
		if (tremoloValue == null) {
			tremoloValue = false;
		}
		tremolo.field.setSelected(tremoloValue);

		if (selected.size() == 1) {
			selectionBendEditor.onChangeSelection(selected.stream().findAny().get().selectable);
			selectionBendEditor.setVisible(true);
		} else {
			selectionBendEditor.setVisible(false);
		}
	}
}
