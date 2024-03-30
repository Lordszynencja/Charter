package log.charter.sound.mp3;

import static java.util.Arrays.copyOf;
import static javax.sound.sampled.AudioSystem.getAudioInputStream;
import static log.charter.io.Logger.error;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;

import log.charter.sound.data.AudioDataShort;
import log.charter.util.RW;

public class Mp3Loader {
	public static AudioDataShort load(final File path) {
		try {
			final AudioInputStream in = getAudioInputStream(
					new BufferedInputStream(new ByteArrayInputStream(RW.readB(path))));
			final int channels = in.getFormat().getChannels();
			final float rate = in.getFormat().getSampleRate();
			final AudioFormat outFormat = new AudioFormat(Encoding.PCM_SIGNED, rate, 16, channels, 2 * channels, rate,
					false);
			final AudioInputStream formattedIn = getAudioInputStream(outFormat, in);

			final List<byte[]> bytesList = new ArrayList<>();
			int last = 0;
			int length = 0;
			while (last >= 0) {
				final byte[] bytes = new byte[10000];
				last = formattedIn.read(bytes);
				if (last > 0) {
					bytesList.add(copyOf(bytes, last));
					length += last;
				}
			}
			in.close();

			final byte[] buffer = new byte[length];
			last = 0;
			for (final byte[] bytes : bytesList) {
				System.arraycopy(bytes, 0, buffer, last, bytes.length);
				last += bytes.length;
			}

			return new AudioDataShort(buffer, rate, channels, 2);
		} catch (final Exception e) {
			error("Couldnt load mp3 file " + path, e);
		}
		return null;
	}
}
