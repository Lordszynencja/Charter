package log.charter.data.copySystem.data;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.data.ChartData;
import log.charter.data.copySystem.data.positions.CopiedToneChangePosition;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.util.CollectionUtils.ArrayList2;

@XStreamAlias("toneChangesCopyData")
public class ToneChangesCopyData implements ICopyData {
	public final ArrayList2<CopiedToneChangePosition> toneChanges;

	public ToneChangesCopyData(final ArrayList2<CopiedToneChangePosition> toneChanges) {
		this.toneChanges = toneChanges;
	}

	@Override
	public boolean isEmpty() {
		return toneChanges.isEmpty();
	}

	@Override
	public void paste(final ChartData chartData, final SelectionManager selectionManager, final int time,
			final boolean convertFromBeats) {
		ICopyData.simplePaste(chartData, selectionManager, PositionType.TONE_CHANGE, time, toneChanges,
				convertFromBeats);
	}

}
