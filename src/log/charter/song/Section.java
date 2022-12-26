package log.charter.song;

import static java.util.stream.Collectors.toCollection;

import java.util.List;

import log.charter.io.rs.xml.song.ArrangementSection;
import log.charter.util.CollectionUtils.ArrayList2;

public class Section extends OnBeat {
	public static ArrayList2<Section> fromArrangementSections(final ArrayList2<Beat> beats,
			final List<ArrangementSection> arrangementSections) {
		return arrangementSections.stream()//
				.map(arrangementSection -> new Section(beats, arrangementSection))//
				.collect(toCollection(ArrayList2::new));
	}

	public SectionType type;

	public Section(final Beat beat, final SectionType type) {
		super(beat);
		this.type = type;
	}

	private Section(final ArrayList2<Beat> beats, final ArrangementSection arrangementSection) {
		super(beats, arrangementSection.startTime);
		type = SectionType.findByRSName(arrangementSection.name);
	}

	public Section(final ArrayList2<Beat> beats, final Section other) {
		super(beats, other);
		type = other.type;
	}
}
