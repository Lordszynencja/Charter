package log.charter.sound.wav;

import java.io.File;

import log.charter.io.Logger;
import log.charter.sound.MusicData;

public class WavWriter {
	public static void write(final MusicData musicData, final File file) {
		try {
			final WavFile wavFile = WavFile.newWavFile(file, musicData.outFormat.getChannels(),
					musicData.data[0].length, musicData.outFormat.getSampleSizeInBits(),
					(long) musicData.outFormat.getFrameRate());

			wavFile.writeFrames(musicData.data, musicData.data[0].length);
			wavFile.close();
		} catch (final Exception e) {
			Logger.error("Couldn't save wav file " + file.getAbsolutePath(), e);
		}
	}
}
