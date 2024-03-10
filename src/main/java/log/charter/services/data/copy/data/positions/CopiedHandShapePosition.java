package log.charter.services.data.copy.data.positions;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.data.song.BeatsMap;
import log.charter.data.song.HandShape;

@XStreamAlias("copiedHandShape")
public class CopiedHandShapePosition extends CopiedPositionWithLength<HandShape> {
	public final int chordId;

	public CopiedHandShapePosition(final BeatsMap beatsMap, final int basePosition, final double baseBeatPosition,
			final HandShape handShape) {
		super(beatsMap, basePosition, baseBeatPosition, handShape);
		chordId = handShape.templateId;
	}

	@Override
	protected HandShape prepareValue() {
		return new HandShape(chordId);
	}
}
