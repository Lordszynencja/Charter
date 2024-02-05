package log.charter.gui.components.preview3D.data;

import static log.charter.gui.components.preview3D.Preview3DUtils.visibility;
import static log.charter.gui.components.preview3D.data.AnchorDrawData.getAnchorsForTimeSpanWithRepeats;
import static log.charter.gui.components.preview3D.data.BeatDrawData.getBeatsForTimeSpanWithRepeats;
import static log.charter.gui.components.preview3D.data.HandShapeDrawData.getHandShapesForTimeSpanWithRepeats;
import static log.charter.gui.components.preview3D.data.Preview3DNotesData.getNotesForTimeSpanWithRepeats;
import static log.charter.song.notes.IConstantPosition.findFirstAfter;
import static log.charter.song.notes.IConstantPosition.findLastBeforeEqual;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import log.charter.data.ChartData;
import log.charter.data.managers.RepeatManager;
import log.charter.song.Anchor;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.IntRange;

public class Preview3DDrawData {

	private final ArrayList2<Anchor> levelAnchors;
	private final Map<Integer, IntRange> fretsCache = new HashMap<>();

	public final List<AnchorDrawData> anchors;
	public final List<BeatDrawData> beats;
	public final List<HandShapeDrawData> handShapes;
	public final Preview3DNotesData notes;

	public Preview3DDrawData(final ChartData data, final RepeatManager repeatManager) {
		final int timeFrom = data.time;
		final int timeTo = data.time + visibility;

		levelAnchors = data.getCurrentArrangementLevel().anchors;

		anchors = getAnchorsForTimeSpanWithRepeats(data, repeatManager, timeFrom, timeTo);
		beats = getBeatsForTimeSpanWithRepeats(data, repeatManager, timeFrom, timeTo);
		handShapes = getHandShapesForTimeSpanWithRepeats(data, repeatManager, timeFrom, timeTo);
		notes = getNotesForTimeSpanWithRepeats(data, repeatManager, timeFrom, timeTo);
	}

	public IntRange getFrets(final int t) {
		if (!fretsCache.containsKey(t)) {
			Anchor anchor = findLastBeforeEqual(levelAnchors, t);
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
