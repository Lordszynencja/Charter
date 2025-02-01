package log.charter.sound.system;

import static javax.sound.sampled.AudioSystem.getLine;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import log.charter.data.config.Config;
import log.charter.io.Logger;
import log.charter.sound.system.data.EmptySoundLine;
import log.charter.sound.system.data.ISoundLine;
import log.charter.sound.system.data.ISoundSystem;

public class StandardSoundSystem implements ISoundSystem {
	public class StandardSoundLine implements ISoundLine {
		private final SourceDataLine line;
		private final int maxBytes;
		private boolean stopped = false;

		private StandardSoundLine(final AudioFormat format) throws LineUnavailableException {
			maxBytes = (int) (format.getFrameRate() * format.getFrameSize() * Config.audioBufferMs / 1000);

			final Info info = new Info(SourceDataLine.class, format);

			line = (SourceDataLine) getLine(info);
			line.open(format);

			line.start();
		}

		@Override
		public int write(final byte[] bytes) {
			return line.write(bytes, 0, bytes.length);
		}

		@Override
		public boolean wantsMoreData() {
			final int bufferSize = line.getBufferSize();
			if (bufferSize < maxBytes) {
				return true;
			}

			final int availableBufferLimit = bufferSize - maxBytes;
			return availableBufferLimit <= line.available();
		}

		@Override
		public void close() {
			stopped = true;

			line.drain();
			line.close();
		}

		@Override
		public void stop() {
			stopped = true;

			line.flush();
			line.close();
		}

		@Override
		public boolean stopped() {
			return stopped;
		}

	}

	@Override
	public ISoundLine getNewLine(final AudioFormat format) {
		try {
			return new StandardSoundLine(format);
		} catch (final LineUnavailableException e) {
			Logger.error("Couldn't open standard line", e);
			return new EmptySoundLine();
		}
	}

}
