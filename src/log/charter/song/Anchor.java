package log.charter.song;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.io.rs.xml.song.ArrangementAnchor;
import log.charter.song.notes.Position;

@XStreamAlias("anchor")
public class Anchor extends Position {
	public int fret;

	public Anchor(final int position, final int fret) {
		super(position);
		this.fret = fret;
	}

	public Anchor(final ArrangementAnchor arrangementAnchor) {
		super(arrangementAnchor.time);
		fret = arrangementAnchor.fret;
	}

	public Anchor(final Anchor other) {
		super(other);
		fret = other.fret;
	}
}
