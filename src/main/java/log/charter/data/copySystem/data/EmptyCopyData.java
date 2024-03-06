package log.charter.data.copySystem.data;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.data.ChartData;
import log.charter.data.managers.selection.SelectionManager;

@XStreamAlias("emptyCopyData")
public class EmptyCopyData implements ICopyData, FullCopyData {

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public void paste(final ChartData chartData, final SelectionManager selectionManager, final int time,
			final boolean convertFromBeats) {
	}

}
