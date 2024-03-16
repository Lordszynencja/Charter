package log.charter.services.data.copy.data.positions;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.HandShape;
import log.charter.data.song.position.FractionalPosition;

@XStreamAlias("copiedHandShape")
public class CopiedHandShapePosition extends CopiedFractionalPositionWithEnd<HandShape> {
	public final int chordId;

	public CopiedHandShapePosition(final ImmutableBeatsMap beats, final FractionalPosition basePosition,
			final HandShape handShape) {
		super(beats, basePosition, handShape);
		chordId = handShape.templateId;
	}

	@Override
	protected HandShape prepareValue() {
		return new HandShape(chordId);
	}
}
