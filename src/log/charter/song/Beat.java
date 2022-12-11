package log.charter.song;

import static java.util.stream.Collectors.toCollection;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.gui.SelectionManager.Selectable;
import log.charter.io.rs.xml.song.EBeat;
import log.charter.util.CollectionUtils.ArrayList2;

@XStreamAlias("beat")
public class Beat extends Position implements Selectable {
	public static ArrayList2<Beat> fromEbeats(final List<EBeat> ebeats) {
		return ebeats.stream()//
				.map(Beat::new)//
				.collect(toCollection(ArrayList2::new));
	}

	public int beatsInMeasure = 4;
	public boolean firstInMeasure = false;
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

	@Override
	public String getSignature() {
		return position + "";
	}
}