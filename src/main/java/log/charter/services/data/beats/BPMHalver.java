package log.charter.services.data.beats;

import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.Arrangement;
import log.charter.data.song.Beat;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.Level;
import log.charter.data.song.notes.Chord;
import log.charter.data.song.notes.ChordNote;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.notes.Note;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IFractionalPosition;
import log.charter.data.song.position.fractional.IFractionalPositionWithEnd;
import log.charter.data.song.vocals.VocalPath;
import log.charter.data.types.PositionType;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.utils.ComponentUtils;
import log.charter.services.mouseAndKeyboard.HighlightManager;
import log.charter.util.data.Fraction;

public class BPMHalver {
	private ChartData chartData;
	private CharterFrame charterFrame;
	private HighlightManager highlightManager;
	private UndoSystem undoSystem;

	private Label validateHalving(final int fromBeat) {
		final ImmutableBeatsMap beats = chartData.beats();
		if (!beats.get(fromBeat).firstInMeasure) {
			return Label.HALVING_BPM_MUST_START_ON_MEASURE_BEGINNING;
		}

		Beat previousBeat = beats.get(fromBeat);
		for (int i = fromBeat + 1; i < beats.size(); i++) {
			final Beat currentBeat = beats.get(i);
			if (currentBeat.beatsInMeasure != previousBeat.beatsInMeasure && (i - fromBeat) % 2 != 0) {
				return Label.HALVING_BPM_UNEVEN_BEATS_IN_MEASURE;
			}
			previousBeat = currentBeat;
		}

		return null;
	}

	private void halveBeats(final int fromBeat) {
		final List<Beat> beats = chartData.songChart.beatsMap.beats;
		for (int i = fromBeat + 1; i < beats.size(); i++) {
			beats.remove(i);
		}

		beats.get(fromBeat).anchor = true;
	}

	private FractionalPosition halveDistanceFrom(final int fromBeat, final FractionalPosition position) {
		return position.add(-fromBeat)//
				.multiply(new Fraction(1, 2))//
				.add(fromBeat);
	}

	private void halvePositionDistanceFrom(final int fromBeat, final IFractionalPosition position) {
		if (position.position().beatId >= fromBeat) {
			position.position(halveDistanceFrom(fromBeat, position.position()));
		}
	}

	private void halvePositionEndDistanceFrom(final int fromBeat, final IFractionalPositionWithEnd position) {
		if (position.endPosition().beatId >= fromBeat) {
			position.endPosition(halveDistanceFrom(fromBeat, position.endPosition()));
		}
	}

	private void halvePositionWithEndDistanceFrom(final int fromBeat, final IFractionalPositionWithEnd position) {
		halvePositionDistanceFrom(fromBeat, position);
		halvePositionEndDistanceFrom(fromBeat, position);
	}

	private void moveVocalsForHalvedBeats(final int fromBeat) {
		for (final VocalPath vocalPath : chartData.songChart.vocalPaths) {
			vocalPath.vocals.forEach(p -> halvePositionWithEndDistanceFrom(fromBeat, p));
		}
	}

	private void halveSoundDistancesFrom(final int fromBeat, final ChordOrNote sound) {
		if (sound.isNote()) {
			final Note note = sound.note();
			halvePositionWithEndDistanceFrom(fromBeat, note);
			note.bendValues.forEach(p -> halvePositionDistanceFrom(fromBeat, p));
			return;
		}

		final Chord chord = sound.chord();
		halvePositionDistanceFrom(fromBeat, chord);
		for (final ChordNote chordNote : chord.chordNotes.values()) {
			halvePositionEndDistanceFrom(fromBeat, chordNote);
			chordNote.bendValues.forEach(p -> halvePositionDistanceFrom(fromBeat, p));
		}
	}

	private void moveArrangementsContentsForHalvedBeats(final int fromBeat) {
		for (final Arrangement arrangement : chartData.songChart.arrangements) {
			arrangement.eventPoints.forEach(p -> halvePositionDistanceFrom(fromBeat, p));
			arrangement.toneChanges.forEach(p -> halvePositionDistanceFrom(fromBeat, p));

			for (final Level level : arrangement.levels) {
				level.fhps.forEach(p -> halvePositionDistanceFrom(fromBeat, p));
				level.sounds.forEach(p -> halveSoundDistancesFrom(fromBeat, p));
				level.handShapes.forEach(p -> halvePositionWithEndDistanceFrom(fromBeat, p));
			}
		}
	}

	private void moveContentForHalvedBeats(final int fromBeat) {
		moveVocalsForHalvedBeats(fromBeat);
		moveArrangementsContentsForHalvedBeats(fromBeat);
	}

	public void halveBPM() {
		final PositionWithIdAndType highlight = highlightManager.getHighlight();
		if (!highlight.existingPosition || highlight.type != PositionType.BEAT) {
			return;
		}

		undoSystem.addUndo();

		final Label validation = validateHalving(highlight.id);
		if (validation != null) {
			ComponentUtils.showPopup(charterFrame, validation);
			return;
		}

		halveBeats(highlight.id);
		moveContentForHalvedBeats(highlight.id);
		chartData.songChart.beatsMap.fixFirstBeatInMeasures();
	}
}
