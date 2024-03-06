package log.charter.data.copySystem.data;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.data.ChartData;
import log.charter.data.copySystem.data.positions.CopiedAnchorPosition;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
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
	public void paste(final ChartData chartData, final SelectionManager selectionManager, final int time,
			final boolean convertFromBeats) {

		ICopyData.simplePaste(chartData, selectionManager, PositionType.ANCHOR, time, anchors, convertFromBeats);
	}

}
