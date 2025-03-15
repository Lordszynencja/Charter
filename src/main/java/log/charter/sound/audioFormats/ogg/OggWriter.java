package log.charter.sound.audioFormats.ogg;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import log.charter.data.config.Localization.Label;
import log.charter.io.Logger;
import log.charter.sound.SoundFileType.WriteProgressHolder;
import log.charter.sound.audioFormats.wav.WavWriter;
import log.charter.sound.data.AudioData;
import log.charter.util.RW;

public class OggWriter {
	private static final String oggEncPath = new File(RW.getJarDirectory(),
			"oggenc" + File.separator + "oggenc2.exe").getAbsolutePath();

	public static void write(final AudioData data, final File file, final WriteProgressHolder progress) {
		final File wav = new File(file.getAbsolutePath() + "_tmp_" + System.currentTimeMillis() + ".wav");
		wav.deleteOnExit();

		WavWriter.write(data, wav, progress);
		progress.changeStep(Label.TRANSFORMING_WAV_TO_OGG, 1);
		final AtomicBoolean finished = new AtomicBoolean(false);

		final Thread oggEncThread = new Thread(() -> {
			runOggEnc(wav, file);
			finished.set(true);
		});

		oggEncThread.start();

		while (!finished.get()) {
			try {
				Thread.sleep(1000 - (System.currentTimeMillis() - progress.startTime) % 1000);
			} catch (final InterruptedException e) {
				return;
			}
		}

		progress.updateProgress(Label.TRANSFORMING_WAV_TO_OGG, 1);
		wav.delete();
	}

	private static void runOggEnc(final File input, final File output) {
		final String exe = oggEncPath;
		final String inputPath = input.getAbsolutePath();
		final String outputPath = output.getAbsolutePath();

		final String[] cmd = { exe, "--quiet", "-q 10", "-s", "0", inputPath, "--output=" + outputPath };

		try {
			runCmd(cmd);
		} catch (final IOException e) {
			Logger.error("Couldn't run oggenc!", e);
		}
	}

	private static void runCmd(final String[] cmd) throws IOException {
		final Process process = Runtime.getRuntime().exec(cmd);
		final InputStream in = process.getInputStream();
		final InputStream err = process.getErrorStream();

		final byte[] buffer = new byte[1024];
		while (process.isAlive()) {
			if (in.available() > 0) {
				in.read(buffer);
			}
			if (err.available() > 0) {
				err.read(buffer);
			}

			try {
				Thread.sleep(1);
			} catch (final InterruptedException e) {
				return;
			}
		}
	}
}
