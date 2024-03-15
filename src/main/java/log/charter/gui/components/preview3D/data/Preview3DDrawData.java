package log.charter.gui.components.preview3D.data;

import static log.charter.gui.components.preview3D.Preview3DUtils.getVisibility;
import static log.charter.gui.components.preview3D.data.AnchorDrawData.getAnchorsForTimeSpanWithRepeats;
import static log.charter.gui.components.preview3D.data.BeatDrawData.getBeatsForTimeSpanWithRepeats;
import static log.charter.gui.components.preview3D.data.HandShapeDrawData.getHandShapesForTimeSpanWithRepeats;
import static log.charter.gui.components.preview3D.data.Preview3DNotesData.getNotesForTimeSpanWithRepeats;
import static log.charter.util.CollectionUtils.firstAfter;
import static log.charter.util.CollectionUtils.lastBeforeEqual;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import log.charter.data.ChartData;
import log.charter.data.song.Anchor;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.Position;
import log.charter.data.song.position.fractional.IConstantFractionalPosition;
import log.charter.services.RepeatManager;
import log.charter.services.data.ChartTimeHandler;
import log.charter.util.data.IntRange;

public class Preview3DDrawData {
	private final ImmutableBeatsMap songBeats;
	private final List<Anchor> levelAnchors;
	private final Map<Integer, IntRange> fretsCache = new HashMap<>();

	public final int time;
	public final List<AnchorDrawData> anchors;
	public final List<BeatDrawData> beats;
	public final List<HandShapeDrawData> handShapes;
	public final Preview3DNotesData notes;

	public Preview3DDrawData(final ChartTimeHandler chartTimeHandler, final ChartData data,
			final RepeatManager repeatManager) {
		songBeats = data.beats();
		time = chartTimeHandler.time();
		levelAnchors = data.currentArrangementLevel().anchors;

		final int timeTo = time + getVisibility();
		anchors = getAnchorsForTimeSpanWithRepeats(data, repeatManager, chartTimeHandler.maxTime(), time, timeTo);
		beats = getBeatsForTimeSpanWithRepeats(data, repeatManager, time, timeTo);
		handShapes = getHandShapesForTimeSpanWithRepeats(data, repeatManager, time, timeTo);
		notes = getNotesForTimeSpanWithRepeats(data, repeatManager, time, timeTo);
	}

	public IntRange getFrets(final int t) {
		if (!fretsCache.containsKey(t)) {
			final IConstantFractionalPosition p = new Position(t).toFraction(songBeats);
			Anchor anchor = lastBeforeEqual(levelAnchors, p, IConstantFractionalPosition::compareTo).find();
			if (anchor == null) {
				anchor = firstAfter(levelAnchors, p, IConstantFractionalPosition::compareTo).find();
			}
			if (anchor == null) {
				fretsCache.put(t, null);
			} else {
				fretsCache.put(t, new IntRange(anchor.fret, anchor.topFret()));
			}
		}

		return fretsCache.get(t);
	}
}
