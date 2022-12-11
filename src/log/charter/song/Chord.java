package log.charter.song;

import static log.charter.util.Utils.mapInteger;

import log.charter.gui.SelectionManager.Selectable;
import log.charter.io.rs.xml.song.ArrangementChord;
import log.charter.util.CollectionUtils.ArrayList2;

public class Chord extends Position implements Selectable {
	public int chordId;
	public boolean palmMute;
	public boolean fretHandMute;
	public boolean accent;
	public ArrayList2<Note> chordNotes = new ArrayList2<>();

	public Chord(final int pos, final int chordId) {
		super(pos);
		this.chordId = chordId;
	}

	public Chord(final ArrangementChord arrangementChord) {
		super(arrangementChord.time);
		chordId = arrangementChord.chordId;
		palmMute = mapInteger(arrangementChord.palmMute);
		fretHandMute = mapInteger(arrangementChord.fretHandMute);
		accent = mapInteger(arrangementChord.accent);
		chordNotes = arrangementChord.chordNotes == null ? new ArrayList2<>()
				: arrangementChord.chordNotes.map(Note::new);
	}

	@Override
	public String getSignature() {
		return position + "";
	}

	public int length() {
		int maxLength = 0;
		for (final Note note : chordNotes) {
			if (note.sustain > maxLength) {
				maxLength = note.sustain;
			}
		}

		return maxLength;
	}
}
