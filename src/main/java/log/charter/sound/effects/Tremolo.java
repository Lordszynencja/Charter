package log.charter.sound.effects;

public class Tremolo implements Effect {
	private final float period;
	private int position;

	public Tremolo(final float period) {
		this.period = period;
	}

	@Override
	public float apply(final int channel, final float sample) {
		position++;
		final float multiplier = (float) Math.sin(position / period * Math.PI);
		return sample * multiplier;
	}

}
