package log.charter.sound.system.data;

public class EmptySoundLine implements ISoundLine {
	@Override
	public int write(final byte[] bytes) {
		return bytes.length;
	}

	@Override
	public void close() {
	}

	@Override
	public void stop() {
	}

	@Override
	public boolean wantsMoreData() {
		return true;
	}

	@Override
	public boolean stopped() {
		return true;
	}

}