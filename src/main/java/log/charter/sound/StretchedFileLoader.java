package log.charter.sound;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import log.charter.data.config.Config;
import log.charter.io.Logger;
import log.charter.sound.wav.WavLoader;
import log.charter.sound.wav.WavWriter;

public class StretchedFileLoader {
	private static final String tmpFileName = "guitar_tmp.wav";

	private static String getResultFileName(final int speed) {
		return "guitar_" + speed + ".wav";
	}

	private final MusicData musicData;
	private final String dir;
	private final int speed;
	private final File targetFile;
	public MusicData result;

	public StretchedFileLoader(final MusicData musicData, final String dir, final int speed) {
		this.musicData = musicData;
		this.dir = dir;
		this.speed = speed;
		targetFile = new File(dir, getResultFileName(speed));

		new Thread(this::run).start();

	}

	private void run() {
		if (targetFile.exists()) {
			loadResult();
			return;
		}

		createTempFile();
		runRubberBand();
		loadResult();
	}

	private void createTempFile() {
		final File file = new File(dir, tmpFileName);
		if (!file.exists()) {
			WavWriter.write(musicData, file);
		}
	}

	private void runRubberBand(final String[] cmd) throws IOException {
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
				e.printStackTrace();
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
			e.printStackTrace();
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

	private void loadResult() {
		result = WavLoader.load(targetFile);
	}
}
