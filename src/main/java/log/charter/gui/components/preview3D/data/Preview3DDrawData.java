package log.charter.gui.components.preview3D.data;

import static log.charter.gui.components.preview3D.Preview3DUtils.visibility;
import static log.charter.gui.components.preview3D.data.AnchorDrawData.getAnchorsForTimeSpanWithRepeats;
import static log.charter.gui.components.preview3D.data.BeatDrawData.getBeatsForTimeSpanWithRepeats;
import static log.charter.gui.components.preview3D.data.HandShapeDrawData.getHandShapesForTimeSpanWithRepeats;
import static log.charter.gui.components.preview3D.data.Preview3DNotesData.getNotesForTimeSpanWithRepeats;

import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.managers.RepeatManager;

public class Preview3DDrawData {
	public final List<AnchorDrawData> anchors;
	public final List<BeatDrawData> beats;
	public final List<HandShapeDrawData> handShapes;
	public final Preview3DNotesData notes;

	public Preview3DDrawData(final ChartData data, final RepeatManager repeatManager) {
		final int timeFrom = data.time;
		final int timeTo = data.time + visibility;

		anchors = getAnchorsForTimeSpanWithRepeats(data, repeatManager, timeFrom, timeTo);
		beats = getBeatsForTimeSpanWithRepeats(data, repeatManager, timeFrom, timeTo);
		handShapes = getHandShapesForTimeSpanWithRepeats(data, repeatManager, timeFrom, timeTo);
		notes = getNotesForTimeSpanWithRepeats(data, repeatManager, timeFrom, timeTo);
	}
}
