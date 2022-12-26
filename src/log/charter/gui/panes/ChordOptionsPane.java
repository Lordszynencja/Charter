package log.charter.gui.panes;

import static log.charter.gui.components.TextInputWithValidation.ValueValidator.createIntValidator;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.managers.selection.ChordOrNote;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.ChordTemplateEditor;
import log.charter.song.ChordTemplate;
import log.charter.song.enums.HOPO;
import log.charter.song.enums.Harmonic;
import log.charter.song.enums.Mute;
import log.charter.song.notes.Chord;
import log.charter.song.notes.Note;
import log.charter.util.CollectionUtils.ArrayList2;

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
		return !chordOrNote.isChord() || chordOrNote.chord.chordId == -1 ? new ChordTemplate()
				: new ChordTemplate(data.getCurrentArrangement().chordTemplates.get(chordOrNote.chord.chordId));
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

	public ChordOptionsPane(final ChartData data, final CharterFrame frame, final UndoSystem undoSystem,
			final ArrayList2<ChordOrNote> notes) {
		super(data, frame, Label.NOTE_OPTIONS_PANE, 18 + data.getCurrentArrangement().tuning.strings, getSizes(),
				prepareTemplateFromData(data, notes.get(0)));
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
		mute = note.mute;
		hopo = note.hopo;
		harmonic = note.harmonic;
		accent = note.accent;
		linkNext = note.linkNext;
		slideTo = note.slideTo;
		unpitchedSlide = note.unpitchedSlide;
	}

	private void getChordValues(final Chord chord, final ChordTemplate chordTemplate) {
		mute = chord.mute;
		hopo = chord.hopo;
		harmonic = chord.harmonic;
		accent = chord.accent;
		linkNext = chord.linkNext;
		slideTo = chord.slideTo;
		unpitchedSlide = chord.unpitchedSlide;
	}

	private void addInputs(final int strings) {
		addChordNameSuggestionButton(100, 0);
		addChordNameInput(100, 1);

		addChordTemplateEditor(3);

		int row = 4 + data.currentStrings();

		addLabel(row++, 20, Label.MUTE);
		addConfigRadioButtons(row++, 30, mute.ordinal(), i -> mute = Mute.values()[i], //
				Label.MUTE_STRING, Label.MUTE_PALM, Label.MUTE_NONE);

		addLabel(row++, 20, Label.HOPO);
		addConfigRadioButtons(row++, 30, hopo.ordinal(), i -> hopo = HOPO.values()[i], //
				Label.HOPO_HAMMER_ON, Label.HOPO_PULL_OFF, Label.HOPO_TAP, Label.HOPO_NONE);

		addLabel(row++, 20, Label.HARMONIC);
		addConfigRadioButtons(row++, 30, harmonic.ordinal(), i -> harmonic = Harmonic.values()[i], //
				Label.HARMONIC_NORMAL, Label.HARMONIC_PINCH, Label.HARMONIC_NONE);

		row++;
		addConfigCheckbox(row++, 20, 70, Label.ACCENT, accent, val -> accent = val);
		addConfigCheckbox(row++, 20, 70, Label.LINK_NEXT, linkNext, val -> linkNext = val);
		addIntegerConfigValue(row, 20, 0, Label.SLIDE_PANE_FRET, slideTo, 40, createIntValidator(0, 28, true),
				val -> slideTo = val, false);
		addConfigCheckbox(row++, 120, 0, Label.SLIDE_PANE_UNPITCHED, unpitchedSlide, val -> unpitchedSlide = val);

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
	}

	private void changeNoteToChord(final ChordOrNote chordOrNote, final int chordId) {
		final Chord chord = new Chord(chordOrNote.position, chordId);
		setChordValues(chord);
		chordOrNote.note = null;
		chordOrNote.chord = chord;
	}

	private void onSave() {
		dispose();
		undoSystem.addUndo();

		if (chordTemplate.frets.isEmpty()) {
			data.getCurrentArrangementLevel().chordsAndNotes.removeAll(chordsAndNotes);
			return;
		}

		final int chordId = getSavedTemplateId();
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
