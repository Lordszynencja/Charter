package log.charter.sound;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import log.charter.data.config.Config;
import log.charter.io.Logger;
import log.charter.sound.data.AudioDataShort;
import log.charter.sound.wav.WavLoader;
import log.charter.sound.wav.WavWriter;

public class StretchedFileLoader {
	private static final AtomicInteger stopperIdGenerator = new AtomicInteger(0);
	private static final String tmpFileName = "guitar_tmp.wav";

	private static String getResultFileName(final int speed) {
		return "guitar_" + speed + ".wav";
	}

	private static final Map<Integer, Runnable> stoppers = new HashMap<>();

	public static void stopAllProcesses() {
		for (final Runnable stopper : new ArrayList<>(stoppers.values())) {
			stopper.run();
		}
	}

	private final AudioDataShort musicData;
	private final String dir;
	private final int speed;
	private final File targetFile;

	public StretchedFileLoader(final AudioDataShort musicData, final String dir, final int speed) {
		this.musicData = musicData;
		this.dir = dir;
		this.speed = speed;
		targetFile = new File(dir, getResultFileName(speed));
	}

	public AudioDataShort quickLoad() {
		if (targetFile.exists()) {
			final AudioDataShort result = loadResult();

			if (result != null && result.data.length > 0) {
				return result;
			}
		}

		return null;
	}

	public boolean generate() {
		if (quickLoad() != null) {
			return true;
		}

		createTempFile();
		runRubberBand();

		return quickLoad() != null;
	}

	private void createTempFile() {
		final File file = new File(dir, tmpFileName);
		if (!file.exists()) {
			WavWriter.write(musicData, file);
		}
	}

	private void runRubberBand(final String[] cmd) throws IOException {
		final Process process = Runtime.getRuntime().exec(cmd);
		final int stopperId = stopperIdGenerator.getAndIncrement();
		stoppers.put(stopperId, () -> {
			stoppers.remove(stopperId);
			process.destroyForcibly();
		});
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
			}
		}
	}

	private void runRubberBandWindows() {
		final String exe = Config.rubberbandPath;
		final String source = new File(dir, tmpFileName).getAbsolutePath();
		final String target = targetFile.getAbsolutePath();

		final String[] cmd = { exe, "-3", "-t" + (100.0 / speed), source, target };

		try {
			runRubberBand(cmd);
		} catch (final IOException e) {
			Logger.error("Couldn't run rubber band!", e);
		}
	}

	private void runRubberBand() {
		final String os = System.getProperty("os.name").toLowerCase();
		if (os.startsWith("windows")) {
			runRubberBandWindows();
		} else if (os.startsWith("mac")) {
			// TODO run on mac
		} else {
			// TODO run on linux
		}
	}

	private AudioDataShort loadResult() {
		return WavLoader.load(targetFile);
	}
}
