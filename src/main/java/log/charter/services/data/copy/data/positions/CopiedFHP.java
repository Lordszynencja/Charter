package log.charter.services.data.copy.data.positions;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.FHP;
import log.charter.data.song.position.FractionalPosition;

@XStreamAlias("copiedFHP")
public class CopiedFHP extends CopiedFractionalPosition<FHP> {
	@XStreamAsAttribute
	public final int fret;
	@XStreamAsAttribute
	public final int width;

	public CopiedFHP(final FractionalPosition basePosition, final FHP fhp) {
		super(basePosition, fhp);
		fret = fhp.fret;
		width = fhp.width;
	}

	@Override
	public FHP prepareValue(final ImmutableBeatsMap beats, final FractionalPosition basePosition) {
		final FHP fhp = new FHP();
		fhp.fret = fret;
		fhp.width = width;

		return fhp;
	}
}
