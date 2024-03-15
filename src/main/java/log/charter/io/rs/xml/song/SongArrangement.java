package log.charter.io.rs.xml.song;

import static log.charter.util.CollectionUtils.contains;
import static log.charter.util.CollectionUtils.firstAfterEqual;
import static log.charter.util.CollectionUtils.lastBeforeEqual;
import static log.charter.util.CollectionUtils.map;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import log.charter.data.song.Arrangement;
import log.charter.data.song.Beat;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.EventPoint;
import log.charter.data.song.SectionType;
import log.charter.data.song.SongChart;
import log.charter.data.song.ToneChange;
import log.charter.io.rs.xml.converters.ArrangementTypeConverter;
import log.charter.io.rs.xml.converters.CountedListConverter.CountedList;
import log.charter.io.rs.xml.converters.DateTimeConverter;
import log.charter.io.rs.xml.converters.TimeConverter;

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

		final ImmutableBeatsMap beats = songChart.beatsMap.immutable;
		ebeats = new CountedList<>(map(songChart.beatsMap.beats, EBeat::new));
		sections = new CountedList<>(
				ArrangementSection.fromSections(beats, arrangement.getFilteredEventPoints(p -> p.section != null)));
		setPhrases(beats, arrangement);
		setTones(beats, arrangement);
		chordTemplates = new CountedList<>(arrangement.chordTemplates.stream()//
				.map(ArrangementChordTemplate::new)//
				.collect(Collectors.toList()));
		events = new CountedList<>(ArrangementEvent.fromEventsAndBeatMap(beats,
				arrangement.getFilteredEventPoints(p -> !p.events.isEmpty()), songChart.beatsMap));

		levels = new CountedList<>(ArrangementLevel.fromLevels(songChart.beatsMap.immutable, arrangement.levels,
				arrangement.chordTemplates));

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
		final Integer id = firstAfterEqual(phraseIterations.list, phraseIteration).findId();
		if (id == null) {
			phraseIterations.list.add(phraseIteration);
		} else {
			phraseIterations.list.add(id, phraseIteration);
		}
	}

	private void addDefaultPhrase(final Map<String, Integer> phraseIds, final String name,
			final IntSupplier positionGenerator) {
		if (contains(phraseIterations.list,
				phraseIteration -> phrases.list.get(phraseIteration.phraseId).name.equals(name))) {
			return;
		}

		addPhraseIfNotExisting(phraseIds, name);
		addPhraseIteration(new ArrangementPhraseIteration(positionGenerator.getAsInt(), phraseIds.get(name)));
	}

	private void addDefaultCountPhraseIfNeeded(final ImmutableBeatsMap beats, final Arrangement arrangement,
			final Map<String, Integer> phraseIds, final List<EventPoint> phraseEventPoints) {
		final IntSupplier countPositionTimeGenerator = () -> {
			if (phraseEventPoints.isEmpty()) {
				return beats.get(0).position();
			}

			int beatId = lastBeforeEqual(beats, phraseEventPoints.get(0).toPosition(beats)).findId(0);
			while (!beats.get(beatId).firstInMeasure && beatId > 0) {
				beatId--;
			}
			beatId--;
			while (!beats.get(beatId).firstInMeasure && beatId > 0) {
				beatId--;
			}

			return beats.get(beatId).position();
		};

		addDefaultPhrase(phraseIds, "COUNT", countPositionTimeGenerator);
	}

	private void addDefaultEndPhraseIfNeeded(final ImmutableBeatsMap beats, final Arrangement arrangement,
			final Map<String, Integer> phraseIds, final List<EventPoint> eventPoints) {
		final IntSupplier endPositionTimeGenerator = () -> {
			final EventPoint lastEventPoint = eventPoints.get(eventPoints.size() - 1);
			if (lastEventPoint == null || lastEventPoint.section != SectionType.NO_GUITAR) {
				return beats.get(beats.size() - 1).position();
			}

			int closestBeatId = firstAfterEqual(beats, lastEventPoint.toPosition(beats)).findId(beats.size());
			while (!beats.get(closestBeatId).firstInMeasure && closestBeatId < beats.size()) {
				closestBeatId++;
			}
			closestBeatId++;
			while (!beats.get(closestBeatId).firstInMeasure && closestBeatId < beats.size()) {
				closestBeatId++;
			}

			return beats.get(closestBeatId).position();
		};

		addDefaultPhrase(phraseIds, "END", endPositionTimeGenerator);
	}

	private void setPhrases(final ImmutableBeatsMap beats, final Arrangement arrangement) {
		phrases = new CountedList<>(map(arrangement.phrases, ArrangementPhrase::new));
		final List<EventPoint> phraseEventPoints = arrangement.getFilteredEventPoints(EventPoint::hasPhrase);
		final Map<String, Integer> phraseIds = new HashMap<>();
		for (int i = 0; i < phrases.list.size(); i++) {
			phraseIds.put(phrases.list.get(i).name, i);
		}
		phraseIterations = new CountedList<>(map(phraseEventPoints,
				p -> new ArrangementPhraseIteration(p.position(beats), phraseIds.get(p.phrase))));

		addDefaultCountPhraseIfNeeded(beats, arrangement, phraseIds, phraseEventPoints);
		addDefaultEndPhraseIfNeeded(beats, arrangement, phraseIds, arrangement.eventPoints);
	}

	private void setTones(final ImmutableBeatsMap beats, final Arrangement arrangement) {
		tonebase = arrangement.baseTone;
		final List<String> tonesList = new ArrayList<>(arrangement.toneChanges.size());
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
				: new CountedList<>(map(arrangement.toneChanges, //
						t -> new ArrangementTone(t.position(beats), t.toneName)));
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
