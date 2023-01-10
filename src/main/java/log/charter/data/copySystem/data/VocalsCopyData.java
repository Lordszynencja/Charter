package log.charter.data.copySystem.data;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.data.ChartData;
import log.charter.data.copySystem.data.positions.CopiedVocalPosition;
import log.charter.song.BeatsMap;
import log.charter.song.vocals.Vocal;
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
	public void paste(final ChartData data) {
		final BeatsMap beatsMap = data.songChart.beatsMap;
		final ArrayList2<Vocal> vocals = data.songChart.vocals.vocals;

		ICopyData.simplePaste(beatsMap, data.time, vocals, this.vocals);
	}

}
