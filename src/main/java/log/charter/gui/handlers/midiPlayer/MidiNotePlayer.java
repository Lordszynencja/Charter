package log.charter.gui.handlers.midiPlayer;

import static log.charter.data.config.Config.sfxVolume;
import static log.charter.song.notes.IConstantPosition.findLastBeforeEqual;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;

import log.charter.data.ChartData;
import log.charter.io.Logger;
import log.charter.io.rs.xml.song.ArrangementType;
import log.charter.song.BendValue;
import log.charter.song.ChordTemplate;
import log.charter.song.ToneChange;
import log.charter.song.enums.Harmonic;
import log.charter.song.enums.Mute;
import log.charter.song.notes.Chord;
import log.charter.song.notes.ChordNote;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.Note;
import log.charter.util.CollectionUtils.ArrayList2;

public class MidiNotePlayer {
	private enum GuitarSoundType {
		CLEAN("Clean Gt."), //
		OVERDRIVE("Overdrive Gt."), //
		DISTORTION("DistortionGt"), //
		MUTE("Muted Gt."), //
		HARMONIC("Gt.Harmonics");

		public final String midiInstrumentName;

		private GuitarSoundType(final String midiInstrumentName) {
			this.midiInstrumentName = midiInstrumentName;
		}

	}

	private static final int baseGuitarNote = 40;
	private static final int baseBassNote = 28;
	private static final int pitchBendBaseValue = 8192;
	private static final int pitchBendRange = 8191;

	private static int getGuitarMidiNote(int string, final int fret, final int strings) {
		string = string - (strings - 6);

		final int stringOffset = switch (string) {
		case -3 -> -14;
		case -2 -> -9;
		case -1 -> -5;
		case 0 -> 0;
		case 1 -> 5;
		case 2 -> 10;
		case 3 -> 15;
		case 4 -> 19;
		case 5 -> 24;
		default -> 0;
		};

		return baseGuitarNote + stringOffset + fret;
	}

	private static int getBassMidiNote(final int string, final int fret, final int strings) {
		final int stringOffset = switch (string) {
		case 0 -> 0;
		case 1 -> 5;
		case 2 -> 10;
		case 3 -> 15;
		case 4 -> 19;
		case 5 -> 24;
		case 6 -> 29;
		default -> 0;
		};

		return baseBassNote + stringOffset + fret;
	}

	private boolean available = true;
	private ChartData data;

	private MidiChannel[] channels;
	private int[] lastNotes;
	private int[] lastActualNotes;
	private final Map<GuitarSoundType, Instrument> instruments = new HashMap<>();

	public void init(final ChartData data) {
		this.data = data;
		try {
			final Synthesizer synthesizer = MidiSystem.getSynthesizer();
			synthesizer.open();

			final Instrument defaultInstrument = synthesizer.getAvailableInstruments()[0];
			for (final GuitarSoundType guitarSoundType : GuitarSoundType.values()) {
				instruments.put(guitarSoundType, defaultInstrument);
			}

			for (final Instrument instrument : synthesizer.getAvailableInstruments()) {
				for (final GuitarSoundType guitarSoundType : GuitarSoundType.values()) {
					if (instrument.getName().startsWith(guitarSoundType.midiInstrumentName)) {
						instruments.put(guitarSoundType, instrument);
					}
				}
			}

			channels = synthesizer.getChannels();
			lastNotes = new int[channels.length];
			lastActualNotes = new int[channels.length];
			for (int i = 0; i < channels.length; i++) {
				channels[i].controlChange(7, 127);
				lastNotes[i] = -1;
				lastActualNotes[i] = -1;
			}
		} catch (final MidiUnavailableException e) {
			available = false;
			Logger.error("Midi unavailable");
		}
	}

	private int getMidiNote(final int string, final int fret, final int strings) {
		final boolean isBass = data.getCurrentArrangement().arrangementType == ArrangementType.Bass || strings < 6;
		return isBass ? getBassMidiNote(string, fret, strings) : getGuitarMidiNote(string, fret, strings);
	}

	private int getPitchBend(double bendStep) {
		if (bendStep < -2) {
			bendStep = -2;
		}
		if (bendStep > 2) {
			bendStep = 2;
		}

		return pitchBendBaseValue + (int) (bendStep * pitchBendRange / 2);
	}

	private void playMidiNote(final GuitarSoundType soundType, final int string, final int note, double bendValue) {
		if (lastNotes[string] != -1) {
			return;
		}

		final MidiChannel channel = channels[string];
		channel.allNotesOff();
		channel.programChange(instruments.get(soundType).getPatch().getProgram());

		int actualNote = note;
		while (bendValue >= 2) {
			bendValue -= 2;
			actualNote += 2;
		}
		channel.setPitchBend(getPitchBend(bendValue));
		channel.noteOn(actualNote, 127);
		lastNotes[string] = note;
		lastActualNotes[string] = actualNote;
	}

	public void updateBend(final int string, final int fret, double bendValue) {
		if (lastNotes[string] == -1) {
			return;
		}

		final MidiChannel channel = channels[string];

		int actualNote = lastNotes[string];
		bendValue += getMidiNote(string, fret, data.currentStrings())
				+ data.getCurrentArrangement().tuning.getTuning()[string] - actualNote;
		while (bendValue >= 2) {
			bendValue -= 2;
			actualNote += 2;
		}
		while (bendValue < 0) {
			bendValue += 2;
			actualNote -= 2;
		}

		final int pitchBend = getPitchBend(bendValue);
		if (lastActualNotes[string] != actualNote) {
			channel.noteOff(lastActualNotes[string]);
			channel.setPitchBend(pitchBend);
			channel.noteOn(actualNote, 96);
			lastActualNotes[string] = actualNote;
		} else {
			channel.setPitchBend(pitchBend);
		}
	}

	public void updateVolume() {
		for (final MidiChannel channel : channels) {
			channel.controlChange(7, (int) (sfxVolume * 127.0));
		}
	}

	private void playSimpleNote(final int string, final int fret, final boolean mute, final boolean harmonic,
			final List<BendValue> bendValues, final String toneName) {
		GuitarSoundType soundType;
		if (mute) {
			soundType = GuitarSoundType.MUTE;
		} else if (harmonic) {
			soundType = GuitarSoundType.HARMONIC;
		} else if (toneName.contains("distortion")) {
			soundType = GuitarSoundType.DISTORTION;
		} else if (toneName.contains("overdrive")) {
			soundType = GuitarSoundType.OVERDRIVE;
		} else {
			soundType = GuitarSoundType.CLEAN;
		}

		final int strings = data.getCurrentArrangement().tuning.strings;
		final int midiNote = getMidiNote(string, fret, strings)
				+ data.getCurrentArrangement().tuning.getTuning()[string];

		double bendValue = 0;
		if (!bendValues.isEmpty()) {
			final BendValue noteBendValue = bendValues.get(0);
			if (noteBendValue.position() == 0) {
				bendValue = noteBendValue.bendValue.doubleValue();
			}
		}
		bendValue += data.getCurrentArrangement().centOffset.multiply(new BigDecimal("0.01")).doubleValue();

		playMidiNote(soundType, string, midiNote, bendValue);
	}

	private String getToneName(final int position) {
		final ToneChange lastToneChange = findLastBeforeEqual(data.getCurrentArrangement().toneChanges, position);
		if (lastToneChange != null) {
			return lastToneChange.toneName;
		}

		return data.getCurrentArrangement().baseTone;
	}

	private void playNote(final Note note) {
		final int string = note.string;
		final int fret = note.fret;
		final boolean mute = note.mute != Mute.NONE;
		final boolean harmonic = note.harmonic != Harmonic.NONE;
		final ArrayList2<BendValue> bendValues = note.bendValues;
		final String toneName = getToneName(note.position());

		playSimpleNote(string, fret, mute, harmonic, bendValues, toneName);
	}

	private void playChord(final Chord chord) {
		final String toneName = getToneName(chord.position());
		final ChordTemplate template = data.getCurrentArrangement().chordTemplates.get(chord.templateId());

		for (final Entry<Integer, ChordNote> chordNoteData : chord.chordNotes.entrySet()) {
			final int string = chordNoteData.getKey();
			final int fret = template.frets.get(string);
			final boolean mute = chordNoteData.getValue().mute != Mute.NONE;
			final boolean harmonic = chordNoteData.getValue().harmonic != Harmonic.NONE;
			final ArrayList2<BendValue> bendValues = chordNoteData.getValue().bendValues;

			playSimpleNote(string, fret, mute, harmonic, bendValues, toneName);
		}
	}

	public void playSound(final ChordOrNote sound) {
		if (!available) {
			return;
		}

		if (sound.isNote()) {
			playNote(sound.note);
		} else {
			playChord(sound.chord);
		}
	}

	public void stopSound(final int string) {
		channels[string].allNotesOff();
		lastNotes[string] = -1;
		lastActualNotes[string] = -1;
	}

	public void stopSound() {
		if (!available) {
			return;
		}

		for (int string = 0; string < channels.length; string++) {
			stopSound(string);
		}
	}
}
