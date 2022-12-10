package log.charter.io.rs.xml.song;

import static log.charter.util.Utils.mapInteger;

import log.charter.song.Note;
import log.charter.song.Position;
import log.charter.util.CollectionUtils.ArrayList2;

public class Chord extends Position {
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

}
