package log.charter.song;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.gui.SelectionManager.Selectable;
import log.charter.io.rs.xml.song.ArrangementHandShape;

@XStreamAlias("handShape")
public class HandShape extends Position implements Selectable {
	public int length;

	public HandShape(final ArrangementHandShape arrangementHandShape) {
		super(arrangementHandShape.startTime);
		length = arrangementHandShape.endTime - position;
	}

	@Override
	public String getSignature() {
		return "" + position;
	}
}
