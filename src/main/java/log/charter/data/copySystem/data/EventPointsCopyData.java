package log.charter.data.copySystem.data;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.data.ChartData;
import log.charter.data.copySystem.data.positions.CopiedArrangementEventsPointPosition;
import log.charter.io.Logger;
import log.charter.song.ArrangementChart;
import log.charter.song.EventPoint;
import log.charter.song.BeatsMap;
import log.charter.song.Phrase;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;

@XStreamAlias("eventPointsCopyData")
public class EventPointsCopyData implements ICopyData {
	public final HashMap2<String, Phrase> phrases;
	public final ArrayList2<CopiedArrangementEventsPointPosition> arrangementEventsPoints;

	public EventPointsCopyData(final HashMap2<String, Phrase> phrases,
			final ArrayList2<CopiedArrangementEventsPointPosition> arrangementEventsPoints) {
		this.phrases = phrases;
		this.arrangementEventsPoints = arrangementEventsPoints;
	}

	@Override
	public boolean isEmpty() {
		return phrases.isEmpty() && arrangementEventsPoints.isEmpty();
	}

	@Override
	public void paste(final ChartData data) {
		paste(data, true, true, true);
	}

	public void paste(final ChartData data, final boolean sections, final boolean phrases, final boolean events) {
		final ArrangementChart arrangement = data.getCurrentArrangement();
		if (phrases) {
			for (final CopiedArrangementEventsPointPosition arrangementEventsPoint : arrangementEventsPoints) {
				final String phraseName = arrangementEventsPoint.phrase;
				if (!arrangement.phrases.containsKey(phraseName)) {
					arrangement.phrases.put(phraseName, this.phrases.get(phraseName));
				}
			}
		}

		final BeatsMap beatsMap = data.songChart.beatsMap;
		final double basePositionInBeats = beatsMap.getPositionInBeats(data.time);

		for (final CopiedArrangementEventsPointPosition copiedPosition : arrangementEventsPoints) {
			try {
				final EventPoint value = copiedPosition.getValue(beatsMap, basePositionInBeats);

				if (value != null) {
					if (!sections) {
						value.section = null;
					}
					if (!phrases) {
						value.phrase = null;
					}
					if (!events) {
						value.events = new ArrayList2<>();
					}

					final EventPoint eventPoint = arrangement
							.findOrCreateArrangementEventsPoint(value.position());
					eventPoint.merge(value);
				}
			} catch (final Exception e) {
				Logger.error("Couldn't paste position", e);
			}
		}
	}
}
