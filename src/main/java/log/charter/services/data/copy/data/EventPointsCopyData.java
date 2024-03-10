package log.charter.services.data.copy.data;

import java.util.HashSet;
import java.util.Set;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.data.ChartData;
import log.charter.data.song.Arrangement;
import log.charter.data.song.BeatsMap;
import log.charter.data.song.EventPoint;
import log.charter.data.song.Phrase;
import log.charter.data.types.PositionType;
import log.charter.io.Logger;
import log.charter.services.data.copy.data.positions.CopiedArrangementEventsPointPosition;
import log.charter.services.data.selection.SelectionManager;
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
		return arrangementEventsPoints.isEmpty();
	}

	@Override
	public void paste(final ChartData chartData, final SelectionManager selectionManager, final int time,
			final boolean convertFromBeats) {
		paste(chartData, selectionManager, time, true, true, true, convertFromBeats);
	}

	public void paste(final ChartData chartData, final SelectionManager selectionManager, final int time,
			final boolean sections, final boolean phrases, final boolean events, final boolean convertFromBeats) {
		final Arrangement arrangement = chartData.getCurrentArrangement();
		if (phrases) {
			for (final CopiedArrangementEventsPointPosition arrangementEventsPoint : arrangementEventsPoints) {
				final String phraseName = arrangementEventsPoint.phrase;
				if (!arrangement.phrases.containsKey(phraseName)) {
					arrangement.phrases.put(phraseName, this.phrases.get(phraseName));
				}
			}
		}

		final BeatsMap beatsMap = chartData.songChart.beatsMap;
		final double basePositionInBeats = beatsMap.getPositionInBeats(time);
		final Set<Integer> positionsToSelect = new HashSet<>(arrangementEventsPoints.size());

		for (final CopiedArrangementEventsPointPosition copiedPosition : arrangementEventsPoints) {
			try {
				final EventPoint value = copiedPosition.getValue(beatsMap, time, basePositionInBeats, convertFromBeats);

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

					final EventPoint eventPoint = arrangement.findOrCreateArrangementEventsPoint(value.position());
					eventPoint.merge(value);
					positionsToSelect.add(value.position());
				}
			} catch (final Exception e) {
				Logger.error("Couldn't paste position", e);
			}
		}

		selectionManager.addSelectionForPositions(PositionType.EVENT_POINT, positionsToSelect);
	}
}
