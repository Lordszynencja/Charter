package log.charter.sound.effects.pass.band;

import log.charter.sound.effects.Effect;
import uk.me.berndporr.iirj.ChebyshevII;

public class BandPassFilterChebyshevII implements Effect {
	private final ChebyshevII[] channelFilters;

	public BandPassFilterChebyshevII(final int channels, final int order, final double sampleRate,
			final double centerFrequency, final double frequencyWidth, final double rippleDb) {
		channelFilters = new ChebyshevII[channels];

		for (int channel = 0; channel < channels; channel++) {
			channelFilters[channel] = new ChebyshevII();
			channelFilters[channel].bandPass(order, sampleRate, centerFrequency, frequencyWidth, rippleDb);
		}
	}

	@Override
	public float apply(final int channel, final float sample) {
		return (float) channelFilters[channel].filter(sample);
	}
}
