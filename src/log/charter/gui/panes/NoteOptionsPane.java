package log.charter.gui.panes;

import static log.charter.gui.components.TextInputWithValidation.ValueValidator.createIntValidator;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.managers.selection.ChordOrNote;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.ParamsPane;
import log.charter.song.ChordTemplate;
import log.charter.song.enums.BassPickingTechnique;
import log.charter.song.enums.HOPO;
import log.charter.song.enums.Harmonic;
import log.charter.song.enums.Mute;
import log.charter.song.notes.Chord;
import log.charter.song.notes.Note;
import log.charter.util.CollectionUtils.ArrayList2;

public class NoteOptionsPane extends ParamsPane {
	private static final long serialVersionUID = 1L;

	private static PaneSizes getSizes() {
		final PaneSizes sizes = new PaneSizes();
		sizes.labelWidth = 80;
		sizes.width = 300;
		sizes.rowHeight = 20;

		return sizes;
	}

	private final UndoSystem undoSystem;

	private final ArrayList2<ChordOrNote> chordsAndNotes;

	private int string;
	private int fret;
	private Mute mute;
	private HOPO hopo;
	private BassPickingTechnique bassPicking;
	private Harmonic harmonic;
	private boolean accent;
	private boolean linkNext;
	private Integer slideTo;
	private boolean unpitchedSlide;
	private Integer vibrato;

	public NoteOptionsPane(final ChartData data, final CharterFrame frame, final UndoSystem undoSystem,
			final ArrayList2<ChordOrNote> notes) {
		super(frame, Label.NOTE_OPTIONS_PANE.label(), 20, getSizes());
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

	private void addInputs(final int strings) {
		addIntegerConfigValue(0, 20, 0, Label.STRING, string + 1, 30, createIntValidator(1, strings, false),
				val -> string = val - 1, false);
		addIntegerConfigValue(0, 100, 0, Label.FRET, fret, 30, createIntValidator(0, 28, false), val -> fret = val,
				false);

		addLabel(2, 20, Label.MUTE);
		addConfigRadioButtons(3, 30, mute.ordinal(), i -> mute = Mute.values()[i], //
				Label.MUTE_STRING, Label.MUTE_PALM, Label.MUTE_NONE);

		addLabel(4, 20, Label.HOPO);
		addConfigRadioButtons(5, 30, hopo.ordinal(), i -> hopo = HOPO.values()[i], //
				Label.HOPO_HAMMER_ON, Label.HOPO_PULL_OFF, Label.HOPO_TAP, Label.HOPO_NONE);

		addLabel(6, 20, Label.BASS_PICKING_TECHNIQUE);
		addConfigRadioButtons(7, 30, bassPicking.ordinal(), i -> bassPicking = BassPickingTechnique.values()[i], //
				Label.BASS_PICKING_POP, Label.BASS_PICKING_SLAP, Label.BASS_PICKING_NONE);

		addLabel(8, 20, Label.HARMONIC);
		addConfigRadioButtons(9, 30, harmonic.ordinal(), i -> harmonic = Harmonic.values()[i], //
				Label.HARMONIC_NORMAL, Label.HARMONIC_PINCH, Label.HARMONIC_NONE);

		addConfigCheckbox(11, 20, 70, Label.ACCENT, accent, val -> accent = val);
		addConfigCheckbox(12, 20, 70, Label.LINK_NEXT, linkNext, val -> linkNext = val);
		addIntegerConfigValue(13, 50, 0, Label.SLIDE_PANE_FRET, slideTo, 40, createIntValidator(0, 28, true),
				val -> slideTo = val, false);
		addConfigCheckbox(13, 120, 0, Label.SLIDE_PANE_UNPITCHED, unpitchedSlide, val -> unpitchedSlide = val);
		addIntegerConfigValue(14, 50, 100, Label.VIBRATO, vibrato, 40, createIntValidator(0, 1000, true),
				val -> vibrato = val, false);

		addDefaultFinish(16, this::onSave);
	}

	private void getNoteValues(final Note note) {
		string = note.string;
		fret = note.fret;
		mute = note.mute;
		hopo = note.hopo;
		bassPicking = note.bassPicking;
		harmonic = note.harmonic;
		accent = note.accent;
		linkNext = note.linkNext;
		slideTo = note.slideTo;
		unpitchedSlide = note.unpitchedSlide;
		vibrato = note.vibrato;
	}

	private void getChordValues(final Chord chord, final ChordTemplate chordTemplate) {
		string = chordTemplate.getStringRange().min;
		fret = chordTemplate.frets.get(string);
		mute = chord.mute;
		hopo = HOPO.NONE;
		bassPicking = BassPickingTechnique.NONE;
		harmonic = Harmonic.NONE;
		accent = chord.accent;
		linkNext = chord.linkNext;
		slideTo = chord.slideTo;
		unpitchedSlide = chord.unpitchedSlide;
		vibrato = null;
	}

	private void setNoteValues(final Note note) {
		note.string = string;
		note.fret = fret;
		note.mute = mute;
		note.hopo = hopo;
		note.bassPicking = bassPicking;
		note.harmonic = harmonic;
		note.slideTo = slideTo;
		note.unpitchedSlide = unpitchedSlide;
		note.accent = accent;
		note.linkNext = linkNext;
		note.vibrato = vibrato;
	}

	private void changeChordToNote(final ChordOrNote chordOrNote) {
		final Note note = new Note(chordOrNote.position, string, fret);
		note.mute = mute;
		note.hopo = hopo;
		note.bassPicking = bassPicking;
		note.harmonic = harmonic;
		note.slideTo = slideTo;
		note.unpitchedSlide = unpitchedSlide;
		note.accent = accent;
		note.linkNext = linkNext;
		note.vibrato = vibrato;
		chordOrNote.note = note;
		chordOrNote.chord = null;
	}

	private void onSave() {
		undoSystem.addUndo();

		for (final ChordOrNote chordOrNote : chordsAndNotes) {
			if (chordOrNote.isChord()) {
				changeChordToNote(chordOrNote);
			} else {
				setNoteValues(chordOrNote.note);
			}
		}

		dispose();
	}

}
