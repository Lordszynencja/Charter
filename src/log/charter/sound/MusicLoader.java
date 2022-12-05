package log.charter.sound;

public class MusicLoader {
	public static MusicData load(final String path) {
		if (path.toLowerCase().endsWith(".mp3"))
			return Mp3Loader.load(path);
		return null;
	}
}
