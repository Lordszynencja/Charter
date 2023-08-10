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

		vocals = new Vocals(readVocals(RW.read(dir + "Vocals_RS2.xml")));

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
		final ArrayList2<GPBarUnwrapper> unwrapped = unwrapGP5File(gp5File);
		for (final Entry<Integer, List<GPBar>> trackBars : gp5File.bars.entrySet()) {
			final int trackId = trackBars.getKey();
			final GPTrackData trackData = gp5File.tracks.get(trackId);
			if (trackData.isPercussion) {
				continue;
			}

			final ArrangementChart chart = new ArrangementChart(unwrapped, trackData);
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
		int current_bpm = gp5File.tempo * gp5File.masterBars.get(0).timeSignatureDenominator / 4;
		final ArrayList2<Beat> tempo_map_beats = beatsMap.beats;
		ArrayList2<GPBarUnwrapper> voice_list = new ArrayList2<>();

		beatsMap.setBPM(0, current_bpm, true);

		final int master_bars_count = gp5File.masterBars.size();
		final List<GPBar> bars = gp5File.bars.get(0);
		final int other_bars_count = bars.size();
		// TODO: final int voices = bars.get(0).voices.size();
		final int voices = 1;

		if (other_bars_count == master_bars_count) {
			for (int voice = 0; voice < voices; voice++) {
				GPBarUnwrapper wrapped_GP_bars_in_voice = new GPBarUnwrapper();
				int total_bar_beats = 0;

				for (int bar = 0; bar < other_bars_count; bar++) {
					final GPMasterBar master_bar = gp5File.masterBars.get(bar);
					wrapped_GP_bars_in_voice.add_bar(new CombinedGPBars(master_bar,bar+1));
					
					final int time_signature_num = master_bar.timeSignatureNumerator;
					final int time_signature_den = master_bar.timeSignatureDenominator;

					for (int bar_beat = 0; bar_beat < time_signature_num; bar_beat++) {
						if (total_bar_beats + bar_beat >= tempo_map_beats.size()) {
							beatsMap.append_last_beat(); // Ensure there is a new beat to set up
						}

						final Beat tempo_map_beat = tempo_map_beats.get(total_bar_beats + bar_beat);
						tempo_map_beat.beatsInMeasure = time_signature_num;
						tempo_map_beat.noteDenominator = time_signature_den;
						wrapped_GP_bars_in_voice.getLast().bar_beats.add(tempo_map_beat);
					}

					wrapped_GP_bars_in_voice.getLast().available_space_in_64ths =
					(int)(((double)time_signature_num / time_signature_den) * 64);
					
					final int notes_in_bar = bars.get(bar).voices.get(voice).size();
					for (int note_beat = 0; note_beat < notes_in_bar; note_beat++) {
						final GPBeat current_note_beat = bars.get(bar).voices.get(voice).get(note_beat);
						wrapped_GP_bars_in_voice.get(bar).note_beats.add(current_note_beat);
						if (current_bpm != current_note_beat.tempo) {
							current_bpm = current_note_beat.tempo;
							tempo_map_beats.get(total_bar_beats).anchor = true;
						}
					}
					wrapped_GP_bars_in_voice.getLast().notes_in_bar = notes_in_bar;
					total_bar_beats += time_signature_num;
				}

				wrapped_GP_bars_in_voice.unwrap();
				// TODO: multiple beat maps for voices and tracks
				beatsMap = new BeatsMap(wrapped_GP_bars_in_voice.get_unwrapped_beats_map(beatsMap.songLengthMs));
				voice_list.add(wrapped_GP_bars_in_voice);
			}
		}

		return voice_list;
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
