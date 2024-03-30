package log.charter.data.song;

import static java.lang.Math.abs;
import static java.util.stream.Collectors.toCollection;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.data.song.position.time.Position;
import log.charter.io.rs.xml.song.EBeat;
import log.charter.io.rsc.xml.converters.BeatConverter;
import log.charter.util.collections.ArrayList2;
import log.charter.util.data.TimeSignature;

@XStreamAlias("beat2")
@XStreamConverter(BeatConverter.class)
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

	public int beatsInMeasure = 4;
	public int noteDenominator = 4;
	public boolean firstInMeasure = false;
	public boolean anchor = false;

	public Beat(final int position) {
		super(position);
	}

	public Beat(final int position, final TimeSignature timeSignature, final boolean firstInMeasure,
			final boolean anchor) {
		super(position);
		beatsInMeasure = timeSignature.numerator;
		noteDenominator = timeSignature.denominator;
		this.firstInMeasure = firstInMeasure;
		this.anchor = anchor;
	}

	public Beat(final int position, final int beatsInMeasure, final int noteDenominator, final boolean firstInMeasure,
			final boolean anchor) {
		super(position);
		this.beatsInMeasure = beatsInMeasure;
		this.noteDenominator = noteDenominator;
		this.firstInMeasure = firstInMeasure;
		this.anchor = anchor;
	}

	public Beat(final int pos, final int beatsInMeasure, final int noteDenominator, final boolean firstInMeasure) {
		super(pos);
		this.beatsInMeasure = beatsInMeasure;
		this.noteDenominator = noteDenominator;
		this.firstInMeasure = firstInMeasure;
	}

	private Beat(final EBeat ebeat) {
		super(ebeat.time);
		firstInMeasure = ebeat.measure != null && ebeat.measure == 1;
	}

	public Beat(final Beat other) {
		super(other);
		beatsInMeasure = other.beatsInMeasure;
		noteDenominator = other.noteDenominator;
		firstInMeasure = other.firstInMeasure;
		anchor = other.anchor;
	}

	public void setTimeSignature(final int beatsInMeasure, final int noteDenominator) {
		this.beatsInMeasure = beatsInMeasure;
		this.noteDenominator = noteDenominator;
	}
}