package log.charter.data.copySystem.data.positions;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.song.BeatsMap;
import log.charter.song.HandShape;

@XStreamAlias("copiedHandShape")
public class CopiedHandShapePosition extends CopiedPositionWithLength<HandShape> {
	public final int chordId;

	public CopiedHandShapePosition(final BeatsMap beatsMap, final double baseBeatPosition, final HandShape handShape) {
		super(beatsMap, baseBeatPosition, handShape);
		chordId = handShape.chordId;
	}

	@Override
	protected HandShape prepareValue() {
		return new HandShape(chordId);
	}
}
