package log.charter.sound;

import java.io.File;
import java.util.function.Function;

import log.charter.data.config.Localization.Label;
import log.charter.gui.components.simple.LoadingDialog;
import log.charter.sound.audioFormats.flac.FlacLoader;
import log.charter.sound.audioFormats.flac.FlacWriter;
import log.charter.sound.audioFormats.mp3.Mp3Loader;
import log.charter.sound.audioFormats.ogg.OggLoader;
import log.charter.sound.audioFormats.ogg.OggWriter;
import log.charter.sound.audioFormats.wav.WavLoader;
import log.charter.sound.audioFormats.wav.WavWriter;
import log.charter.sound.data.AudioData;
import log.charter.util.Utils.TimeFormatter;
import log.charter.util.Utils.TimeUnit;

public enum SoundFileType {
	FLAC("Flac", "flac", FlacLoader::load, FlacWriter::write), //
	MP3("MP3", "mp3", Mp3Loader::load, null), //
	OGG("Ogg", "ogg", OggLoader::load, OggWriter::write), //
	WAV("Wav", "wav", WavLoader::load, WavWriter::write), //
	;

	private interface AudioWriter {
		void write(AudioData data, File file, WriteProgressHolder progress);
	}

	public static class WriteProgressHolder {
		private static final TimeFormatter timeFormatter = new TimeFormatter(TimeUnit.SECONDS)//
				.minUnitShown(TimeUnit.MINUTES).maxUnitShown(TimeUnit.MINUTES);

		public final LoadingDialog loadingDialog;

		public final long startTime = System.currentTimeMillis();
		public long maxProgress = 1;

		public WriteProgressHolder(final LoadingDialog loadingDialog) {
			this.loadingDialog = loadingDialog;
		}

		public int getSecondsSinceStart() {
			return (int) ((System.currentTimeMillis() - startTime) / 1000);
		}

		public void updateProgress(final Label message, final int progress) {
			if (loadingDialog == null) {
				return;
			}

			final String messageString = "<html>" + message.format(timeFormatter.format(getSecondsSinceStart()))
					+ "</html>";
			loadingDialog.setProgress(progress, messageString);
		}

		public void changeStep(final Label message, final int maxProgress) {
			if (loadingDialog == null) {
				return;
			}

			this.maxProgress = maxProgress;

			final String messageString = "<html>" + message.format(timeFormatter.format(getSecondsSinceStart()))
					+ "</html>";

			loadingDialog.changeMaxProgress(maxProgress);
			loadingDialog.setProgress(0, messageString);
		}
	}

	public static SoundFileType fromExtension(final String extension) {
		return switch (extension) {
			case "flac" -> FLAC;
			case "mp3" -> MP3;
			case "ogg" -> OGG;
			case "wav" -> WAV;
			default -> null;
		};
	}

	public final String name;
	public final String extension;
	public final Function<File, AudioData> loader;
	private final AudioWriter writer;

	private SoundFileType(final String name, final String extension, final Function<File, AudioData> loader,
			final AudioWriter writer) {
		this.name = name;
		this.extension = extension;
		this.loader = loader;
		this.writer = writer;
	}

	public void write(final LoadingDialog loadingDialog, final AudioData audioData, final File file) {
		writer.write(audioData, file, new WriteProgressHolder(loadingDialog));
	}

	public boolean canBeWritten() {
		return writer != null;
	}
}
