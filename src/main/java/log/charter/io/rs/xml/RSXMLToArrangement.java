package log.charter.io.rs.xml;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.util.CollectionUtils.map;
import static log.charter.util.CollectionUtils.toMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import log.charter.data.song.Arrangement;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.EventPoint;
import log.charter.data.song.EventType;
import log.charter.data.song.Phrase;
import log.charter.data.song.SectionType;
import log.charter.data.song.ToneChange;
import log.charter.data.song.configs.Tuning;
import log.charter.data.song.configs.Tuning.TuningType;
import log.charter.data.song.position.FractionalPosition;
import log.charter.io.rs.xml.song.ArrangementPhrase;
import log.charter.io.rs.xml.song.ArrangementTone;
import log.charter.io.rs.xml.song.ArrangementTuning;
import log.charter.io.rs.xml.song.ArrangementType;
import log.charter.io.rs.xml.song.SongArrangement;

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

	public static Map<String, Phrase> getArrangementPhrases(final List<ArrangementPhrase> arrangementPhrases) {
		return toMap(arrangementPhrases, (map, arrangementPhrase) -> {
			final String name = arrangementPhrase.name;
			final int maxDifficulty = arrangementPhrase.maxDifficulty;
			final boolean solo = arrangementPhrase.solo != null && arrangementPhrase.solo == 1;
			map.put(name, new Phrase(maxDifficulty, solo));
		});
	}

	private static List<ToneChange> getToneChanges(final ImmutableBeatsMap beats,
			final List<ArrangementTone> arrangementTones) {
		return map(arrangementTones, t -> new ToneChange(FractionalPosition.fromTimeRounded(beats, t.time), t.name));
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

	private static void addChordTemplates(final SongArrangement arrangementData, final Arrangement arrangement,
			final ImmutableBeatsMap beats) {
		arrangement.chordTemplates = arrangementData.chordTemplates.list.stream()//
				.map(ChordTemplate::new)//
				.peek(chordTemplate -> {
					for (final Integer string : chordTemplate.frets.keySet()) {
						chordTemplate.frets.put(string, max(arrangement.capo, chordTemplate.frets.get(string)));
					}
				}).collect(Collectors.toCollection(ArrayList::new));
	}

	private static void addSections(final SongArrangement arrangementData, final Arrangement arrangement,
			final ImmutableBeatsMap beats) {
		arrangementData.sections.list.forEach(arrangementSection -> {
			final EventPoint arrangementEventsPoint = arrangement.findOrCreateArrangementEventsPoint(
					FractionalPosition.fromTimeRounded(beats, arrangementSection.startTime));
			arrangementEventsPoint.section = findSectionByRSName(arrangementSection.name);
		});
	}

	private static void addPhrases(final SongArrangement arrangementData, final Arrangement arrangement,
			final ImmutableBeatsMap beats) {
		arrangementData.phraseIterations.list.forEach(arrangementPhraseIteration -> {
			final EventPoint arrangementEventsPoint = arrangement.findOrCreateArrangementEventsPoint(
					FractionalPosition.fromTimeRounded(beats, arrangementPhraseIteration.time));
			final String phraseName = arrangementData.phrases.list.get(arrangementPhraseIteration.phraseId).name;
			arrangementEventsPoint.phrase = phraseName;
		});
	}

	private static void addEvents(final SongArrangement arrangementData, final Arrangement arrangement,
			final ImmutableBeatsMap beats) {
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

			final EventPoint arrangementEventsPoint = arrangement.findOrCreateArrangementEventsPoint(
					FractionalPosition.fromTimeRounded(beats, arrangementEvent.time));
			arrangementEventsPoint.events.add(findEventByRSName(arrangementEvent.code));
		});
	}

	public static Arrangement toArrangement(final SongArrangement arrangementData, final ImmutableBeatsMap beats) {
		final Arrangement arrangement = new Arrangement();

		arrangement.arrangementType = arrangementData.arrangementProperties.getType();
		arrangement.arrangementSubtype = arrangementData.arrangementProperties.getSubtype();
		arrangement.tuning = getTuning(arrangementData.tuning, arrangement.arrangementType);
		arrangement.capo = arrangementData.capo;
		arrangement.centOffset = arrangementData.centOffset;
		arrangement.startingTone = arrangementData.tonebase == null ? "" : arrangementData.tonebase;
		arrangement.pickedBass = arrangementData.arrangementProperties.bassPick == 1;

		arrangement.toneChanges = arrangementData.tones == null ? new ArrayList<>()
				: getToneChanges(beats, arrangementData.tones.list);
		arrangement.tones = map(arrangement.toneChanges, toneChange -> toneChange.toneName, new HashSet<>());
		addChordTemplates(arrangementData, arrangement, beats);
		addSections(arrangementData, arrangement, beats);
		arrangement.phrases = getArrangementPhrases(arrangementData.phrases.list);
		addPhrases(arrangementData, arrangement, beats);
		addEvents(arrangementData, arrangement, beats);

		arrangement.levels = RSXMLLevelTransformer.fromArrangementDataLevels(arrangement, arrangementData.levels.list,
				beats);

		return arrangement;
	}
}
