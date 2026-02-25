package log.charter.services.data.copy.data;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.data.ChartData;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.types.PositionType;
import log.charter.services.data.copy.data.positions.CopiedShowlight;
import log.charter.services.data.selection.SelectionManager;

@XStreamAlias("showlightsCopyData")
public class ShowlightsCopyData implements ICopyData {
	public final List<CopiedShowlight> showlights;

	public ShowlightsCopyData(final List<CopiedShowlight> showlights) {
		this.showlights = showlights;
	}

	@Override
	public PositionType type() {
		return PositionType.SHOWLIGHT;
	}

	@Override
	public boolean isEmpty() {
		return showlights.isEmpty();
	}

	@Override
	public void paste(final ChartData chartData, final SelectionManager selectionManager,
			final FractionalPosition basePosition) {
		ICopyData.simplePasteFractional(chartData, selectionManager, PositionType.SHOWLIGHT, basePosition, showlights);
	}

}
