package log.charter.sound.system.data;

public interface ISoundLine {
	public int write(byte[] bytes);

	void close();

	void stop();

	boolean wantsMoreData();

	boolean stopped();
}