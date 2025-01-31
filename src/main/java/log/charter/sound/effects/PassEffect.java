package log.charter.sound.effects;

import static java.lang.Math.max;
import static java.lang.Math.min;

import log.charter.sound.PassFilter;
import log.charter.sound.PassFilter.PassType;

public class PassEffect implements Effect {
	private final PassFilter[] filters;

	public PassEffect(final int channels, final int sampleRate, final float frequency, final float resonance,
			final PassType type) {
		filters = new PassFilter[channels];
		for (int i = 0; i < channels; i++) {
			filters[i] = new PassFilter(sampleRate, frequency, resonance, type);
		}
	}

	@Override
	public float apply(final int channel, final float sample) {
		if (channel < 0 || channel >= filters.length) {
			return sample;
		}

		return max(0, min(1, filters[channel].update(sample)));
	}
}
