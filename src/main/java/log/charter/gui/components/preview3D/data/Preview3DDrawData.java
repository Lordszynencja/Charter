package log.charter.gui.components.preview3D.data;

import static java.util.Arrays.asList;
import static log.charter.gui.components.preview3D.Preview3DUtils.getVisibility;
import static log.charter.gui.components.preview3D.data.AnchorDrawData.getAnchorsForTimeSpanWithRepeats;
import static log.charter.gui.components.preview3D.data.BeatDrawData.getBeatsForTimeSpanWithRepeats;
import static log.charter.gui.components.preview3D.data.HandShapeDrawData.getHandShapesForTimeSpanWithRepeats;
import static log.charter.gui.components.preview3D.data.Preview3DNotesData.getNotesForTimeSpanWithRepeats;
import static log.charter.util.CollectionUtils.findFirstAfter;
import static log.charter.util.CollectionUtils.findLastBeforeEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import log.charter.data.ChartData;
import log.charter.data.song.Anchor;
import log.charter.data.song.position.Position;
import log.charter.services.RepeatManager;
import log.charter.services.data.ChartTimeHandler;
import log.charter.util.data.IntRange;

public class Preview3DDrawData {
	private final List<Anchor> levelAnchors;
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
			levelAnchors = asList(new Anchor(0, 1));
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
			final Position p = new Position(t);
			Anchor anchor = findLastBeforeEquals(levelAnchors, p);
			if (anchor == null) {
				anchor = findFirstAfter(levelAnchors, p);
			}
			if (anchor == null) {
				anchor = new Anchor(0, 1);
			}

			fretsCache.put(t, new IntRange(anchor.fret, anchor.topFret()));
		}

		return fretsCache.get(t);
	}
}
