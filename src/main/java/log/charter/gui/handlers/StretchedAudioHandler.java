package log.charter.gui.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import log.charter.data.managers.CharterContext.Initiable;
import log.charter.io.Logger;
import log.charter.sound.StretchedFileLoader;
import log.charter.sound.data.AudioDataShort;

public class StretchedAudioHandler implements Initiable {
	private String dir;
	private AudioDataShort musicData;

	private List<Integer> speedsToGenerate = new ArrayList<>();
	private Map<Integer, Boolean> readySpeeds = new HashMap<>();

	public void clear() {
		StretchedFileLoader.stopAllProcesses();

		speedsToGenerate = new ArrayList<>();
		readySpeeds = new HashMap<>();
	}

	public void init() {
		clear();

		new Thread(this::run).start();
	}

	public void setData(final String dir, final AudioDataShort musicData) {
		this.dir = dir;
		this.musicData = musicData;
	}

	public AudioDataShort get(final int speed) {
		final AudioDataShort result = new StretchedFileLoader(musicData, dir, speed).quickLoad();
		if (result != null) {
			readySpeeds.put(speed, true);
			synchronized (speedsToGenerate) {
				speedsToGenerate.removeIf(s -> (int) s == speed);
			}

			return result;
		}

		synchronized (speedsToGenerate) {
			speedsToGenerate.add(speed);
		}

		return null;
	}

	public void addSpeedToGenerate(final int speed) {
		if (readySpeeds.getOrDefault(speed, false)) {
			return;
		}

		synchronized (speedsToGenerate) {
			speedsToGenerate.add(speed);
		}
	}

	public void addPrioritySpeedToGenerate(final int speed) {
		if (readySpeeds.getOrDefault(speed, false)) {
			return;
		}

		synchronized (speedsToGenerate) {
			speedsToGenerate.add(0, speed);
		}
	}

	private void generate(final int speed) {
		if (readySpeeds.getOrDefault(speed, false)) {
			synchronized (speedsToGenerate) {
				speedsToGenerate.removeIf(s -> (int) s == speed);
			}

			return;
		}

		final StretchedFileLoader fileLoader = new StretchedFileLoader(musicData, dir, speed);
		if (fileLoader.quickLoad() != null || fileLoader.generate()) {
			readySpeeds.put(speed, true);
			synchronized (speedsToGenerate) {
				speedsToGenerate.removeIf(s -> (int) s == speed);
			}
		}
	}

	private void run() {
		int lastSpeed = -1;
		int tries = 0;

		while (true) {

			if (!speedsToGenerate.isEmpty()) {
				if (speedsToGenerate.get(0) == lastSpeed) {
					tries++;
					if (tries == 10) {
						Logger.error("Couldn't run rubberband, exiting the thread that uses it");
						return;
					}
				} else {
					lastSpeed = speedsToGenerate.get(0);
					tries = 0;
				}
				generate(speedsToGenerate.get(0));
			}

			try {
				Thread.sleep(1000);
			} catch (final InterruptedException e) {
			}
		}
	}
}
