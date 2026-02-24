package log.charter.services.data.copy.data;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.data.ChartData;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.types.PositionType;
import log.charter.services.data.copy.data.positions.CopiedFHP;
import log.charter.services.data.selection.SelectionManager;

@XStreamAlias("fhpsCopyData")
public class FHPsCopyData implements ICopyData {
	public final List<CopiedFHP> fhps;

	public FHPsCopyData(final List<CopiedFHP> fhps) {
		this.fhps = fhps;
	}

	@Override
	public PositionType type() {
		return PositionType.FHP;
	}

	@Override
	public boolean isEmpty() {
		return fhps.isEmpty();
	}

	@Override
	public void paste(final ChartData chartData, final SelectionManager selectionManager,
			final FractionalPosition basePosition) {
		ICopyData.simplePasteFractional(chartData, selectionManager, PositionType.FHP, basePosition, fhps);
	}
}
