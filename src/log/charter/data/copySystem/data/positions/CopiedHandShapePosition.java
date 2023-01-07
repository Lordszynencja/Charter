package log.charter.data.copySystem.data.positions;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.song.Beat;
import log.charter.song.HandShape;

@XStreamAlias("copiedHandShape")
public class CopiedHandShapePosition extends CopiedPositionWithLength<HandShape> {
	public final int chordId;

	public CopiedHandShapePosition(final List<Beat> beats, final double baseBeatPosition, final HandShape handShape) {
		super(beats, baseBeatPosition, handShape);
		chordId = handShape.chordId;
	}

	@Override
	protected HandShape prepareValue() {
		return new HandShape(chordId);
	}
}
