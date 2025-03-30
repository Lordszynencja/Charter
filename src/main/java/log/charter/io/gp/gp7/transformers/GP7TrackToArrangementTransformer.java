package log.charter.io.gp.gp7.transformers;

import static log.charter.data.song.configs.Tuning.getStringDistance;
import static log.charter.services.ArrangementFretHandPositionsCreator.createFHPs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import log.charter.data.song.Arrangement;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.Level;
import log.charter.data.song.configs.Tuning;
import log.charter.data.song.configs.Tuning.TuningType;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.notes.NoteInterface;
import log.charter.io.rs.xml.song.ArrangementType;

class GP7TrackToArrangementTransformer {
	public static Arrangement transform(final ImmutableBeatsMap beats, final Track track) {
		return new GP7TrackToArrangementTransformer(beats, track).transform();
	}

	private final ImmutableBeatsMap beats;
	private final Track track;

	private final Arrangement arrangement = new Arrangement();

	private final Map<Integer, NoteInterface> shouldSetSlideTo = new HashMap<>();

	private GP7TrackToArrangementTransformer(final ImmutableBeatsMap beats, final Track track) {
		this.beats = beats;
		this.track = track;
	}

	private void setTuning() {
		if (track.trackInfo.tuningValues.length == 0) {
			return;
		}

		final boolean bass = track.trackInfo.type == ArrangementType.Bass;
		final int[] tuningValues = track.trackInfo.tuningValues;

		final int strings = tuningValues.length;
		final int[] convertedTuning = new int[strings];

		for (int string = 0; string < strings; string++) {
			convertedTuning[string] = tuningValues[string] - (bass ? 28 : 40) - getStringDistance(string, strings)
					+ track.trackInfo.capoFret;
		}

		final TuningType tuningType = TuningType.fromTuning(convertedTuning);
		arrangement.tuning = new Tuning(tuningType, strings, convertedTuning);
	}

	private void addNotes() {
		final Level level = arrangement.getLevel(0);

		for (final GP7NotesWithPosition notes : track.notes) {
			if (notes.notes.isEmpty()) {
				continue;
			}

			final List<ChordOrNote> sounds = switch (notes.notes.size()) {
				case 1 -> new SingleNoteCreator(beats, arrangement, shouldSetSlideTo, notes)//
						.generateNote().getCreatedSounds();
				default -> new ChordCreator(beats, arrangement, shouldSetSlideTo, notes)//
						.setChordValues().getCreatedSounds();
			};

			level.sounds.addAll(sounds);
		}

		createFHPs(beats, arrangement.chordTemplates, level.sounds, level.fhps);
	}

	private Arrangement transform() {
		arrangement.arrangementType = track.trackInfo.type;
		setTuning();
		arrangement.capo = track.trackInfo.capoFret;
		addNotes();

		return arrangement;
	}
}
