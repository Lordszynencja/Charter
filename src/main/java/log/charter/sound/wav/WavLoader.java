package log.charter.sound.wav;

import java.io.File;

import log.charter.io.Logger;
import log.charter.sound.data.AudioDataShort;

public class WavLoader {
	public static AudioDataShort load(final File file) {
		try {
			final WavFile wavFile = WavFile.openWavFile(file);
			final int channels = wavFile.getNumChannels();
			final int frames = (int) wavFile.getNumFrames();

			final short[][] sound = new short[channels][(int) wavFile.getNumFrames()];
			wavFile.readFrames(sound, frames);
			wavFile.close();

			return new AudioDataShort(sound, wavFile.getSampleRate());
		} catch (final Exception e) {
			Logger.error("Couldn't load wav " + file.getAbsolutePath() + ", deleting it", e);
			file.delete();
		}

		return null;
	}
}
