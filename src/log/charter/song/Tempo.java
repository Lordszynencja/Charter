package log.charter.song;

public class Tempo extends Position {
	public int id;
	public int kbpm;
	public int beats;

	public Tempo(final int id, final double pos, final int kbpm, final boolean sync) {
		super(pos);
		this.id = id;
		this.kbpm = sync ? kbpm : -1;
		beats = sync ? -1 : kbpm;
	}

	public Tempo(final int id, final double pos, final int kbpm, final int beats) {
		super(pos);
		this.id = id;
		this.kbpm = kbpm;
		this.beats = beats;
	}

	public Tempo(final Tempo t) {
		super(t);
		id = t.id;
		kbpm = t.kbpm;
		beats = t.beats;
	}

	@Override
	public String toString() {
		return "Tempo{id: " + id + ", pos: " + pos + ", kbpm: " + kbpm + ", beats: " + beats + "}";
	}
}
