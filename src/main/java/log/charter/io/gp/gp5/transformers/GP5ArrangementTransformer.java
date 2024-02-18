package log.charter.io.gp.gp5.transformers;

import static java.lang.Math.max;
import static log.charter.data.ArrangementFretHandPositionsCreator.createFretHandPositions;
import static log.charter.song.configs.Tuning.standardStringDistances;

import java.util.List;

import log.charter.data.config.Config;
import log.charter.io.gp.gp5.data.GPBar;
import log.charter.io.gp.gp5.data.GPBeat;
import log.charter.io.gp.gp5.data.GPNote;
import log.charter.io.gp.gp5.data.GPTrackData;
import log.charter.io.rs.xml.song.ArrangementType;
import log.charter.song.Arrangement;
import log.charter.song.BeatsMap;
import log.charter.song.Level;
import log.charter.song.configs.Tuning;
import log.charter.song.configs.Tuning.TuningType;

public class GP5ArrangementTransformer {
	private static ArrangementType getGPArrangementType(final GPTrackData trackData) {
		final String trackName = trackData.trackName.toLowerCase();
		if (trackName.contains("lead")) {
			return ArrangementType.Lead;
		}
		if (trackName.contains("rhythm")) {
			return ArrangementType.Rhythm;
		}
		if (trackName.contains("bass") || trackData.tuning.length < 6) {
			return ArrangementType.Bass;
		}

		return ArrangementType.Lead;
	}

	private static final Tuning getTuningFromGPTuning(final int[] gpTuning, final int capo) {
		final int strings = gpTuning.length;
		final int[] convertedTuning = new int[strings];

		final int offset = standardStringDistances.length - max(6, strings);
		for (int i = 0; i < strings; i++) {
			// A default E standard is offset by 40 from the Tuning E standard, and ordered
			// in the opposite order
			final int gpStringPosition = strings - 1 - i;

			convertedTuning[i] = gpTuning[gpStringPosition] - 40 - standardStringDistances[i + offset] + capo;
		}
		final TuningType tuningType = TuningType.fromTuning(convertedTuning);

		return new Tuning(tuningType, strings, convertedTuning);
	}

	private static void addNote(final GP5SoundsTransformer noteTransformer, final GPBeat gpBeat,
			final MusicalNotePositionIn64s position, final boolean[] wasHOPOStart, final int[] hopoFrom) {
		if (gpBeat.notes.isEmpty()) {
			return;
		}

		if (gpBeat.notes.size() == 1) {
			noteTransformer.addNote(gpBeat, position, wasHOPOStart, hopoFrom);
		} else if (gpBeat.notes.size() > 1) {
			noteTransformer.addChord(gpBeat, position, wasHOPOStart, hopoFrom);
		}

		for (final GPNote note : gpBeat.notes) {
			final int string = note.string;
			wasHOPOStart[string] = note.effects.isHammerPullOrigin;
			hopoFrom[string] = note.fret;
		}
	}

	private static Level generateLevel(final BeatsMap beatsMap, final Arrangement arrangement,
			final List<Integer> barsOrder, final List<GPBar> bars) {
		final Level level = new Level();
		final GP5SoundsTransformer noteTransformer = new GP5SoundsTransformer(level, arrangement);

		final boolean[] wasHOPOStart = new boolean[Config.maxStrings];
		final int[] hopoFrom = new int[Config.maxStrings];

		int barBeatId = 0;
		for (final int barId : barsOrder) {
			if (bars.size() <= barId - 1) {
				barBeatId += beatsMap.beats.get(barBeatId).beatsInMeasure;
				continue;
			}

			for (final List<GPBeat> voice : bars.get(barId - 1).voices) {
				MusicalNotePositionIn64s position = new MusicalNotePositionIn64s(beatsMap.beats, barBeatId);
				for (final GPBeat gpBeat : voice) {
					addNote(noteTransformer, gpBeat, position, wasHOPOStart, hopoFrom);
					position = position.move(gpBeat.duration, gpBeat.tupletNumerator, gpBeat.tupletDenominator);
				}

			}

			barBeatId += beatsMap.beats.get(barBeatId).beatsInMeasure;
		}

		createFretHandPositions(arrangement.chordTemplates, level.sounds, level.anchors);

		return level;
	}

	public static Arrangement makeArrangement(final BeatsMap beatsMap, final List<Integer> barsOrder,
			final GPTrackData trackData, final List<GPBar> bars) {
		final ArrangementType arrangementType = getGPArrangementType(trackData);
		final Arrangement arrangement = new Arrangement(arrangementType, beatsMap.beats);

		arrangement.capo = trackData.capo;
		arrangement.tuning = getTuningFromGPTuning(trackData.tuning, arrangement.capo);
		arrangement.setLevel(0, generateLevel(beatsMap, arrangement, barsOrder, bars));

		return arrangement;
	}
}
