package log.charter.sound.audioFormats;

import static log.charter.io.Logger.debug;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.jcodec.common.logging.Logger;

import helliker.id3.MP3File;
import io.nayuki.flac.decode.FlacDecoder;

public class AudioFileMetadata {
	private static AudioFileMetadata tryToReadMpegMetadata(final File file) {
		try {
			final MP3File mp3File = new MP3File(file);
			if (!mp3File.id3v1Exists() && !mp3File.id3v2Exists()) {
				return null;
			}
			final AudioFileMetadata metadata = new AudioFileMetadata();

			try {
				metadata.artist = mp3File.getArtist();
			} catch (final Exception e) {
				debug("Couldn't get artist from mpeg tags data", e);
			}
			try {
				metadata.title = mp3File.getTitle();
			} catch (final Exception e) {
				debug("Couldn't get title from mpeg tags data", e);
			}
			try {
				metadata.album = mp3File.getAlbum();
			} catch (final Exception e) {
				debug("Couldn't get album from mpeg tags data", e);
			}
			try {
				final String year = mp3File.getYear();
				if (year != null && !year.isBlank()) {
					metadata.year = Integer.valueOf(mp3File.getYear());
				}
			} catch (final Exception e) {
				debug("Couldn't get year from mpeg tags data", e);
			}

			return metadata;
		} catch (final Exception e) {
			debug("Couldn't get mpeg tags data", e);
		}

		return null;
	}

	private static byte[] findVorbisComment(final FlacDecoder decoder) throws IOException {
		Object[] metadataBlock;
		while ((metadataBlock = decoder.readAndHandleMetadataBlock()) != null) {
			if ((Integer) metadataBlock[0] == 4) {
				return (byte[]) metadataBlock[1];
			}
		}

		return null;
	}

	private static int readLittleEndian(final byte[] data, final int offset) {
		return ((data[offset + 3] * 256 + data[offset + 2]) * 256 + data[offset + 1]) * 256 + data[offset];
	}

	private static AudioFileMetadata tryToReadVorbisMetadata(final File file) {
		try {
			try (FlacDecoder decoder = new FlacDecoder(file)) {
				final byte[] vorbisComment = findVorbisComment(decoder);
				if (vorbisComment == null) {
					decoder.close();
					return null;
				}

				final int vendorStringLength = readLittleEndian(vorbisComment, 0);
				int offset = 4 + vendorStringLength;
				final int fields = readLittleEndian(vorbisComment, offset);
				offset += 4;
				final AudioFileMetadata metadata = new AudioFileMetadata();

				for (int i = 0; i < fields; i++) {
					final int fieldLength = readLittleEndian(vorbisComment, offset);
					offset += 4;
					final String field = new String(Arrays.copyOfRange(vorbisComment, offset, offset + fieldLength),
							"UTF-8");
					final int equalityPosition = field.indexOf('=');
					final String name = field.substring(0, equalityPosition);
					final String value = field.substring(equalityPosition + 1);

					switch (name) {
						case "ARTIST" -> metadata.artist = value;
						case "TITLE" -> metadata.title = value;
						case "ALBUM" -> metadata.album = value;
						case "YEAR" -> {
							if (value != null && !value.isBlank()) {
								try {
									metadata.year = Integer.valueOf(value);
								} catch (final NumberFormatException e) {
									Logger.error("Wrong year value in metadata: " + value);
								}
							}
						}
					}

					offset += fieldLength;
				}

				decoder.close();

				return metadata;
			}
		} catch (final Throwable t) {
			return null;
		}
	}

	public static AudioFileMetadata readMetadata(final File file) {
		AudioFileMetadata metadata = tryToReadMpegMetadata(file);
		if (metadata != null) {
			return metadata;
		}
		metadata = tryToReadVorbisMetadata(file);
		if (metadata != null) {
			return metadata;
		}

		return new AudioFileMetadata();
	}

	public String artist = "";
	public String title = "";
	public String album = "";
	public Integer year = null;

	@Override
	public String toString() {
		return "AudioFileMetadata [artist=" + artist + ", title=" + title + ", album=" + album + ", year=" + year + "]";
	}
}
