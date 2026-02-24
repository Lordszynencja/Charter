package log.charter.services.data.copy.data.positions;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.Showlight;
import log.charter.data.song.Showlight.ShowlightType;
import log.charter.data.song.position.FractionalPosition;

@XStreamAlias("copiedShowlight")
public class CopiedShowlight extends CopiedFractionalPosition<Showlight> {
	@XStreamAsAttribute
	public final List<ShowlightType> types;

	public CopiedShowlight(final FractionalPosition basePosition, final Showlight showlight) {
		super(basePosition, showlight);
		types = showlight.types;
	}

	@Override
	public Showlight prepareValue(final ImmutableBeatsMap beats, final FractionalPosition basePosition) {
		return new Showlight(FractionalPosition.zero, types);
	}
}
