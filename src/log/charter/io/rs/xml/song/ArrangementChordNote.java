package log.charter.io.rs.xml.song;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.song.Note;

@XStreamAlias("chordNote")
public class ArrangementChordNote extends ArrangementNote {
	public ArrangementChordNote() {
	}

	public ArrangementChordNote(final Note note) {
		super(note);
	}
}
