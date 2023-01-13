package log.charter.song;

import java.math.BigDecimal;

import log.charter.io.rs.xml.song.ArrangementProperties;
import log.charter.io.rs.xml.song.ArrangementType;
import log.charter.io.rs.xml.song.SongArrangement;
import log.charter.song.configs.Tuning;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;
import log.charter.util.CollectionUtils.HashSet2;

public class ArrangementChart {
	public enum ArrangementSubtype {
		MAIN("Main"), //
		BONUS("Bonus"), //
		ALTERNATE("Alternate");

		public final String name;

		private ArrangementSubtype(final String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public ArrangementType arrangementType = ArrangementType.Lead;
	public ArrangementProperties arrangementProperties = new ArrangementProperties();
	public Tuning tuning = new Tuning();
	public int capo = 0;
	public BigDecimal centOffset = BigDecimal.ZERO;
	public String baseTone = "base";

	public HashMap2<Integer, Level> levels = new HashMap2<>();

	public ArrayList2<Section> sections = new ArrayList2<>();
	public HashMap2<String, Phrase> phrases = new HashMap2<>();
	public ArrayList2<PhraseIteration> phraseIterations = new ArrayList2<>();
	public ArrayList2<Event> events = new ArrayList2<>();
	public HashSet2<String> tones = new HashSet2<>();
	public ArrayList2<ToneChange> toneChanges = new ArrayList2<>();
	public ArrayList2<ChordTemplate> chordTemplates = new ArrayList2<>();
	public ArrayList2<ChordTemplate> fretHandMuteTemplates = new ArrayList2<>();

	public ArrangementChart(final ArrangementType arrangementType, final ArrayList2<Beat> beats) {
		this.arrangementType = arrangementType;
		phrases.put("COUNT", new Phrase(0, false));
		phrases.put("END", new Phrase(0, false));
		phraseIterations.add(new PhraseIteration(beats.get(0), "COUNT"));
		phraseIterations.add(new PhraseIteration(beats.getLast(), "END"));

		levels.put(0, new Level());
	}

	public ArrangementChart(final SongArrangement songArrangement, final ArrayList2<Beat> beats) {
		arrangementType = songArrangement.arrangement;
		arrangementProperties = songArrangement.arrangementProperties;
		tuning = new Tuning(arrangementType.strings, songArrangement.tuning);
		capo = songArrangement.capo;
		centOffset = songArrangement.centOffset;

		baseTone = songArrangement.tonebase == null ? "" : songArrangement.tonebase;
		toneChanges = songArrangement.tones == null ? new ArrayList2<>()
				: ToneChange.fromArrangementTones(songArrangement.tones.list);
		tones = new HashSet2<>(toneChanges.map(toneChange -> toneChange.toneName));
		chordTemplates = songArrangement.chordTemplates.list.map(ChordTemplate::new);

		sections = Section.fromArrangementSections(beats, songArrangement.sections.list);
		phrases = Phrase.fromArrangementPhrases(songArrangement.phrases.list);
		phraseIterations = PhraseIteration.fromArrangementPhraseIterations(beats, songArrangement.phrases.list,
				songArrangement.phraseIterations.list);
		events = Event.fromArrangement(beats, songArrangement.events.list);

		if (songArrangement.fretHandMuteTemplates != null) {
			fretHandMuteTemplates = songArrangement.fretHandMuteTemplates.list.map(ChordTemplate::new);
		}

		levels = Level.fromArrangementLevels(this, songArrangement.levels.list);
	}

	public ArrangementSubtype getSubType() {
		if (arrangementProperties.represent == 1) {
			return ArrangementSubtype.MAIN;
		} else if (arrangementProperties.bonusArr == 1) {
			return ArrangementSubtype.BONUS;
		}

		return ArrangementSubtype.ALTERNATE;
	}

	public void setSubType(final ArrangementSubtype subType) {
		arrangementProperties.represent = 0;
		arrangementProperties.bonusArr = 0;

		if (subType == ArrangementSubtype.MAIN) {
			arrangementProperties.represent = 1;
		} else if (subType == ArrangementSubtype.BONUS) {
			arrangementProperties.bonusArr = 1;
		}
	}

	public String getTypeName() {
		return arrangementType.name() + "_" + getSubType().name;
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
