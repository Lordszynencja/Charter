package log.charter.io.rs.xml.vocals;

import static log.charter.util.CollectionUtils.map;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.vocals.Vocals;
import log.charter.util.collections.ArrayList2;

@XStreamAlias("vocals")
@XStreamInclude(value = { ArrangementVocal.class })
public class ArrangementVocals {
	public List<ArrangementVocal> vocals = new ArrayList<>();

	public ArrangementVocals() {
	}

	public ArrangementVocals(final ImmutableBeatsMap beats, final Vocals vocals) {
		this.vocals = map(vocals.vocals, vocal -> {
			final int time = (int) vocal.position(beats);
			final int length = (int) vocal.endPosition().position(beats) - time;
			return new ArrangementVocal(time, length, vocal.lyrics());
		});
	}

	public ArrangementVocals(final ArrayList2<ArrangementVocal> vocals) {
		this.vocals = vocals;
	}
}