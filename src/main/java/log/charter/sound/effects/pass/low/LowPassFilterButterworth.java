package log.charter.sound.effects.pass.low;

import log.charter.sound.effects.Effect;
import uk.me.berndporr.iirj.Butterworth;

public class LowPassFilterButterworth implements Effect {
	private final Butterworth[] channelFilters;

	public LowPassFilterButterworth(final int channels, final int order, final double sampleRate,
			final double frequency) {
		channelFilters = new Butterworth[channels];

		for (int channel = 0; channel < channels; channel++) {
			channelFilters[channel] = new Butterworth();
			channelFilters[channel].lowPass(order, sampleRate, frequency);
		}
	}

	@Override
	public float apply(final int channel, final float sample) {
		return (float) channelFilters[channel].filter(sample);
	}
}
