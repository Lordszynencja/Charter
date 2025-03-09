package log.charter.services.data.beats;

import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.song.Arrangement;
import log.charter.data.song.Beat;
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
import log.charter.services.mouseAndKeyboard.HighlightManager;

public class BPMDoubler {
	private ChartData chartData;
	private HighlightManager highlightManager;
	private UndoSystem undoSystem;

	private void doubleBeats(final int fromBeat) {
		final List<Beat> beats = chartData.songChart.beatsMap.beats;

		Beat currentBeat = beats.get(fromBeat);
		for (int i = fromBeat; i < beats.size() - 1; i += 2) {
			final Beat nextBeat = beats.get(i + 1);

			final Beat intermediateBeat = new Beat(currentBeat);
			intermediateBeat.anchor = false;
			intermediateBeat.position((currentBeat.position() + nextBeat.position()) / 2);
			beats.add(i + 1, intermediateBeat);

			currentBeat = nextBeat;
		}
		
		beats.get(fromBeat).anchor = true;
	}

	private FractionalPosition doubleDistanceFrom(final int fromBeat, final FractionalPosition position) {
		return new FractionalPosition(fromBeat + (position.beatId - fromBeat) * 2, position.fraction.multiply(2));
	}

	private void doublePositionDistanceFrom(final int fromBeat, final IFractionalPosition position) {
		if (position.position().beatId >= fromBeat) {
			position.position(doubleDistanceFrom(fromBeat, position.position()));
		}
	}

	private void doublePositionEndDistanceFrom(final int fromBeat, final IFractionalPositionWithEnd position) {
		if (position.endPosition().beatId >= fromBeat) {
			position.endPosition(doubleDistanceFrom(fromBeat, position.endPosition()));
		}
	}

	private void doublePositionWithEndDistanceFrom(final int fromBeat, final IFractionalPositionWithEnd position) {
		doublePositionDistanceFrom(fromBeat, position);
		doublePositionEndDistanceFrom(fromBeat, position);
	}

	private void moveVocalsForDoubledBeats(final int fromBeat) {
		for (final VocalPath vocalPath : chartData.songChart.vocalPaths) {
			vocalPath.vocals.forEach(p -> doublePositionWithEndDistanceFrom(fromBeat, p));
		}
	}

	private void doubleSoundDistancesFrom(final int fromBeat, final ChordOrNote sound) {
		if (sound.isNote()) {
			final Note note = sound.note();
			doublePositionWithEndDistanceFrom(fromBeat, note);
			note.bendValues.forEach(p -> doublePositionDistanceFrom(fromBeat, p));
			return;
		}

		final Chord chord = sound.chord();
		doublePositionDistanceFrom(fromBeat, chord);
		for (final ChordNote chordNote : chord.chordNotes.values()) {
			doublePositionEndDistanceFrom(fromBeat, chordNote);
			chordNote.bendValues.forEach(p -> doublePositionDistanceFrom(fromBeat, p));
		}
	}

	private void moveArrangementsContentsForDoubledBeats(final int fromBeat) {
		for (final Arrangement arrangement : chartData.songChart.arrangements) {
			arrangement.eventPoints.forEach(p -> doublePositionDistanceFrom(fromBeat, p));
			arrangement.toneChanges.forEach(p -> doublePositionDistanceFrom(fromBeat, p));

			for (final Level level : arrangement.levels) {
				level.fhps.forEach(p -> doublePositionDistanceFrom(fromBeat, p));
				level.sounds.forEach(p -> doubleSoundDistancesFrom(fromBeat, p));
				level.handShapes.forEach(p -> doublePositionWithEndDistanceFrom(fromBeat, p));
			}
		}
	}

	private void moveContentForDoubledBeats(final int fromBeat) {
		moveVocalsForDoubledBeats(fromBeat);
		moveArrangementsContentsForDoubledBeats(fromBeat);
	}

	public void doubleBPM() {
		final PositionWithIdAndType highlight = highlightManager.getHighlight();
		if (!highlight.existingPosition || highlight.type != PositionType.BEAT) {
			return;
		}

		undoSystem.addUndo();

		doubleBeats(highlight.id);
		moveContentForDoubledBeats(highlight.id);
		chartData.songChart.beatsMap.fixFirstBeatInMeasures();
	}
}
