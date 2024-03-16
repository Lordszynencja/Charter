package log.charter.sound;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import log.charter.data.config.Config;
import log.charter.io.Logger;
import log.charter.sound.data.AudioDataShort;
import log.charter.sound.wav.WavLoader;
import log.charter.sound.wav.WavWriter;

public class StretchedFileLoader {
	private static interface StoppableProcess {
		void waitForFinish() throws InterruptedException;

		void forceStop();
	}

	private static final StoppableProcess emptyStoppableProcess = new StoppableProcess() {
		@Override
		public void waitForFinish() {
		}

		@Override
		public void forceStop() {
		}
	};

	private static class WindowsStoppableProcess implements StoppableProcess {
		private final Process process;

		public WindowsStoppableProcess(final Process process) {
			this.process = process;
		}

		@Override
		public void waitForFinish() throws InterruptedException {
			final InputStream in = process.getInputStream();
			final InputStream err = process.getErrorStream();

			final byte[] buffer = new byte[1024];
			try {
				while (process.isAlive()) {
					if (in.available() > 0) {
						in.read(buffer);
					}
					if (err.available() > 0) {
						err.read(buffer);
					}

					Thread.sleep(1);
				}
			} catch (final IOException e) {
				forceStop();
			}
		}

		@Override
		public void forceStop() {
			try {
				process.destroyForcibly().waitFor();
			} catch (final InterruptedException e) {
			}
		}

	}

	private static final AtomicInteger stopperIdGenerator = new AtomicInteger(0);
	private static final String tmpFileName = "guitar_tmp.wav";

	private static String getResultFileName(final int speed) {
		return "guitar_" + speed + ".wav";
	}

	private static final Set<Integer> speeds = new HashSet<>();
	private static final Map<Integer, Runnable> stoppers = new HashMap<>();

	public static void removeGeneratedAndClear(final String dir) {
		for (final Runnable stopper : new ArrayList<>(stoppers.values())) {
			stopper.run();
		}
		new File(dir, tmpFileName).delete();

		for (final int speed : speeds) {
			target(dir, speed).delete();
		}
		for (final File oldWav : new File(dir).listFiles(s -> s.getName().matches("guitar_([0-9]+).wav"))) {
			oldWav.delete();
		}

		speeds.clear();
	}

	public static void clear() {
		for (final Runnable stopper : new ArrayList<>(stoppers.values())) {
			stopper.run();
		}
		speeds.clear();
	}

	private static File target(final String dir, final int speed) {
		return new File(dir, getResultFileName(speed));
	}

	private static AudioDataShort load(final File file) {
		if (!file.exists()) {
			return null;
		}

		final AudioDataShort result = WavLoader.load(file);
		if (result == null || result.data.length == 0) {
			Logger.debug("stretched file " + file.getName() + " wasn't created properly, removing it");
			file.delete();
			return null;
		}

		return result;
	}

	private static void createTempFile(final File file, final AudioDataShort musicData) {
		final AudioDataShort result = WavLoader.load(file);
		if (result != null) {
			return;
		}

		WavWriter.write(musicData, file);
	}

	private static StoppableProcess runRubberBandWindows(final File source, final File target, final int speed) {
		final String exe = Config.rubberbandPath;

		final String[] cmd = { exe, "-3", "-t" + (100.0 / speed), source.getAbsolutePath(), target.getAbsolutePath() };

		try {
			final Process process = Runtime.getRuntime().exec(cmd);
			return new WindowsStoppableProcess(process);
		} catch (final IOException e) {
			Logger.error("Couldn't run rubber band!", e);
			return emptyStoppableProcess;
		}
	}

	private static StoppableProcess runRubberBand(final File source, final File target, final int speed) {
		final String os = System.getProperty("os.name").toLowerCase();
		if (os.startsWith("windows")) {
			return runRubberBandWindows(source, target, speed);
		} else if (os.startsWith("mac")) {
			// TODO run on mac
			return emptyStoppableProcess;
		} else {
			// TODO run on linux
			return emptyStoppableProcess;
		}
	}

	private static void onInterrupt(final File target, final StoppableProcess process) {
		Logger.info("stretching audio to " + target.getName() + " didn't finish, deleting file.");
		process.forceStop();
		target.delete();
	}

	private static void startStretching(final File source, final File target, final int speed) {
		Logger.info("started stretching for " + target.getAbsolutePath());
		final int stopperId = stopperIdGenerator.getAndIncrement();
		final StoppableProcess process = runRubberBand(source, target, speed);
		final Thread thread = new Thread(() -> {
			try {
				process.waitForFinish();
			} catch (final InterruptedException e) {
				onInterrupt(target, process);
			}
			stoppers.remove(stopperId);
		});
		thread.start();

		stoppers.put(stopperId, () -> {
			stoppers.remove(stopperId);
			if (!thread.isInterrupted()) {
				thread.interrupt();
				try {
					thread.join();
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}

				if (target.exists()) {
					onInterrupt(target, process);
				}
			}
		});
	}

	private static File getSourceFile(final AudioDataShort musicData, final String dir, final String musicFileName) {
		if (musicFileName.endsWith(".wav")) {
			return new File(dir, musicFileName);
		}

		final File source = new File(dir, tmpFileName);
		createTempFile(source, musicData);

		return source;
	}

	public static AudioDataShort loadStretchedAudio(final AudioDataShort musicData, final String dir,
			final String musicFileName, final int speed) {
		final File target = target(dir, speed);
		final AudioDataShort audio = load(target);
		if (audio != null) {
			return audio;
		}
		if (speeds.contains(speed)) {
			return null;
		}

		speeds.add(speed);

		final File source = getSourceFile(musicData, dir, musicFileName);

		startStretching(source, target, speed);

		return null;
	}
}
