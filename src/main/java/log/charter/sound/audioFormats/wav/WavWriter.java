package log.charter.sound.audioFormats.wav;

import java.io.File;

import log.charter.data.config.Localization.Label;
import log.charter.io.Logger;
import log.charter.sound.SoundFileType.WriteProgressHolder;
import log.charter.sound.data.AudioData;
import log.charter.sound.utils.IntSampleUtils;

public class WavWriter {
	private static final int framesToWritePerCycle = 4096;

	public static void write(final AudioData musicData, final File file, final WriteProgressHolder progress) {
		try {
			final int channels = musicData.format.getChannels();
			final int frames = musicData.frames();
			final int sampleSizeInBits = musicData.format.getSampleSizeInBits();
			final float sampleRate = musicData.format.getSampleRate();
			final int[][] samples = IntSampleUtils.readSamples(musicData.data, channels, sampleSizeInBits / 8);

			progress.changeStep(Label.WRITING_WAV_FILE, frames);
			final WavFile wavFile = new WavFile().openForWriting(file, channels, frames, sampleSizeInBits,
					(long) sampleRate);

			int offset = 0;
			while (offset < frames) {
				offset += wavFile.writeFrames(samples, offset, framesToWritePerCycle);
				progress.updateProgress(Label.WRITING_WAV_FILE, offset);
			}

			wavFile.close();
		} catch (final Exception e) {
			Logger.error("Couldn't save wav file " + file.getAbsolutePath(), e);
		}
	}
}
