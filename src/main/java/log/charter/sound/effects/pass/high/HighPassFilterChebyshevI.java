package log.charter.sound.effects.pass.high;

import log.charter.sound.effects.Effect;
import uk.me.berndporr.iirj.ChebyshevI;

public class HighPassFilterChebyshevI implements Effect {
	private final ChebyshevI[] channelFilters;

	public HighPassFilterChebyshevI(final int channels, final int order, final double sampleRate,
			final double frequency, final double rippleDb) {
		channelFilters = new ChebyshevI[channels];

		for (int channel = 0; channel < channels; channel++) {
			channelFilters[channel] = new ChebyshevI();
			channelFilters[channel].highPass(order, sampleRate, frequency, rippleDb);
		}
	}

	@Override
	public float apply(final int channel, final float sample) {
		return (float) channelFilters[channel].filter(sample);
	}
}
