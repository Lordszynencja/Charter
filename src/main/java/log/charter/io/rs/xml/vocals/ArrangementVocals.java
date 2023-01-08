package log.charter.io.rs.xml.vocals;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import log.charter.song.vocals.Vocals;
import log.charter.util.CollectionUtils.ArrayList2;

@XStreamAlias("vocals")
@XStreamInclude(value = { ArrangementVocal.class })
public class ArrangementVocals {
	public ArrayList2<ArrangementVocal> vocals = new ArrayList2<>();

	public ArrangementVocals() {
	}

	public ArrangementVocals(final Vocals vocals) {
		this.vocals = vocals.vocals.map(ArrangementVocal::new);
	}

	public ArrangementVocals(final ArrayList2<ArrangementVocal> vocals) {
		this.vocals = vocals;
	}
}