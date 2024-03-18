package log.charter.services.data.copy.data;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.data.ChartData;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.types.PositionType;
import log.charter.services.data.copy.data.positions.CopiedAnchor;
import log.charter.services.data.selection.SelectionManager;

@XStreamAlias("anchorsCopyData")
public class AnchorsCopyData implements ICopyData {
	public final List<CopiedAnchor> anchors;

	public AnchorsCopyData(final List<CopiedAnchor> anchors) {
		this.anchors = anchors;
	}

	@Override
	public boolean isEmpty() {
		return anchors.isEmpty();
	}

	@Override
	public void paste(final ChartData chartData, final SelectionManager selectionManager,
			final FractionalPosition basePosition, final boolean convertFromBeats) {
		ICopyData.simplePasteFractional(chartData, selectionManager, PositionType.ANCHOR, basePosition, anchors,
				convertFromBeats);
	}

}
