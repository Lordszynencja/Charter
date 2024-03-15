package log.charter.data.song;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IFractionalPosition;
import log.charter.io.rsc.xml.converters.EventPointConverter;
import log.charter.io.rsc.xml.converters.SimpleCollectionToStringConverter.EventTypesListConverter;

@XStreamAlias("eventPoint")
@XStreamConverter(EventPointConverter.class)
public class EventPoint implements IFractionalPosition {

	private FractionalPosition position;
	public SectionType section = null;
	public String phrase = null;
	@XStreamConverter(EventTypesListConverter.class)
	public List<EventType> events = new ArrayList<>();

	public EventPoint() {
		position = new FractionalPosition(0);
	}

	public EventPoint(final FractionalPosition position) {
		this.position = position;
	}

	public EventPoint(final EventPoint other) {
		position = other.position;
		section = other.section;
		phrase = other.phrase;
		events = new ArrayList<>(other.events);
	}

	public void merge(final EventPoint other) {
		if (other.section != null) {
			section = other.section;
		}
		if (other.phrase != null) {
			phrase = other.phrase;
		}
		events.addAll(other.events);
	}

	public boolean hasPhrase() {
		return phrase != null;
	}

	@Override
	public FractionalPosition fractionalPosition() {
		return position;
	}

	@Override
	public void fractionalPosition(final FractionalPosition newPosition) {
		if (newPosition == null) {
			throw new IllegalArgumentException("new position can't be null");
		}

		position = newPosition;
	}
}
