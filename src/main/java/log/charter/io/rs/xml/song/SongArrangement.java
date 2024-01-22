package log.charter.io.rs.xml.song;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import log.charter.io.rs.xml.converters.ArrangementTypeConverter;
import log.charter.io.rs.xml.converters.CountedListConverter.CountedList;
import log.charter.io.rs.xml.converters.DateTimeConverter;
import log.charter.io.rs.xml.converters.TimeConverter;
import log.charter.song.ArrangementChart;
import log.charter.song.Beat;
import log.charter.song.SongChart;
import log.charter.song.ToneChange;
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
	public CountedList<ArrangementChordTemplate> fretHandMuteTemplates;
	public CountedList<EBeat> ebeats;
	public CountedList<ArrangementSection> sections;
	public CountedList<ArrangementEvent> events;
	public TranscriptionTrack transcriptionTrack = new TranscriptionTrack();
	public CountedList<ArrangementLevel> levels;

	public SongArrangement(final SongChart songChart, final ArrangementChart arrangementChart) {
		final List<Beat> beatsTmp = songChart.beatsMap.beats;

		title = songChart.title;
		arrangement = arrangementChart.arrangementType;
		offset = -beatsTmp.get(0).position();
		centOffset = arrangementChart.centOffset;
		songLength = songChart.beatsMap.songLengthMs;
		lastConversionDateTime = LocalDateTime.now();
		startBeat = -offset;
		averageTempo = new BigDecimal(
				(double) (beatsTmp.get(beatsTmp.size() - 1).position() - startBeat) / (beatsTmp.size() - 1))
				.setScale(3, RoundingMode.HALF_UP);
		tuning = new ArrangementTuning(arrangementChart.tuning);
		capo = arrangementChart.capo;
		artistName = songChart.artistName;
		artistNameSort = songChart.artistNameSort;
		albumName = songChart.albumName;
		albumYear = songChart.albumYear;
		arrangementProperties = arrangementChart.arrangementProperties;
		setTones(arrangementChart);
		phrases = new CountedList<ArrangementPhrase>(arrangementChart.phrases.map(ArrangementPhrase::new));
		phraseIterations = new CountedList<>(ArrangementPhraseIteration.fromPhraseIterations(phrases.list,
				arrangementChart.getFilteredEventPoints(p -> p.phrase != null)));
		chordTemplates = new CountedList<>(arrangementChart.chordTemplates.map(ArrangementChordTemplate::new));
		fretHandMuteTemplates = new CountedList<>(
				arrangementChart.fretHandMuteTemplates.map(ArrangementChordTemplate::new));
		ebeats = new CountedList<>(songChart.beatsMap.beats.map(EBeat::new));
		sections = new CountedList<>(
				ArrangementSection.fromSections(arrangementChart.getFilteredEventPoints(p -> p.section != null)));
		events = new CountedList<>(ArrangementEvent.fromEventsAndBeatMap(
				arrangementChart.getFilteredEventPoints(p -> !p.events.isEmpty()), songChart.beatsMap));
		levels = new CountedList<>(
				ArrangementLevel.fromLevels(arrangementChart.levels, arrangementChart.chordTemplates));

		fixMeasureNumbers();
	}

	private void setTones(final ArrangementChart arrangementChart) {
		tonebase = arrangementChart.baseTone;
		final ArrayList2<String> tonesList = new ArrayList2<>();
		for (final ToneChange toneChange : arrangementChart.toneChanges) {
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

		tones = arrangementChart.toneChanges.isEmpty() ? null
				: new CountedList<>(arrangementChart.toneChanges.map(ArrangementTone::new));
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
