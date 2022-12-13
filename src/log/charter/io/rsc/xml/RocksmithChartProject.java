package log.charter.io.rsc.xml;

import java.math.BigDecimal;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.song.Beat;
import log.charter.song.Event;
import log.charter.song.Phrase;
import log.charter.song.PhraseIteration;
import log.charter.song.Section;
import log.charter.song.SongChart;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;

@XStreamAlias("project")
public class RocksmithChartProject {
	public int chartFormatVersion = 1;

	public String artistName;
	public String artistNameSort;
	public String title;
	public String albumName;
	public Integer albumYear;
	public BigDecimal crowdSpeed = new BigDecimal("1.0");

	public String musicFileName;
	public ArrayList2<String> arrangementFiles = new ArrayList2<>();

	public ArrayList2<Beat> beats = new ArrayList2<>();
	public ArrayList2<Event> events = new ArrayList2<>();
	public ArrayList2<Section> sections = new ArrayList2<>();
	public HashMap2<String, Phrase> phrases = new HashMap2<>();
	public ArrayList2<PhraseIteration> phraseIterations = new ArrayList2<>();

	public RocksmithChartProject() {
	}

	public RocksmithChartProject(final SongChart songChart) {
		artistName = songChart.artistName;
		artistNameSort = songChart.artistNameSort;
		title = songChart.title;
		albumName = songChart.albumName;
		albumYear = songChart.albumYear;
		crowdSpeed = songChart.crowdSpeed;

		musicFileName = songChart.musicFileName;

		beats = songChart.beatsMap.beats;
		events = songChart.beatsMap.events;
		sections = songChart.beatsMap.sections;
		phrases = songChart.beatsMap.phrases;
		phraseIterations = songChart.beatsMap.phraseIterations;
	}
}
