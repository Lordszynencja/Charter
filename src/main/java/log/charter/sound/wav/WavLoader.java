package log.charter.sound.wav;

import java.io.File;

import log.charter.io.Logger;
import log.charter.sound.MusicData;

public class WavLoader {
	public static MusicData load(final File file) {
		try {
			final WavFile wavFile = WavFile.openWavFile(file);
			final int channels = wavFile.getNumChannels();
			final int frames = (int) wavFile.getNumFrames();

			final int[][] sound = new int[channels][(int) wavFile.getNumFrames()];
			wavFile.readFrames(sound, frames);
			wavFile.close();

			return new MusicData(sound, wavFile.getSampleRate());
		} catch (final Exception e) {
			Logger.error("Couldn't load wav " + file.getAbsolutePath(), e);
		}

		return new MusicData(new byte[0], MusicData.DEF_RATE);
	}
}
