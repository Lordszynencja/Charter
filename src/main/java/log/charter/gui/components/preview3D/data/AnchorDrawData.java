package log.charter.gui.components.preview3D.data;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static log.charter.util.CollectionUtils.findFirstAfter;

import java.util.ArrayList;
import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.song.Anchor;
import log.charter.data.song.EventPoint;
import log.charter.data.song.position.IConstantPosition;
import log.charter.data.song.position.IConstantPositionWithLength;
import log.charter.services.RepeatManager;
import log.charter.util.collections.ArrayList2;

public class AnchorDrawData implements IConstantPositionWithLength {
	public static List<AnchorDrawData> getAnchorsForTimeSpan(final ChartData data, final int audioLength,
			final int timeFrom, final int timeTo) {
		if (data.getCurrentArrangementLevel() == null) {
			return asList(new AnchorDrawData(timeFrom, timeTo, 0, 4));
		}

		final List<AnchorDrawData> anchorsToDraw = new ArrayList<>();
		final ArrayList2<Anchor> anchors = data.getCurrentArrangementLevel().anchors;

		int anchorsFrom = IConstantPosition.findLastIdBeforeEqual(anchors, timeFrom);
		if (anchorsFrom == -1) {
			anchorsFrom = 0;
		}
		final int anchorsTo = IConstantPosition.findLastIdBeforeEqual(anchors, timeTo);

		for (int i = anchorsFrom; i <= anchorsTo; i++) {
			final Anchor anchor = anchors.get(i);

			final int anchorTimeFrom = max(anchor.position(), timeFrom);
			int anchorTimeTo;
			if (i < anchors.size() - 1) {
				anchorTimeTo = anchors.get(i + 1).position() - 1;
			} else {
				anchorTimeTo = audioLength;
			}

			final EventPoint nextPhraseIteration = findFirstAfter(
					data.getCurrentArrangement().getFilteredEventPoints(p -> p.hasPhrase()), anchor);
			if (nextPhraseIteration != null) {
				anchorTimeTo = min(anchorTimeTo, nextPhraseIteration.position());
			}
			anchorTimeTo = min(anchorTimeTo, timeTo);

			anchorsToDraw.add(new AnchorDrawData(anchorTimeFrom, anchorTimeTo, anchor.fret - 1, anchor.topFret()));
		}

		return anchorsToDraw;
	}

	public static List<AnchorDrawData> getAnchorsForTimeSpanWithRepeats(final ChartData data,
			final RepeatManager repeatManager, final int audioLength, final int timeFrom, final int timeTo) {
		int maxTime = timeTo;
		if (repeatManager.isRepeating()) {
			maxTime = min(maxTime, repeatManager.getRepeatEnd() - 1);
		}

		final List<AnchorDrawData> anchorsToDraw = getAnchorsForTimeSpan(data, audioLength, timeFrom, maxTime);

		if (!repeatManager.isRepeating()) {
			return anchorsToDraw;
		}

		final List<AnchorDrawData> repeatedAnchors = getAnchorsForTimeSpan(data, audioLength,
				repeatManager.getRepeatStart(), repeatManager.getRepeatEnd() - 1);
		int repeatStart = repeatManager.getRepeatEnd();
		while (repeatStart < timeFrom) {
			repeatStart += repeatManager.getRepeatEnd() - repeatManager.getRepeatStart();
		}

		while (repeatStart < timeTo) {
			for (final AnchorDrawData anchorDrawData : repeatedAnchors) {
				final int start = anchorDrawData.timeFrom - repeatManager.getRepeatStart() + repeatStart;
				int end = start + anchorDrawData.timeTo - anchorDrawData.timeFrom;
				if (start > timeTo) {
					break;
				}
				if (end > timeTo) {
					end = timeTo;
				}
				anchorsToDraw.add(new AnchorDrawData(start, end, anchorDrawData.fretFrom, anchorDrawData.fretTo));
			}

			repeatStart += repeatManager.getRepeatEnd() - repeatManager.getRepeatStart();
		}

		return anchorsToDraw;
	}

	public final int timeFrom;
	public final int timeTo;
	public final int fretFrom;
	public final int fretTo;

	public AnchorDrawData(final int timeFrom, final int timeTo, final int fretFrom, final int fretTo) {
		this.timeFrom = timeFrom;
		this.timeTo = timeTo;
		this.fretFrom = fretFrom;
		this.fretTo = fretTo;
	}

	@Override
	public int position() {
		return timeFrom;
	}

	@Override
	public int length() {
		return timeTo - timeFrom;
	}
}