package log.charter.song;

import static java.util.stream.Collectors.toCollection;

import java.util.List;

import log.charter.io.rs.xml.song.ArrangementTone;
import log.charter.song.notes.Position;
import log.charter.util.CollectionUtils.ArrayList2;

public class ToneChange extends Position {
	public static ArrayList2<ToneChange> fromArrangementTones(final List<ArrangementTone> arrangementTones) {
		return arrangementTones.stream()//
				.map(arrangementTone -> new ToneChange(arrangementTone))//
				.collect(toCollection(ArrayList2::new));
	}

	public String toneName;

	public ToneChange() {
		super(0);
	}

	public ToneChange(final int position, final String toneName) {
		super(position);
		this.toneName = toneName;
	}

	public ToneChange(final ToneChange other) {
		super(other);
		toneName = other.toneName;
	}

	public ToneChange(final ArrangementTone arrangementTone) {
		super(arrangementTone.time);
		toneName = arrangementTone.name;
	}
}
