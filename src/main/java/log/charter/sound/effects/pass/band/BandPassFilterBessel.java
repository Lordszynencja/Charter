package log.charter.sound.effects.pass.band;

import log.charter.sound.effects.Effect;
import uk.me.berndporr.iirj.Bessel;

public class BandPassFilterBessel implements Effect {
	private final Bessel[] channelFilters;

	public BandPassFilterBessel(final int channels, final int order, final double sampleRate,
			final double centerFrequency, final double frequencyWidth) {
		channelFilters = new Bessel[channels];

		for (int channel = 0; channel < channels; channel++) {
			channelFilters[channel] = new Bessel();
			channelFilters[channel].bandPass(order, sampleRate, centerFrequency, frequencyWidth);
		}
	}

	@Override
	public float apply(final int channel, final float sample) {
		return (float) channelFilters[channel].filter(sample);
	}
}
