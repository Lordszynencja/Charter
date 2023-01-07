package log.charter.song;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.io.rs.xml.song.ArrangementAnchor;
import log.charter.song.notes.Position;

@XStreamAlias("anchor")
public class Anchor extends Position {
	public int fret;
	public int width;

	public Anchor(final int position, final int fret) {
		super(position);
		this.fret = fret;
		width = 4;
	}

	public Anchor() {
		super(0);
	}

	public Anchor(final ArrangementAnchor arrangementAnchor) {
		super(arrangementAnchor.time);
		fret = arrangementAnchor.fret;
		width = arrangementAnchor.width == null ? 4 : arrangementAnchor.width.intValue();
	}

	public Anchor(final Anchor other) {
		super(other);
		fret = other.fret;
		width = other.width;
	}
}
