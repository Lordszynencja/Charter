package log.charter.gui.components.preview3D.data;

import static log.charter.gui.components.preview3D.Preview3DUtils.getVisibility;
import static log.charter.gui.components.preview3D.data.BeatDrawData.getBeatsForTimeSpanWithRepeats;
import static log.charter.gui.components.preview3D.data.FHPDrawData.getFHPsForTimeSpanWithRepeats;
import static log.charter.gui.components.preview3D.data.HandShapeDrawData.getHandShapesForTimeSpanWithRepeats;
import static log.charter.gui.components.preview3D.data.Preview3DNotesData.getNotesForTimeSpanWithRepeats;
import static log.charter.util.CollectionUtils.firstAfter;
import static log.charter.util.CollectionUtils.lastBeforeEqual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import log.charter.data.ChartData;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.FHP;
import log.charter.data.song.position.fractional.IConstantFractionalPosition;
import log.charter.data.song.position.time.Position;
import log.charter.services.RepeatManager;
import log.charter.services.data.ChartTimeHandler;
import log.charter.util.data.IntRange;

public class Preview3DDrawData {
	private final ImmutableBeatsMap songBeats;
	private final RepeatManager repeatManager;

	private final List<FHP> levelFHPs;
	private final Map<Integer, IntRange> fretsCache = new HashMap<>();

	public final double time;
	public final List<FHPDrawData> fhps;
	public final List<BeatDrawData> beats;
	public final List<HandShapeDrawData> handShapes;
	public final Preview3DNotesData notes;

	public Preview3DDrawData(final ChartData chartData, final ChartTimeHandler chartTimeHandler,
			final RepeatManager repeatManager) {
		songBeats = chartData.beats();
		this.repeatManager = repeatManager;

		time = chartTimeHandler.time();
		levelFHPs = chartData.currentArrangementLevel().fhps;

		final double timeTo = time + getVisibility();
		if (timeTo < songBeats.get(0).position()) {
			fhps = new ArrayList<>();
			beats = new ArrayList<>();
			handShapes = new ArrayList<>();
			notes = new Preview3DNotesData();
		} else {
			fhps = getFHPsForTimeSpanWithRepeats(chartData, repeatManager, chartTimeHandler.maxTime(), time, timeTo);
			beats = getBeatsForTimeSpanWithRepeats(chartData, repeatManager, time, timeTo);
			handShapes = getHandShapesForTimeSpanWithRepeats(chartData, repeatManager, time, timeTo);
			notes = getNotesForTimeSpanWithRepeats(chartData, repeatManager, time, timeTo);
		}
	}

	private double getTimeWithRepeat(double t) {
		if (!repeatManager.isRepeating()) {
			return t;
		}

		final double end = repeatManager.repeatEnd();
		final double length = end - repeatManager.repeatStart();
		while (t >= end) {
			t -= length;
		}

		return t;
	}

	private void putFHPInCache(final double t) {
		final FHPDrawData drawnFHP = lastBeforeEqual(fhps, new Position(t)).find();
		if (drawnFHP != null) {
			fretsCache.put((int) t, new IntRange(drawnFHP.fretFrom + 1, drawnFHP.fretTo));
			return;
		}

		final IConstantFractionalPosition p = new Position(t).toFraction(songBeats);

		FHP fhp = lastBeforeEqual(levelFHPs, p).find();
		if (fhp == null) {
			fhp = firstAfter(levelFHPs, p).find();
		}
		if (fhp == null) {
			fretsCache.put((int) t, null);
			return;
		}

		fretsCache.put((int) t, new IntRange(fhp.fret, fhp.topFret()));
	}

	public IntRange getFrets(double t) {
		t = getTimeWithRepeat(t);

		if (!fretsCache.containsKey((int) t)) {
			putFHPInCache(t);
		}

		return fretsCache.get((int) t);
	}
}
