package log.charter.services.data.copy.data.positions;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.song.Anchor;
import log.charter.song.BeatsMap;

@XStreamAlias("copiedAnchor")
public class CopiedAnchorPosition extends CopiedPosition<Anchor> {
	@XStreamAsAttribute
	public final int fret;
	@XStreamAsAttribute
	public final int width;

	public CopiedAnchorPosition(final BeatsMap beatsMap, final int basePosition, final double baseBeatPosition,
			final Anchor anchor) {
		super(beatsMap, basePosition, baseBeatPosition, anchor);
		fret = anchor.fret;
		width = anchor.width;
	}

	@Override
	protected Anchor prepareValue() {
		final Anchor anchor = new Anchor();
		anchor.fret = fret;
		anchor.width = width;

		return anchor;
	}
}
