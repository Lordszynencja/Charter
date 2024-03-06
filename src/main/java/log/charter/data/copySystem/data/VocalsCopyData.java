package log.charter.data.copySystem.data;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.data.ChartData;
import log.charter.data.copySystem.data.positions.CopiedVocalPosition;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.util.CollectionUtils.ArrayList2;

@XStreamAlias("vocalsCopyData")
public class VocalsCopyData implements ICopyData {
	public final ArrayList2<CopiedVocalPosition> vocals;

	public VocalsCopyData(final ArrayList2<CopiedVocalPosition> vocals) {
		this.vocals = vocals;
	}

	@Override
	public boolean isEmpty() {
		return vocals.isEmpty();
	}

	@Override
	public void paste(final ChartData chartData, final SelectionManager selectionManager, final int time,
			final boolean convertFromBeats) {
		ICopyData.simplePaste(chartData, selectionManager, PositionType.VOCAL, time, vocals, convertFromBeats);
	}

}
