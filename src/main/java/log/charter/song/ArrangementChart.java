package log.charter.song;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.data.config.Config.minTailLength;
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
import log.charter.io.gp.gp5.GPDuration;
import log.charter.io.gp.gp5.GPGraceNote;
import log.charter.io.gp.gp5.GPNote;
import log.charter.io.gp.gp5.GPNoteEffects;
import log.charter.io.gp.gp5.GPTrackData;
import log.charter.io.rs.xml.song.ArrangementProperties;
import log.charter.io.rs.xml.song.ArrangementType;
import log.charter.io.rs.xml.song.SongArrangement;
import log.charter.song.CombinedGPBars.GPBeatUnwrapper;
import log.charter.song.configs.Tuning;
import log.charter.song.enums.HOPO;
import log.charter.song.enums.Harmonic;
import log.charter.song.enums.Mute;
import log.charter.song.notes.Chord;
import log.charter.song.notes.ChordNote;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.CommonNote;
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

	private static class NotePositionInformation {
		public final List<Beat> beats;
		public final int positionIn64s;
		public final int barBeatId;

		public NotePositionInformation(final List<Beat> beats, final int positionIn64s, final int barBeatId) {
			this.beats = beats;
			this.positionIn64s = positionIn64s;
			this.barBeatId = barBeatId;
		}

		private NotePositionInformation move(final int offsetIn64s) {
			return new NotePositionInformation(beats, positionIn64s + offsetIn64s, barBeatId);
		}

		public NotePositionInformation move(final GPDuration duration) {
			return move(duration.length);
		}

		public NotePositionInformation moveBackwards(final GPDuration duration) {
			return move(-duration.length);
		}

		public int getPosition() {
			final Beat startingBeat = beats.get(barBeatId);
			int beatOffset64 = positionIn64s * startingBeat.noteDenominator;
			int beatOffset = beatOffset64 / 64;
			if (beatOffset64 < 0) {
				beatOffset64 += 64;
				beatOffset--;
			}
			if (barBeatId + beatOffset < 0) {
				return max(0, beats.get(0).position());
			}

			if (barBeatId + beatOffset >= beats.size()) {
				return beats.get(beats.size() - 1).position();
			}

			final Beat beatFrom = beats.get(barBeatId + beatOffset);
			if (barBeatId + beatOffset + 1 == beats.size()) {
				return beatFrom.position();
			}

			final Beat beatTo = beatOffset64 >= 0 ? beats.get(barBeatId + beatOffset + 1)
					: beats.get(barBeatId + beatOffset - 1);

			final int beatLength = beatTo.position() - beatFrom.position();
			final int positionInBeat = beatLength * (beatOffset64 % 64) / 64;

			return beatFrom.position() + positionInBeat;
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

	private void addCountEndPhrases2(final ArrayList2<GPBarUnwrapper> unwrap) {
		phrases.put("COUNT", new Phrase(0, false));
		phrases.put("END", new Phrase(0, false));

		final EventPoint count = new EventPoint(0);
		count.phrase = "COUNT";
		eventPoints.add(0, count);

		final EventPoint end = new EventPoint(unwrap.get(0).get_last_bar_start_position());
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
			final EventPoint arrangementEventsPoint = findOrCreateArrangementEventsPoint(arrangementSection.startTime);
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

			final EventPoint arrangementEventsPoint = findOrCreateArrangementEventsPoint(arrangementEvent.time);
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
	
	public ArrangementChart(final ArrayList2<GPBarUnwrapper> unwrap, final GPTrackData trackData) {
		capo = trackData.capo;
		arrangementType = getGPArrangementType(trackData);
		addBars(unwrap);
		addCountEndPhrases2(unwrap);
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
		final boolean[] wasHOPOStart = new boolean[9];
		final int[] hopoFrom = new int[9];
		HandShape lastHandShape = null;
		for (final GPBar gpBar : gpBars) {
			if (currentBarBeatId >= beats.size()) {
				return;
			}

			if (gpBar.voices.isEmpty()) {
				continue;
			}

			final List<GPBeat> voice = gpBar.voices.get(0);
			NotePositionInformation notePosition = new NotePositionInformation(beats, 0, currentBarBeatId);
			for (final GPBeat gpBeat : voice) {
				int unmanipulatedLength = gpBeat.duration.length;
				gpBeat.duration.length = (gpBeat.duration.length * gpBeat.tupletDenominator) / gpBeat.tupletNumerator;
		
				if (gpBeat.notes.isEmpty()) {
					lastHandShape = null;
					notePosition = notePosition.move(gpBeat.duration);
					gpBeat.duration.length = unmanipulatedLength;
					continue;
				}

				if (gpBeat.notes.size() == 1) {
					addNote(level, gpBeat, gpBeat.notes.get(0), notePosition, wasHOPOStart, hopoFrom);
					lastHandShape = null;
				} else if (gpBeat.notes.size() > 1) {
					addChord(level, gpBeat, gpBeat.notes, notePosition, wasHOPOStart, hopoFrom, lastHandShape);
					lastHandShape = level.handShapes.getLast();
				}

				notePosition = notePosition.move(gpBeat.duration);
				gpBeat.duration.length = unmanipulatedLength;

				for (final GPNote note : gpBeat.notes) {
					final int string = note.string;
					wasHOPOStart[string] = note.effects.isHammerPullOrigin;
					hopoFrom[string] = note.fret;
				}
			}

			do {
				currentBarBeatId++;
			} while (currentBarBeatId < beats.size() && !beats.get(currentBarBeatId).firstInMeasure);
		}
	}
	private void addBars(final ArrayList2<GPBarUnwrapper> unwrap) {
		final Level level = new Level();
		levels = new HashMap2<>();
		levels.put(0, level);

		final boolean[] wasHOPOStart = new boolean[9];
		final int[] hopoFrom = new int[9];
		HandShape lastHandShape = null;

		for (GPBarUnwrapper voice : unwrap) {
			double note_start_position = 0;
			for (CombinedGPBars bar : voice.unwrapped_bars) {
				// Initial note position is the first is a bar
				if (bar.bar.timeSignatureNumerator != bar.bar_beats.size()) {
					lastHandShape = null; // Throw error
				}
				for (GPBeatUnwrapper note_beat : bar.note_beats) {
					// Ex unmanip 32 -> 21 if triplet (trunkated 21,33)
					if (note_beat.notes.isEmpty()) {
						lastHandShape = null;
						note_start_position += note_beat.note_time_ms; // Rest notes take time too
						continue;
					}

					if (note_beat.notes.size() == 1) {
						addNote(level, note_beat, (int)note_start_position, wasHOPOStart, hopoFrom);
						lastHandShape = null;
					}
					else if (note_beat.notes.size() > 1) {
						addChord(level, note_beat, (int)note_start_position, wasHOPOStart, hopoFrom, lastHandShape);
						lastHandShape = level.handShapes.getLast();
					}

					note_start_position += note_beat.note_time_ms;

					for (final GPNote note : note_beat.notes) {
						final int string = note.string;
						wasHOPOStart[string] = note.effects.isHammerPullOrigin;
						hopoFrom[string] = note.fret;
					}
				}
			}
		}
	}

	private void setStatuses(final CommonNote note, final GPBeat gpBeat, final GPNote gpNote,
			final boolean[] wasHOPOStart, final int[] hopoFrom) {
		final GPNoteEffects effects = gpNote.effects;

		note.vibrato(gpBeat.beatEffects.vibrato || effects.vibrato);
		note.tremolo(effects.tremoloPickingSpeed != null);
		if (gpBeat.beatEffects.bassPickingTechnique != null) {
			note.bassPicking(gpBeat.beatEffects.bassPickingTechnique);
		}

		if (gpBeat.beatEffects.hopo != null && gpBeat.beatEffects.hopo != HOPO.NONE) {
			note.hopo(gpBeat.beatEffects.hopo);
		} else if (wasHOPOStart[gpNote.string]) {
			note.hopo(hopoFrom[gpNote.string] > gpNote.fret ? HOPO.PULL_OFF : HOPO.HAMMER_ON);
		}
		if (gpBeat.beatEffects.harmonic != null && gpBeat.beatEffects.harmonic != Harmonic.NONE) {
			note.harmonic(gpBeat.beatEffects.harmonic);
		} else if (effects.harmonic != null) {
			note.harmonic(effects.harmonic);
		}
		if (gpNote.dead) {
			note.mute(Mute.FULL);
		} else if (effects.palmMute) {
			note.mute(Mute.PALM);
		}
	}

	private void addNote(final Level level, final GPBeatUnwrapper gpBeat, final int note_start_position,
		final boolean[] wasHOPOStart, final int[] hopoFrom) {
		
		if (gpBeat.notes.size() != 1) {
			return;
		}
		final GPNote gpNote = gpBeat.notes.get(0);
		if (gpNote.fret < 0 || gpNote.fret > Config.frets) {
			return;
		}
		if (gpNote.string < 0 || gpNote.string > arrangementType.strings) {
			return;
		}

		final ChordOrNote previousNote = level.chordsAndNotes.getLast();
		if (previousNote != null) {
			if (gpNote.tied) {
				previousNote.notes().forEach(n -> n.linkNext(true));
			}
			if (previousNote.isNote() && previousNote.note.linkNext && previousNote.note.fret != gpNote.fret) {
				previousNote.note.slideTo = gpNote.fret;
			}
		}

		final Note note = new Note(note_start_position, gpNote.string - 1, gpNote.fret);
		final int note_length = (int)gpBeat.note_time_ms;
		final GPNoteEffects effects = gpNote.effects;

		setStatuses(CommonNote.create(note), gpBeat, gpNote, wasHOPOStart, hopoFrom);
		if (note.vibrato || note.tremolo) {
			note.length(note_length);
		}

		note.accent = gpNote.accent;
		note.ignore = gpNote.ghost;

		Note lastNote = note;
		final List<Note> afterNotes = new ArrayList<>();
		if (!effects.bends.isEmpty()) {
			note.length(note_length);

			int lastBendValue = 0;
			for (final GPBend bendPoint : effects.bends) {
				if (bendPoint.offset == 0 && bendPoint.value == 0) {
					continue;
				}
				if (bendPoint.offset == 60 && bendPoint.value == lastBendValue) {
					break;
				}

				final int bendPositionOffset = note_length * bendPoint.offset / 60;

				if (bendPoint.vibrato && !lastNote.vibrato) {
					if (bendPoint.offset == 0) {
						lastNote.vibrato = bendPoint.vibrato;
					} else {
						final Note split = new Note(note_start_position + bendPositionOffset, note.string, note.fret);
						split.vibrato = bendPoint.vibrato;
						split.endPosition(note_start_position + note_length);

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
			int trillNotePosition = (int)((double)note_start_position + gpBeat.note_time_ms/(double)notes);
			for (int i = 1; i < notes; i++) {
				final int fret = gpNote.fret + (i % 2) * effects.trill.value;
				final Note trillNote = new Note(trillNotePosition, note.string, fret);
				trillNote.hopo = i % 2 == 0 ? HOPO.PULL_OFF : HOPO.HAMMER_ON;
				trillNote.ignore = true;
				afterNotes.add(trillNote);

				trillNotePosition += (int)(gpBeat.note_time_ms/(double)notes);
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

		Note graceNote = null;
		if (effects.graceNote != null) {
			final GPGraceNote graceNoteData = effects.graceNote;
			if (graceNoteData.beforeBeat) {
				final int graceNotePosition = note_start_position - (int)gpBeat.gp_duration_to_time(graceNoteData.duration);
				graceNote = new Note(graceNotePosition, note.string, graceNoteData.fret);
			} else {
				graceNote = new Note(note_start_position, note.string, graceNoteData.fret);
				note.position(note_start_position + (int)gpBeat.gp_duration_to_time(graceNoteData.duration));
				note.endPosition(note_start_position + note_length);
			}

			if (graceNoteData.dead) {
				graceNote.mute = Mute.FULL;
			}
			if (graceNoteData.slide) {
				graceNote.slideTo = note.fret;
				graceNote.linkNext = true;
				graceNote.endPosition(note.position() - 1);
				note.length(max(note.length(), minTailLength));
			}
			if (graceNoteData.legato) {
				note.hopo = graceNote.fret < note.fret ? HOPO.HAMMER_ON : HOPO.PULL_OFF;
			}
		}

		if (graceNote != null) {
			level.chordsAndNotes.add(new ChordOrNote(graceNote));
		}
		level.chordsAndNotes.add(new ChordOrNote(note));
		afterNotes.forEach(afterNote -> level.chordsAndNotes.add(new ChordOrNote(afterNote)));
	}

	private void addNote(final Level level, final GPBeat gpBeat, final GPNote gpNote,
			final NotePositionInformation position, final boolean[] wasHOPOStart, final int[] hopoFrom) {
		if (gpNote.fret < 0 || gpNote.fret > Config.frets) {
			return;
		}
		if (gpNote.string < 0 || gpNote.string > arrangementType.strings) {
			return;
		}

		final ChordOrNote previousNote = level.chordsAndNotes.getLast();
		if (previousNote != null) {
			if (gpNote.tied) {
				previousNote.notes().forEach(n -> n.linkNext(true));
			}
			if (previousNote.isNote() && previousNote.note.linkNext && previousNote.note.fret != gpNote.fret) {
				previousNote.note.slideTo = gpNote.fret;
			}
		}

		final Note note = new Note(position.getPosition(), gpNote.string - 1, gpNote.fret);
		final int noteLength = position.move(gpBeat.duration).getPosition() - note.position();
		final GPNoteEffects effects = gpNote.effects;

		setStatuses(CommonNote.create(note), gpBeat, gpNote, wasHOPOStart, hopoFrom);
		if (note.vibrato || note.tremolo) {
			note.length(noteLength);
		}

		note.accent = gpNote.accent;
		note.ignore = gpNote.ghost;

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
						split.endPosition(position.move(gpBeat.duration).getPosition());

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
			NotePositionInformation trillNotePosition = position.move(effects.trill.speed);
			for (int i = 1; i < notes; i++) {
				final int fret = gpNote.fret + (i % 2) * effects.trill.value;
				final Note trillNote = new Note(trillNotePosition.getPosition(), note.string, fret);
				trillNote.hopo = i % 2 == 0 ? HOPO.PULL_OFF : HOPO.HAMMER_ON;
				trillNote.ignore = true;
				afterNotes.add(trillNote);

				trillNotePosition = trillNotePosition.move(effects.trill.speed);
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

		Note graceNote = null;
		if (effects.graceNote != null) {
			final GPGraceNote graceNoteData = effects.graceNote;
			if (graceNoteData.beforeBeat) {
				final int graceNotePosition = position.moveBackwards(graceNoteData.duration).getPosition();
				graceNote = new Note(graceNotePosition, note.string, graceNoteData.fret);
			} else {
				graceNote = new Note(note.position(), note.string, graceNoteData.fret);
				final int noteEndPosition = note.endPosition();
				note.position(position.move(graceNoteData.duration).getPosition());
				note.endPosition(noteEndPosition);
			}

			if (graceNoteData.dead) {
				graceNote.mute = Mute.FULL;
			}
			if (graceNoteData.slide) {
				graceNote.slideTo = note.fret;
				graceNote.linkNext = true;
				graceNote.endPosition(note.position() - 1);
				note.length(max(note.length(), minTailLength));
			}
			if (graceNoteData.legato) {
				note.hopo = graceNote.fret < note.fret ? HOPO.HAMMER_ON : HOPO.PULL_OFF;
			}
		}

		if (graceNote != null) {
			level.chordsAndNotes.add(new ChordOrNote(graceNote));
		}
		level.chordsAndNotes.add(new ChordOrNote(note));
		afterNotes.forEach(afterNote -> level.chordsAndNotes.add(new ChordOrNote(afterNote)));
	}

	private void addChord(final Level level, final GPBeat gpBeat, final List<GPNote> notes,
			final NotePositionInformation position, final boolean[] wasHOPOStart, final int[] hOPOFrom,
			final HandShape lastHandShape) {
		final ChordTemplate chordTemplate = new ChordTemplate();
		chordTemplate.chordName = gpBeat.chord == null ? "" : gpBeat.chord.chordName;
		if (chordTemplate.chordName == null) {
			chordTemplate.chordName = "";
		}

		final Chord chord = new Chord(position.getPosition(), -1, chordTemplate);
		final int length = position.move(gpBeat.duration).getPosition() - chord.position();
		boolean setLength = false;

		for (final GPNote gpNote : notes) {
			final int string = gpNote.string - 1;
			chordTemplate.fingers.put(string, gpNote.finger == -1 ? null : gpNote.finger);
			chordTemplate.frets.put(string, gpNote.fret);

			final ChordNote chordNote = new ChordNote();
			chord.chordNotes.put(string, chordNote);
			setStatuses(CommonNote.create(chord, string, chordNote), gpBeat, gpNote, wasHOPOStart, hOPOFrom);
			if (chordNote.vibrato || chordNote.tremolo) {
				setLength = true;
			}
			chord.ignore |= gpNote.ghost;

			final GPNoteEffects effects = gpNote.effects;
			if (!effects.bends.isEmpty()) {
				setLength = true;

				int lastBendValue = 0;
				for (final GPBend bendPoint : effects.bends) {
					if (bendPoint.offset == 0 && bendPoint.value == 0) {
						continue;
					}
					if (bendPoint.offset == 60 && bendPoint.value == lastBendValue) {
						break;
					}
					chordNote.vibrato |= bendPoint.vibrato;

					final int bendPosition = length * bendPoint.offset / 60;
					final BigDecimal bendValue = new BigDecimal("0.01").multiply(new BigDecimal(bendPoint.value));
					chordNote.bendValues.add(new BendValue(bendPosition, bendValue));

					lastBendValue = bendPoint.value;
				}
			}
		}
		if (setLength) {
			chord.chordNotes.values().forEach(n -> n.length = length);
		}

		final int templateId = getChordTemplateIdWithSave(chordTemplate);
		chord.updateTemplate(templateId, chordTemplate);
		level.chordsAndNotes.add(new ChordOrNote(chord));

		final int handshapeEndPosition = position.move(gpBeat.duration).moveBackwards(GPDuration.NOTE_32).getPosition();
		if (lastHandShape != null && lastHandShape.templateId == chord.templateId()) {
			lastHandShape.endPosition(handshapeEndPosition);
		} else {
			level.handShapes.add(new HandShape(chord, handshapeEndPosition - chord.position()));
		}
	}

	private void addChord(final Level level, final GPBeatUnwrapper gpBeat, final int note_start_position,
		final boolean[] wasHOPOStart, final int[] hOPOFrom,	final HandShape lastHandShape) {
		final ChordTemplate chordTemplate = new ChordTemplate();
		chordTemplate.chordName = gpBeat.chord == null ? "" : gpBeat.chord.chordName;
		if (chordTemplate.chordName == null) {
			chordTemplate.chordName = "";
		}

		final Chord chord = new Chord(note_start_position, -1, chordTemplate);
		final int length = (int)gpBeat.note_time_ms;
		boolean setLength = false;

		for (final GPNote gpNote : gpBeat.notes) {
			final int string = gpNote.string - 1;
			chordTemplate.fingers.put(string, gpNote.finger == -1 ? null : gpNote.finger);
			chordTemplate.frets.put(string, gpNote.fret);

			final ChordNote chordNote = new ChordNote();
			chord.chordNotes.put(string, chordNote);
			setStatuses(CommonNote.create(chord, string, chordNote), gpBeat, gpNote, wasHOPOStart, hOPOFrom);
			if (chordNote.vibrato || chordNote.tremolo) {
				setLength = true;
			}
			chord.ignore |= gpNote.ghost;

			final GPNoteEffects effects = gpNote.effects;
			if (!effects.bends.isEmpty()) {
				setLength = true;

				int lastBendValue = 0;
				for (final GPBend bendPoint : effects.bends) {
					if (bendPoint.offset == 0 && bendPoint.value == 0) {
						continue;
					}
					if (bendPoint.offset == 60 && bendPoint.value == lastBendValue) {
						break;
					}
					chordNote.vibrato |= bendPoint.vibrato;

					final int bendPosition = length * bendPoint.offset / 60;
					final BigDecimal bendValue = new BigDecimal("0.01").multiply(new BigDecimal(bendPoint.value));
					chordNote.bendValues.add(new BendValue(bendPosition, bendValue));

					lastBendValue = bendPoint.value;
				}
			}
		}
		if (setLength) {
			chord.chordNotes.values().forEach(n -> n.length = length);
		}

		final int templateId = getChordTemplateIdWithSave(chordTemplate);
		chord.updateTemplate(templateId, chordTemplate);
		level.chordsAndNotes.add(new ChordOrNote(chord));

		final int handshapeEndPosition = note_start_position + length - (int)gpBeat.gp_duration_to_time(GPDuration.NOTE_32);

		if (lastHandShape != null && lastHandShape.templateId == chord.templateId()) {
			lastHandShape.endPosition(handshapeEndPosition);
		} else {
			level.handShapes.add(new HandShape(chord, handshapeEndPosition - chord.position()));
		}
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

	public String getFileName(final int id) {
		return id + "_" + getTypeName() + "_RS2.xml";
	}
}
