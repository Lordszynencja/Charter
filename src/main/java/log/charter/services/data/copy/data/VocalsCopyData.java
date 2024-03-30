package log.charter.services.data.copy.data;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.data.ChartData;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.types.PositionType;
import log.charter.services.data.copy.data.positions.CopiedVocalPosition;
import log.charter.services.data.selection.SelectionManager;

@XStreamAlias("vocalsCopyData")
public class VocalsCopyData implements ICopyData {
	public final List<CopiedVocalPosition> vocals;

	public VocalsCopyData(final List<CopiedVocalPosition> vocals) {
		this.vocals = vocals;
	}

	@Override
	public PositionType type() {
		return PositionType.VOCAL;
	}

	@Override
	public boolean isEmpty() {
		return vocals.isEmpty();
	}

	@Override
	public void paste(final ChartData chartData, final SelectionManager selectionManager,
			final FractionalPosition basePosition, final boolean convertFromBeats) {
		ICopyData.simplePasteFractional(chartData, selectionManager, PositionType.VOCAL, basePosition, vocals,
				convertFromBeats);
	}

}
