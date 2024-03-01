package log.charter.sound.data;

import javax.sound.sampled.AudioFormat;

public abstract class AudioData<T extends AudioData<T>> {
	public abstract AudioFormat format();

	public int channels() {
		return format().getChannels();
	}

	public float frameRate() {
		return format().getFrameRate();
	}

	public float sampleRate() {
		return format().getSampleRate();
	}

	public abstract byte[] getBytes();

	public abstract int msLength();

	public abstract T join(final T other);

	public abstract T cut(final double startTime, final double endTime);

	public abstract T remove(final double time);

	public abstract T volume(final double volume);
}
