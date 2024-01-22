package log.charter.song;

import log.charter.song.notes.Position;
import log.charter.util.CollectionUtils.ArrayList2;

public class EventPoint extends Position {
	public SectionType section;
	public String phrase;
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
}
