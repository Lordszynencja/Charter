package log.charter.sound.effects;

import log.charter.sound.PassFilter.PassType;
import uk.me.berndporr.iirj.ChebyshevI;

public class TestFilter implements Effect {

	private final ChebyshevI[] lp;

	public TestFilter(final int channels, final int sampleRate, final int frequency, final PassType type) {
		lp = new ChebyshevI[channels];
		final ChebyshevI c = new ChebyshevI();
		c.lowPass(10, sampleRate, frequency, 10);
		for (int channel = 0; channel < channels; channel++) {
			lp[channel] = new ChebyshevI();
			switch (type) {
				case Lowpass -> lp[channel].lowPass(20, sampleRate, frequency, 1);
				case Highpass -> lp[channel].highPass(2, sampleRate, frequency, 1);
				case BAND_PASS -> lp[channel].bandPass(2, sampleRate, frequency, frequency / 10, 10);
				default -> throw new IllegalArgumentException();
			}
		}
	}

	@Override
	public float apply(final int channel, final float sample) {
		return (float) lp[channel].filter(sample);
	}
}
