package log.charter.sound.effects.pass.high;

import log.charter.sound.effects.Effect;
import uk.me.berndporr.iirj.Butterworth;

public class HighPassFilterButterworth implements Effect {
	private final Butterworth[] channelFilters;

	public HighPassFilterButterworth(final int channels, final int order, final double sampleRate,
			final double frequency) {
		channelFilters = new Butterworth[channels];

		for (int channel = 0; channel < channels; channel++) {
			channelFilters[channel] = new Butterworth();
			channelFilters[channel].highPass(order, sampleRate, frequency);
		}
	}

	@Override
	public float apply(final int channel, final float sample) {
		return (float) channelFilters[channel].filter(sample);
	}
}
