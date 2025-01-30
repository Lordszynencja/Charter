package log.charter.sound.audioFormats.flac;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import io.nayuki.flac.common.StreamInfo;
import io.nayuki.flac.decode.DataFormatException;
import io.nayuki.flac.decode.FlacDecoder;
import log.charter.io.Logger;
import log.charter.sound.data.AudioData;

public class FlacLoader {
	private static class LoadedData {
		public final StreamInfo streamInfo;
		public final int[][] samples;

		public LoadedData(final StreamInfo streamInfo, final int[][] samples) {
			this.streamInfo = streamInfo;
			this.samples = samples;
		}
	}

	private static void readMetadata(final FlacDecoder decoder) throws IOException {
		while (decoder.readAndHandleMetadataBlock() != null) {
			continue;
		}
	}

	private static int[][] readSamples(final FlacDecoder decoder, final StreamInfo streamInfo) throws IOException {
		final int[][] samples = new int[streamInfo.numChannels][(int) streamInfo.numSamples];

		int offset = 0;
		int length = 1;
		while (length > 0) {
			length = decoder.readAudioBlock(samples, offset);
			offset += length;
		}

		return samples;
	}

	private static LoadedData loadData(final File file) throws IOException {
		StreamInfo streamInfo;
		int[][] samples;
		try (FlacDecoder decoder = new FlacDecoder(file)) {
			readMetadata(decoder);

			streamInfo = decoder.streamInfo;
			if (streamInfo.sampleDepth % 8 != 0) {
				throw new UnsupportedOperationException("Only whole-byte sample depth supported");
			}

			samples = readSamples(decoder, streamInfo);
		}

		final byte[] expectHash = streamInfo.md5Hash;
		if (Arrays.equals(expectHash, new byte[16])) {
			System.err.println("Warning: MD5 hash field was blank");
		} else if (!Arrays.equals(StreamInfo.getMd5Hash(samples, streamInfo.sampleDepth), expectHash)) {
			throw new DataFormatException("MD5 hash check failed");
		}

		return new LoadedData(streamInfo, samples);
	}

	public static AudioData load(final File file) {
		try {
			final LoadedData data = loadData(file);
			int minValue = 0;
			int maxValue = 0;
			for (final int[] channel : data.samples) {
				for (final int sample : channel) {
					if (sample < minValue) {
						minValue = sample;
					}
					if (sample > maxValue) {
						maxValue = sample;
					}
				}
			}

			return new AudioData(data.samples, data.streamInfo.sampleRate, data.streamInfo.sampleDepth / 8);
		} catch (final Exception e) {
			Logger.error("Couldn't read FLAC file " + file.getName(), e);
		}

		return null;
	}
}
