package log.charter.gui.components.preview3D.data;

import static log.charter.data.song.notes.IConstantPosition.findFirstAfter;
import static log.charter.data.song.notes.IConstantPosition.findLastBeforeEquals;
import static log.charter.gui.components.preview3D.Preview3DUtils.getVisibility;
import static log.charter.gui.components.preview3D.data.AnchorDrawData.getAnchorsForTimeSpanWithRepeats;
import static log.charter.gui.components.preview3D.data.BeatDrawData.getBeatsForTimeSpanWithRepeats;
import static log.charter.gui.components.preview3D.data.HandShapeDrawData.getHandShapesForTimeSpanWithRepeats;
import static log.charter.gui.components.preview3D.data.Preview3DNotesData.getNotesForTimeSpanWithRepeats;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import log.charter.data.ChartData;
import log.charter.data.song.Anchor;
import log.charter.services.RepeatManager;
import log.charter.services.data.ChartTimeHandler;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.IntRange;

public class Preview3DDrawData {

	private final ArrayList2<Anchor> levelAnchors;
	private final Map<Integer, IntRange> fretsCache = new HashMap<>();

	public final int time;
	public final List<AnchorDrawData> anchors;
	public final List<BeatDrawData> beats;
	public final List<HandShapeDrawData> handShapes;
	public final Preview3DNotesData notes;

	public Preview3DDrawData(final ChartTimeHandler chartTimeHandler, final ChartData data,
			final RepeatManager repeatManager) {
		time = chartTimeHandler.time();

		if (data.getCurrentArrangementLevel() == null) {
			levelAnchors = new ArrayList2<>(new Anchor(0, 1));
		} else {
			levelAnchors = data.getCurrentArrangementLevel().anchors;
		}

		final int timeTo = time + getVisibility();
		anchors = getAnchorsForTimeSpanWithRepeats(data, repeatManager, chartTimeHandler.maxTime(), time, timeTo);
		beats = getBeatsForTimeSpanWithRepeats(data, repeatManager, time, timeTo);
		handShapes = getHandShapesForTimeSpanWithRepeats(data, repeatManager, time, timeTo);
		notes = getNotesForTimeSpanWithRepeats(data, repeatManager, time, timeTo);
	}

	public IntRange getFrets(final int t) {
		if (!fretsCache.containsKey(t)) {
			Anchor anchor = findLastBeforeEquals(levelAnchors, t);
			if (anchor == null) {
				anchor = findFirstAfter(levelAnchors, t);
			}
			if (anchor == null) {
				anchor = new Anchor(0, 1);
			}

			fretsCache.put(t, new IntRange(anchor.fret, anchor.topFret()));
		}

		return fretsCache.get(t);
	}
}
