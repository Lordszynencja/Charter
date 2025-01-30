package log.charter.sound.audioFormats.wav;

import java.io.File;

import log.charter.sound.data.AudioData;

public class WavLoader {
	public static AudioData load(final File file) {
		try {
			final WavFile wavFile = WavFile.openWavFile(file);
			final int channels = wavFile.getNumChannels();
			final int frames = (int) wavFile.getNumFrames();

			final int[][] sound = new int[channels][(int) wavFile.getNumFrames()];
			wavFile.readFrames(sound, frames);
			wavFile.close();

			return new AudioData(sound, wavFile.getSampleRate(), wavFile.getBytesPerSample());
		} catch (final Exception e) {
			return null;
		}
	}
}
