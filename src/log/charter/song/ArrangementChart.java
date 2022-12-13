package log.charter.song;

import java.math.BigDecimal;

import log.charter.io.rs.xml.song.ArrangementProperties;
import log.charter.io.rs.xml.song.ArrangementType;
import log.charter.io.rs.xml.song.ChordTemplate;
import log.charter.io.rs.xml.song.SongArrangement;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;

public class ArrangementChart {
	public ArrangementType arrangementType;
	public ArrangementProperties arrangementProperties = new ArrangementProperties();
	public Tuning tuning = new Tuning();
	public int capo = 0;
	public BigDecimal centOffset = BigDecimal.ZERO;

	public HashMap2<Integer, Level> levels = new HashMap2<>();

	public HashMap2<String, Phrase> phrases = new HashMap2<>();
	public ArrayList2<PhraseIteration> phraseIterations = new ArrayList2<>();
	public ArrayList2<Section> sections = new ArrayList2<>();
	public ArrayList2<Event> events = new ArrayList2<>();
	public ArrayList2<ChordTemplate> chordTemplates = new ArrayList2<>();
	public ArrayList2<ChordTemplate> fretHandMuteTemplates = new ArrayList2<>();

	public ArrangementChart() {
	}

	public ArrangementChart(final SongArrangement songArrangement) {
		arrangementType = songArrangement.arrangement;
		arrangementProperties = songArrangement.arrangementProperties;
		tuning = new Tuning(arrangementType.strings, songArrangement.tuning);
		capo = songArrangement.capo;
		centOffset = songArrangement.centOffset;

		levels = Level.fromArrangementLevels(songArrangement.levels.list);
		phrases = Phrase.fromArrangementPhrases(songArrangement.phrases.list);
		phraseIterations = PhraseIteration.fromArrangementPhraseIterations(songArrangement.phrases.list,
				songArrangement.phraseIterations.list);
		sections = Section.fromArrangementSections(songArrangement.sections.list);
		events = Event.fromArrangement(new ArrayList2<>(), songArrangement.events.list);
		chordTemplates = new ArrayList2<>(songArrangement.chordTemplates.list);
		fretHandMuteTemplates = new ArrayList2<>(songArrangement.fretHandMuteTemplates.list);
	}

	public String getTypeName() {
		String subType = "";
		if (arrangementProperties.bonusArr == 1) {
			subType = "Bonus";
		} else if (arrangementProperties.represent == 1) {
			subType = "Normal";
		} else {
			subType = "Alternate";
		}

		return arrangementType.name() + "_" + subType;
	}

	public String getTypeNameLabel() {
		return getTypeName().replace("_", " ");
	}
}
