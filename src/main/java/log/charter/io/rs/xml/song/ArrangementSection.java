package log.charter.io.rs.xml.song;

import static log.charter.util.CollectionUtils.map;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.EventPoint;
import log.charter.data.song.SectionType;
import log.charter.io.rs.xml.converters.TimeConverter;

@XStreamAlias("section")
public class ArrangementSection {
	public static List<ArrangementSection> fromSections(final ImmutableBeatsMap beats,
			final List<EventPoint> sections) {
		final Map<SectionType, Integer> sectionNumbers = new HashMap<>();

		return map(sections, section -> {
			final SectionType sectionType = section.section;
			sectionNumbers.put(sectionType, sectionNumbers.getOrDefault(sectionType, 0) + 1);
			return new ArrangementSection((int) section.position(beats), sectionType,
					sectionNumbers.getOrDefault(sectionType, 0));
		});
	}

	@XStreamAsAttribute
	public String name;

	/**
	 * 1-based
	 */
	@XStreamAsAttribute
	public int number;

	@XStreamAsAttribute
	@XStreamConverter(TimeConverter.class)
	public int startTime;

	public ArrangementSection() {
	}

	private ArrangementSection(final int position, final SectionType section, final int number) {
		startTime = position;
		name = section.rsName;
		this.number = number;
	}

}
