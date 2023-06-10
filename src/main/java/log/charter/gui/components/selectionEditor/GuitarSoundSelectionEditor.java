package log.charter.gui.components.selectionEditor;

import static java.util.Arrays.asList;
import static log.charter.data.config.Config.frets;
import static log.charter.gui.components.TextInputWithValidation.ValueValidator.createIntValidator;
import static log.charter.gui.components.selectionEditor.CurrentSelectionEditor.getSingleValue;

import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

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
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.Note;
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
				createIntValidator(1, frets, false), (final Integer val) -> changeFret(val), false);
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

	private void addAccentLinkNextIgnoreInputs(final CurrentSelectionEditor parent, final AtomicInteger row) {
		final JCheckBox accentInput = new JCheckBox();
		accentInput.addActionListener(a -> changeAccent(accentInput.isSelected()));
		accent = new FieldWithLabel<JCheckBox>(Label.ACCENT, 50, 20, 20, accentInput, LabelPosition.LEFT_CLOSE);
		accent.setLocation(10, parent.getY(row.get()));
		parent.add(accent);

		final JCheckBox linkNextInput = new JCheckBox();
		linkNextInput.addActionListener(a -> changeLinkNext(linkNextInput.isSelected()));
		linkNext = new FieldWithLabel<JCheckBox>(Label.LINK_NEXT, 60, 20, 20, linkNextInput, LabelPosition.LEFT_CLOSE);
		linkNext.setLocation(100, parent.getY(row.get()));
		parent.add(linkNext);

		final JCheckBox ignoreInput = new JCheckBox();
		ignoreInput.addActionListener(a -> changeIgnore(ignoreInput.isSelected()));
		ignore = new FieldWithLabel<JCheckBox>(Label.IGNORE_NOTE, 60, 20, 20, ignoreInput, LabelPosition.LEFT_CLOSE);
		ignore.setLocation(190, parent.getY(row.getAndIncrement()));
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
		addAccentLinkNextIgnoreInputs(selectionEditor, row);
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

			final Note note = selection.selectable.note;
			final Chord chord = new Chord(templateId, note);
			selection.selectable.note = null;
			selection.selectable.chord = chord;
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

			final Chord chord = selection.selectable.chord;
			final ChordTemplate template = arrangement.chordTemplates.get(chord.chordId);
			final Note note = new Note(chord, template);
			selection.selectable.chord = null;
			selection.selectable.note = note;
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
		if (fretValue != null) {
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
	}

	private void changeFret(final int newFret) {
		undoSystem.addUndo();
		changeSelectedChordsToNotes();

		chordTemplate = new ChordTemplate();
		final String stringValue = string.field.getText();
		if (stringValue != null) {
			chordTemplate.frets.put(Integer.valueOf(stringValue), newFret);
		}

		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		for (final Selection<ChordOrNote> selection : selectionAccessor.getSelectedSet()) {
			selection.selectable.note.fret = newFret;
		}
	}

	private void changeMute(final Mute newMute) {
		undoSystem.addUndo();

		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		for (final Selection<ChordOrNote> selection : selectionAccessor.getSelectedSet()) {
			selection.selectable.asGuitarSound().mute = newMute;
		}
	}

	private void changeHOPO(final HOPO newHOPO) {
		undoSystem.addUndo();

		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		for (final Selection<ChordOrNote> selection : selectionAccessor.getSelectedSet()) {
			selection.selectable.asGuitarSound().hopo = newHOPO;
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
			selection.selectable.asGuitarSound().harmonic = newHarmonic;
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
		for (final Selection<ChordOrNote> selection : selectionAccessor.getSelectedSet()) {
			selection.selectable.asGuitarSound().linkNext = newLinkNext;
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
		for (final Selection<ChordOrNote> selection : selectionAccessor.getSelectedSet()) {
			selection.selectable.asGuitarSound().slideTo = newSlideFret;
		}
	}

	private void changeUnpitchedSlide(final boolean newUnpitchedSlide) {
		undoSystem.addUndo();

		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		for (final Selection<ChordOrNote> selection : selectionAccessor.getSelectedSet()) {
			selection.selectable.asGuitarSound().unpitchedSlide = newUnpitchedSlide;
		}
	}

	private void changeVibrato(final boolean newVibrato) {
		undoSystem.addUndo();

		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		for (final Selection<ChordOrNote> selection : selectionAccessor.getSelectedSet()) {
			selection.selectable.asGuitarSound().vibrato = newVibrato;
		}
	}

	private void changeTremolo(final boolean newTremolo) {
		undoSystem.addUndo();

		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		for (final Selection<ChordOrNote> selection : selectionAccessor.getSelectedSet()) {
			selection.selectable.asGuitarSound().tremolo = newTremolo;
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
					selection.selectable.chord.chordId = templateId;
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
		ignore.setVisible(false);
		slideFret.setVisible(false);
		unpitchedSlide.setVisible(false);
		vibrato.setVisible(false);
		tremolo.setVisible(false);

		selectionBendEditor.setVisible(false);
	}

	private void setFretsOnSelectionChange(final HashSet2<Selection<ChordOrNote>> selected) {
		final Integer templateId = getSingleValue(selected,
				selection -> selection.selectable.isChord() ? selection.selectable.chord.chordId : null);
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
		string.field.setTextWithoutEvent(stringValue == null ? "" : (stringValue + ""));
	}

	public void selectionChanged(final SelectionAccessor<ChordOrNote> selectedChordOrNotesAccessor) {
		final HashSet2<Selection<ChordOrNote>> selected = selectedChordOrNotesAccessor.getSelectedSet();
		setFretsOnSelectionChange(selected);
		string.field.setValidator(createIntValidator(1, data.currentStrings(), false));

		mute.setSelected(getSingleValue(selected, selection -> selection.selectable.asGuitarSound().mute));
		hopo.setSelected(getSingleValue(selected, selection -> selection.selectable.asGuitarSound().hopo));
		bassPickingTechnique.setSelected(getSingleValue(selected,
				selection -> selection.selectable.isNote() ? selection.selectable.note.bassPicking : null));
		harmonic.setSelected(getSingleValue(selected, selection -> selection.selectable.asGuitarSound().harmonic));

		Boolean accentValue = getSingleValue(selected, selection -> selection.selectable.asGuitarSound().accent);
		if (accentValue == null) {
			accentValue = false;
		}
		accent.field.setSelected(accentValue);

		Boolean linkNextValue = getSingleValue(selected, selection -> selection.selectable.asGuitarSound().linkNext);
		if (linkNextValue == null) {
			linkNextValue = false;
		}
		linkNext.field.setSelected(linkNextValue);

		Boolean ignoreValue = getSingleValue(selected, selection -> selection.selectable.asGuitarSound().ignore);
		if (ignoreValue == null) {
			ignoreValue = false;
		}
		ignore.field.setSelected(ignoreValue);

		final Integer slideFretValue = getSingleValue(selected,
				selection -> selection.selectable.asGuitarSound().slideTo);
		slideFret.field.setTextWithoutEvent(slideFretValue == null ? "" : (slideFretValue + ""));

		Boolean unpitchedSlideValue = getSingleValue(selected,
				selection -> selection.selectable.asGuitarSound().unpitchedSlide);
		if (unpitchedSlideValue == null) {
			unpitchedSlideValue = false;
		}
		unpitchedSlide.field.setSelected(unpitchedSlideValue);

		Boolean vibratoValue = getSingleValue(selected, selection -> selection.selectable.asGuitarSound().vibrato);
		if (vibratoValue == null) {
			vibratoValue = false;
		}
		vibrato.field.setSelected(vibratoValue);

		Boolean tremoloValue = getSingleValue(selected, selection -> selection.selectable.asGuitarSound().tremolo);
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
