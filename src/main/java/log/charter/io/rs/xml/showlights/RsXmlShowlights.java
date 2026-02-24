package log.charter.io.rs.xml.showlights;

import static log.charter.util.CollectionUtils.map;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.Showlight;
import log.charter.util.collections.ArrayList2;

@XStreamAlias("showlights")
@XStreamInclude(value = { RsXmlShowlight.class })
public class RsXmlShowlights {
	public List<RsXmlShowlight> showlights = new ArrayList<>();

	public RsXmlShowlights() {
	}

	public RsXmlShowlights(final ImmutableBeatsMap beats, final List<Showlight> showlights) {
		this.showlights = map(showlights, showlight -> {
			final int time = (int) showlight.position(beats);
			final int note = showlight.type.note;
			return new RsXmlShowlight(time, note);
		});
	}

	public RsXmlShowlights(final ArrayList2<RsXmlShowlight> showlights) {
		this.showlights = showlights;
	}
}