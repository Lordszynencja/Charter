package log.charter.services.data.copy.data;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.data.ChartData;
import log.charter.data.song.position.FractionalPosition;
import log.charter.services.data.selection.SelectionManager;

@XStreamAlias("emptyCopyData")
public class EmptyCopyData implements ICopyData, FullCopyData {

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public void paste(final ChartData chartData, final SelectionManager selectionManager,
			final FractionalPosition position, final boolean convertFromBeats) {
	}

}
