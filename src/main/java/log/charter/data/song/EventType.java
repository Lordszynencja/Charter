package log.charter.data.song;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("eventType")
public enum EventType {
	HIGH_PITCH_TICK("B0", "High pitch tick"), //
	LOW_PITCH_TICK("B1", "Low pitch tick"), //
	CROWD_WAVING_HANDS("e0", "Crowd waving hands"), //
	CROWD_HAPPY("e1", "Crowd happy"), //
	CROWD_VERY_HAPPY("e2", "Crowd very happy"), //
	CROWD_APPLAUSE("E3", "Crowd applause"), //
	CROWD_CRITIQUE_APPLAUSE("D3", "Crowd critique applause"), //
	END_CROWD_APPLAUSE("E13", "End crowd applause");

	public final String rsName;
	public final String label;

	private EventType(final String rsName, final String label) {
		this.rsName = rsName;
		this.label = label + " (" + rsName + ")";
	}
}