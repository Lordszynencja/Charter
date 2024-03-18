package log.charter.gui.components.preview3D.data;

import static java.lang.Math.min;
import static log.charter.util.CollectionUtils.firstAfterEqual;
import static log.charter.util.CollectionUtils.lastBeforeEqual;

import java.util.ArrayList;
import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.song.Beat;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.time.Position;
import log.charter.services.RepeatManager;

public class BeatDrawData {
	public static List<BeatDrawData> getBeatsForTimeSpan(final ChartData data, final int timeFrom, final int timeTo) {
		final List<BeatDrawData> beatsToDraw = new ArrayList<>();
		final ImmutableBeatsMap beats = data.beats();

		final Integer beatsFrom = firstAfterEqual(beats, new Position(timeFrom)).findId();
		final Integer beatsTo = lastBeforeEqual(beats, new Position(timeTo)).findId();

		if (beatsFrom == null || beatsTo == null) {
			return beatsToDraw;
		}

		for (int i = beatsFrom; i <= beatsTo; i++) {
			beatsToDraw.add(new BeatDrawData(beats.get(i)));
		}

		return beatsToDraw;
	}

	public static List<BeatDrawData> getBeatsForTimeSpanWithRepeats(final ChartData data,
			final RepeatManager repeatManager, final int timeFrom, final int timeTo) {
		int maxTime = timeTo;
		if (repeatManager.isRepeating()) {
			maxTime = min(maxTime, repeatManager.repeatEnd() - 1);
		}

		final List<BeatDrawData> beatsToDraw = getBeatsForTimeSpan(data, timeFrom, maxTime);

		if (!repeatManager.isRepeating()) {
			return beatsToDraw;
		}

		final List<BeatDrawData> repeatedBeats = getBeatsForTimeSpan(data, repeatManager.repeatStart(),
				repeatManager.repeatEnd() - 1);
		int repeatStart = repeatManager.repeatEnd();
		while (repeatStart < timeFrom) {
			repeatStart += repeatManager.repeatEnd() - repeatManager.repeatStart();
		}

		while (repeatStart < timeTo) {
			for (final BeatDrawData beatDrawData : repeatedBeats) {
				final int position = beatDrawData.time - repeatManager.repeatStart() + repeatStart;
				if (position > timeTo) {
					break;
				}

				beatsToDraw.add(new BeatDrawData(position, beatDrawData));
			}

			repeatStart += repeatManager.repeatEnd() - repeatManager.repeatStart();
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
}
