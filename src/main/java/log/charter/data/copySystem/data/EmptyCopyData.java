package log.charter.data.copySystem.data;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.data.ChartData;

@XStreamAlias("emptyCopyData")
public class EmptyCopyData implements ICopyData, FullCopyData {

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public void paste(final int time, final ChartData data, final boolean convertFromBeats) {
	}

}
