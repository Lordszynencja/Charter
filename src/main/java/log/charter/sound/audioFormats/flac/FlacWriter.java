package log.charter.sound.audioFormats.flac;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.atomic.AtomicBoolean;

import io.nayuki.flac.common.StreamInfo;
import io.nayuki.flac.encode.BitOutputStream;
import io.nayuki.flac.encode.FlacEncoder;
import io.nayuki.flac.encode.RandomAccessFileOutputStream;
import io.nayuki.flac.encode.SubframeEncoder.SearchOptions;
import log.charter.data.config.Localization.Label;
import log.charter.io.Logger;
import log.charter.sound.SoundFileType.WriteProgressHolder;
import log.charter.sound.data.AudioData;

public class FlacWriter {
	private static void writeFLACHeader(final BitOutputStream out) throws IOException {
		out.writeInt(32, 0x664C6143);
	}

	private static StreamInfo generateInfo(final AudioData musicData) {
		final StreamInfo info = new StreamInfo();
		info.sampleRate = (int) musicData.format.getSampleRate();
		info.numChannels = musicData.data.length;
		info.sampleDepth = musicData.format.getSampleSizeInBits();
		info.numSamples = musicData.data[0].length;
		info.md5Hash = StreamInfo.getMd5Hash(musicData.data, info.sampleDepth);

		return info;
	}

	private static void writeSamples(final StreamInfo info, final AudioData data, final BitOutputStream out,
			final WriteProgressHolder progress) throws IOException {
		final FlacEncoder encoder = new FlacEncoder(info, SearchOptions.SUBSET_MEDIUM);
		final AtomicBoolean finished = new AtomicBoolean(false);
		new Thread(() -> {
			try {
				encoder.encode(data.data, out);
			} catch (final Exception e) {
				Logger.error("Couldn't write FLAC file", e);
			}
			finished.set(true);
		}).start();
		int lastProgress = 0;
		while (!finished.get()) {
			if (lastProgress == encoder.getCurrentPosition()) {
				try {
					Thread.sleep(10);
				} catch (final InterruptedException e) {
				}

				continue;
			}

			lastProgress = encoder.getCurrentPosition();
			progress.updateProgress(Label.WRITING_FLAC_FILE, lastProgress);
		}
		out.flush();
	}

	public static void write(final AudioData data, final File file, final WriteProgressHolder progress) {
		progress.changeStep(Label.WRITING_FLAC_FILE, data.data[0].length);

		try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
			raf.setLength(0);

			final BitOutputStream out = new BitOutputStream(
					new BufferedOutputStream(new RandomAccessFileOutputStream(raf)));
			writeFLACHeader(out);

			final StreamInfo info = generateInfo(data);
			info.write(true, out);

			writeSamples(info, data, out, progress);

			raf.seek(4);
			info.write(true, out);
			out.flush();
		} catch (final IOException e) {
			Logger.error("Couldn't save FLAC file", e);
		}
	}
}
