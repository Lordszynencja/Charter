package log.charter.sound.effects.pass.low;

import log.charter.sound.effects.Effect;
import uk.me.berndporr.iirj.Bessel;

public class LowPassFilterBessel implements Effect {
	private final Bessel[] channelFilters;

	public LowPassFilterBessel(final int channels, final int order, final double sampleRate, final double frequency) {
		channelFilters = new Bessel[channels];

		for (int channel = 0; channel < channels; channel++) {
			channelFilters[channel] = new Bessel();
			channelFilters[channel].lowPass(order, sampleRate, frequency);
		}
	}

	@Override
	public float apply(final int channel, final float sample) {
		return (float) channelFilters[channel].filter(sample);
	}
}
