package log.charter.gui.panes;

import static log.charter.gui.components.TextInputWithValidation.ValueValidator.createIntValidator;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.managers.selection.ChordOrNote;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.ParamsPane;
import log.charter.song.ChordTemplate;
import log.charter.song.enums.HOPO;
import log.charter.song.enums.Harmonic;
import log.charter.song.enums.Mute;
import log.charter.song.notes.Chord;
import log.charter.song.notes.Note;
import log.charter.util.CollectionUtils.ArrayList2;

public class ChordOptionsPane extends ParamsPane {
	private static final long serialVersionUID = 1L;

	private static PaneSizes getSizes() {
		final PaneSizes sizes = new PaneSizes();
		sizes.labelWidth = 80;
		sizes.width = 300;
		sizes.rowHeight = 20;

		return sizes;
	}

	private final ChartData data;
	private final UndoSystem undoSystem;

	private final ArrayList2<ChordOrNote> chordsAndNotes;

	private String chordName;
	private Integer chordId;
	private Mute mute;
	private HOPO hopo = HOPO.NONE;
	private Harmonic harmonic = Harmonic.NONE;
	private boolean accent;
	private boolean linkNext;
	private Integer slideTo;
	private boolean unpitchedSlide;

	public ChordOptionsPane(final ChartData data, final CharterFrame frame, final UndoSystem undoSystem,
			final ArrayList2<ChordOrNote> notes) {
		super(frame, Label.NOTE_OPTIONS_PANE.label(), 20, getSizes());
		this.data = data;
		this.undoSystem = undoSystem;

		chordsAndNotes = notes;

		final ChordOrNote chordOrNote = notes.get(0);
		if (chordOrNote.note != null) {
			getNoteValues(chordOrNote.note);
		} else {
			final Chord chord = chordOrNote.chord;
			final ChordTemplate chordTemplate = data.getCurrentArrangement().chordTemplates.get(chord.chordId);
			getChordValues(chord, chordTemplate);
		}

		addInputs(data.getCurrentArrangement().tuning.strings);
	}

	private void getNoteValues(final Note note) {
		chordName = "";
		chordId = null;
		mute = note.mute;
		accent = note.accent;
		linkNext = note.linkNext;
		slideTo = note.slideTo;
		unpitchedSlide = note.unpitchedSlide;
	}

	private void getChordValues(final Chord chord, final ChordTemplate chordTemplate) {
		chordName = chordTemplate.chordName;
		chordId = chord.chordId;
		mute = chord.mute;
		accent = chord.accent;
		linkNext = chord.linkNext;
		slideTo = chord.slideTo;
		unpitchedSlide = chord.unpitchedSlide;
	}

	private void addChordNameInput() {
//		final AutocompleteInput<ArrangementChordTemplate> input = new AutocompleteInput<>(this, 50, chordName,
//				data.getCurrentArrangement().chordTemplates, true,
//				chordTemplate -> chordTemplate.getNameWithFrets(data.getCurrentArrangement().tuning.strings), null);

		addLabel(0, 20, Label.CHORD_NAME);
	}

	private void addInputs(final int strings) {
		addChordNameInput();

		// TODO add frets and fingers
		// TODO add chord shape showing
		addLabel(2, 20, Label.MUTE);
		addConfigRadioButtons(3, 30, mute.ordinal(), i -> mute = Mute.values()[i], //
				Label.MUTE_STRING, Label.MUTE_PALM, Label.MUTE_NONE);

		addLabel(4, 20, Label.HOPO);
		addConfigRadioButtons(5, 30, hopo.ordinal(), i -> hopo = HOPO.values()[i], //
				Label.HOPO_HAMMER_ON, Label.HOPO_PULL_OFF, Label.HOPO_TAP, Label.HOPO_NONE);

		addLabel(6, 20, Label.HARMONIC);
		addConfigRadioButtons(7, 30, harmonic.ordinal(), i -> harmonic = Harmonic.values()[i], //
				Label.HARMONIC_NORMAL, Label.HARMONIC_PINCH, Label.HARMONIC_NONE);

		addConfigCheckbox(11, 20, 70, Label.ACCENT, accent, val -> accent = val);
		addConfigCheckbox(12, 20, 70, Label.LINK_NEXT, linkNext, val -> linkNext = val);
		addIntegerConfigValue(13, 20, 0, Label.SLIDE_PANE_FRET, slideTo, 40, createIntValidator(0, 28, true),
				val -> slideTo = val, false);
		addConfigCheckbox(13, 120, 0, Label.SLIDE_PANE_UNPITCHED, unpitchedSlide, val -> unpitchedSlide = val);

		addDefaultFinish(16, this::onSave);
	}

	private void addTemplate() {
//TODO make it add template
	}

	private void setChordValues(final Note note) {
//		note.string = string;
//		note.fret = fret;
//		note.mute = mute;
//		note.hopo = hopo;
//		note.bassPicking = bassPicking;
//		note.harmonic = harmonic;
//		note.slideTo = slideTo;
//		note.unpitchedSlide = unpitchedSlide;
//		note.accent = accent;
//		note.linkNext = linkNext;
//		note.vibrato = vibrato;
	}

	private void changeNoteToChord(final ChordOrNote chordOrNote) {
//		final Note note = new Note(chordOrNote.position, string, fret);
//		note.mute = mute;
//		note.hopo = hopo;
//		note.bassPicking = bassPicking;
//		note.harmonic = harmonic;
//		note.slideTo = slideTo;
//		note.unpitchedSlide = unpitchedSlide;
//		note.accent = accent;
//		note.linkNext = linkNext;
//		note.vibrato = vibrato;
//		chordOrNote.note = note;
//		chordOrNote.chord = null;
	}

	private void onSave() {
		undoSystem.addUndo();

		for (final ChordOrNote chordOrNote : chordsAndNotes) {
			if (chordOrNote.isChord()) {
				setChordValues(chordOrNote.note);
			} else {
				changeNoteToChord(chordOrNote);
			}
		}

		getOwnerlessWindows()[0].requestFocus();

		dispose();
	}

}
