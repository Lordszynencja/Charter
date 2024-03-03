package log.charter.song;

import static log.charter.song.notes.IConstantPosition.findClosest;
import static log.charter.song.notes.IConstantPosition.findClosestId;

import java.math.BigDecimal;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import log.charter.data.config.Localization.Label;
import log.charter.io.rs.xml.song.ArrangementType;
import log.charter.io.rsc.xml.converters.PhraseDataConverter;
import log.charter.song.configs.Tuning;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;
import log.charter.util.CollectionUtils.HashSet2;

@XStreamAlias("arrangement")
@XStreamInclude({ EventPoint.class, Phrase.class, ToneChange.class })
public class Arrangement {
	public enum ArrangementSubtype {
		MAIN(Label.ARRANGEMENT_SUBTYPE_MAIN), //
		BONUS(Label.ARRANGEMENT_SUBTYPE_BONUS), //
		ALTERNATE(Label.ARRANGEMENT_SUBTYPE_ALTERNATE);

		public final Label label;

		private ArrangementSubtype(final Label label) {
			this.label = label;
		}

		@Override
		public String toString() {
			return label.label();
		}
	}

	@XStreamAsAttribute
	@XStreamAlias("type")
	public ArrangementType arrangementType = ArrangementType.Lead;
	@XStreamAsAttribute
	@XStreamAlias("subtype")
	public ArrangementSubtype arrangementSubtype = ArrangementSubtype.MAIN;
	public Tuning tuning = new Tuning();
	@XStreamAsAttribute
	public int capo = 0;
	@XStreamAsAttribute
	public BigDecimal centOffset = BigDecimal.ZERO;
	@XStreamAsAttribute
	public String baseTone = "base";

	public ArrayList2<EventPoint> eventPoints = new ArrayList2<>();
	@XStreamConverter(PhraseDataConverter.class)
	public HashMap2<String, Phrase> phrases = new HashMap2<>();
	public HashSet2<String> tones = new HashSet2<>();
	public ArrayList2<ToneChange> toneChanges = new ArrayList2<>();
	public ArrayList2<ChordTemplate> chordTemplates = new ArrayList2<>();
	public ArrayList2<Level> levels = new ArrayList2<>();

	public Arrangement() {
		setLevel(0, new Level());
	}

	public Arrangement(final ArrangementType arrangementType, final int songStart, final int songEnd) {
		setPhrase(songStart, "COUNT");
		setPhrase(songEnd, "END");
		this.arrangementType = arrangementType;
		setLevel(0, new Level());
	}

	public Level getLevel(final int id) {
		return id < 0 || id >= levels.size() ? new Level() : levels.get(id);
	}

	public void setLevel(final int id, final Level level) {
		while (levels.size() <= id) {
			levels.add(new Level());
		}

		levels.set(id, level);
	}

	public EventPoint findOrCreateArrangementEventsPoint(final int position) {
		EventPoint arrangementEventsPoint = findClosest(eventPoints, position);
		if (arrangementEventsPoint == null || arrangementEventsPoint.position() != position) {
			arrangementEventsPoint = new EventPoint(position);
			eventPoints.add(arrangementEventsPoint);
			eventPoints.sort(null);
		}

		return arrangementEventsPoint;
	}

	public String getTypeNameLabel() {
		return arrangementType.label.label() + " " + arrangementSubtype.label.label();
	}

	public int getChordTemplateIdWithSave(final ChordTemplate chordTemplate) {
		for (int i = 0; i < chordTemplates.size(); i++) {
			final ChordTemplate existingChordTemplate = chordTemplates.get(i);
			if (existingChordTemplate.equals(chordTemplate)) {
				return i;
			}
		}

		chordTemplates.add(chordTemplate);
		return chordTemplates.size() - 1;
	}

	public ArrayList2<EventPoint> getFilteredEventPoints(final Predicate<EventPoint> filter) {
		return eventPoints.stream().filter(filter).collect(Collectors.toCollection(ArrayList2::new));
	}

	public String getTuningName(final String format) {
		return tuning.getFullName(format, isBass());
	}

	public String[] getSimpleStringNames() {
		return tuning.getStringNames(true, isBass());
	}

	public boolean isBass() {
		return arrangementType == ArrangementType.Bass || tuning.strings() < 6;
	}

	public void setPhrase(final int position, final String name) {
		if (!phrases.containsKey(name)) {
			phrases.put(name, new Phrase());
		}

		final Integer closestId = findClosestId(eventPoints, position);
		if (closestId == null) {
			final EventPoint count = new EventPoint(position);
			count.phrase = name;
			eventPoints.add(count);
			return;
		}

		final EventPoint closestEventPoint = eventPoints.get(closestId);
		if (closestEventPoint.position() != position) {
			final EventPoint count = new EventPoint(position);
			count.phrase = name;

			eventPoints.add(closestEventPoint.position() < position ? closestId + 1 : closestId, count);
		} else {
			closestEventPoint.phrase = name;
		}
	}
}
