package log.charter.sound;

import log.charter.sound.mp3.Mp3Loader;

public class MusicLoader {
	public static MusicData load(final String path) {
		if (path.toLowerCase().endsWith(".mp3"))
			return Mp3Loader.load(path);
		return null;
	}
}
