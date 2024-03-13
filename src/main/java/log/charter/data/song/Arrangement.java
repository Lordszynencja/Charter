package log.charter.data.song;

import static log.charter.util.CollectionUtils.closest;
import static log.charter.util.CollectionUtils.lastBeforeEqual;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import log.charter.data.config.Localization.Label;
import log.charter.data.song.configs.Tuning;
import log.charter.data.song.position.IConstantPosition;
import log.charter.data.song.position.Position;
import log.charter.io.rs.xml.song.ArrangementType;
import log.charter.io.rsc.xml.converters.PhraseDataConverter;
import log.charter.util.collections.ArrayList2;

@XStreamAlias("arrangement")
@XStreamInclude({ ChordTemplate.class, EventPoint.class, Level.class, Phrase.class, ToneChange.class })
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

	public List<EventPoint> eventPoints = new ArrayList<>();
	@XStreamConverter(PhraseDataConverter.class)
	public Map<String, Phrase> phrases = new HashMap<>();
	public Set<String> tones = new HashSet<>();
	public List<ToneChange> toneChanges = new ArrayList<>();
	public List<ChordTemplate> chordTemplates = new ArrayList<>();
	public List<Level> levels = new ArrayList<>();

	public Arrangement() {
		setLevel(0, new Level());
	}

	public Arrangement(final ArrangementType arrangementType) {
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
		EventPoint eventPoint = lastBeforeEqual(eventPoints, new Position(position), IConstantPosition::compareTo)
				.find();
		if (eventPoint == null || eventPoint.position() != position) {
			eventPoint = new EventPoint(position);
			eventPoints.add(eventPoint);
			eventPoints.sort(null);
		}

		return eventPoint;
	}

	public String getTypeNameLabel() {
		return arrangementType.label.label() + " " + arrangementSubtype.label.label();
	}

	public String getTypeNameLabel(final int id) {
		return "[%d] %s".formatted(id + 1, getTypeNameLabel());
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

		final Integer closestId = closest(eventPoints, new Position(position), IConstantPosition::compareTo,
				p -> p.position()).findId();
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
