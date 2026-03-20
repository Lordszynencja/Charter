package log.charter.sound.audioFormats.wav;

import java.io.File;
import java.io.IOException;

import log.charter.sound.data.AudioData;
import log.charter.sound.data.AudioUtils;

public class WavLoader {
	public static AudioData load(final File file) {
		final WavFile wavFile = new WavFile();
		try {
			wavFile.openForReading(file);
			final int channels = wavFile.getNumChannels();
			final int frames = (int) wavFile.getNumFrames();

			final int[][] sound = new int[channels][frames];
			wavFile.readFrames(sound, frames);
			wavFile.close();

			final int bytesPerSample = wavFile.getBytesPerSample();
			AudioUtils.fixValues(bytesPerSample, sound);

			return new AudioData(sound, wavFile.getSampleRate(), bytesPerSample);
		} catch (final Exception e) {
			try {
				wavFile.close();
			} catch (final IOException e1) {
				e1.printStackTrace();
			}
			return null;
		}
	}
}
