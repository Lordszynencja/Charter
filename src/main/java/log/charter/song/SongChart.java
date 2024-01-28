package log.charter.song;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.io.rs.xml.song.SongArrangementXStreamHandler.readSong;
import static log.charter.io.rs.xml.vocals.VocalsXStreamHandler.readVocals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import log.charter.data.ArrangementFretHandPositionsCreator;
import log.charter.data.config.Localization.Label;
import log.charter.io.Logger;
import log.charter.io.gp.gp5.GP5File;
import log.charter.io.gp.gp5.GPBar;
import log.charter.io.gp.gp5.GPBeat;
import log.charter.io.gp.gp5.GPMasterBar;
import log.charter.io.gp.gp5.GPTrackData;
import log.charter.io.rs.xml.song.SongArrangement;
import log.charter.io.rsc.xml.RocksmithChartProject;
import log.charter.song.notes.IPosition;
import log.charter.song.notes.Note;
import log.charter.song.vocals.Vocals;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;
import log.charter.util.RW;

public class SongChart {
	public static final String vocalsFileName = "Vocals_RS2.xml";

	public static void addNote(final List<List<Note>> list, final Note n, final int diff) {
		if ((diff < 0) || (diff > 255)) {
			return;
		}

		while (list.size() < diff) {
			list.add(new ArrayList<>());
		}

		list.get(diff).add(n);
	}

	public String musicFileName;

	public String artistName;
	public String artistNameSort;
	public String title;
	public String albumName;
	public Integer albumYear;

	public BeatsMap beatsMap;
	public ArrayList2<ArrangementChart> arrangements;
	public Vocals vocals = new Vocals();

	public HashMap2<Integer, Integer> bookmarks;

	/**
	 * creates empty chart
	 */
	public SongChart(final int songLengthMs, final String musicFileName) {
		this.musicFileName = musicFileName;

		beatsMap = new BeatsMap(songLengthMs);
		arrangements = new ArrayList2<>();

		bookmarks = new HashMap2<>();
	}

	/**
	 * creates chart from loaded project
	 */
	public SongChart(final int songLengthMs, final RocksmithChartProject project, final String dir) throws IOException {
		musicFileName = project.musicFileName;

		artistName = project.artistName;
		artistNameSort = project.artistNameSort;
		title = project.title;
		albumName = project.albumName;
		albumYear = project.albumYear;

		beatsMap = new BeatsMap(songLengthMs, project);

		arrangements = new ArrayList2<>();

		for (final String filename : project.arrangementFiles) {
			try {
				final String xml = RW.read(dir + filename);
				final SongArrangement songArrangement = readSong(xml);
				arrangements.add(new ArrangementChart(songArrangement, beatsMap.beats));
			} catch (final Exception e) {
				Logger.error("Couldn't load arrangement file " + filename, e);
				throw new IOException(String.format(Label.MISSING_ARRANGEMENT_FILE.label(), filename));
			}
		}

		vocals = new Vocals(readVocals(RW.read(dir + vocalsFileName)));

		bookmarks = project.bookmarks;
		if (bookmarks == null) {
			bookmarks = new HashMap2<>();
		}
	}

	/**
	 * creates chart from rs xml
	 */
	public SongChart(final int songLengthMs, final String musicFileName, final SongArrangement songArrangement) {
		this.musicFileName = musicFileName;

		artistName = songArrangement.artistName;
		artistNameSort = songArrangement.artistNameSort;
		title = songArrangement.title;
		albumName = songArrangement.albumName;
		albumYear = songArrangement.albumYear;

		beatsMap = new BeatsMap(songLengthMs, songArrangement);
		arrangements = new ArrayList2<>();
		arrangements.add(new ArrangementChart(songArrangement, beatsMap.beats));

		bookmarks = new HashMap2<>();
	}

	public void addGP5Arrangements(final GP5File gp5File) {
		checkSongDataFromGP5File(gp5File);
		for (final Entry<Integer, List<GPBar>> trackBars : gp5File.bars.entrySet()) {
			final int trackId = trackBars.getKey();
			final ArrayList2<GPBarUnwrapper> unwrappedTrack = unwrapGP5File(gp5File, trackId);
			final GPTrackData trackData = gp5File.tracks.get(trackId);
			if (trackData.isPercussion) {
				continue;
			}

			final ArrangementChart chart = new ArrangementChart(unwrappedTrack, trackData);
			final Level level = chart.levels.get(0);
			arrangements.add(chart);
			ArrangementFretHandPositionsCreator.createFretHandPositions(chart.chordTemplates, level.chordsAndNotes,
					level.anchors);
		}
	}

	private void checkSongDataFromGP5File(final GP5File gp5File) {
		if (artistName == null || artistName.isBlank()) {
			artistName = gp5File.scoreInformation.artist;
		}
		if (title == null || title.isBlank()) {
			title = gp5File.scoreInformation.title;
		}
		if (albumName == null || albumName.isBlank()) {
			albumName = gp5File.scoreInformation.album;
		}
	}

	public ArrayList2<GPBarUnwrapper> unwrapGP5File(final GP5File gp5File) {
		return unwrapGP5File(gp5File, 0);
	}

	public ArrayList2<GPBarUnwrapper> unwrapGP5File(final GP5File gp5File, final int trackId) {
		final BeatsMap tempoMap = new BeatsMap(beatsMap.songLengthMs);
		final ArrayList2<Beat> tempoMapBeats = tempoMap.beats;

		final ArrayList2<GPBarUnwrapper> voiceList = new ArrayList2<>();

		final int masterBarsCount = gp5File.masterBars.size();
		final List<GPBar> bars = gp5File.bars.get(trackId);
		final int otherBarsCount = bars.size();
		// final int voices = bars.get(trackId).voices.size(); // TODO: Fix multiple
		// voices messing up beats
		final int voices = 1;

		if (otherBarsCount == masterBarsCount) {
			for (int voice = 0; voice < voices; voice++) {
				final GPBarUnwrapper wrappedGPBarsInVoice = new GPBarUnwrapper(gp5File.directions);
				int totalBarBeats = 0;
				int previousTempo = gp5File.tempo;

				// Create
				for (int bar = 0; bar < otherBarsCount; bar++) {
					final GPMasterBar masterBar = gp5File.masterBars.get(bar);
					if (bar > 0) {
						previousTempo = wrappedGPBarsInVoice.getLast().noteBeats.getLast().tempo;
					}
					wrappedGPBarsInVoice.addBar(new CombinedGPBars(masterBar, bar + 1));

					final int timeSignatureNum = masterBar.timeSignatureNumerator;
					final int timeSignatureDen = masterBar.timeSignatureDenominator;

					// Add bar lines and and store time signature on them
					for (int barBeat = 0; barBeat < timeSignatureNum; barBeat++) {
						if (totalBarBeats + barBeat >= tempoMapBeats.size()) {
							tempoMap.appendLastBeat(); // Ensure there is a new beat to set up
						}

						final Beat tempoMapBeat = tempoMapBeats.get(totalBarBeats + barBeat);
						tempoMapBeat.setTimeSignature(timeSignatureNum, timeSignatureDen);
						wrappedGPBarsInVoice.getLast().barBeats
								.add(wrappedGPBarsInVoice.get(bar).new BeatUnwrapper(tempoMapBeat));
					}

					wrappedGPBarsInVoice
							.getLast().availableSpaceIn_64ths = (int) (((double) timeSignatureNum / timeSignatureDen)
									* 64);

					// Add note beats of this bar to the combined class
					final int notesInBar = bars.get(bar).voices.get(voice).size();
					for (int noteBeat = 0; noteBeat < notesInBar; noteBeat++) {
						final GPBeat currentNoteBeat = bars.get(bar).voices.get(voice).get(noteBeat);
						wrappedGPBarsInVoice.get(bar).noteBeats
								.add(wrappedGPBarsInVoice.get(bar).new GPBeatUnwrapper(currentNoteBeat));
					}
					wrappedGPBarsInVoice.getLast().notesInBar = notesInBar;
					wrappedGPBarsInVoice.getLast().updateBarsFromNoteTempo(previousTempo);
					totalBarBeats += timeSignatureNum;
				}

				voiceList.add(wrappedGPBarsInVoice);
			}

			// Unwrap them after all wrapped voices have been parsed so that shared beats
			// aren't mangled
			for (int i = 0; i < voiceList.size(); i++) {
				voiceList.get(i).unwrap();
			}
			beatsMap = new BeatsMap(voiceList.get(0).getUnwrappedBeatsMap(beatsMap.songLengthMs));
		}

		return voiceList;
	}

	public void moveEverything(final int positionDifference) {
		final List<IPosition> positionsToMove = new LinkedList<>();
		positionsToMove.addAll(beatsMap.beats);
		for (final ArrangementChart arrangement : arrangements) {
			positionsToMove.addAll(arrangement.toneChanges);
			for (final Level level : arrangement.levels.values()) {
				positionsToMove.addAll(level.anchors);
				positionsToMove.addAll(level.chordsAndNotes);
				positionsToMove.addAll(level.handShapes);
			}
		}
		positionsToMove.addAll(vocals.vocals);

		for (final IPosition positionToMove : positionsToMove) {
			positionToMove.position(min(beatsMap.songLengthMs, max(0, positionToMove.position() + positionDifference)));
		}

		beatsMap.makeBeatsUntilSongEnd();
	}
}
