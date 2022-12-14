package log.charter.song;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.io.rs.xml.song.ArrangementAnchor;

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
}
