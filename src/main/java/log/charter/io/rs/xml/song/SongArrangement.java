package log.charter.io.rs.xml.song;

import static log.charter.data.song.notes.IConstantPosition.findClosestId;
import static log.charter.data.song.notes.IConstantPosition.findFirstIdAfterEqual;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import log.charter.data.song.Arrangement;
import log.charter.data.song.Beat;
import log.charter.data.song.BeatsMap;
import log.charter.data.song.EventPoint;
import log.charter.data.song.SectionType;
import log.charter.data.song.SongChart;
import log.charter.data.song.ToneChange;
import log.charter.io.rs.xml.converters.ArrangementTypeConverter;
import log.charter.io.rs.xml.converters.CountedListConverter.CountedList;
import log.charter.io.rs.xml.converters.DateTimeConverter;
import log.charter.io.rs.xml.converters.TimeConverter;
import log.charter.util.CollectionUtils.ArrayList2;

@XStreamAlias("song")
@XStreamInclude({ ArrangementTuning.class, ArrangementProperties.class, TranscriptionTrack.class })
public class SongArrangement {
	@XStreamAsAttribute
	public int version = 8;
	public String title;
	@XStreamConverter(ArrangementTypeConverter.class)
	public ArrangementType arrangement;
	public int part = 1;
	@XStreamConverter(TimeConverter.class)
	public Integer offset;
	public BigDecimal centOffset;
	@XStreamConverter(TimeConverter.class)
	public int songLength;
	@XStreamConverter(DateTimeConverter.class)
	public LocalDateTime lastConversionDateTime;
	@XStreamConverter(TimeConverter.class)
	public int startBeat;
	public BigDecimal averageTempo;
	public ArrangementTuning tuning;
	public int capo;
	public String artistName;
	public String artistNameSort;
	public String albumName;
	public Integer albumYear;
	public ArrangementProperties arrangementProperties = new ArrangementProperties();
	public String tonebase;
	public String tonea;
	public String toneb;
	public String tonec;
	public String toned;
	public CountedList<ArrangementTone> tones;
	public CountedList<ArrangementPhrase> phrases;
	public CountedList<ArrangementPhraseIteration> phraseIterations;
	public CountedList<String> phraseProperties = new CountedList<>();
	public CountedList<ArrangementChordTemplate> chordTemplates;
	public CountedList<ArrangementChordTemplate> fretHandMuteTemplates = new CountedList<>();
	public CountedList<EBeat> ebeats;
	public CountedList<ArrangementSection> sections;
	public CountedList<ArrangementEvent> events;
	public TranscriptionTrack transcriptionTrack = new TranscriptionTrack();
	public CountedList<ArrangementLevel> levels;

	public SongArrangement(final int audioLength, final SongChart songChart, final Arrangement arrangement) {
		final List<Beat> beatsTmp = songChart.beatsMap.beats;

		title = songChart.title();
		this.arrangement = arrangement.arrangementType;
		offset = -beatsTmp.get(0).position();
		centOffset = arrangement.centOffset;
		songLength = audioLength;
		lastConversionDateTime = LocalDateTime.now();
		startBeat = -offset;
		averageTempo = new BigDecimal(
				(double) (beatsTmp.get(beatsTmp.size() - 1).position() - startBeat) / (beatsTmp.size() - 1))
				.setScale(3, RoundingMode.HALF_UP);
		tuning = new ArrangementTuning(arrangement.tuning);
		capo = arrangement.capo;
		artistName = songChart.artistName();
		artistNameSort = songChart.artistNameSort();
		albumName = songChart.albumName();
		albumYear = songChart.albumYear;
		setArrangementProperties(arrangement);

		ebeats = new CountedList<>(songChart.beatsMap.beats.map(EBeat::new));
		sections = new CountedList<>(
				ArrangementSection.fromSections(arrangement.getFilteredEventPoints(p -> p.section != null)));
		setPhrases(songChart.beatsMap, arrangement);
		setTones(arrangement);
		chordTemplates = new CountedList<>(arrangement.chordTemplates.map(ArrangementChordTemplate::new));
		events = new CountedList<>(ArrangementEvent.fromEventsAndBeatMap(
				arrangement.getFilteredEventPoints(p -> !p.events.isEmpty()), songChart.beatsMap));

		levels = new CountedList<>(ArrangementLevel.fromLevels(arrangement.levels, arrangement.chordTemplates));

		fixMeasureNumbers();
	}

	private void setArrangementProperties(final Arrangement arrangementChart) {
		arrangementProperties = new ArrangementProperties();
		arrangementProperties.setType(arrangementChart.arrangementType);
		arrangementProperties.setSubtype(arrangementChart.arrangementSubtype);
	}

	private void addPhraseIfNotExisting(final Map<String, Integer> phraseIds, final String name) {
		if (phraseIds.containsKey(name)) {
			return;
		}

		phrases.list.add(new ArrangementPhrase(name));
		phraseIds.put(name, phrases.list.size() - 1);
	}

	private void addPhraseIteration(final ArrangementPhraseIteration phraseIteration) {
		final int id = findFirstIdAfterEqual(phraseIterations.list, phraseIteration.time);
		if (id < 0) {
			phraseIterations.list.add(phraseIteration);
		} else {
			phraseIterations.list.add(id, phraseIteration);
		}
	}

	private void addDefaultPhrase(final Map<String, Integer> phraseIds, final String name,
			final IntSupplier positionGenerator) {
		if (phraseIterations.list
				.contains(phraseIteration -> phrases.list.get(phraseIteration.phraseId).name.equals(name))) {
			return;
		}

		addPhraseIfNotExisting(phraseIds, name);
		addPhraseIteration(new ArrangementPhraseIteration(positionGenerator.getAsInt(), phraseIds.get(name)));
	}

	private void addDefaultCountPhraseIfNeeded(final BeatsMap beatsMap, final Arrangement arrangement,
			final Map<String, Integer> phraseIds, final ArrayList2<EventPoint> phraseEventPoints) {
		final IntSupplier countPositionTimeGenerator = () -> {
			if (phraseEventPoints.isEmpty()) {
				return beatsMap.getBeatSafe(0).position();
			}

			Integer beatId = findClosestId(beatsMap.beats, phraseEventPoints.get(0).position());
			while (!beatsMap.getBeatSafe(beatId).firstInMeasure && beatId > 0) {
				beatId--;
			}
			beatId--;
			while (!beatsMap.getBeatSafe(beatId).firstInMeasure && beatId > 0) {
				beatId--;
			}

			return beatsMap.getBeatSafe(beatId).position();
		};

		addDefaultPhrase(phraseIds, "COUNT", countPositionTimeGenerator);
	}

	private void addDefaultEndPhraseIfNeeded(final BeatsMap beatsMap, final Arrangement arrangement,
			final Map<String, Integer> phraseIds, final ArrayList2<EventPoint> eventPoints) {
		final IntSupplier endPositionTimeGenerator = () -> {
			final EventPoint lastEventPoint = eventPoints.getLast();
			if (lastEventPoint == null || lastEventPoint.section != SectionType.NO_GUITAR) {
				return beatsMap.getBeatSafe(beatsMap.beats.size() - 1).position();
			}

			Integer closestBeatId = findClosestId(beatsMap.beats, lastEventPoint.position());
			while (!beatsMap.getBeatSafe(closestBeatId).firstInMeasure && closestBeatId < beatsMap.beats.size()) {
				closestBeatId++;
			}
			closestBeatId++;
			while (!beatsMap.getBeatSafe(closestBeatId).firstInMeasure && closestBeatId < beatsMap.beats.size()) {
				closestBeatId++;
			}

			return beatsMap.getBeatSafe(closestBeatId).position();
		};

		addDefaultPhrase(phraseIds, "END", endPositionTimeGenerator);
	}

	private void setPhrases(final BeatsMap beatsMap, final Arrangement arrangement) {
		phrases = new CountedList<ArrangementPhrase>(arrangement.phrases.map(ArrangementPhrase::new));
		final ArrayList2<EventPoint> phraseEventPoints = arrangement.getFilteredEventPoints(EventPoint::hasPhrase);
		final Map<String, Integer> phraseIds = new HashMap<>();
		for (int i = 0; i < phrases.list.size(); i++) {
			phraseIds.put(phrases.list.get(i).name, i);
		}
		phraseIterations = new CountedList<>(
				phraseEventPoints.map(phraseIteration -> new ArrangementPhraseIteration(phraseIteration.position(),
						phraseIds.get(phraseIteration.phrase))));

		addDefaultCountPhraseIfNeeded(beatsMap, arrangement, phraseIds, phraseEventPoints);
		addDefaultEndPhraseIfNeeded(beatsMap, arrangement, phraseIds, arrangement.eventPoints);
	}

	private void setTones(final Arrangement arrangement) {
		tonebase = arrangement.baseTone;
		final ArrayList2<String> tonesList = new ArrayList2<>();
		for (final ToneChange toneChange : arrangement.toneChanges) {
			if (!tonesList.contains(toneChange.toneName)) {
				tonesList.add(toneChange.toneName);
			}
		}

		tonesList.sort((a, b) -> a.equals(tonebase) ? -1 : b.equals(tonebase) ? 1 : 0);
		if (tonesList.size() >= 1) {
			tonea = tonesList.get(0);
		}
		if (tonesList.size() >= 2) {
			toneb = tonesList.get(1);
		}
		if (tonesList.size() >= 3) {
			tonec = tonesList.get(2);
		}
		if (tonesList.size() >= 4) {
			toned = tonesList.get(3);
		}

		tones = arrangement.toneChanges.isEmpty() ? null
				: new CountedList<>(arrangement.toneChanges.map(ArrangementTone::new));
	}

	private void fixMeasureNumbers() {
		int measuresCount = 1;
		for (final EBeat ebeat : ebeats.list) {
			if (ebeat.measure != null) {
				ebeat.measure = measuresCount++;
			}
		}
	}

}
