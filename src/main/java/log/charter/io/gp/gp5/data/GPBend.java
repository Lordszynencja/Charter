package log.charter.io.gp.gp5.data;

public class GPBend {
	public final int offset;
	public final int value;
	public final boolean vibrato;

	public GPBend(final int offset, final int value, final boolean vibrato) {
		this.offset = offset;
		this.value = value;
		this.vibrato = vibrato;
	}

	@Override
	public String toString() {
		return "GPBend [offset=" + offset + ", value=" + value + ", vibrato=" + vibrato + "]";
	}

}
