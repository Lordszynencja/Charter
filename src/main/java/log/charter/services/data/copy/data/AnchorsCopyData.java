package log.charter.services.data.copy.data;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.data.ChartData;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.types.PositionType;
import log.charter.services.data.copy.data.positions.CopiedAnchorPosition;
import log.charter.services.data.selection.SelectionManager;

@XStreamAlias("anchorsCopyData")
public class AnchorsCopyData implements ICopyData {
	public final List<CopiedAnchorPosition> anchors;

	public AnchorsCopyData(final List<CopiedAnchorPosition> anchors) {
		this.anchors = anchors;
	}

	@Override
	public boolean isEmpty() {
		return anchors.isEmpty();
	}

	@Override
	public void paste(final ChartData chartData, final SelectionManager selectionManager,
			final FractionalPosition position, final boolean convertFromBeats) {
		ICopyData.simplePaste(chartData, selectionManager, PositionType.ANCHOR, position, anchors, convertFromBeats);
	}

}
