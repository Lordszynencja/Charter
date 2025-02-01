package log.charter.sound.effects.pass.low;

import log.charter.sound.effects.Effect;
import uk.me.berndporr.iirj.ChebyshevII;

public class LowPassFilterChebyshevII implements Effect {
	private final ChebyshevII[] channelFilters;

	public LowPassFilterChebyshevII(final int channels, final int order, final double sampleRate,
			final double frequency, final double rippleDb) {
		channelFilters = new ChebyshevII[channels];

		for (int channel = 0; channel < channels; channel++) {
			channelFilters[channel] = new ChebyshevII();
			channelFilters[channel].lowPass(order, sampleRate, frequency, rippleDb);
		}
	}

	@Override
	public float apply(final int channel, final float sample) {
		return (float) channelFilters[channel].filter(sample);
	}
}
