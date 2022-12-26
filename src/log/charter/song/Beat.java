package log.charter.song;

import static java.util.stream.Collectors.toCollection;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.io.rs.xml.song.EBeat;
import log.charter.song.enums.Position;
import log.charter.util.CollectionUtils.ArrayList2;

@XStreamAlias("beat")
public class Beat extends Position {
	public static ArrayList2<Beat> fromEbeats(final List<EBeat> ebeats) {
		return ebeats.stream()//
				.map(Beat::new)//
				.collect(toCollection(ArrayList2::new));
	}

	@XStreamAsAttribute
	public int beatsInMeasure = 4;
	@XStreamAsAttribute
	public boolean firstInMeasure = false;
	@XStreamAsAttribute
	public boolean anchor = false;

	public Beat(final int pos, final int beatsInMeasure, final boolean firstInMeasure) {
		super(pos);
		this.beatsInMeasure = beatsInMeasure;
		this.firstInMeasure = firstInMeasure;
	}

	private Beat(final EBeat ebeat) {
		super(ebeat.time);
		firstInMeasure = ebeat.measure != null && ebeat.measure == 1;
	}

	public Beat(final Beat other) {
		super(other);
		beatsInMeasure = other.beatsInMeasure;
		firstInMeasure = other.firstInMeasure;
		anchor = other.anchor;
	}
}