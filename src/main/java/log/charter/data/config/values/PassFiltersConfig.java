package log.charter.data.config.values;

import static log.charter.data.config.values.ValueAccessor.forDouble;
import static log.charter.data.config.values.ValueAccessor.forInteger;
import static log.charter.data.config.values.ValueAccessor.forString;

import java.util.Map;

import log.charter.sound.effects.Effect;
import log.charter.sound.effects.pass.PassFilterAlgorithm;
import log.charter.sound.effects.pass.band.BandPassFilterBessel;
import log.charter.sound.effects.pass.band.BandPassFilterButterworth;
import log.charter.sound.effects.pass.band.BandPassFilterChebyshevI;
import log.charter.sound.effects.pass.band.BandPassFilterChebyshevII;
import log.charter.sound.effects.pass.high.HighPassFilterBessel;
import log.charter.sound.effects.pass.high.HighPassFilterButterworth;
import log.charter.sound.effects.pass.high.HighPassFilterChebyshevI;
import log.charter.sound.effects.pass.high.HighPassFilterChebyshevII;
import log.charter.sound.effects.pass.low.LowPassFilterBessel;
import log.charter.sound.effects.pass.low.LowPassFilterButterworth;
import log.charter.sound.effects.pass.low.LowPassFilterChebyshevI;
import log.charter.sound.effects.pass.low.LowPassFilterChebyshevII;

public class PassFiltersConfig implements ConfigValue {
	public PassFilterAlgorithm lowPassAlgorithm = PassFilterAlgorithm.BESSEL;
	public int lowPassOrder = 2;
	public double lowPassFrequency = 440;
	public double lowPassRippleDb = 1;

	public PassFilterAlgorithm bandPassAlgorithm = PassFilterAlgorithm.BESSEL;
	public int bandPassOrder = 2;
	public double bandPassCenterFrequency = 880;
	public double bandPassFrequencyWidth = 440;
	public double bandPassRippleDb = 1;

	public PassFilterAlgorithm highPassAlgorithm = PassFilterAlgorithm.BESSEL;
	public int highPassOrder = 2;
	public double highPassFrequency = 880;
	public double highPassRippleDb = 1;

	@Override
	public void installValueAccessors(final Map<String, ValueAccessor> valueAccessors, final String name) {
		valueAccessors.put(name + ".lowPassAlgorithm",
				forString(v -> lowPassAlgorithm = PassFilterAlgorithm.valueOf(v), () -> lowPassAlgorithm.name()));
		valueAccessors.put(name + ".lowPassOrder", forInteger(v -> lowPassOrder = v, () -> lowPassOrder));
		valueAccessors.put(name + ".lowPassFrequency", forDouble(v -> lowPassFrequency = v, () -> lowPassFrequency));
		valueAccessors.put(name + ".lowPassRippleDb", forDouble(v -> lowPassRippleDb = v, () -> lowPassRippleDb));

		valueAccessors.put(name + ".bandPassAlgorithm",
				forString(v -> bandPassAlgorithm = PassFilterAlgorithm.valueOf(v), () -> bandPassAlgorithm.name()));
		valueAccessors.put(name + ".bandPassOrder", forInteger(v -> bandPassOrder = v, () -> bandPassOrder));
		valueAccessors.put(name + ".bandPassCenterFrequency",
				forDouble(v -> bandPassCenterFrequency = v, () -> bandPassCenterFrequency));
		valueAccessors.put(name + ".bandPassFrequencyWidth",
				forDouble(v -> bandPassFrequencyWidth = v, () -> bandPassFrequencyWidth));
		valueAccessors.put(name + ".bandPassRippleDb", forDouble(v -> bandPassRippleDb = v, () -> bandPassRippleDb));

		valueAccessors.put(name + ".highPassAlgorithm",
				forString(v -> highPassAlgorithm = PassFilterAlgorithm.valueOf(v), () -> highPassAlgorithm.name()));
		valueAccessors.put(name + ".highPassOrder", forInteger(v -> highPassOrder = v, () -> highPassOrder));
		valueAccessors.put(name + ".highPassFrequency", forDouble(v -> highPassFrequency = v, () -> highPassFrequency));
		valueAccessors.put(name + ".highPassRippleDb", forDouble(v -> highPassRippleDb = v, () -> highPassRippleDb));
	}

	public Effect createLowPassFilter(final int channels, final double sampleRate) {
		return switch (lowPassAlgorithm) {
			case BESSEL -> new LowPassFilterBessel(channels, lowPassOrder, sampleRate, lowPassFrequency);
			case BUTTERWORTH -> new LowPassFilterButterworth(channels, lowPassOrder, sampleRate, lowPassFrequency);
			case CHEBYSHEV_I ->
				new LowPassFilterChebyshevI(channels, lowPassOrder, sampleRate, lowPassFrequency, lowPassRippleDb);
			case CHEBYSHEV_II ->
				new LowPassFilterChebyshevII(channels, lowPassOrder, sampleRate, lowPassFrequency, lowPassRippleDb);
			default -> throw new IllegalArgumentException();
		};
	}

	public Effect createBandPassFilter(final int channels, final double sampleRate) {
		return switch (bandPassAlgorithm) {
			case BESSEL -> new BandPassFilterBessel(channels, bandPassOrder, sampleRate, bandPassCenterFrequency,
					bandPassFrequencyWidth);
			case BUTTERWORTH -> new BandPassFilterButterworth(channels, bandPassOrder, sampleRate,
					bandPassCenterFrequency, bandPassFrequencyWidth);
			case CHEBYSHEV_I -> new BandPassFilterChebyshevI(channels, bandPassOrder, sampleRate,
					bandPassCenterFrequency, bandPassFrequencyWidth, bandPassRippleDb);
			case CHEBYSHEV_II -> new BandPassFilterChebyshevII(channels, bandPassOrder, sampleRate,
					bandPassCenterFrequency, bandPassFrequencyWidth, bandPassRippleDb);
			default -> throw new IllegalArgumentException();
		};
	}

	public Effect createHighPassFilter(final int channels, final double sampleRate) {
		return switch (highPassAlgorithm) {
			case BESSEL -> new HighPassFilterBessel(channels, highPassOrder, sampleRate, highPassFrequency);
			case BUTTERWORTH -> new HighPassFilterButterworth(channels, highPassOrder, sampleRate, highPassFrequency);
			case CHEBYSHEV_I ->
				new HighPassFilterChebyshevI(channels, highPassOrder, sampleRate, highPassFrequency, highPassRippleDb);
			case CHEBYSHEV_II ->
				new HighPassFilterChebyshevII(channels, highPassOrder, sampleRate, highPassFrequency, highPassRippleDb);
			default -> throw new IllegalArgumentException();
		};
	}

}
