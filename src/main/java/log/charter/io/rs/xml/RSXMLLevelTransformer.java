package log.charter.io.rs.xml;

import static log.charter.util.CollectionUtils.map;
import static log.charter.util.CollectionUtils.toMap;
import static log.charter.util.Utils.mapInteger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import log.charter.data.song.FHP;
import log.charter.data.song.Arrangement;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.BendValue;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.HandShape;
import log.charter.data.song.Level;
import log.charter.data.song.enums.BassPickingTechnique;
import log.charter.data.song.enums.HOPO;
import log.charter.data.song.enums.Harmonic;
import log.charter.data.song.enums.Mute;
import log.charter.data.song.notes.Chord;
import log.charter.data.song.notes.ChordNote;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.notes.Note;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPosition;
import log.charter.io.rs.xml.song.ArrangementAnchor;
import log.charter.io.rs.xml.song.ArrangementBendValue;
import log.charter.io.rs.xml.song.ArrangementChord;
import log.charter.io.rs.xml.song.ArrangementChordNote;
import log.charter.io.rs.xml.song.ArrangementHandShape;
import log.charter.io.rs.xml.song.ArrangementLevel;
import log.charter.io.rs.xml.song.ArrangementNote;

public class RSXMLLevelTransformer {
	public static List<Level> fromArrangementDataLevels(final Arrangement arrangement,
			final List<ArrangementLevel> arrangementLevels, final ImmutableBeatsMap beats) {
		final Map<Integer, Level> levelsMap = toMap(arrangementLevels, (map, arrangementLevel) -> map
				.put(arrangementLevel.difficulty, toLevel(arrangementLevel, arrangement, beats)));

		final List<Level> levels = new ArrayList<>();
		levelsMap.forEach((id, level) -> {
			while (levels.size() <= id) {
				levels.add(new Level());
			}

			levels.set(id, level);
		});

		if (levels.isEmpty()) {
			levels.add(new Level());
		}

		return levels;
	}

	private static FHP fhp(final ImmutableBeatsMap beats, final ArrangementAnchor arrangementAnchor) {
		final FractionalPosition position = FractionalPosition.fromTimeRounded(beats, arrangementAnchor.time);
		final int fret = arrangementAnchor.fret;
		final int width = arrangementAnchor.width == null ? 4 : arrangementAnchor.width.intValue();

		return new FHP(position, fret, width);
	}

	private static BendValue bendValue(final ImmutableBeatsMap beats, final ArrangementBendValue arrangementBendValue) {
		final FractionalPosition position = FractionalPosition.fromTime(beats, arrangementBendValue.time);
		final BigDecimal bendValue = arrangementBendValue.step == null ? BigDecimal.ZERO : arrangementBendValue.step;
		return new BendValue(position, bendValue);
	}

	private static Chord chord(final ImmutableBeatsMap beats, final ArrangementChord arrangementChord,
			final ChordTemplate template) {
		final Chord chord = new Chord(FractionalPosition.fromTimeRounded(beats, arrangementChord.time),
				arrangementChord.chordId);
		chord.accent = mapInteger(arrangementChord.accent);
		chord.ignore = mapInteger(arrangementChord.ignore);
		final Mute mute = Mute.fromArrangmentChord(arrangementChord);

		if (arrangementChord.chordNotes != null) {
			for (final ArrangementChordNote arrangementNote : arrangementChord.chordNotes) {
				final FractionalPosition endPosition = arrangementNote.sustain == null ? chord.position()
						: FractionalPosition.fromTimeRounded(beats, arrangementChord.time + arrangementNote.sustain);

				final ChordNote chordNote = new ChordNote(chord, endPosition);

				if (mapInteger(arrangementNote.mute)) {
					chordNote.mute = Mute.FULL;
				} else if (mapInteger(arrangementNote.palmMute)) {
					chordNote.mute = Mute.PALM;
				} else {
					chordNote.mute = mute;
				}

				if (arrangementNote.slideTo != null) {
					chordNote.slideTo = arrangementNote.slideTo;
				}
				if (arrangementNote.slideUnpitchTo != null) {
					chordNote.slideTo = arrangementNote.slideUnpitchTo;
					chordNote.unpitchedSlide = true;
				}

				chordNote.vibrato = mapInteger(arrangementNote.vibrato);
				chordNote.tremolo = mapInteger(arrangementNote.tremolo);

				if (mapInteger(arrangementNote.hammerOn)) {
					chordNote.hopo = HOPO.HAMMER_ON;
				}
				if (mapInteger(arrangementNote.pullOff)) {
					chordNote.hopo = HOPO.PULL_OFF;
				}
				if (mapInteger(arrangementNote.tap)) {
					chordNote.hopo = HOPO.TAP;
				}
				if (mapInteger(arrangementNote.harmonic)) {
					chordNote.harmonic = Harmonic.NORMAL;
				}
				if (mapInteger(arrangementNote.harmonicPinch)) {
					chordNote.harmonic = Harmonic.PINCH;
				}

				if (arrangementNote.bendValues != null && !arrangementNote.bendValues.list.isEmpty()) {
					for (final ArrangementBendValue bendValue : arrangementNote.bendValues.list) {
						chordNote.bendValues.add(bendValue(beats, bendValue));
					}
				}

				chordNote.linkNext = mapInteger(arrangementNote.linkNext);

				chord.chordNotes.put(arrangementNote.string, chordNote);
			}
		}

		Set<Integer> existingChordNoteStrings;

		if (arrangementChord.chordNotes == null || arrangementChord.chordNotes.isEmpty()) {
			for (final Integer string : template.frets.keySet()) {
				if (!chord.chordNotes.containsKey(string)) {
					chord.chordNotes.put(string, new ChordNote(chord, chord.position()));
				}
			}

			existingChordNoteStrings = new HashSet<>();
		} else {
			existingChordNoteStrings = arrangementChord.chordNotes.stream()//
					.map(arrangementChordNote -> arrangementChordNote.string)//
					.collect(Collectors.toSet());
		}

		chord.updateTemplate(arrangementChord.chordId, template);

		chord.chordNotes.forEach((string, chordNote) -> {
			if (!existingChordNoteStrings.contains(string)) {
				chordNote.mute = mute;
			}
		});

		return chord;
	}

	private static Map<FractionalPosition, List<ArrangementNote>> getNotesOnPositions(
			final ArrangementLevel arrangementLevel, final ImmutableBeatsMap beats) {
		final Map<FractionalPosition, List<ArrangementNote>> arrangementNotesMap = new HashMap<>();
		for (final ArrangementNote arrangementNote : arrangementLevel.notes.list) {
			final FractionalPosition position = FractionalPosition.fromTimeRounded(beats, arrangementNote.time);
			List<ArrangementNote> positionNotes = arrangementNotesMap.get(position);
			if (positionNotes == null) {
				positionNotes = new ArrayList<>();
				arrangementNotesMap.put(position, positionNotes);
			}

			positionNotes.add(arrangementNote);
		}
		return arrangementNotesMap;
	}

	private static Note note(final Arrangement arrangement, final ImmutableBeatsMap beats,
			final ArrangementNote arrangementNote) {
		final Note note = new Note(FractionalPosition.fromTimeRounded(beats, arrangementNote.time),
				arrangementNote.string, Math.max(arrangementNote.fret, arrangement.capo));

		note.endPosition(arrangementNote.sustain == null ? note.position()
				: FractionalPosition.fromTimeRounded(beats, arrangementNote.time + arrangementNote.sustain));
		note.accent(mapInteger(arrangementNote.accent));
		note.ignore(mapInteger(arrangementNote.ignore));
		note.bassPicking(BassPickingTechnique.fromArrangmentNote(arrangementNote));
		note.mute(Mute.fromArrangmentNote(arrangementNote));
		note.hopo(HOPO.fromArrangmentNote(arrangementNote));
		note.harmonic(Harmonic.fromArrangmentNote(arrangementNote));
		note.vibrato(mapInteger(arrangementNote.vibrato));
		note.tremolo(mapInteger(arrangementNote.tremolo));
		note.linkNext(mapInteger(arrangementNote.linkNext));
		note.slideTo(arrangementNote.slideTo == null ? arrangementNote.slideUnpitchTo : arrangementNote.slideTo);
		note.unpitchedSlide(arrangementNote.slideUnpitchTo != null);
		note.bendValues(arrangementNote.bendValues == null ? new ArrayList<>()
				: map(arrangementNote.bendValues.list, b -> bendValue(beats, b)));

		return note;
	}

	private static Chord splitChord(final ImmutableBeatsMap beats, final Arrangement arrangement,
			final FractionalPosition position, final List<ArrangementNote> chordNotes, final int chordId) {
		final Chord chord = new Chord(position, chordId, arrangement.chordTemplates.get(chordId));
		chord.splitIntoNotes = true;

		for (final ArrangementNote arrangementNote : chordNotes) {
			final ChordNote chordNote = chord.chordNotes.get(arrangementNote.string);
			chordNote.linkNext = mapInteger(arrangementNote.linkNext);
			chordNote.endPosition(arrangementNote.sustain == null ? chord.position()
					: FractionalPosition.fromTimeRounded(beats, arrangementNote.time + arrangementNote.sustain));

			if (arrangementNote.bendValues != null && !arrangementNote.bendValues.list.isEmpty()) {
				for (final ArrangementBendValue bendValue : arrangementNote.bendValues.list) {
					chordNote.bendValues.add(bendValue(beats, bendValue));
				}
			}

			if (arrangementNote.slideTo != null) {
				chordNote.slideTo = arrangementNote.slideTo;
			}
			if (arrangementNote.slideUnpitchTo != null) {
				chordNote.slideTo = arrangementNote.slideUnpitchTo;
				chordNote.unpitchedSlide = true;
			}
		}

		return chord;
	}

	private static void addNotes(final ImmutableBeatsMap beats, final Arrangement arrangement, final Level level,
			final Map<FractionalPosition, List<ArrangementNote>> arrangementNotesMap) {
		for (final Entry<FractionalPosition, List<ArrangementNote>> notesPosition : arrangementNotesMap.entrySet()) {
			if (notesPosition.getValue().size() == 1) {
				level.sounds.add(ChordOrNote.from(note(arrangement, beats, notesPosition.getValue().get(0))));
				continue;
			}

			final ChordTemplate specialTemplate = new ChordTemplate();
			for (final ArrangementNote arrangementNote : notesPosition.getValue()) {
				specialTemplate.frets.put(arrangementNote.string, arrangementNote.fret);
			}
			final int chordId = arrangement.getChordTemplateIdWithSave(specialTemplate);
			final Chord chord = splitChord(beats, arrangement, notesPosition.getKey(), notesPosition.getValue(),
					chordId);

			level.sounds.add(ChordOrNote.from(chord));
		}
	}

	private static void sortSoundsAndSetCrazyFlag(final List<ChordOrNote> sounds) {
		sounds.sort(IConstantFractionalPosition::compareTo);

		for (int i = 0; i < sounds.size() - 1; i++) {
			final ChordOrNote sound = sounds.get(i);
			final ChordOrNote nextSound = sounds.get(i + 1);
			if (sound.endPosition().position().compareTo(nextSound.position()) >= 0) {
				sound.asGuitarSound().passOtherNotes = true;
			}
		}
	}

	private static HandShape handShape(final ImmutableBeatsMap beats, final ArrangementHandShape arrangementHandShape) {
		final FractionalPosition position = FractionalPosition.fromTimeRounded(beats, arrangementHandShape.startTime);
		final FractionalPosition endPosition = FractionalPosition.fromTimeRounded(beats, arrangementHandShape.endTime);

		return new HandShape(position, endPosition, arrangementHandShape.chordId);
	}

	private static Level toLevel(final ArrangementLevel arrangementLevel, final Arrangement arrangement,
			final ImmutableBeatsMap beats) {
		final Level level = new Level();
		level.fhps = map(arrangementLevel.anchors.list, a -> fhp(beats, a));
		level.sounds = map(arrangementLevel.chords.list,
				c -> ChordOrNote.from(chord(beats, c, arrangement.chordTemplates.get(c.chordId))));

		final Map<FractionalPosition, List<ArrangementNote>> arrangementNotesMap = getNotesOnPositions(arrangementLevel,
				beats);

		addNotes(beats, arrangement, level, arrangementNotesMap);

		sortSoundsAndSetCrazyFlag(level.sounds);
		level.handShapes = map(arrangementLevel.handShapes.list, h -> handShape(beats, h));

		return level;
	}

}
