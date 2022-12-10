package log.charter.song;

import java.util.List;
import java.util.stream.Collectors;

import log.charter.io.rs.xml.song.ArrangementEvent;
import log.charter.util.CollectionUtils.ArrayList2;

public class Event extends Position {
	public enum EventType {
		HIGH_PITCH_TICK("B0", "High pitch tick (B0)"), //
		LOW_PITCH_TICK("B1", "Low pitch tick (B1)"), //
		CROWD_WAVING_HANDS("E0", "Crowd waving hands (E0)"), //
		CROWD_HAPPY("E1", "Crowd happy (E1)"), //
		CROWD_VERY_HAPPY("E2", "Crowd very happy (E2)"), //
		CROWD_APPLAUSE("E3", "Crowd applause (E3)"), //
		CROWD_CRITIQUE_APPLAUSE("D3", "Crowd critique applause (D3)"), //
		END_CROWD_APPLAUSE("E13", "End crowd applause (E13)");

		public final String rsName;
		public final String label;

		private EventType(final String rsName, final String label) {
			this.rsName = rsName;
			this.label = label;
		}

		public static EventType findByRSName(final String rsName) {
			for (final EventType eventType : values()) {
				if (eventType.rsName.equals(rsName)) {
					return eventType;
				}
			}

			return null;
		}
	}

	public static ArrayList2<Event> fromArrangement(final ArrayList2<Beat> beats,
			final List<ArrangementEvent> arrangementEvents) {
		final ArrayList2<Event> events = arrangementEvents.stream()//
				.filter(arrangementEvent -> {
					if (arrangementEvent.code.startsWith("TS:")) {
						final int time = arrangementEvent.time;
						final int beatsInMeasure = Integer.valueOf(arrangementEvent.code.split(":")[1].split("/")[0]);
						beats.stream()//
								.filter(beat -> beat.position >= time)//
								.forEach(beat -> beat.beatsInMeasure = beatsInMeasure);

						return false;
					}

					return true;
				})//
				.map(Event::new)//
				.collect(Collectors.toCollection(ArrayList2::new));

		return events;
	}

	public EventType type;

	public Event(final int pos, final EventType type) {
		super(pos);
		this.type = type;
	}

	private Event(final ArrangementEvent arrangementEvent) {
		super(arrangementEvent.time);
		type = EventType.findByRSName(arrangementEvent.code);
	}

}
