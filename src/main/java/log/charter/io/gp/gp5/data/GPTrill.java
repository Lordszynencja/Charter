package log.charter.io.gp.gp5.data;

public class GPTrill {
	public final int value;
	public final GPDuration speed;

	public GPTrill(final int value, final GPDuration speed) {
		this.value = value;
		this.speed = speed;
	}

	@Override
	public String toString() {
		return "GPTrill [value=" + value + ", speed=" + speed + "]";
	}
}
