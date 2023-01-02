package log.charter.song;

import java.math.BigDecimal;

import log.charter.io.rs.xml.song.ArrangementProperties;
import log.charter.io.rs.xml.song.ArrangementType;
import log.charter.io.rs.xml.song.SongArrangement;
import log.charter.song.configs.Tuning;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;

public class ArrangementChart {
	public ArrangementType arrangementType;
	public ArrangementProperties arrangementProperties = new ArrangementProperties();
	public Tuning tuning = new Tuning();
	public int capo = 0;
	public BigDecimal centOffset = BigDecimal.ZERO;

	public HashMap2<Integer, Level> levels = new HashMap2<>();

	public ArrayList2<Section> sections = new ArrayList2<>();
	public HashMap2<String, Phrase> phrases = new HashMap2<>();
	public ArrayList2<PhraseIteration> phraseIterations = new ArrayList2<>();
	public ArrayList2<Event> events = new ArrayList2<>();
	public ArrayList2<ChordTemplate> chordTemplates = new ArrayList2<>();
	public ArrayList2<ChordTemplate> fretHandMuteTemplates = new ArrayList2<>();

	public ArrangementChart(final ArrayList2<Beat> beats) {
		phrases.put("COUNT", new Phrase(0, false));
		phrases.put("END", new Phrase(0, false));
		phraseIterations.add(new PhraseIteration(beats.get(0), "COUNT"));
		phraseIterations.add(new PhraseIteration(beats.getLast(), "END"));
	}

	public ArrangementChart(final SongArrangement songArrangement, final ArrayList2<Beat> beats) {
		arrangementType = songArrangement.arrangement;
		arrangementProperties = songArrangement.arrangementProperties;
		tuning = new Tuning(arrangementType.strings, songArrangement.tuning);
		capo = songArrangement.capo;
		centOffset = songArrangement.centOffset;

		chordTemplates = songArrangement.chordTemplates.list.map(ChordTemplate::new);

		sections = Section.fromArrangementSections(beats, songArrangement.sections.list);
		phrases = Phrase.fromArrangementPhrases(songArrangement.phrases.list);
		phraseIterations = PhraseIteration.fromArrangementPhraseIterations(beats, songArrangement.phrases.list,
				songArrangement.phraseIterations.list);
		events = Event.fromArrangement(new ArrayList2<>(), songArrangement.events.list);

		if (songArrangement.fretHandMuteTemplates != null) {
			fretHandMuteTemplates = songArrangement.fretHandMuteTemplates.list.map(ChordTemplate::new);
		}

		levels = Level.fromArrangementLevels(this, songArrangement.levels.list);
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
}
