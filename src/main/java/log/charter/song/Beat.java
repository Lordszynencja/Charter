package log.charter.song;

import static java.lang.Math.abs;
import static java.util.stream.Collectors.toCollection;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.io.rs.xml.song.EBeat;
import log.charter.song.notes.Position;
import log.charter.util.CollectionUtils.ArrayList2;

@XStreamAlias("beat")
public class Beat extends Position {
	public static ArrayList2<Beat> fromEbeats(final List<EBeat> ebeats) {
		final ArrayList2<Beat> beats = ebeats.stream()//
				.map(Beat::new)//
				.collect(toCollection(ArrayList2::new));

		beats.get(0).anchor = true;
		for (int i = 1; i < beats.size() - 1; i++) {
			final Beat current = beats.get(i);
			final Beat previous = beats.get(i - 1);
			final Beat next = beats.get(i + 1);
			final int previousDistance = current.position() - previous.position();
			final int nextDistance = next.position() - current.position();
			if (abs(previousDistance - nextDistance) > 10) {
				current.anchor = true;
			}
		}

		return beats;
	}

	@XStreamAsAttribute
	public int beatsInMeasure = 4;
	@XStreamAsAttribute
	public int noteDenominator = 4;
	@XStreamAsAttribute
	public boolean firstInMeasure = false;
	@XStreamAsAttribute
	public boolean anchor = false;

	public Beat(final int pos, final int beatsInMeasure, final int noteDenominator, final boolean firstInMeasure) {
		super(pos);
		this.beatsInMeasure = beatsInMeasure;
		this.firstInMeasure = firstInMeasure;
		this.noteDenominator = noteDenominator;
	}

	private Beat(final EBeat ebeat) {
		super(ebeat.time);
		firstInMeasure = ebeat.measure != null && ebeat.measure == 1;
	}

	public Beat(final Beat other) {
		super(other);
		beatsInMeasure = other.beatsInMeasure;
		firstInMeasure = other.firstInMeasure;
		noteDenominator = other.noteDenominator;
		anchor = other.anchor;
	}

	public void setTimeSignature(final int beatsInMeasure, final int noteDenominator) {
		this.beatsInMeasure = beatsInMeasure;
		this.noteDenominator = noteDenominator;
	}
}