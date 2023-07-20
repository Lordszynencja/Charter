package log.charter.song;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.song.notes.IPosition.findClosestPosition;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import log.charter.data.config.Config;
import log.charter.io.gp.gp5.GPBar;
import log.charter.io.gp.gp5.GPBeat;
import log.charter.io.gp.gp5.GPBend;
import log.charter.io.gp.gp5.GPNote;
import log.charter.io.gp.gp5.GPNoteEffects;
import log.charter.io.gp.gp5.GPTrackData;
import log.charter.io.rs.xml.song.ArrangementProperties;
import log.charter.io.rs.xml.song.ArrangementType;
import log.charter.io.rs.xml.song.SongArrangement;
import log.charter.song.configs.Tuning;
import log.charter.song.enums.HOPO;
import log.charter.song.enums.Mute;
import log.charter.song.notes.Chord;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.Note;
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

	public ArrayList2<EventPoint> eventPoints = new ArrayList2<>();
	public HashMap2<String, Phrase> phrases = new HashMap2<>();
	public HashSet2<String> tones = new HashSet2<>();
	public ArrayList2<ToneChange> toneChanges = new ArrayList2<>();
	public ArrayList2<ChordTemplate> chordTemplates = new ArrayList2<>();
	public ArrayList2<ChordTemplate> fretHandMuteTemplates = new ArrayList2<>();

	private void addCountEndPhrases(final ArrayList2<Beat> beats) {
		phrases.put("COUNT", new Phrase(0, false));
		phrases.put("END", new Phrase(0, false));

		final EventPoint count = new EventPoint(0);
		count.phrase = "COUNT";
		eventPoints.add(0, count);

		final EventPoint end = new EventPoint(beats.getLast().position());
		end.phrase = "END";
		eventPoints.add(end);
	}

	public ArrangementChart(final ArrangementType arrangementType, final ArrayList2<Beat> beats) {
		this.arrangementType = arrangementType;
		addCountEndPhrases(beats);
		levels.put(0, new Level());
	}

	public EventPoint findOrCreateArrangementEventsPoint(final int position) {
		EventPoint arrangementEventsPoint = findClosestPosition(eventPoints, position);
		if (arrangementEventsPoint == null || arrangementEventsPoint.position() != position) {
			arrangementEventsPoint = new EventPoint(position);
			eventPoints.add(arrangementEventsPoint);
			eventPoints.sort(null);
		}

		return arrangementEventsPoint;
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

		songArrangement.sections.list.forEach(arrangementSection -> {
			final EventPoint arrangementEventsPoint = findOrCreateArrangementEventsPoint(
					arrangementSection.startTime);
			arrangementEventsPoint.section = SectionType.findByRSName(arrangementSection.name);
		});
		phrases = Phrase.fromArrangementPhrases(songArrangement.phrases.list);
		songArrangement.phraseIterations.list.forEach(arrangementPhraseIteration -> {
			final EventPoint arrangementEventsPoint = findOrCreateArrangementEventsPoint(
					arrangementPhraseIteration.time);
			final String phraseName = songArrangement.phrases.list.get(arrangementPhraseIteration.phraseId).name;
			arrangementEventsPoint.phrase = phraseName;
		});
		songArrangement.events.list.forEach(arrangementEvent -> {
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

			final EventPoint arrangementEventsPoint = findOrCreateArrangementEventsPoint(
					arrangementEvent.time);
			arrangementEventsPoint.events.add(EventType.findByRSName(arrangementEvent.code));
		});

		if (songArrangement.fretHandMuteTemplates != null) {
			fretHandMuteTemplates = songArrangement.fretHandMuteTemplates.list.map(ChordTemplate::new);
		}

		levels = Level.fromArrangementLevels(this, songArrangement.levels.list);
	}

	public ArrangementChart(final ArrayList2<Beat> beats, final List<GPBar> gpBars, final GPTrackData trackData) {
		capo = trackData.capo;
		arrangementType = getGPArrangementType(trackData);
		addBars(beats, gpBars);
		addCountEndPhrases(beats);
	}

	private ArrangementType getGPArrangementType(final GPTrackData trackData) {
		if (trackData.trackName.toLowerCase().contains("lead")) {
			return ArrangementType.Lead;
		}
		if (trackData.trackName.toLowerCase().contains("rhythm")) {
			return ArrangementType.Rhythm;
		}
		if (trackData.trackName.toLowerCase().contains("bass") || trackData.tuning.length < 6) {
			return ArrangementType.Bass;
		}

		return ArrangementType.Lead;
	}

	private void addBars(final ArrayList2<Beat> beats, final List<GPBar> gpBars) {
		final Level level = new Level();
		levels = new HashMap2<>();
		levels.put(0, level);

		int currentBarBeatId = 0;
		for (final GPBar gpBar : gpBars) {
			if (currentBarBeatId >= beats.size()) {
				return;
			}

			if (gpBar.voices.isEmpty()) {
				continue;
			}

			final List<GPBeat> voice = gpBar.voices.get(0);
			int noteOffset = 0;
			for (final GPBeat gpBeat : voice) {
				final int position = noteOffsetToPosition(beats, noteOffset, currentBarBeatId);
				noteOffset += switch (gpBeat.duration) {
				case NOTE_1 -> 64;
				case NOTE_2 -> 32;
				case NOTE_4 -> 16;
				case NOTE_8 -> 8;
				case NOTE_16 -> 4;
				case NOTE_32 -> 2;
				case NOTE_64 -> 1;
				default -> 1;
				};
				final int nextPosition = noteOffsetToPosition(beats, noteOffset, currentBarBeatId);
				if (gpBeat.notes.size() == 1) {
					addNote(level, gpBeat, gpBeat.notes.get(0), position, nextPosition);
				} else if (gpBeat.notes.size() > 1) {
					addChord(level, gpBeat, gpBeat.notes, position);
				}
			}

			do {
				currentBarBeatId++;
			} while (currentBarBeatId < beats.size() && !beats.get(currentBarBeatId).firstInMeasure);
		}
	}

	private int noteOffsetToPosition(final ArrayList2<Beat> beats, final int noteOffsetIn64s, final int barBeatId) {
		final Beat startingBeat = beats.get(barBeatId);
		final int beatOffset64 = noteOffsetIn64s * startingBeat.noteDenominator;
		final int beatOffset = beatOffset64 / 64;

		final Beat beatFrom = beats.get(barBeatId + beatOffset);
		if (barBeatId + beatOffset + 1 == beats.size()) {
			return beatFrom.position();
		}

		final Beat beatTo = beats.get(barBeatId + beatOffset + 1);
		final int beatLength = beatTo.position() - beatFrom.position();
		final int positionInBeat = beatLength * (beatOffset64 % 64) / 64;

		return beatFrom.position() + positionInBeat;
	}

	private void addNote(final Level level, final GPBeat gpBeat, final GPNote gpNote, final int position,
			final int nextPosition) {
		if (gpNote.fret < 0 || gpNote.fret > Config.frets) {
			return;
		}
		if (gpNote.string < 0 || gpNote.string > arrangementType.strings) {
			return;
		}

		final ChordOrNote previousNote = level.chordsAndNotes.getLast();
		if (previousNote != null) {
			if (gpNote.tied) {
				if (previousNote.isChord()) {
					previousNote.chord.chordNotes.values().forEach(n -> n.linkNext = true);
				} else {
					previousNote.note.linkNext = true;
				}
			}
			if (previousNote.isNote() && previousNote.note.linkNext && previousNote.note.fret != gpNote.fret) {
				previousNote.note.slideTo = gpNote.fret;
			}
		}

		final int noteLength = nextPosition - position;
		final GPNoteEffects effects = gpNote.effects;

		final Note note = new Note(position, gpNote.string - 1, gpNote.fret);
		note.vibrato = gpBeat.beatEffects.vibrato || effects.vibrato;
		note.tremolo = effects.tremoloPickingSpeed != null;
		if (note.vibrato || note.tremolo) {
			note.length(noteLength);
		}
		if (gpBeat.beatEffects.bassPickingTechnique != null) {
			note.bassPicking = gpBeat.beatEffects.bassPickingTechnique;
		}
		if (gpBeat.beatEffects.hopo != null) {
			note.hopo = gpBeat.beatEffects.hopo;
		}
		if (gpBeat.beatEffects.harmonic != null) {
			note.harmonic = gpBeat.beatEffects.harmonic;
		} else if (effects.harmonic != null) {
			note.harmonic = effects.harmonic;
		}

		note.accent = gpNote.accent;
		if (gpNote.dead) {
			note.mute = Mute.FULL;
		}
		note.ignore = gpNote.ghost;

		if (effects.palmMute) {
			note.mute = Mute.PALM;
		}

		Note lastNote = note;
		final List<Note> afterNotes = new ArrayList<>();
		if (!effects.bends.isEmpty()) {
			note.length(noteLength);

			int lastBendValue = 0;
			for (final GPBend bendPoint : effects.bends) {
				if (bendPoint.offset == 0 && bendPoint.value == 0) {
					continue;
				}
				if (bendPoint.offset == 60 && bendPoint.value == lastBendValue) {
					break;
				}

				final int bendPositionOffset = noteLength * bendPoint.offset / 60;

				if (bendPoint.vibrato && !lastNote.vibrato) {
					if (bendPoint.offset == 0) {
						lastNote.vibrato = bendPoint.vibrato;
					} else {
						final Note split = new Note(note.position() + bendPositionOffset, note.string, note.fret);
						split.vibrato = bendPoint.vibrato;
						split.endPosition(nextPosition);

						lastNote.linkNext = true;
						lastNote.endPosition(split.position() - 1);

						lastNote = split;
						afterNotes.add(split);
					}
				}

				final int bendPosition = bendPositionOffset - lastNote.position() + note.position();
				final BigDecimal bendValue = new BigDecimal("0.01").multiply(new BigDecimal(bendPoint.value));
				lastNote.bendValues.add(new BendValue(bendPosition, bendValue));

				lastBendValue = bendPoint.value;
			}
		}

		if (effects.trill != null) {
			final int notes = gpBeat.duration.length / effects.trill.speed.length;
			for (int i = 1; i < notes; i++) {
				final int trillNotePosition = position + noteLength * i / notes;
				final int fret = gpNote.fret + (i % 2) * effects.trill.value;
				final Note trillNote = new Note(trillNotePosition, note.string, fret);
				trillNote.hopo = i % 2 == 0 ? HOPO.PULL_OFF : HOPO.HAMMER_ON;
				trillNote.ignore = true;
				afterNotes.add(trillNote);
			}
		}

		if (effects.slideOut != null) {
			switch (effects.slideOut) {
			case OUT_DOWN:
				lastNote.slideTo = max(1, lastNote.fret - 5);
				lastNote.unpitchedSlide = true;
				break;
			case OUT_UP:
				lastNote.slideTo = min(Config.frets, lastNote.fret + 5);
				lastNote.unpitchedSlide = true;
				break;
			case OUT_WITHOUT_PLUCK:
				lastNote.linkNext = true;
				break;
			case OUT_WITH_PLUCK:
				break;
			default:
				break;
			}
		}

		if (effects.slideIn != null) {
			switch (effects.slideIn) {
			case IN_FROM_ABOVE:
				final Note slideInNoteFromAbove = new Note(note.position() + 50, note.string, note.fret);
				slideInNoteFromAbove.endPosition(max(slideInNoteFromAbove.position() + 25, note.endPosition()));
				afterNotes.add(slideInNoteFromAbove);
				note.linkNext = true;
				note.slideTo = note.fret;
				note.fret = min(Config.frets, note.fret + 5);
				note.endPosition(slideInNoteFromAbove.position() - 1);
				break;
			case IN_FROM_BELOW:
				final Note slideInNoteFromBelow = new Note(note.position() + 50, note.string, note.fret);
				slideInNoteFromBelow.endPosition(max(slideInNoteFromBelow.position() + 25, note.endPosition()));
				afterNotes.add(slideInNoteFromBelow);
				note.linkNext = true;
				note.slideTo = note.fret;
				note.fret = max(1, note.fret - 5);
				note.endPosition(slideInNoteFromBelow.position() - 1);
				break;
			default:
				break;
			}
		}

//		public final GPGraceNote graceNote;

//TODO statuses etc.
		level.chordsAndNotes.add(new ChordOrNote(note));
		afterNotes.forEach(afterNote -> level.chordsAndNotes.add(new ChordOrNote(afterNote)));
	}

	private void addChord(final Level level, final GPBeat gpBeat, final List<GPNote> notes, final int position) {
		final ChordTemplate chordTemplate = new ChordTemplate();
		chordTemplate.chordName = gpBeat.chord == null ? "" : gpBeat.chord.chordName;
		if (chordTemplate.chordName == null) {
			chordTemplate.chordName = "";
		}

		final Chord chord = new Chord(position, -1, chordTemplate);
		if (gpBeat.beatEffects.hopo != null) {
			chord.chordNotes.values().forEach(n -> n.hopo = gpBeat.beatEffects.hopo);
		}
		chord.chordNotes.values().forEach(n -> n.vibrato = gpBeat.beatEffects.vibrato);
		if (gpBeat.beatEffects.harmonic != null) {
			chord.chordNotes.values().forEach(n -> n.harmonic = gpBeat.beatEffects.harmonic);
		}

		for (final GPNote gpNote : notes) {
			chordTemplate.fingers.put(gpNote.string - 1, gpNote.finger == -1 ? null : gpNote.finger);
			chordTemplate.frets.put(gpNote.string - 1, gpNote.fret);

			if (gpNote.effects.tremoloPickingSpeed != null) {
				chord.chordNotes.values().forEach(n -> n.tremolo = true);
			}
		}
		// TODO add statuses

		final int templateId = getChordTemplateIdWithSave(chordTemplate);
		chord.updateTemplate(templateId, chordTemplate);
		level.chordsAndNotes.add(new ChordOrNote(chord));
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

	public ArrayList2<EventPoint> getFilteredEventPoints(final Predicate<EventPoint> filter) {
		return eventPoints.stream().filter(filter).collect(Collectors.toCollection(ArrayList2::new));
	}
}
