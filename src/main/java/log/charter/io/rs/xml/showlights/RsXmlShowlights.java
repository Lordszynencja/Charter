package log.charter.io.rs.xml.showlights;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
		this.showlights = showlights.stream().flatMap(showlight -> {
			final int time = (int) showlight.position(beats);
			return showlight.types.stream().map(type -> new RsXmlShowlight(time, type.note));
		}).collect(Collectors.toList());
	}

	public RsXmlShowlights(final ArrayList2<RsXmlShowlight> showlights) {
		this.showlights = showlights;
	}
}