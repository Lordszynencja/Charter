package log.charter.services.data.copy.data.positions;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.data.song.Anchor;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.FractionalPosition;

@XStreamAlias("copiedAnchor")
public class CopiedAnchorPosition extends CopiedFractionalPosition<Anchor> {
	@XStreamAsAttribute
	public final int fret;
	@XStreamAsAttribute
	public final int width;

	public CopiedAnchorPosition(final ImmutableBeatsMap beats, final FractionalPosition basePosition,
			final Anchor anchor) {
		super(beats, basePosition, anchor);
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
