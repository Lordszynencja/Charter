package log.charter.gui.panes;

import static java.util.Arrays.asList;
import static log.charter.gui.components.TextInputSelectAllOnFocus.addSelectTextOnFocus;
import static log.charter.gui.components.TextInputWithValidation.ValueValidator.createIntValidator;

import javax.swing.JTextField;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.ChordTemplateEditor;
import log.charter.song.ChordTemplate;
import log.charter.song.enums.HOPO;
import log.charter.song.enums.Harmonic;
import log.charter.song.enums.Mute;
import log.charter.song.notes.Chord;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.Note;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.Pair;

public class ChordOptionsPane extends ChordTemplateEditor {
	private static final long serialVersionUID = 1L;

	private static PaneSizes getSizes() {
		final PaneSizes sizes = new PaneSizes();
		sizes.labelWidth = 80;
		sizes.width = 350;
		sizes.rowHeight = 20;

		return sizes;
	}

	private static ChordTemplate prepareTemplateFromData(final ChartData data, final ChordOrNote chordOrNote) {
		if (chordOrNote.isChord()) {
			if (chordOrNote.chord.chordId < 0
					|| chordOrNote.chord.chordId >= data.getCurrentArrangement().chordTemplates.size()) {
				return new ChordTemplate();
			}

			return new ChordTemplate(data.getCurrentArrangement().chordTemplates.get(chordOrNote.chord.chordId));
		}

		final ChordTemplate chordTemplate = new ChordTemplate();
		chordTemplate.frets.put(chordOrNote.note.string, chordOrNote.note.fret);
		return chordTemplate;
	}

	private final UndoSystem undoSystem;

	private final ArrayList2<ChordOrNote> chordsAndNotes;

	private Mute mute;
	private HOPO hopo = HOPO.NONE;
	private Harmonic harmonic = Harmonic.NONE;
	private boolean accent;
	private boolean linkNext;
	private Integer slideTo;
	private boolean unpitchedSlide;
	private boolean tremolo;

	public ChordOptionsPane(final ChartData data, final CharterFrame frame, final UndoSystem undoSystem,
			final ArrayList2<ChordOrNote> notes) {
		super(data, frame, Label.CHORD_OPTIONS_PANE, 19 + data.getCurrentArrangement().tuning.strings, getSizes(),
				prepareTemplateFromData(data, notes.get(0)));
		this.undoSystem = undoSystem;

		chordsAndNotes = notes;

		final ChordOrNote chordOrNote = notes.get(0);
		if (chordOrNote.isNote()) {
			getNoteValues(chordOrNote.note);
		} else {
			final Chord chord = chordOrNote.chord;
			final ChordTemplate chordTemplate = data.getCurrentArrangement().chordTemplates.get(chord.chordId);
			getChordValues(chord, chordTemplate);
		}

		addInputs(data.getCurrentArrangement().tuning.strings);
	}

	private void getNoteValues(final Note note) {
		mute = note.mute;
		hopo = note.hopo;
		harmonic = note.harmonic;
		accent = note.accent;
		linkNext = note.linkNext;
		slideTo = note.slideTo;
		unpitchedSlide = note.unpitchedSlide;
		tremolo = note.tremolo;
	}

	private void getChordValues(final Chord chord, final ChordTemplate chordTemplate) {
		mute = chord.mute;
		hopo = chord.hopo;
		harmonic = chord.harmonic;
		accent = chord.accent;
		linkNext = chord.linkNext;
		slideTo = chord.slideTo;
		unpitchedSlide = chord.unpitchedSlide;
		tremolo = chord.tremolo;
	}

	private void addInputs(final int strings) {
		addChordNameSuggestionButton(100, 0);
		addChordNameInput(100, 1, newChordTemplate -> {
		});

		addChordTemplateEditor(3);

		int row = 4 + data.currentStrings();
		final int radioButtonWidth = 65;

		row++;
		final int muteLabelY = getY(row++);
		addLabelExact(muteLabelY, 20, Label.MUTE);
		final int muteRadioY = getY(row++) - 3;
		addConfigRadioButtonsExact(muteRadioY, 30, radioButtonWidth, mute, val -> mute = val, //
				asList(new Pair<>(Mute.FULL, Label.MUTE_FULL), //
						new Pair<>(Mute.PALM, Label.MUTE_PALM), //
						new Pair<>(Mute.NONE, Label.MUTE_NONE)));

		final int hopoLabelY = getY(row++) + 3;
		addLabelExact(hopoLabelY, 20, Label.HOPO);
		final int hopoRadioY = getY(row++);
		addConfigRadioButtonsExact(hopoRadioY, 30, radioButtonWidth, hopo, val -> hopo = val, //
				asList(new Pair<>(HOPO.HAMMER_ON, Label.HOPO_HAMMER_ON), //
						new Pair<>(HOPO.PULL_OFF, Label.HOPO_PULL_OFF), //
						new Pair<>(HOPO.TAP, Label.HOPO_TAP), //
						new Pair<>(HOPO.NONE, Label.HOPO_NONE)));

		final int harmonicLabelY = getY(row++) + 6;
		addLabelExact(harmonicLabelY, 20, Label.HARMONIC);
		final int harmonicRadioY = getY(row++) + 3;
		addConfigRadioButtonsExact(harmonicRadioY, 30, radioButtonWidth, harmonic, val -> harmonic = val, //
				asList(new Pair<>(Harmonic.NORMAL, Label.HARMONIC_NORMAL), //
						new Pair<>(Harmonic.PINCH, Label.HARMONIC_PINCH), //
						new Pair<>(Harmonic.NONE, Label.HARMONIC_NONE)));

		row++;
		addConfigCheckbox(row, 20, 45, Label.ACCENT, accent, val -> accent = val);
		addConfigCheckbox(row++, 110, 0, Label.LINK_NEXT, linkNext, val -> linkNext = val);

		row++;
		addIntegerConfigValue(row, 20, 45, Label.SLIDE_PANE_FRET, slideTo, 30,
				createIntValidator(1, Config.frets, true), val -> slideTo = val, false);
		final JTextField input = (JTextField) components.getLast();
		input.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(input);
		addConfigCheckbox(row, 120, unpitchedSlide, val -> unpitchedSlide = val);
		addLabel(row++, 140, Label.SLIDE_PANE_UNPITCHED);

		addConfigCheckbox(row++, 110, 0, Label.TREMOLO, tremolo, val -> tremolo = val);

		row++;
		row++;
		addDefaultFinish(row, this::onSave);
	}

	private void setChordValues(final Chord chord) {
		chord.mute = mute;
		chord.hopo = hopo;
		chord.harmonic = harmonic;
		chord.accent = accent;
		chord.linkNext = linkNext;
		chord.slideTo = slideTo;
		chord.unpitchedSlide = unpitchedSlide;
		chord.tremolo = tremolo;
	}

	private void changeNoteToChord(final ChordOrNote chordOrNote, final int chordId) {
		final Chord chord = new Chord(chordOrNote.position(), chordId);
		setChordValues(chord);
		chordOrNote.note = null;
		chordOrNote.chord = chord;
	}

	private void onSave() {
		undoSystem.addUndo();

		if (chordTemplate.frets.isEmpty()) {
			data.getCurrentArrangementLevel().chordsAndNotes.removeAll(chordsAndNotes);
			return;
		}

		final int chordId = data.getCurrentArrangement().getChordTemplateIdWithSave(chordTemplate);
		for (final ChordOrNote chordOrNote : chordsAndNotes) {
			if (chordOrNote.isChord()) {
				chordOrNote.chord.chordId = chordId;
				setChordValues(chordOrNote.chord);
			} else {
				changeNoteToChord(chordOrNote, chordId);
			}
		}
	}

}
