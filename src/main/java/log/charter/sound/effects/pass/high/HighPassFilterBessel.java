package log.charter.sound.effects.pass.high;

import log.charter.sound.effects.Effect;
import uk.me.berndporr.iirj.Bessel;

public class HighPassFilterBessel implements Effect {
	private final Bessel[] channelFilters;

	public HighPassFilterBessel(final int channels, final int order, final double sampleRate, final double frequency) {
		channelFilters = new Bessel[channels];

		for (int channel = 0; channel < channels; channel++) {
			channelFilters[channel] = new Bessel();
			channelFilters[channel].highPass(order, sampleRate, frequency);
		}
	}

	@Override
	public float apply(final int channel, final float sample) {
		return (float) channelFilters[channel].filter(sample);
	}
}
