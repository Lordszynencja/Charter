package log.charter.services.data.copy.data;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.data.ChartData;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.types.PositionType;
import log.charter.services.data.copy.data.positions.CopiedToneChangePosition;
import log.charter.services.data.selection.SelectionManager;

@XStreamAlias("toneChangesCopyData")
public class ToneChangesCopyData implements ICopyData {
	public final List<CopiedToneChangePosition> toneChanges;

	public ToneChangesCopyData(final List<CopiedToneChangePosition> toneChanges) {
		this.toneChanges = toneChanges;
	}

	@Override
	public boolean isEmpty() {
		return toneChanges.isEmpty();
	}

	@Override
	public void paste(final ChartData chartData, final SelectionManager selectionManager,
			final FractionalPosition basePosition, final boolean convertFromBeats) {
		ICopyData.simplePasteFractional(chartData, selectionManager, PositionType.TONE_CHANGE, basePosition,
				toneChanges, convertFromBeats);
	}

}
