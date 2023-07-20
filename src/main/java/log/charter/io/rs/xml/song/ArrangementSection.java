package log.charter.io.rs.xml.song;

import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.io.rs.xml.converters.TimeConverter;
import log.charter.song.EventPoint;
import log.charter.song.SectionType;
import log.charter.util.CollectionUtils.ArrayList2;

@XStreamAlias("section")
public class ArrangementSection {
	public static ArrayList2<ArrangementSection> fromSections(final ArrayList2<EventPoint> sections) {
		final Map<SectionType, Integer> sectionNumbers = new HashMap<>();

		return sections.map(section -> {
			final SectionType sectionType = section.section;
			sectionNumbers.put(sectionType, sectionNumbers.getOrDefault(sectionType, 0) + 1);
			return new ArrangementSection(section.position(), sectionType, sectionNumbers.getOrDefault(sectionType, 0));
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
