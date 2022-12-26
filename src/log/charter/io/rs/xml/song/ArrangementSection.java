package log.charter.io.rs.xml.song;

import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.io.rs.xml.converters.TimeConverter;
import log.charter.song.Section;
import log.charter.song.SectionType;
import log.charter.util.CollectionUtils.ArrayList2;

@XStreamAlias("section")
public class ArrangementSection {
	public static ArrayList2<ArrangementSection> fromSections(final ArrayList2<Section> sections) {
		final Map<SectionType, Integer> sectionNumbers = new HashMap<>();

		return sections.map(section -> {
			sectionNumbers.put(section.type, sectionNumbers.getOrDefault(section.type, 0) + 1);
			return new ArrangementSection(section, sectionNumbers.getOrDefault(section.type, 0));
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

	private ArrangementSection(final Section section, final int number) {
		name = section.type.rsName;
		this.number = number;
		startTime = section.beat.position;
	}

}
