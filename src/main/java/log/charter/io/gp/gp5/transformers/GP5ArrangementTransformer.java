package log.charter.io.gp.gp5.transformers;

import static log.charter.data.song.configs.Tuning.getStringDistance;
import static log.charter.services.ArrangementFretHandPositionsCreator.createFHPs;

import java.util.List;

import log.charter.data.config.Config;
import log.charter.data.song.Arrangement;
import log.charter.data.song.BeatsMap;
import log.charter.data.song.Level;
import log.charter.data.song.configs.Tuning;
import log.charter.data.song.configs.Tuning.TuningType;
import log.charter.io.gp.gp5.GP5FractionalPosition;
import log.charter.io.gp.gp5.data.GPBar;
import log.charter.io.gp.gp5.data.GPBeat;
import log.charter.io.gp.gp5.data.GPNote;
import log.charter.io.gp.gp5.data.GPTrackData;
import log.charter.io.rs.xml.song.ArrangementType;

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

	private static final Tuning getTuningFromGPTuning(final int[] gpTuning, final int capo, final boolean bass) {
		final int strings = gpTuning.length;
		final int[] convertedTuning = new int[strings];

		for (int string = 0; string < strings; string++) {
			// A default E standard is offset by 40 from the Tuning E standard
			// and ordered in the opposite direction
			final int gpStringPosition = strings - 1 - string;

			convertedTuning[string] = gpTuning[gpStringPosition] - 40 - getStringDistance(string, strings) + capo
					+ (bass ? 12 : 0);
		}
		final TuningType tuningType = TuningType.fromTuning(convertedTuning);

		return new Tuning(tuningType, strings, convertedTuning);
	}

	private static void addNote(final GP5SoundsTransformer noteTransformer, final GPBeat gpBeat,
			final GP5FractionalPosition position, final GP5FractionalPosition endPosition, final boolean[] wasHOPOStart,
			final int[] hopoFrom) {
		if (gpBeat.notes.isEmpty()) {
			return;
		}

		if (gpBeat.notes.size() == 1) {
			noteTransformer.addNote(gpBeat, position, endPosition, wasHOPOStart, hopoFrom);
		} else if (gpBeat.notes.size() > 1) {
			noteTransformer.addChord(gpBeat, position.position(), endPosition.position(), wasHOPOStart, hopoFrom);
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
				barBeatId += beatsMap.getBeatSafe(barBeatId).beatsInMeasure;
				continue;
			}

			for (final List<GPBeat> voice : bars.get(barId - 1).voices) {
				GP5FractionalPosition position = new GP5FractionalPosition(beatsMap.immutable, barBeatId);
				for (final GPBeat gpBeat : voice) {
					final GP5FractionalPosition endPosition = position.move(gpBeat.duration, gpBeat.tupletNumerator,
							gpBeat.tupletDenominator, gpBeat.dots);
					addNote(noteTransformer, gpBeat, position, endPosition, wasHOPOStart, hopoFrom);
					position = endPosition;
				}
			}

			barBeatId += beatsMap.getBeatSafe(barBeatId).beatsInMeasure;
		}

		createFHPs(beatsMap.immutable, arrangement.chordTemplates, level.sounds, level.fhps);

		return level;
	}

	public static Arrangement makeArrangement(final BeatsMap beatsMap, final List<Integer> barsOrder,
			final GPTrackData trackData, final List<GPBar> bars) {
		final ArrangementType arrangementType = getGPArrangementType(trackData);
		final Arrangement arrangement = new Arrangement(arrangementType);

		arrangement.capo = trackData.capo;
		arrangement.tuning = getTuningFromGPTuning(trackData.tuning, arrangement.capo,
				arrangementType == ArrangementType.Bass);
		arrangement.setLevel(0, generateLevel(beatsMap, arrangement, barsOrder, bars));

		return arrangement;
	}
}
