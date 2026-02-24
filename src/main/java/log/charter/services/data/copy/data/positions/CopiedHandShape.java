package log.charter.services.data.copy.data.positions;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.HandShape;
import log.charter.data.song.position.FractionalPosition;

@XStreamAlias("copiedHandShape")
public class CopiedHandShape extends CopiedFractionalPositionWithEnd<HandShape> {
	public final int chordId;

	public CopiedHandShape(final FractionalPosition basePosition, final HandShape handShape) {
		super(basePosition, handShape);
		chordId = handShape.templateId;
	}

	@Override
	public HandShape prepareValue(final ImmutableBeatsMap beats, final FractionalPosition basePosition) {
		return new HandShape(chordId);
	}
}
