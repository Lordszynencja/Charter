package log.charter.io.rs.xml.song;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import log.charter.io.rs.xml.converters.TimeConverter;
import log.charter.song.Chord;
import log.charter.util.CollectionUtils.ArrayList2;

@XStreamAlias("chord")
@XStreamInclude(ArrangementChordNote.class)
public class ArrangementChord {
	@XStreamAsAttribute
	@XStreamConverter(TimeConverter.class)
	public int time;
	@XStreamAsAttribute
	public int chordId;
	@XStreamAsAttribute
	public Integer palmMute;
	@XStreamAsAttribute
	public Integer fretHandMute;
	@XStreamAsAttribute
	public Integer accent;
	@XStreamImplicit
	public ArrayList2<ArrangementChordNote> chordNotes;

	public ArrangementChord() {
	}

	public ArrangementChord(final Chord chord) {
		time = chord.position;
		chordId = chord.chordId;
		palmMute = chord.palmMute ? 1 : null;
		fretHandMute = chord.fretHandMute ? 1 : null;
		accent = chord.accent ? 1 : null;
		chordNotes = chord.chordNotes.isEmpty() ? null : chord.chordNotes.map(ArrangementChordNote::new);
	}
}
