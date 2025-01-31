package log.charter.sound.audioFormats.wav;

import java.io.File;

import log.charter.sound.data.AudioData;
import log.charter.sound.data.AudioUtils;

public class WavLoader {
	public static AudioData load(final File file) {
		try {
			final WavFile wavFile = WavFile.openWavFile(file);
			final int channels = wavFile.getNumChannels();
			final int frames = (int) wavFile.getNumFrames();

			final int[][] sound = new int[channels][frames];
			wavFile.readFrames(sound, frames);
			wavFile.close();

			final int bytesPerSample = wavFile.getBytesPerSample();
			AudioUtils.fixValues(bytesPerSample, sound);

			return new AudioData(sound, wavFile.getSampleRate(), bytesPerSample);
		} catch (final Exception e) {
			return null;
		}
	}
}
