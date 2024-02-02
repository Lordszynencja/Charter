package log.charter.data.copySystem.data.positions;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.song.BeatsMap;
import log.charter.song.EventPoint;
import log.charter.song.EventType;
import log.charter.song.SectionType;
import log.charter.util.CollectionUtils.ArrayList2;

@XStreamAlias("copiedArrangementEventsPointPosition")
public class CopiedArrangementEventsPointPosition extends CopiedPosition<EventPoint> {
	@XStreamAsAttribute
	public final SectionType section;
	@XStreamAsAttribute
	public final String phrase;
	@XStreamAsAttribute
	public final ArrayList2<EventType> events;

	@SuppressWarnings("unchecked")
	public CopiedArrangementEventsPointPosition(final BeatsMap beatsMap, final int basePosition,
			final double baseBeatPosition, final EventPoint arrangementEventsPoint) {
		super(beatsMap, basePosition, baseBeatPosition, arrangementEventsPoint);
		section = arrangementEventsPoint.section;
		phrase = arrangementEventsPoint.phrase;
		events = (ArrayList2<EventType>) arrangementEventsPoint.events.clone();
	}

	@Override
	protected EventPoint prepareValue() {
		final EventPoint arrangementEventsPoint = new EventPoint();
		arrangementEventsPoint.section = section;
		arrangementEventsPoint.phrase = phrase;
		arrangementEventsPoint.events = events;

		return arrangementEventsPoint;
	}
}
