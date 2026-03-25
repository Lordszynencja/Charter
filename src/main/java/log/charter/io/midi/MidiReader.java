package log.charter.io.midi;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;

import log.charter.data.song.BeatsMap;
import log.charter.data.song.vocals.Vocal;

public final class MidiReader {
	public static class MidiFileData {
		public BeatsMap beats;
		public List<Vocal> vocals = null;

		public MidiFileData(final BeatsMap beats) {
			this.beats = beats;
		}

		public void vocals(final List<Vocal> vocals) {
			this.vocals = vocals;
		}

		public boolean isEmpty() {
			return beats == null && vocals == null;
		}
	}

	private static void addVocalsIfAvailable(final Sequence seq, final MidiFileData midiFileData) {
		final List<Vocal> vocals = MidiVocalsReader.read(seq);
		if (vocals != null) {
			midiFileData.vocals(vocals);
		}
	}

	public static MidiFileData readBeatsMapFromMidi(final String path) throws InvalidMidiDataException, IOException {
		final Sequence sequence = MidiSystem.getSequence(new File(path));

		final MidiFileData midiFileData = new MidiFileData(MidiBeatsMapReader.readBeatsMap(sequence));
		addVocalsIfAvailable(sequence, midiFileData);

		return midiFileData;
	}

}
