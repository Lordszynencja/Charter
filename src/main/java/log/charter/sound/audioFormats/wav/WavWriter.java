package log.charter.sound.audioFormats.wav;

import java.io.File;

import log.charter.data.config.Localization.Label;
import log.charter.io.Logger;
import log.charter.sound.SoundFileType.WriteProgressHolder;
import log.charter.sound.data.AudioData;

public class WavWriter {
	private static final int framesToWritePerCycle = 4096;

	public static void write(final AudioData musicData, final File file, final WriteProgressHolder progress) {
		try {
			final int samples = musicData.data[0].length;
			progress.changeStep(Label.WRITING_WAV_FILE, samples);
			final WavFile wavFile = WavFile.newWavFile(file, musicData.data.length, musicData.data[0].length,
					musicData.format.getSampleSizeInBits(), (long) musicData.format.getFrameRate());

			int offset = 0;
			while (offset < samples) {
				offset += wavFile.writeFrames(musicData.data, offset, framesToWritePerCycle);
				progress.updateProgress(Label.WRITING_WAV_FILE, offset);
			}
			wavFile.close();
		} catch (final Exception e) {
			Logger.error("Couldn't save wav file " + file.getAbsolutePath(), e);
		}
	}
}
