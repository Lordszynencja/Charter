package log.charter.sound.effects;

public class Distortion implements Effect {

	private final float gain;

	public Distortion(final float gain) {
		if (gain < 0) {
			throw new IllegalArgumentException("Gain must be positive");
		}

		this.gain = gain;
	}

	private float clip(final float sample) {
		if (sample < -1) {
			return -1;
		}
		if (sample > 1) {
			return 1;
		}
		return sample;
	}

	@Override
	public float apply(final int channel, float sample) {
		sample = clip(sample);
		if (sample < 0) {
			sample = -sample;
		}
		sample = (float) Math.pow(sample, 1 / (1 + gain));
		return clip(sample);
	}

}
