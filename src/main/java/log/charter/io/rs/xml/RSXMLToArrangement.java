package log.charter.io.rs.xml;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.stream.Collectors.toCollection;

import java.util.Arrays;
import java.util.List;

import log.charter.data.song.Arrangement;
import log.charter.data.song.Beat;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.EventPoint;
import log.charter.data.song.EventType;
import log.charter.data.song.Phrase;
import log.charter.data.song.SectionType;
import log.charter.data.song.ToneChange;
import log.charter.data.song.configs.Tuning;
import log.charter.data.song.configs.Tuning.TuningType;
import log.charter.io.rs.xml.song.ArrangementPhrase;
import log.charter.io.rs.xml.song.ArrangementTone;
import log.charter.io.rs.xml.song.ArrangementTuning;
import log.charter.io.rs.xml.song.ArrangementType;
import log.charter.io.rs.xml.song.SongArrangement;
import log.charter.util.CollectionUtils;
import log.charter.util.collections.ArrayList2;
import log.charter.util.collections.HashMap2;
import log.charter.util.collections.HashSet2;

public class RSXMLToArrangement {
	private static Tuning getTuning(final ArrangementTuning arrangementTuning, final ArrangementType arrangementType) {
		final int strings = arrangementTuning.strings == 0 ? arrangementType.defaultStrings : arrangementTuning.strings;
		int[] tuning = new int[] { //
				arrangementTuning.string0, //
				arrangementTuning.string1, //
				arrangementTuning.string2, //
				arrangementTuning.string3, //
				arrangementTuning.string4, //
				arrangementTuning.string5, //
				arrangementTuning.string6, //
				arrangementTuning.string7, //
				arrangementTuning.string8 };
		tuning = Arrays.copyOf(tuning, strings);
		final TuningType tuningType = TuningType.fromTuning(tuning);

		return new Tuning(tuningType, strings, tuning);
	}

	public static HashMap2<String, Phrase> getArrangementPhrases(final List<ArrangementPhrase> arrangementPhrases) {
		return new HashMap2<>(CollectionUtils.toMap(arrangementPhrases, (map, arrangementPhrase) -> {
			final String name = arrangementPhrase.name;
			final int maxDifficulty = arrangementPhrase.maxDifficulty;
			final boolean solo = arrangementPhrase.solo != null && arrangementPhrase.solo == 1;
			map.put(name, new Phrase(maxDifficulty, solo));
		}));
	}

	private static ArrayList2<ToneChange> getToneChanges(final List<ArrangementTone> arrangementTones) {
		return arrangementTones.stream()//
				.map(arrangementTone -> new ToneChange(arrangementTone.time, arrangementTone.name))//
				.collect(toCollection(ArrayList2::new));
	}

	private static SectionType findSectionByRSName(final String rsName) {
		for (final SectionType sectionType : SectionType.values()) {
			if (sectionType.rsName.equals(rsName)) {
				return sectionType;
			}
		}

		return null;
	}

	private static EventType findEventByRSName(final String rsName) {
		for (final EventType eventType : EventType.values()) {
			if (eventType.rsName.equals(rsName)) {
				return eventType;
			}
		}

		return EventType.HIGH_PITCH_TICK;
	}

	public static Arrangement toArrangement(final SongArrangement arrangementData, final ArrayList2<Beat> beats) {
		final Arrangement arrangement = new Arrangement();

		arrangement.arrangementType = arrangementData.arrangementProperties.getType();
		arrangement.arrangementSubtype = arrangementData.arrangementProperties.getSubtype();
		arrangement.tuning = getTuning(arrangementData.tuning, arrangement.arrangementType);
		arrangement.capo = arrangementData.capo;
		arrangement.centOffset = arrangementData.centOffset;

		arrangement.baseTone = arrangementData.tonebase == null ? "" : arrangementData.tonebase;
		arrangement.toneChanges = arrangementData.tones == null ? new ArrayList2<>()
				: getToneChanges(arrangementData.tones.list);
		arrangement.tones = new HashSet2<>(arrangement.toneChanges.map(toneChange -> toneChange.toneName));
		arrangement.chordTemplates = arrangementData.chordTemplates.list.map(ChordTemplate::new);

		arrangementData.sections.list.forEach(arrangementSection -> {
			final EventPoint arrangementEventsPoint = arrangement
					.findOrCreateArrangementEventsPoint(arrangementSection.startTime);
			arrangementEventsPoint.section = findSectionByRSName(arrangementSection.name);
		});
		arrangement.phrases = getArrangementPhrases(arrangementData.phrases.list);
		arrangementData.phraseIterations.list.forEach(arrangementPhraseIteration -> {
			final EventPoint arrangementEventsPoint = arrangement
					.findOrCreateArrangementEventsPoint(arrangementPhraseIteration.time);
			final String phraseName = arrangementData.phrases.list.get(arrangementPhraseIteration.phraseId).name;
			arrangementEventsPoint.phrase = phraseName;
		});
		arrangementData.events.list.forEach(arrangementEvent -> {
			if (arrangementEvent.code.startsWith("TS:")) {
				final int time = arrangementEvent.time;
				final String[] timeSignatureParts = arrangementEvent.code.split(":")[1].split("/");
				final int beatsInMeasure = max(1, min(1024, Integer.valueOf(timeSignatureParts[0])));
				final int noteDenominator = max(1, min(1024, Integer.valueOf(timeSignatureParts[1])));
				beats.stream()//
						.filter(beat -> beat.position() >= time)//
						.forEach(beat -> beat.setTimeSignature(beatsInMeasure, noteDenominator));
				return;
			}

			final EventPoint arrangementEventsPoint = arrangement
					.findOrCreateArrangementEventsPoint(arrangementEvent.time);
			arrangementEventsPoint.events.add(findEventByRSName(arrangementEvent.code));
		});

		arrangement.levels = RSXMLLevelTransformer.fromArrangementDataLevels(arrangement, arrangementData.levels.list);

		return arrangement;
	}
}
