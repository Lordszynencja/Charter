package log.charter.io.rs.xml.song;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import log.charter.io.rs.xml.converters.ArrangementConverter;
import log.charter.io.rs.xml.converters.DateTimeConverter;
import log.charter.io.rs.xml.converters.TimeConverter;
import log.charter.io.rs.xml.converters.CountedListConverter.CountedList;

@XStreamAlias("song")
@XStreamInclude({ Tuning.class, ArrangementProperties.class })
public class Song {
	public String title;
	@XStreamConverter(ArrangementConverter.class)
	public Arrangement arrangement;
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
	public Tuning tuning;
	public int capo;
	public String artistName;
	public String artistNameSort;
	public String albumName;
	public Integer albumYear;
	public BigDecimal crowdSpeed;
	public ArrangementProperties arrangementProperties;
	public CountedList<Phrase> phrases;
	public CountedList<PhraseIteration> phraseIterations;
	public CountedList<String> newLinkedDiffs;
	public CountedList<String> linkedDiffs;
	public CountedList<String> phraseProperties;
	public CountedList<ChordTemplate> chordTemplates;
	public CountedList<ChordTemplate> fretHandMuteTemplates;
	public CountedList<EBeat> ebeats;
	public TranscriptionTrack transcriptionTrack;
	public CountedList<Level> levels;
}
