package log.charter.data.config.values;

import static log.charter.data.config.values.accessors.DoubleValueAccessor.forDouble;
import static log.charter.data.config.values.accessors.EnumValueAccessor.forEnum;
import static log.charter.data.config.values.accessors.IntValueAccessor.forInteger;

import java.util.Map;

import log.charter.data.config.values.accessors.ValueAccessor;
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

public class PassFiltersConfig {
	public static PassFilterAlgorithm lowPassAlgorithm = PassFilterAlgorithm.BESSEL;
	public static int lowPassOrder = 2;
	public static double lowPassFrequency = 440;
	public static double lowPassRippleDb = 1;

	public static PassFilterAlgorithm bandPassAlgorithm = PassFilterAlgorithm.BESSEL;
	public static int bandPassOrder = 2;
	public static double bandPassCenterFrequency = 880;
	public static double bandPassFrequencyWidth = 440;
	public static double bandPassRippleDb = 1;

	public static PassFilterAlgorithm highPassAlgorithm = PassFilterAlgorithm.BESSEL;
	public static int highPassOrder = 2;
	public static double highPassFrequency = 880;
	public static double highPassRippleDb = 1;

	public static void init(final Map<String, ValueAccessor> valueAccessors, final String name) {
		valueAccessors.put(name + ".lowPassAlgorithm", forEnum(PassFilterAlgorithm.class, v -> lowPassAlgorithm = v,
				() -> lowPassAlgorithm, lowPassAlgorithm));
		valueAccessors.put(name + ".lowPassOrder", forInteger(v -> lowPassOrder = v, () -> lowPassOrder, lowPassOrder));
		valueAccessors.put(name + ".lowPassFrequency",
				forDouble(v -> lowPassFrequency = v, () -> lowPassFrequency, lowPassFrequency));
		valueAccessors.put(name + ".lowPassRippleDb",
				forDouble(v -> lowPassRippleDb = v, () -> lowPassRippleDb, lowPassRippleDb));

		valueAccessors.put(name + ".bandPassAlgorithm", forEnum(PassFilterAlgorithm.class, v -> bandPassAlgorithm = v,
				() -> bandPassAlgorithm, bandPassAlgorithm));
		valueAccessors.put(name + ".bandPassOrder",
				forInteger(v -> bandPassOrder = v, () -> bandPassOrder, bandPassOrder));
		valueAccessors.put(name + ".bandPassCenterFrequency",
				forDouble(v -> bandPassCenterFrequency = v, () -> bandPassCenterFrequency, bandPassCenterFrequency));
		valueAccessors.put(name + ".bandPassFrequencyWidth",
				forDouble(v -> bandPassFrequencyWidth = v, () -> bandPassFrequencyWidth, bandPassFrequencyWidth));
		valueAccessors.put(name + ".bandPassRippleDb",
				forDouble(v -> bandPassRippleDb = v, () -> bandPassRippleDb, bandPassRippleDb));

		valueAccessors.put(name + ".highPassAlgorithm", forEnum(PassFilterAlgorithm.class, v -> highPassAlgorithm = v,
				() -> highPassAlgorithm, highPassAlgorithm));
		valueAccessors.put(name + ".highPassOrder",
				forInteger(v -> highPassOrder = v, () -> highPassOrder, highPassOrder));
		valueAccessors.put(name + ".highPassFrequency",
				forDouble(v -> highPassFrequency = v, () -> highPassFrequency, highPassFrequency));
		valueAccessors.put(name + ".highPassRippleDb",
				forDouble(v -> highPassRippleDb = v, () -> highPassRippleDb, highPassRippleDb));
	}

	public static Effect createLowPassFilter(final int channels, final double sampleRate) {
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

	public static Effect createBandPassFilter(final int channels, final double sampleRate) {
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

	public static Effect createHighPassFilter(final int channels, final double sampleRate) {
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
