package log.charter.sound;

import java.io.File;
import java.util.function.BiConsumer;
import java.util.function.Function;

import log.charter.sound.data.AudioDataShort;
import log.charter.sound.mp3.Mp3Loader;
import log.charter.sound.ogg.OggLoader;
import log.charter.sound.ogg.OggWriter;
import log.charter.sound.wav.WavLoader;
import log.charter.sound.wav.WavWriter;

public enum SoundFileType {
	WAV("Wav", "wav", WavLoader::load, WavWriter::write), //
	OGG("Ogg", "ogg", OggLoader::load, OggWriter::write), //
	MP3("MP3", "mp3", Mp3Loader::load, null);

	public static SoundFileType fromExtension(final String extension) {
		return switch (extension) {
			case "wav" -> WAV;
			case "ogg" -> WAV;
			case "mp3" -> WAV;
			default -> null;
		};
	}

	public final String name;
	public final String extension;
	public final Function<File, AudioDataShort> loader;
	public final BiConsumer<AudioDataShort, File> writer;

	private SoundFileType(final String name, final String extension, final Function<File, AudioDataShort> loader,
			final BiConsumer<AudioDataShort, File> writer) {
		this.name = name;
		this.extension = extension;
		this.loader = loader;
		this.writer = writer;
	}

}
