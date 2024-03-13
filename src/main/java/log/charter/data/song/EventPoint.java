package log.charter.data.song;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.data.song.position.Position;
import log.charter.io.rsc.xml.converters.SimpleCollectionToStringConverter.EventTypesList;

@XStreamAlias("eventPoint")
public class EventPoint extends Position {
	@XStreamAsAttribute
	public SectionType section = null;
	@XStreamAsAttribute
	public String phrase = null;
	@XStreamAsAttribute
	@XStreamConverter(EventTypesList.class)
	public List<EventType> events = new ArrayList<>();

	public EventPoint() {
		super(0);
	}

	public EventPoint(final int position) {
		super(position);
	}

	public EventPoint(final EventPoint other) {
		super(other);
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
}
