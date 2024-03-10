package log.charter.services.data.files;

import static log.charter.io.Logger.debug;

import java.util.HashMap;
import java.util.Map;

import helliker.id3.MP3File;

public class MetaDataUtils {
	public static Map<String, String> extractSongMetaData(final String path) {
		final Map<String, String> data = new HashMap<>();
		try {
			final MP3File mp3File = new MP3File(path);

			try {
				data.put("artist", mp3File.getArtist());
			} catch (final Exception e) {
				data.put("artist", "");
				debug("Couldn't get artist from mp3 tags data", e);
			}
			try {
				data.put("title", mp3File.getTitle());
			} catch (final Exception e) {
				data.put("title", "");
				debug("Couldn't get title from mp3 tags data", e);
			}
			try {
				data.put("album", mp3File.getAlbum());
			} catch (final Exception e) {
				data.put("album", "");
				debug("Couldn't get album from mp3 tags data", e);
			}
			try {
				data.put("year", mp3File.getYear());
			} catch (final Exception e) {
				data.put("year", "");
				debug("Couldn't get year from mp3 tags data", e);
			}
		} catch (final Exception e) {
			debug("Couldn't get mp3 tags data", e);
		}

		return data;
	}
}
