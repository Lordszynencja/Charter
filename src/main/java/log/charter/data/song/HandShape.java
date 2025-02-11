package log.charter.data.song;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPositionWithEnd;
import log.charter.data.song.position.fractional.IFractionalPositionWithEnd;
import log.charter.io.rsc.xml.converters.HandShapeConverter;

@XStreamAlias("handShape")
@XStreamConverter(HandShapeConverter.class)
public class HandShape implements IFractionalPositionWithEnd {
	private FractionalPosition position;
	private FractionalPosition endPosition;
	public Integer templateId;

	public HandShape() {
	}

	public HandShape(final FractionalPosition position, final FractionalPosition endPosition) {
		this.position = position;
		this.endPosition = endPosition;
	}

	public HandShape(final FractionalPosition position, final FractionalPosition endPosition, final int templateId) {
		this.position = position;
		this.endPosition = endPosition;
		this.templateId = templateId;
	}

	public HandShape(final int templateId) {
		position = new FractionalPosition();
		endPosition = new FractionalPosition();
		this.templateId = templateId;
	}

	public HandShape(final HandShape other) {
		position = other.position;
		endPosition = other.endPosition;
		templateId = other.templateId;
	}

	@Override
	public FractionalPosition position() {
		return position;
	}

	@Override
	public void position(final FractionalPosition newPosition) {
		position = newPosition;
	}

	@Override
	public FractionalPosition endPosition() {
		return endPosition;
	}

	@Override
	public void endPosition(final FractionalPosition newEndPosition) {
		endPosition = newEndPosition;
	}

	@Override
	public IConstantFractionalPositionWithEnd toFraction(final ImmutableBeatsMap beats) {
		return this;
	}
}
