package log.charter.sound.effects.pass.band;

import log.charter.sound.effects.Effect;
import uk.me.berndporr.iirj.ChebyshevI;

public class BandPassFilterChebyshevI implements Effect {
	private final ChebyshevI[] channelFilters;

	public BandPassFilterChebyshevI(final int channels, final int order, final double sampleRate,
			final double centerFrequency, final double frequencyWidth, final double rippleDb) {
		channelFilters = new ChebyshevI[channels];

		for (int channel = 0; channel < channels; channel++) {
			channelFilters[channel] = new ChebyshevI();
			channelFilters[channel].bandPass(order, sampleRate, centerFrequency, frequencyWidth, rippleDb);
		}
	}

	@Override
	public float apply(final int channel, final float sample) {
		return (float) channelFilters[channel].filter(sample);
	}
}
