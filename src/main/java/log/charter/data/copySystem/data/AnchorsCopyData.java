package log.charter.data.copySystem.data;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.data.ChartData;
import log.charter.data.copySystem.data.positions.CopiedAnchorPosition;
import log.charter.song.Anchor;
import log.charter.song.BeatsMap;
import log.charter.util.CollectionUtils.ArrayList2;

@XStreamAlias("anchorsCopyData")
public class AnchorsCopyData implements ICopyData {
	public final ArrayList2<CopiedAnchorPosition> anchors;

	public AnchorsCopyData(final ArrayList2<CopiedAnchorPosition> anchors) {
		this.anchors = anchors;
	}

	@Override
	public boolean isEmpty() {
		return anchors.isEmpty();
	}

	@Override
	public void paste(final ChartData data, final boolean convertFromBeats) {
		final BeatsMap beatsMap = data.songChart.beatsMap;
		final ArrayList2<Anchor> anchors = data.getCurrentArrangementLevel().anchors;

		ICopyData.simplePaste(beatsMap, data.time, anchors, this.anchors, convertFromBeats);
	}

}
