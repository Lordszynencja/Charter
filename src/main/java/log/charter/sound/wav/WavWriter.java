package log.charter.sound.wav;

import java.io.File;

import log.charter.io.Logger;
import log.charter.sound.data.AudioDataShort;

public class WavWriter {
	public static void write(final AudioDataShort musicData, final File file) {
		try {
			final WavFile wavFile = WavFile.newWavFile(file, musicData.channels(), musicData.data[0].length,
					musicData.format().getSampleSizeInBits(), (long) musicData.format().getFrameRate());

			wavFile.writeFrames(musicData.data, musicData.data[0].length);
			wavFile.close();
		} catch (final Exception e) {
			Logger.error("Couldn't save wav file " + file.getAbsolutePath(), e);
		}
	}
}
