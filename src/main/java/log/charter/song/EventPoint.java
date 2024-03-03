package log.charter.song;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.io.rsc.xml.converters.SimpleCollectionToStringConverter.EventTypesList;
import log.charter.song.notes.Position;
import log.charter.util.CollectionUtils.ArrayList2;

@XStreamAlias("eventPoint")
public class EventPoint extends Position {
	@XStreamAsAttribute
	public SectionType section = null;
	@XStreamAsAttribute
	public String phrase = null;
	@XStreamAsAttribute
	@XStreamConverter(EventTypesList.class)
	public ArrayList2<EventType> events = new ArrayList2<>();

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
		events = new ArrayList2<>(other.events);
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
