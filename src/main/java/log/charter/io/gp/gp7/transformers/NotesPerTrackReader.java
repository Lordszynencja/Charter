package log.charter.io.gp.gp7.transformers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.FractionalPosition;
import log.charter.io.gp.gp7.GP7FractionalPosition;
import log.charter.io.gp.gp7.data.GP7Bar;
import log.charter.io.gp.gp7.data.GP7Beat;
import log.charter.io.gp.gp7.data.GP7MasterBar;
import log.charter.io.gp.gp7.data.GP7Rhythm;
import log.charter.io.gp.gp7.data.GP7Track;
import log.charter.io.gp.gp7.data.GPIF;
import log.charter.io.gp.gp7.transformers.GP7NotesWithPosition.GP7BeatWithNoteAndEndPosition;
import log.charter.util.collections.Pair;

class NotesPerTrackReader {
	private final ImmutableBeatsMap beats;
	private final GPIF gpif;

	public NotesPerTrackReader(final ImmutableBeatsMap beats, final GPIF gpif) {
		this.beats = beats;
		this.gpif = gpif;
	}

	private Map<Integer, List<Pair<GP7MasterBar, Integer>>> tracksData() {
		final Map<Integer, List<Pair<GP7MasterBar, Integer>>> tracksData = new HashMap<>();

		for (final int trackId : gpif.masterTrack.tracks) {
			tracksData.put(trackId, new ArrayList<>());
		}

		for (final GP7MasterBar masterBar : gpif.masterBars) {
			for (int i = 0; i < masterBar.bars.size(); i++) {
				final int trackId = gpif.masterTrack.tracks.get(i);
				tracksData.get(trackId).add(new Pair<>(masterBar, masterBar.bars.get(i)));
			}
		}

		return tracksData;
	}

	private List<GP7NotesWithPosition> getNotesForTrack(final List<Pair<GP7MasterBar, Integer>> trackBarIds) {
		final List<GP7NotesWithPosition> trackNotes = new ArrayList<>();
		int currentBeatId = 0;
		for (final Pair<GP7MasterBar, Integer> barId : trackBarIds) {
			final GP7Bar bar = gpif.bars.get(barId.b);

			final Map<FractionalPosition, List<GP7BeatWithNoteAndEndPosition>> barNotesMap = new HashMap<>();

			for (final int voiceId : bar.voices) {
				if (!gpif.voices.containsKey(voiceId)) {
					continue;
				}

				GP7FractionalPosition currentPosition = new GP7FractionalPosition(beats,
						new FractionalPosition(currentBeatId));

				for (final int beatId : gpif.voices.get(voiceId).beats) {
					final GP7Beat beat = gpif.beats.get(beatId);
					final GP7Rhythm rhythm = gpif.rhythms.get(beat.rhythmReference);
					final GP7FractionalPosition nextPosition = currentPosition.move(rhythm);

					if (!barNotesMap.containsKey(currentPosition.position())) {
						barNotesMap.put(currentPosition.position(), new ArrayList<>());
					}

					for (final int noteId : beat.notes) {
						final GP7BeatWithNoteAndEndPosition note = new GP7BeatWithNoteAndEndPosition(beat,
								gpif.notes.get(noteId), nextPosition.position());
						barNotesMap.get(currentPosition.position()).add(note);
					}

					currentPosition = nextPosition;
				}

				final List<GP7NotesWithPosition> barNotes = new ArrayList<>();
				for (final Entry<FractionalPosition, List<GP7BeatWithNoteAndEndPosition>> barNote : barNotesMap
						.entrySet()) {
					barNotes.add(new GP7NotesWithPosition(barNote.getKey(), barNote.getValue()));
				}

				barNotes.sort(null);
				trackNotes.addAll(barNotes);
			}

			currentBeatId += barId.a.timeSignature.numerator;
		}

		return trackNotes;
	}

	public List<Track> getTracks() {
		final Map<Integer, List<Pair<GP7MasterBar, Integer>>> tracksData = tracksData();
		final List<Track> tracks = new ArrayList<>();

		for (final Entry<Integer, List<Pair<GP7MasterBar, Integer>>> trackData : tracksData.entrySet()) {
			final GP7Track gp7Track = gpif.tracks.get(trackData.getKey());
			if (!TrackInfo.isImportableTrack(gp7Track)) {
				continue;
			}

			final List<GP7NotesWithPosition> trackNotes = getNotesForTrack(trackData.getValue());

			tracks.add(new Track(new TrackInfo(gp7Track), trackNotes));
		}

		return tracks;
	}
}