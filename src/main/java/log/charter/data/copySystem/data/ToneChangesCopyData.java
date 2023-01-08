package log.charter.data.copySystem.data;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.data.ChartData;
import log.charter.data.copySystem.data.positions.CopiedToneChangePosition;
import log.charter.song.Beat;
import log.charter.song.ToneChange;
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
	public void paste(final ChartData data) {
		final ArrayList2<Beat> beats = data.songChart.beatsMap.beats;
		final ArrayList2<ToneChange> toneChanges = data.getCurrentArrangement().toneChanges;

		ICopyData.simplePaste(beats, data.time, toneChanges, this.toneChanges);
	}

}
