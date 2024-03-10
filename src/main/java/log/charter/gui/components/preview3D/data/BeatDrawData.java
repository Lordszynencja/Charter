package log.charter.gui.components.preview3D.data;

import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.song.Beat;
import log.charter.data.song.notes.IConstantPosition;
import log.charter.services.RepeatManager;
import log.charter.util.CollectionUtils.ArrayList2;

public class BeatDrawData implements IConstantPosition {
	public static List<BeatDrawData> getBeatsForTimeSpan(final ChartData data, final int timeFrom, final int timeTo) {
		final List<BeatDrawData> beatsToDraw = new ArrayList<>();
		final ArrayList2<Beat> beats = data.songChart.beatsMap.beats;

		int beatsFrom = IConstantPosition.findFirstIdAfterEqual(beats, timeFrom);
		if (beatsFrom < 0) {
			beatsFrom = 0;
		}
		final int beatsTo = IConstantPosition.findLastIdBeforeEqual(beats, timeTo);

		for (int i = beatsFrom; i <= beatsTo; i++) {
			beatsToDraw.add(new BeatDrawData(beats.get(i)));
		}

		return beatsToDraw;
	}

	public static List<BeatDrawData> getBeatsForTimeSpanWithRepeats(final ChartData data,
			final RepeatManager repeatManager, final int timeFrom, final int timeTo) {
		int maxTime = timeTo;
		if (repeatManager.isRepeating()) {
			maxTime = min(maxTime, repeatManager.getRepeatEnd() - 1);
		}

		final List<BeatDrawData> beatsToDraw = getBeatsForTimeSpan(data, timeFrom, maxTime);

		if (!repeatManager.isRepeating()) {
			return beatsToDraw;
		}

		final List<BeatDrawData> repeatedBeats = getBeatsForTimeSpan(data, repeatManager.getRepeatStart(),
				repeatManager.getRepeatEnd() - 1);
		int repeatStart = repeatManager.getRepeatEnd();
		while (repeatStart < timeFrom) {
			repeatStart += repeatManager.getRepeatEnd() - repeatManager.getRepeatStart();
		}

		while (repeatStart < timeTo) {
			for (final BeatDrawData beatDrawData : repeatedBeats) {
				final int position = beatDrawData.time - repeatManager.getRepeatStart() + repeatStart;
				if (position > timeTo) {
					break;
				}

				beatsToDraw.add(new BeatDrawData(position, beatDrawData));
			}

			repeatStart += repeatManager.getRepeatEnd() - repeatManager.getRepeatStart();
		}

		return beatsToDraw;
	}

	public final int originalTime;
	public final int time;
	public final boolean firstInMeasure;

	public BeatDrawData(final Beat beat) {
		originalTime = beat.position();
		time = originalTime;
		firstInMeasure = beat.firstInMeasure;
	}

	public BeatDrawData(final int time, final BeatDrawData other) {
		originalTime = other.originalTime;
		this.time = time;
		firstInMeasure = other.firstInMeasure;
	}

	@Override
	public int position() {
		return time;
	}
}
