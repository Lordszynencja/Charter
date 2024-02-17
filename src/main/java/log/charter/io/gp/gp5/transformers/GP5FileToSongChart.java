package log.charter.io.gp.gp5.transformers;

import static log.charter.song.configs.Tuning.standardStringDistances;

import java.util.List;
import java.util.Map.Entry;

import log.charter.data.ArrangementFretHandPositionsCreator;
import log.charter.data.config.Config;
import log.charter.io.gp.gp5.data.GP5File;
import log.charter.io.gp.gp5.data.GPBar;
import log.charter.io.gp.gp5.data.GPBeat;
import log.charter.io.gp.gp5.data.GPMasterBar;
import log.charter.io.gp.gp5.data.GPNote;
import log.charter.io.gp.gp5.data.GPTrackData;
import log.charter.io.gp.gp5.transformers.CombinedGPBars.BeatUnwrapper;
import log.charter.io.gp.gp5.transformers.CombinedGPBars.GPBeatUnwrapper;
import log.charter.io.rs.xml.song.ArrangementType;
import log.charter.song.Arrangement;
import log.charter.song.Beat;
import log.charter.song.BeatsMap;
import log.charter.song.EventPoint;
import log.charter.song.Level;
import log.charter.song.Phrase;
import log.charter.song.SongChart;
import log.charter.song.configs.Tuning;
import log.charter.song.configs.Tuning.TuningType;
import log.charter.util.CollectionUtils.ArrayList2;

public class GP5FileToSongChart {
	private static void addSongData(final SongChart chart, final GP5File gp5File) {
		if (chart.artistName == null || chart.artistName.isBlank()) {
			chart.artistName = gp5File.scoreInformation.artist;
		}
		if (chart.title == null || chart.title.isBlank()) {
			chart.title = gp5File.scoreInformation.title;
		}
		if (chart.albumName == null || chart.albumName.isBlank()) {
			chart.albumName = gp5File.scoreInformation.album;
		}
	}

	private static ArrangementType getGPArrangementType(final GPTrackData trackData) {
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

	private static final Tuning getTuningFromGPTuning(final int[] gpTuning, final int capo) {
		final int strings = gpTuning.length;
		final int[] convertedTuning = new int[strings];

		final int offset = standardStringDistances.length - Math.max(9, strings);
		for (int i = 0; i < strings; i++) {
			// A default E standard is offset by 40 from the Tuning E standard, and ordered
			// in the opposite order
			final int gpStringPosition = strings - 1 - i;

			convertedTuning[i] = gpTuning[gpStringPosition] - 40 - standardStringDistances[i + offset] + capo;
		}
		final TuningType tuningType = TuningType.fromTuning(convertedTuning);

		return new Tuning(tuningType, strings, convertedTuning);
	}

	private static void addVoice(final GP5SoundsTransformer noteTransformer, final BeatsMap beatsMap,
			final GPBarUnwrapper voice) {
		final boolean[] wasHOPOStart = new boolean[Config.maxStrings];
		final int[] hopoFrom = new int[Config.maxStrings];
		int barBeatId = 0;
		double noteStartPosition = beatsMap.beats.get(0).position();

		for (final CombinedGPBars bar : voice.unwrappedBars) {
			final MusicalNotePositionIn64s position = new MusicalNotePositionIn64s(beatsMap.beats, barBeatId);
			// Initial note position is the first is a bar

			for (final GPBeatUnwrapper noteBeat : bar.noteBeats) {
				if (noteBeat.notes.isEmpty()) {
					position.move(noteBeat.duration, noteBeat.tupletNumerator, noteBeat.tupletDenominator);
					noteStartPosition += noteBeat.getNoteTimeMs();
					continue;
				}

				if (noteBeat.notes.size() == 1) {
					noteTransformer.addNote(noteBeat, (int) noteStartPosition, wasHOPOStart, hopoFrom);
				} else if (noteBeat.notes.size() > 1) {
					noteTransformer.addChord(noteBeat, (int) noteStartPosition, wasHOPOStart, hopoFrom);
				}

				position.move(noteBeat.duration);
				noteStartPosition += noteBeat.getNoteTimeMs();

				for (final GPNote note : noteBeat.notes) {
					final int string = note.string;
					wasHOPOStart[string] = note.effects.isHammerPullOrigin;
					hopoFrom[string] = note.fret;
				}
			}

			barBeatId++;
		}
	}

	private static Level addBars(final Arrangement arrangement, final ArrayList2<GPBarUnwrapper> unwrap,
			final BeatsMap beatsMap) {
		final Level level = new Level();
		final GP5SoundsTransformer noteTransformer = new GP5SoundsTransformer(level, arrangement);

		for (final GPBarUnwrapper voice : unwrap) {
			addVoice(noteTransformer, beatsMap, voice);
		}

		return level;
	}

	private static void addPhrase(final Arrangement arrangement, final String name, final int position) {
		arrangement.phrases.put(name, new Phrase(0, false));
		final EventPoint count = new EventPoint(position);
		count.phrase = name;
		arrangement.eventPoints.add(0, count);
	}

	private static void addCountEndPhrases(final Arrangement arrangement, final ArrayList2<GPBarUnwrapper> unwrap) {
		addPhrase(arrangement, "COUNT", 0);
		addPhrase(arrangement, "END", unwrap.get(0).getLastBarStartPosition());
	}

	private static Arrangement generateArrangement(final GPTrackData trackData,
			final ArrayList2<GPBarUnwrapper> unwrappedTrack, final BeatsMap beatsMap) {
		final ArrangementType arrangementType = getGPArrangementType(trackData);
		final Arrangement arrangement = new Arrangement(arrangementType);

		arrangement.capo = trackData.capo;
		arrangement.tuning = getTuningFromGPTuning(trackData.tuning, arrangement.capo);
		arrangement.setLevel(0, addBars(arrangement, unwrappedTrack, beatsMap));
		addCountEndPhrases(arrangement, unwrappedTrack);

		return arrangement;
	}

	private static void addGP5Arrangements(final SongChart chart, final GP5File gp5File) {
		for (final int trackId : gp5File.trackBars.keySet()) {
			final ArrayList2<GPBarUnwrapper> unwrappedTrack = unwrapGP5Track(chart, gp5File, trackId);
			final GPTrackData trackData = gp5File.tracks.get(trackId);
			if (trackData.isPercussion) {
				continue;
			}

			final Arrangement arrangement = generateArrangement(trackData, unwrappedTrack, chart.beatsMap);

			final Level level = arrangement.getLevel(0);
			chart.arrangements.add(arrangement);
			ArrangementFretHandPositionsCreator.createFretHandPositions(arrangement.chordTemplates, level.sounds,
					level.anchors);
		}
	}

	private static void addWrappedNoteBeats(final CombinedGPBars combinedBar, final List<GPBeat> barVoice) {
		// Add note beats of this bar to the combined class
		for (int noteBeat = 0; noteBeat < barVoice.size(); noteBeat++) {
			final GPBeat currentNoteBeat = barVoice.get(noteBeat);
			combinedBar.noteBeats.add(new GPBeatUnwrapper(currentNoteBeat));
		}

		combinedBar.notesInBar = barVoice.size();
	}

	private static void wrapVoice(final GP5File gp5File, final List<GPBar> bars,
			final ArrayList2<GPBarUnwrapper> voiceList, final BeatsMap tempoMap, final int voice) {
		final GPBarUnwrapper wrappedGPBarsInVoice = new GPBarUnwrapper(gp5File.directions);
		int totalBarBeats = 0;
		int previousTempo = gp5File.tempo;

		// Create
		for (int bar = 0; bar < bars.size(); bar++) {
			final GPMasterBar masterBar = gp5File.masterBars.get(bar);

			final CombinedGPBars combinedBar = new CombinedGPBars(masterBar, bar + 1);
			wrappedGPBarsInVoice.combinedBars.add(combinedBar);

			final int timeSignatureNum = masterBar.timeSignatureNumerator;
			final int timeSignatureDen = masterBar.timeSignatureDenominator;
			// Add bar lines and and store time signature on them
			for (int barBeat = 0; barBeat < timeSignatureNum; barBeat++) {
				if (totalBarBeats + barBeat >= tempoMap.beats.size()) {
					tempoMap.appendLastBeat(); // Ensure there is a new beat to set up
				}

				final Beat tempoMapBeat = tempoMap.beats.get(totalBarBeats + barBeat);
				tempoMapBeat.setTimeSignature(timeSignatureNum, timeSignatureDen);
				combinedBar.barBeats.add(new BeatUnwrapper(tempoMapBeat));
			}

			combinedBar.availableSpaceIn_64ths = 64 * timeSignatureNum / timeSignatureDen;

			addWrappedNoteBeats(combinedBar, bars.get(bar).voices.get(voice));
			combinedBar.updateBarsFromNoteTempo(previousTempo);

			totalBarBeats += timeSignatureNum;
			previousTempo = combinedBar.noteBeats.getLast().tempo;
		}

		voiceList.add(wrappedGPBarsInVoice);
	}

	private static ArrayList2<GPBarUnwrapper> unwrapGP5Track(final SongChart chart, final GP5File gp5File,
			final int trackId) {
		final BeatsMap tempoMap = new BeatsMap(chart.beatsMap.songLengthMs);
		final ArrayList2<GPBarUnwrapper> voiceList = new ArrayList2<>();

		final int masterBarsCount = gp5File.masterBars.size();
		final List<GPBar> bars = gp5File.trackBars.get(trackId);

		if (bars.size() != masterBarsCount) {
			return voiceList;
		}

		for (int voice = 0; voice < 1/* bars.get(trackId).voices.size() */; voice++) {
			wrapVoice(gp5File, bars, voiceList, tempoMap, voice);
		}

		// Unwrap them after all wrapped voices have been parsed so that shared beats
		// aren't mangled
		for (int i = 0; i < voiceList.size(); i++) {
			voiceList.get(i).unwrap();
		}
		chart.beatsMap = new BeatsMap(voiceList.get(0).getUnwrappedBeatsMap(chart.beatsMap.songLengthMs));

		return voiceList;
	}

	public static SongChart transform(final int songLength, final int firstBeatPosition, final GP5File gp5File) {
		final SongChart chart = new SongChart(songLength, 0);

		addSongData(chart, gp5File);
		addGP5Arrangements(chart, gp5File);
		chart.moveEverything(firstBeatPosition);

		return chart;
	}

	public static BeatsMap getTempoMap(final GP5File gp5File, final int length) {
		final BeatsMap tempoMap = new BeatsMap(length);

		for (final Entry<Integer, List<GPBar>> trackBars : gp5File.trackBars.entrySet()) {
			final int masterBarsCount = gp5File.masterBars.size();
			final List<GPBar> bars = gp5File.trackBars.get(trackBars.getKey());

			if (bars.size() != masterBarsCount) {
				continue;
			}

			final ArrayList2<GPBarUnwrapper> voiceList = new ArrayList2<>();
			for (int voice = 0; voice < 1/* bars.get(trackId).voices.size() */; voice++) {
				wrapVoice(gp5File, bars, voiceList, tempoMap, voice);
			}

			// Unwrap them after all wrapped voices have been parsed so that shared beats
			// aren't mangled
			for (int i = 0; i < voiceList.size(); i++) {
				voiceList.get(i).unwrap();
			}

			return new BeatsMap(voiceList.get(0).getUnwrappedBeatsMap(length));
		}

		return tempoMap;
	}
}
