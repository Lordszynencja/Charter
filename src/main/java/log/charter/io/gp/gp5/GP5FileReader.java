package log.charter.io.gp.gp5;

import static log.charter.io.gp.gp5.GP5BinaryUtils.readBoolean;
import static log.charter.io.gp.gp5.GP5BinaryUtils.readColor;
import static log.charter.io.gp.gp5.GP5BinaryUtils.readDouble;
import static log.charter.io.gp.gp5.GP5BinaryUtils.readInt16LE;
import static log.charter.io.gp.gp5.GP5BinaryUtils.readInt32LE;
import static log.charter.io.gp.gp5.GP5BinaryUtils.readShortInt8;
import static log.charter.io.gp.gp5.GP5BinaryUtils.readStringWithByteSkip;
import static log.charter.io.gp.gp5.GP5BinaryUtils.readStringWithSize;
import static log.charter.io.gp.gp5.GP5BinaryUtils.readStringWithSizeSkip;
import static log.charter.io.gp.gp5.GP5BinaryUtils.readStringWithSkip;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import log.charter.io.Logger;
import log.charter.song.enums.BassPickingTechnique;
import log.charter.song.enums.HOPO;
import log.charter.song.enums.Harmonic;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.RW;

public class GP5FileReader {
	private static final String versionString = "FICHIER GUITAR PRO";

	private static final int timeSignatureNumeratorFlag = 1 << 0;
	private static final int timeSignatureDenominatorFlag = 1 << 1;
	private static final int repeatStartFlag = 1 << 2;
	private static final int repeatCountFlag = 1 << 3;
	private static final int alternateEndingsFlag = 1 << 4;
	private static final int markerFlag = 1 << 5;
	private static final int keySignatureFlag = 1 << 6;
	private static final int isDoubleBarFlag = 1 << 7;

	private static final int percussionTrackFlag = 1 << 0;

	public static GP5File importGPFile(final File file) {
		final GP5File gp5File = new GP5FileReader(new ByteArrayInputStream(RW.readB(file))).readScore();

		return gp5File;
	}

	private final ByteArrayInputStream data;
	private int version;
	private TripletFeel globalTripletFeel;
	private int tempo;
	private int barCount;
	private int trackCount;
	private List<GPMasterBar> masterBars;
	private List<GPTrackData> tracks;
	private Map<Integer, List<GPBar>> bars;
	private Directions directions;
	private GP5File file;

	private GP5FileReader(final ByteArrayInputStream data) {
		this.data = data;
	}

	private GP5File readScore() {
		if (file != null) {
			return file;
		}

		version = readVersion();
		if (version == -1) {
			return null;
		}

		final ScoreInformation scoreInformation = readScoreInformation();
		globalTripletFeel = readGlobalTripletFeel();
		final List<GPLyrics> lyrics = readLyrics();
		readSettings();

		barCount = readInt32LE(data);
		trackCount = readInt32LE(data);
		masterBars = readMasterBars();
		tracks = readTracksData();
		bars = readBars();

		file = new GP5File(version, scoreInformation, tempo, masterBars, tracks, bars, lyrics, directions);
		return file;
	}

	private int readVersion() {
		String version = readStringWithSkip(data, 30);
		if (!version.startsWith(versionString)) {
			return -1;
		}

		version = version.substring(versionString.length() + 2);
		final int dotPosition = version.indexOf('.');
		return 100 * Integer.valueOf(version.substring(0, dotPosition))
				+ Integer.valueOf(version.substring(dotPosition + 1));
	}

	private ScoreInformation readScoreInformation() {
		final String title = readStringWithSizeSkip(data);
		final String subtitle = readStringWithSizeSkip(data);
		final String artist = readStringWithSizeSkip(data);
		final String album = readStringWithSizeSkip(data);
		final String words = readStringWithSizeSkip(data);
		final String music = version >= 500 ? readStringWithSizeSkip(data) : words;
		final String copyright = readStringWithSizeSkip(data);
		final String tab = readStringWithSizeSkip(data);
		final String instructions = readStringWithSizeSkip(data);

		final int noticeLines = readInt32LE(data);
		String notices = "";
		for (int i = 0; i < noticeLines; i++) {
			if (i > 0) {
				notices += "\r\n";
			}

			notices += readStringWithSizeSkip(data);
		}

		return new ScoreInformation(title, subtitle, artist, album, words, music, copyright, tab, instructions,
				notices);
	}

	private TripletFeel readGlobalTripletFeel() {
		if (version < 500) {
			return readBoolean(data) ? TripletFeel.EIGHTH : TripletFeel.NONE;
		}

		return TripletFeel.NONE;
	}

	private List<GPLyrics> readLyrics() {
		if (version < 400) {
			return new ArrayList<>();
		}

		final List<GPLyrics> lyrics = new ArrayList<>();
		readInt32LE(data);// lyrics track + 1

		for (int i = 0; i < 5; i++) {
			final int startBar = readInt32LE(data) - 1;
			final String text = readStringWithSize(data);
			lyrics.add(new GPLyrics(startBar, text));
		}

		return lyrics;
	}

	private void readSettings() {
		readMasterSettings();
		readPageSetup();
		tempo = readInt32LE(data);
		if (version >= 510) {
			readBoolean(data); // hide tempo?
		}

		readInt32LE(data); // key signature and octave
		if (version >= 400) {
			data.read();
		}

		readPlaybackInfos();
		readDirections();
	}

	private void readMasterSettings() {
		if (version < 510) {
			return;
		}

		data.skip(4);// master volume
		data.skip(4);// master effect
		data.skip(10);// master equalizer
		data.skip(1);// master equalizer preset
	}

	private void readPageSetup() {
		if (version < 500) {
			return;
		}

		data.skip(4); // Page width
		data.skip(4); // Page heigth
		data.skip(4); // Padding left
		data.skip(4); // Padding right
		data.skip(4); // Padding top
		data.skip(4); // Padding bottom
		data.skip(4); // Size proportion
		data.skip(2); // Header and footer display flags

		readStringWithByteSkip(data); // title format
		readStringWithByteSkip(data); // subtitle format
		readStringWithByteSkip(data); // artist format
		readStringWithByteSkip(data); // album format
		readStringWithByteSkip(data); // words format
		readStringWithByteSkip(data); // music format
		readStringWithByteSkip(data); // words and music format
		readStringWithByteSkip(data); // copyright format
		readStringWithByteSkip(data); // page number format
		readStringWithByteSkip(data);// ???

		readStringWithByteSkip(data);// Tempo label
	}

	private void readPlaybackInfos() {
		for (int i = 0; i < 64; i++) {
			readInt32LE(data);// program
			data.read();// volume
			data.read();// balance
			data.skip(6);// ???
		}
	}

	private void readDirections() {
		if (version < 500) {
			return;
		}
		directions = new Directions();
		directions.coda = readInt16LE(data); // "Coda" bar index
		directions.double_coda = readInt16LE(data); // "Double Coda" bar index
		directions.segno = readInt16LE(data); // "Segno" bar index
		directions.segno_segno = readInt16LE(data); // "Segno Segno" bar index
		directions.fine = readInt16LE(data); // "Fine" bar index
		directions.da_capo = readInt16LE(data); // "Da Capo" bar index
		directions.da_capo_al_coda = readInt16LE(data); // "Da Capo al Coda" bar index
		directions.da_capo_al_double_coda = readInt16LE(data); // "Da Capo al Double Coda" bar index
		directions.da_capo_al_fine = readInt16LE(data); // "Da Capo al Fine" bar index
		directions.da_segno = readInt16LE(data); // "Da Segno" bar index
		directions.da_segno_al_coda = readInt16LE(data); // "Da Segno al Coda" bar index
		directions.da_segno_al_double_coda = readInt16LE(data); // "Da Segno al Double Coda" bar index
		directions.da_segno_al_fine = readInt16LE(data); // "Da Segno al Fine "bar index
		directions.da_segno_segno = readInt16LE(data); // "Da Segno Segno" bar index
		directions.da_segno_segno_al_coda = readInt16LE(data); // "Da Segno Segno al Coda" bar index
		directions.da_segno_segno_al_double_coda = readInt16LE(data); // "Da Segno Segno al Double Coda" bar index
		directions.da_segno_segno_al_fine = readInt16LE(data); // "Da Segno Segno al Fine" bar index
		directions.da_coda = readInt16LE(data); // "Da Coda" bar index
		directions.da_double_coda = readInt16LE(data); // "Da Double Coda" bar index
		data.skip(4); // unknown
	}

	private ArrayList2<GPMasterBar> readMasterBars() {
		final ArrayList2<GPMasterBar> masterBars = new ArrayList2<>(barCount);
		GPMasterBar previous = null;
		for (int i = 0; i < barCount; i++) {
			previous = readMasterBar(masterBars);
			masterBars.add(previous);
		}

		return masterBars;
	}

	private GPMasterBar readMasterBar(final ArrayList2<GPMasterBar> masterBars) {
		final GPMasterBar previous = masterBars.getLast();
		final int flags = data.read();

		final int timeSignatureNumerator = (flags & timeSignatureNumeratorFlag) != 0 ? data.read()
				: previous.timeSignatureNumerator;
		final int timeSignatureDenominator = (flags & timeSignatureDenominatorFlag) != 0 ? data.read()
				: previous.timeSignatureDenominator;
		final boolean isRepeatStart = (flags & repeatStartFlag) != 0;
		final int repeatCount = (flags & repeatCountFlag) != 0 ? data.read() : 0;

		// alternate endings (pre GP5)
		int alternateEndings = 0;
		if ((flags & alternateEndingsFlag) != 0 && version < 500) {
			final int masterBarId = masterBars.size() - 1;
			// get the already existing alternatives to ignore them
			int existentAlternatives = 0;
			while (masterBarId >= 0) {
				final GPMasterBar currentMasterBar = masterBars.get(masterBarId);
				// found another repeat ending?
				if (currentMasterBar.isRepeatEnd() && masterBarId != masterBars.size() - 1) {
					break;
				}
				// found the opening?
				if (currentMasterBar.isRepeatStart) {
					break;
				}
				existentAlternatives |= currentMasterBar.alternateEndings;
			}

			// now calculate the alternative for this bar
			final int repeatMask = data.read();
			for (int i = 0; i < 8; i++) {
				// only add the repeating if it is not existing
				final int repeating = 1 << i;
				if (repeatMask > i && (existentAlternatives & repeating) == 0) {
					alternateEndings |= repeating;
				}
			}
		}

		// marker
		if ((flags & markerFlag) != 0) {
			readStringWithByteSkip(data);// section text
			readColor(data, false);
		}

		// keysignature
		KeySignature keySignature;
		KeySignatureType keySignatureType;
		if ((flags & keySignatureFlag) != 0) {
			keySignature = KeySignature.fromValue(readShortInt8(data));
			keySignatureType = KeySignatureType.fromValue(data.read());
		} else if (previous != null) {
			keySignature = previous.keySignature;
			keySignatureType = previous.keySignatureType;
		} else {
			keySignature = KeySignature.C;
			keySignatureType = KeySignatureType.Major;
		}

		if (version >= 500 && (flags & (timeSignatureNumeratorFlag | timeSignatureDenominatorFlag)) != 0) {
			data.skip(4);
		}
		// better alternate ending mask in GP5
		if (version >= 500) {
			alternateEndings = data.read();
		}

		// tripletfeel
		TripletFeel tripletFeel;
		if (version >= 500) {
			final int tripletFeelValue = data.read();
			switch (tripletFeelValue) {
			case 1:
				tripletFeel = TripletFeel.EIGHTH;
				break;
			case 2:
				tripletFeel = TripletFeel.SIXTEENTH;
				break;
			default:
				tripletFeel = TripletFeel.NONE;
				break;
			}
			data.read();
		} else {
			tripletFeel = globalTripletFeel;
		}
		final boolean isDoubleBar = (flags & isDoubleBarFlag) != 0;

		return new GPMasterBar(timeSignatureNumerator, timeSignatureDenominator, isRepeatStart, repeatCount,
				alternateEndings, keySignature, keySignatureType, tripletFeel, isDoubleBar);
	}

	private List<GPTrackData> readTracksData() {
		final List<GPTrackData> tracks = new ArrayList<>();
		for (int i = 0; i < trackCount; i++) {
			final GPTrackData track = readTrackData();
			if (track != null) {
				tracks.add(track);
			}
		}

		return tracks;
	}

	private GPTrackData readTrackData() {
		final int flags = data.read();
		final String trackName = readStringWithSkip(data, 40);

		final boolean isPercussion = (flags & percussionTrackFlag) != 0;

		final int stringCount = readInt32LE(data);
		final int[] tuning = new int[stringCount];
		// E standard guitar - [64, 59, 55, 50, 45, 40]
		// E standard bass - [43, 38, 33, 28]
		for (int i = 0; i < 7; i++) {
			final int stringTuning = readInt32LE(data);
			if (i < stringCount) {
				tuning[i] = stringTuning;
			}
		}

		readInt32LE(data);// port
		readInt32LE(data);// index + 1
		readInt32LE(data);// effect channel + 1
		final int fretCount = readInt32LE(data);
		final int capo = readInt32LE(data);
		readColor(data, false);// track color
		if (version >= 500) {
			// flags for
			// 0x01 -> show tablature
			// 0x02 -> show standard notation
			data.read();
			// flags for
			// 0x02 -> auto let ring
			// 0x04 -> auto brush
			data.read();
			// unknown
			data.skip(43);
		}

		// unknown
		if (version >= 510) {
			data.skip(4);// ???
			readStringWithByteSkip(data);// tone name
			readStringWithByteSkip(data);// tone type
		}

		return new GPTrackData(trackName, isPercussion, tuning, fretCount, capo);
	}

	private Map<Integer, List<GPBar>> readBars() {
		final Map<Integer, List<GPBar>> barsPerTrack = new HashMap<>();
		for (int i = 0; i < trackCount; i++) {
			barsPerTrack.put(i, new ArrayList<>());
		}

		for (int i = 0; i < barCount; i++) {
			for (int t = 0; t < trackCount; t++) {
				barsPerTrack.get(t).add(readBar(t));
			}
		}

		return barsPerTrack;
	}

	private GPBar readBar(final int trackId) {
		int voiceCount;
		if (version >= 500) {
			data.read();
			voiceCount = 2;
		} else {
			voiceCount = 1;
		}

		final List<List<GPBeat>> voices = new ArrayList<>();
		for (int v = 0; v < voiceCount; v++) {
			final List<GPBeat> voice = readVoice(trackId, v == 0); // Only read tempo changes for voice 0
			if (voice != null) {
				voices.add(voice);
			}
		}

		return new GPBar(voices);
	}

	private List<GPBeat> readVoice(final int trackId, final boolean read_tempo_changes) {
		final int beatCount = readInt32LE(data);
		if (beatCount == 0) {
			return null;
		}

		final List<GPBeat> beats = new ArrayList<>();

		for (int i = 0; i < beatCount; i++) {
			final GPBeat beat = readBeat(trackId, read_tempo_changes);
			beats.add(beat);
		}

		return beats;
	}

	private GPBeat readBeat(final int trackId, final boolean read_tempo_changes) {
		final int flags = data.read();
		final int dots = (flags & 0x01) != 0 ? 1 : 0;
		boolean isEmpty = false;
		if ((flags & 0x40) != 0) {
			final int type = data.read();
			isEmpty = (type & 0x02) == 0;
		}

		final GPDuration duration = GPDuration.fromValue(readShortInt8(data));

		final int[] tupletNumeratorDenominator = readTupletNumeratorDenominator(flags);
		final int tupletNumerator = tupletNumeratorDenominator[0];
		final int tupletDenominator = tupletNumeratorDenominator[1];

		GPChord chord;
		if ((flags & 0x02) != 0) {
			chord = readChord(trackId);
		} else {
			chord = null;
		}

		String text;
		if ((flags & 0x04) != 0) {
			text = readStringWithSizeSkip(data);
		} else {
			text = "";
		}

		final GPBeatEffects beatEffects;
		if ((flags & 0x08) != 0) {
			beatEffects = readBeatEffects();
		} else {
			beatEffects = new GPBeatEffects();
		}

		if ((flags & 0x10) != 0 && read_tempo_changes) {
			tempo = readMixTableChange();
		}

		final int stringFlags = data.read();
		final List<GPNote> notes = new ArrayList<>();
		for (int i = 6; i >= 0; i--) {
			if ((stringFlags & (1 << i)) != 0 && 6 - i < tracks.get(trackId).tuning.length) {
				notes.add(readNote(tracks.get(trackId).tuning.length - 6 + i));
			}
		}

		if (version >= 500) {
			data.read();
			final int flags2 = data.read();
			if ((flags2 & 0x08) != 0) {
				data.read();
			}

		}

		return new GPBeat(tempo, dots, isEmpty, duration, tupletNumerator, tupletDenominator, beatEffects, chord, notes,
				text);
	}

	private final int[][] tupletNumeratorsDenominators = { //
			{ 1, 1 }, // 0
			{ 1, 1 }, // 1
			{ 1, 1 }, // 2
			{ 3, 2 }, // 3
			{ 1, 1 }, // 4
			{ 5, 4 }, // 5
			{ 6, 4 }, // 6
			{ 7, 4 }, // 7
			{ 1, 1 }, // 8
			{ 9, 8 }, // 9
			{ 10, 8 }, // 10
			{ 11, 8 }, // 11
			{ 12, 8 }, // 12
			{ 13, 8 }, // 13
			{ 14, 8 }, // 14
			{ 15, 8 },// 15
	};

	private int[] readTupletNumeratorDenominator(final int flags) {
		if ((flags & 0x20) != 0) {
			final int value = readInt32LE(data);
			return tupletNumeratorsDenominators[value >= 0 && value < tupletNumeratorsDenominators.length ? value : 0];
		}

		return tupletNumeratorsDenominators[0];
	}

	private GPChord readChord(final int trackId) {
		final String chordName;
		int firstFret;
		final int[] chordFrets = new int[tracks.get(trackId).tuning.length];
		byte[] barreFrets = new byte[0];

		if (version >= 500) {
			data.skip(17);
			chordName = readStringWithSkip(data, 21);
			data.skip(4);
			firstFret = readInt32LE(data);
			for (int i = 0; i < 7; i++) {
				final int fret = readInt32LE(data);
				if (i < chordFrets.length) {
					chordFrets[i] = fret;
				}
			}

			final int numberOfBarres = data.read();
			barreFrets = new byte[numberOfBarres];
			try {
				data.read(barreFrets);
				data.skip(5 - numberOfBarres);
			} catch (final IOException e) {
				Logger.error("Couldn't read data", e);
			}
			data.skip(26);
		} else {
			if (data.read() != 0) {
				// gp4
				if (version >= 400) {
					// Sharp (1)
					// Unused (3)
					// Root (1)
					// Major/Minor (1)
					// Nin,Eleven or Thirteen (1)
					// Bass (4)
					// Diminished/Augmented (4)
					// Add (1)
					data.skip(16);
					chordName = readStringWithSkip(data, 21);
					// Unused (2)
					// Fifth (1)
					// Ninth (1)
					// Eleventh (1)
					data.skip(4);
					firstFret = readInt32LE(data);
					for (int i = 0; i < 7; i++) {
						final int fret = readInt32LE(data);
						if (i < chordFrets.length) {
							chordFrets[i] = fret;
						}
					}

					final int numberOfBarres = data.read();
					barreFrets = new byte[numberOfBarres];
					try {
						data.read(barreFrets);
						data.skip(5 - numberOfBarres);
					} catch (final IOException e) {
						Logger.error("Couldn't read data", e);
					}
					// Barree end (5)
					// Omission1,3,5,7,9,11,13 (7)
					// Unused (1)
					// Fingering (7)
					// Show Diagram Fingering (1)
					// ??
					data.skip(26);
				} else {
					// unknown
					data.skip(25);
					chordName = readStringWithSkip(data, 34);
					firstFret = readInt32LE(data);
					barreFrets = new byte[0];
					for (int i = 0; i < 6; i++) {
						final int fret = readInt32LE(data);
						if (i < chordFrets.length) {
							chordFrets[i] = fret;
						}
					}
					// unknown
					data.skip(36);
				}
			} else {
				final int strings = version >= 406 ? 7 : 6;
				chordName = readStringWithByteSkip(data);
				firstFret = readInt32LE(data);
				barreFrets = new byte[0];
				if (firstFret > 0) {
					for (int i = 0; i < strings; i++) {
						final int fret = readInt32LE(data);
						if (i < chordFrets.length) {
							chordFrets[i] = fret;
						}
					}
				}
			}
		}

		if (chordName == null) {
			return null;
		}

		return new GPChord(chordName, firstFret, chordFrets, barreFrets);
	}

	private GPBeatEffects readBeatEffects() {
		final int flags = data.read();
		int flags2 = 0;
		if (version >= 400) {
			flags2 = data.read();
		}

		boolean vibrato = false;
		if ((version < 400 && (flags & 0x01) != 0) || (flags & 0x02) != 0) {
			vibrato = true;
		}

		final boolean rasgueado = (flags2 & 0x01) != 0;
		HOPO hopo = HOPO.NONE;
		BassPickingTechnique bassPickingTechnique = BassPickingTechnique.NONE;
		if ((flags & 0x20) != 0) {
			final int slapPop = readShortInt8(data);
			switch (slapPop) {
			case 1:
				hopo = HOPO.TAP;
				break;
			case 2:
				bassPickingTechnique = BassPickingTechnique.SLAP;
				break;
			case 3:
				bassPickingTechnique = BassPickingTechnique.POP;
				break;
			}
			if (version < 400) {
				data.skip(4);
			}
		}

		List<GPBend> tremoloEffects;
		if ((flags2 & 0x04) != 0) {
			tremoloEffects = readBend();
		} else {
			tremoloEffects = new ArrayList<>();
		}

		if ((flags & 0x40) != 0) {// stroke direction
			if (version < 500) {
				data.read();
				data.read();
			} else {
				data.read();
				data.read();
			}
		}

		if ((flags2 & 0x02) != 0) {
			readShortInt8(data);// pick stroke direction
		}

		Harmonic harmonic = Harmonic.NONE;
		if (version < 400) {
			if ((flags & 0x04) != 0) {
				harmonic = Harmonic.NORMAL;
			} else if ((flags & 0x08) != 0) {
				harmonic = Harmonic.PINCH;
			}
		}

		return new GPBeatEffects(vibrato, rasgueado, hopo, bassPickingTechnique, tremoloEffects, harmonic);
	}

	private List<GPBend> readBend() {
		data.read(); // type
		readInt32LE(data); // value
		final List<GPBend> effects = new ArrayList<>();

		final int pointCount = readInt32LE(data);
		if (pointCount > 0) {
			for (int i = 0; i < pointCount; i++) {
				final int offset = readInt32LE(data); // 0...60
				final int value = readInt32LE(data); // % of full steps
				final boolean vibrato = readBoolean(data); // vibrato
				effects.add(new GPBend(offset, value, vibrato));
			}
		}

		return effects;
	}

	private int readMixTableChange() {
		readShortInt8(data);// instrument
		if (version >= 500) {
			data.skip(16); // Rse Info
		}
		final int volume = readShortInt8(data);
		final int balance = readShortInt8(data);
		final int chorus = readShortInt8(data);
		final int reverb = readShortInt8(data);
		final int phaser = readShortInt8(data);
		final int tremolo = readShortInt8(data);
		if (version >= 500) {
			readStringWithByteSkip(data);
		}
		final int tempo = readInt32LE(data);
		// durations
		if (volume >= 0) {
			data.read();
		}
		if (balance >= 0) {
			data.read();
		}
		if (chorus >= 0) {
			data.read();
		}
		if (reverb >= 0) {
			data.read();
		}
		if (phaser >= 0) {
			data.read();
		}
		if (tremolo >= 0) {
			data.read();
		}
		if (tempo >= 0) {
			readShortInt8(data);
			if (version >= 510) {
				data.read(); // hideTempo (bool)
			}
		}
		if (version >= 400) {
			data.read(); // all tracks flag
		}
		// unknown
		if (version >= 500) {
			data.read();
		}
		// unknown
		if (version >= 510) {
			readStringWithByteSkip(data);
			readStringWithByteSkip(data);
		}

		return tempo;
	}

	private GPNote readNote(final int string) {
		final int flags = data.read();

		if ((flags & 0x01) != 0 && version < 500) {
			data.read(); // duration
			data.read(); // tuplet
		}

		final boolean accent = (flags & 0x02) != 0 || (flags & 0x40) != 0;
		final boolean ghost = (flags & 0x04) != 0;
		boolean dead = false;
		boolean tied = false;
		if ((flags & 0x20) != 0) {
			final int noteType = data.read();
			if (noteType == 3) {
				dead = true;
			} else if (noteType == 2) {
				tied = true;
			}
		}
		if ((flags & 0x10) != 0) {
			readShortInt8(data);// dynamics
		}
		int fret;
		if ((flags & 0x20) != 0) {
			fret = readShortInt8(data);
		} else {
			fret = 0;
		}

		int finger;
		if ((flags & 0x80) != 0) {
			finger = readShortInt8(data);
			readShortInt8(data);// right hand finger
		} else {
			finger = -1;
		}

		double durationPercent = 0;
		if (version >= 500) {
			if ((flags & 0x01) != 0) {
				durationPercent = readDouble(data);
			}

			data.read();// flags 2
		}
		GPNoteEffects effects;
		if ((flags & 0x08) != 0) {
			effects = readNoteEffects();
		} else {
			effects = new GPNoteEffects();
		}

		return new GPNote(string, fret, accent, ghost, dead, tied, finger, durationPercent, effects);
	}

	private GPNoteEffects readNoteEffects() {
		final int flags = data.read();
		int flags2 = 0;
		if (version >= 400) {
			flags2 = data.read();
		}

		final List<GPBend> bends = (flags & 0x01) != 0 ? readBend() : new ArrayList<>();

		GPGraceNote graceNote;
		if ((flags & 0x10) != 0) {
			graceNote = readGrace();
		} else {
			graceNote = null;
		}

		GPDuration tremoloPickingSpeed;
		if ((flags2 & 0x04) != 0) {
			tremoloPickingSpeed = GPDuration.fromValue(data.read());
		} else {
			tremoloPickingSpeed = null;
		}

		GPSlideType slideOut = null;
		GPSlideType slideIn = null;
		if ((flags2 & 0x08) != 0) {
			final GPSlideType[] slides = readSlide();
			slideOut = slides[0];
			slideIn = slides[1];
		} else if (version < 400 && (flags & 0x04) != 0) {
			slideOut = GPSlideType.OUT_WITH_PLUCK;
		}

		Harmonic harmonic;
		if ((flags2 & 0x10) != 0) {
			harmonic = readHarmonic();
		} else {
			harmonic = Harmonic.NONE;
		}

		GPTrill trill;
		if ((flags2 & 0x20) != 0) {
			trill = readTrill();
		} else {
			trill = null;
		}
		final boolean isHammerPullOrigin = (flags & 0x02) != 0;

		final boolean vibrato = (flags2 & 0x40) != 0;
		final boolean staccato = (flags2 & 0x01) != 0;// staccato
		final boolean palmMute = (flags2 & 0x02) != 0;

		return new GPNoteEffects(bends, graceNote, tremoloPickingSpeed, slideOut, slideIn, harmonic, trill,
				isHammerPullOrigin, vibrato, staccato, palmMute);
	}

	private GPGraceNote readGrace() {
		final int fret = readShortInt8(data);
		readShortInt8(data);// dynamics

		final int type = readShortInt8(data);
		final boolean slide = type == 1;
		final boolean legato = type == 3;
		final int durationValue = data.read();
		final GPDuration duration = switch (durationValue) {
		case 1 -> GPDuration.NOTE_64;
		case 2 -> GPDuration.NOTE_32;
		case 3 -> GPDuration.NOTE_16;
		default -> GPDuration.NOTE_64;
		};

		boolean graceBefore = true;
		boolean deadGraceNote = false;
		if (version >= 500) {
			final int flags = data.read();
			graceBefore = (flags & 0x02) == 0;
			deadGraceNote = (flags & 0x01) != 0;
		}

		return new GPGraceNote(fret, duration, slide, graceBefore, deadGraceNote, legato);
	}

	private GPSlideType[] readSlide() {
		final GPSlideType[] types = { null, null };

		if (version >= 500) {
			final int type = readShortInt8(data);
			if ((type & 1) != 0) {
				types[0] = GPSlideType.OUT_WITH_PLUCK;
			} else if ((type & 2) != 0) {
				types[0] = GPSlideType.OUT_WITHOUT_PLUCK;
			} else if ((type & 4) != 0) {
				types[0] = GPSlideType.OUT_DOWN;
			} else if ((type & 8) != 0) {
				types[0] = GPSlideType.OUT_UP;
			}
			if ((type & 16) != 0) {
				types[1] = GPSlideType.IN_FROM_BELOW;
			} else if ((type & 32) != 0) {
				types[1] = GPSlideType.IN_FROM_ABOVE;
			}
		} else {
			final int type = readShortInt8(data);
			switch (type) {
			case 1:
				types[0] = GPSlideType.OUT_WITH_PLUCK;
				break;
			case 2:
				types[0] = GPSlideType.OUT_WITHOUT_PLUCK;
				break;
			case 3:
				types[0] = GPSlideType.OUT_DOWN;
				break;
			case 4:
				types[0] = GPSlideType.OUT_UP;
				break;
			case -1:
				types[1] = GPSlideType.IN_FROM_BELOW;
				break;
			case -2:
				types[1] = GPSlideType.IN_FROM_ABOVE;
				break;
			}
		}

		return types;
	}

	public Harmonic readHarmonic() {
		final int type = data.read();
		if (version >= 500) {
			switch (type) {
			case 1:// natural
				return Harmonic.NORMAL;
			case 2:
				// artificial
				/* let _harmonicTone: number = */ data.read();
				/* let _harmonicKey: number = */ data.read();
				/* let _harmonicOctaveOffset: number = */ data.read();
				return Harmonic.PINCH;
			case 3:// tap
				data.read();// fret
				return Harmonic.NORMAL;
			case 4:// pinch
				return Harmonic.PINCH;
			case 5:// semi
				return Harmonic.NORMAL;
			default:
				return Harmonic.NONE;
			}
		} else if (version >= 400) {
			switch (type) {
			case 1:// natural
				return Harmonic.NORMAL;
			case 3:// tap
				return Harmonic.NORMAL;
			case 4:// pinch
				return Harmonic.PINCH;
			case 5:// semi
				return Harmonic.NORMAL;
			case 15:// artificial
			case 17:// artificial
			case 22:// artificial
				return Harmonic.PINCH;
			default:
				return Harmonic.NONE;
			}
		}

		return Harmonic.NONE;
	}

	public GPTrill readTrill() {
		final int value = data.read();
		final GPDuration speed = switch (data.read()) {
		case 1 -> GPDuration.NOTE_16;
		case 2 -> GPDuration.NOTE_32;
		case 3 -> GPDuration.NOTE_64;
		default -> GPDuration.NOTE_16;
		};

		return new GPTrill(value, speed);
	}
}
