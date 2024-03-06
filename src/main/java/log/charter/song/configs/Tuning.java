package log.charter.song.configs;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.data.config.Config.maxStrings;
import static log.charter.util.SoundUtils.soundToFullName;
import static log.charter.util.SoundUtils.soundToSimpleName;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.data.config.Config;
import log.charter.io.rsc.xml.converters.TuningConverter;

@XStreamAlias("tuning")
@XStreamConverter(TuningConverter.class)
public class Tuning {
	private static final int[] standardStringDistances = { -15, -10, -5, 0, 5, 10, 15, 19, 24 };
	private static final int distanceFromC0 = 28;
	private static final int bassDistanceFromC0 = 16;

	public static int getDistanceFromC0(final boolean bass) {
		return bass ? bassDistanceFromC0 : distanceFromC0;
	}

	public static int getStringDistance(final int string, final int strings) {
		final int offset = standardStringDistances.length - max(6, strings);
		return standardStringDistances[string + offset];
	}

	public static int getStringDistanceFromC0(final int string, final int strings, final boolean bass) {
		return getStringDistance(string, strings) + getDistanceFromC0(bass);
	}

	private static int[] getTuningValues(final int[] tuning, final int strings) {
		final int[] fullTuning = getFullTuning(tuning);
		final int[] tuningValues = new int[strings];
		final int offset = maxStrings - max(6, strings);
		for (int i = 0; i < strings; i++) {
			tuningValues[i] = fullTuning[i + offset];
		}

		return tuningValues;
	}

	private static int[] getFullTuning(final int[] tuning) {
		final int[] fullTuning = new int[Config.maxStrings];
		for (int i = 0; i < Config.maxStrings; i++) {
			fullTuning[i] = tuning[tuning.length - 1];
		}

		final int offset = maxStrings - max(6, tuning.length);
		for (int i = 0; i < tuning.length; i++) {
			fullTuning[i + offset] = tuning[i];
		}

		return fullTuning;
	}

	public enum TuningType {
		E_STANDARD("E standard", new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 }), //
		E_DROP_D("E drop D", new int[] { 0, 0, 0, -2, 0, 0, 0, 0, 0 }), //
		E_DROP_C("E drop C", new int[] { 0, 0, 0, -4, 0, 0, 0, 0, 0 }), //
		E_FLAT_STANDARD("Eb standard", new int[] { -1, -1, -1, -1, -1, -1, -1, -1, -1 }), //
		E_FLAT_DROP_D_FLAT("Eb drop Db", new int[] { -1, -1, -1, -3, -1, -1, -1, -1, -1 }), //
		D_STANDARD("D standard", new int[] { -2, -2, -2, -2, -2, -2, -2, -2, -2 }), //
		D_DROP_C("D drop C", new int[] { -2, -2, -2, -4, -2, -2, -2, -2, -2 }), //
		C_SHARP_STANDARD("Db standard", new int[] { -3, -3, -3, -3, -3, -3, -3, -3, -3 }), //
		C_SHARP_DROP_B("Db drop B", new int[] { -3, -3, -3, -5, -3, -3, -3, -3, -3 }), //
		C_STANDARD("C standard", new int[] { -4, -4, -4, -4, -4, -4, -4, -4, -4 }), //
		B_STANDARD("B standard", new int[] { -5, -5, -5, -5, -5, -5, -5, -5, -5 }), //
		A_SHARP_STANDARD("Bb standard", new int[] { -6, -6, -6, -6, -6, -6, -6, -6, -6 }), //
		A_STANDARD("A standard", new int[] { -7, -7, -7, -7, -7, -7, -7, -7, -7 }), //
		G_SHARP_STANDARD("Ab standard", new int[] { -8, -8, -8, -8, -8, -8, -8, -8, -8 }), //
		G_STANDARD("G standard", new int[] { -9, -9, -9, -9, -9, -9, -9, -9, -9 }), //
		F_SHARP_STANDARD("Gb standard", new int[] { -10, -10, -10, -10, -10, -10, -10, -10, -10 }), //
		F_STANDARD("Gb standard", new int[] { -11, -11, -11, -11, -11, -11, -11, -11, -11 }), //
		OPEN_D("Open D", new int[] { 0, 0, 0, -2, 0, 0, -1, -2, -2 }), //
		OPEN_E("Open E", new int[] { 0, 0, 0, 0, 2, 2, 1, 0, 0 }), //
		OPEN_G("Open G", new int[] { 0, 0, 0, -2, -2, 0, 0, 0, -2 }), //
		OPEN_A("Open A", new int[] { 0, 0, 0, 0, 0, 2, 2, 2, 0 }), //
		DADGAD("DADGAD", new int[] { 0, 0, 0, -2, 0, 0, 0, -2, -2 }), //
		CUSTOM("Custom", new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 });

		public final String name;
		public final int[] tuning;

		private TuningType(final String name, final int[] tuning) {
			this.name = name;
			this.tuning = tuning;
		}

		public String nameWithValues(final int strings) {
			final int baseValue = tuning[0];
			boolean singleValue = true;
			for (int i = 0; i < tuning.length; i++) {
				if (tuning[i] != baseValue) {
					singleValue = false;
				}
			}
			if (singleValue) {
				return name + " (" + baseValue + ")";
			}

			final List<String> values = new ArrayList<>();
			for (final int tuningValue : getTuningValues(tuning, strings)) {
				values.add(tuningValue + "");
			}
			return name + " (" + String.join(",", values) + ")";
		}

		public boolean isFullTuning(final int[] tuning) {
			if (tuning.length != Config.maxStrings) {
				return isFullTuning(getFullTuning(tuning));
			}

			for (int i = 0; i < tuning.length; i++) {
				if (tuning[i] != this.tuning[i]) {
					return false;
				}
			}

			return true;
		}

		private static TuningType fromStandardOrDropTuning(final int[] tuning) {
			final int dropString = tuning.length - min(6, tuning.length);
			final int lastStringTuning = tuning[tuning.length - 1];
			for (int string = 0; string < tuning.length - 1; string++) {
				if (string != dropString && tuning[string] != lastStringTuning) {
					return null;
				}
			}

			final int droppedStringTuning = tuning[dropString];
			return switch (droppedStringTuning - lastStringTuning) {
				case 0 -> switch (lastStringTuning) {
					case 0 -> E_STANDARD;
					case -1 -> E_FLAT_STANDARD;
					case -2 -> D_STANDARD;
					case -3 -> C_SHARP_STANDARD;
					case -4 -> C_STANDARD;
					case -5 -> B_STANDARD;
					case -6 -> A_SHARP_STANDARD;
					case -7 -> A_STANDARD;
					case -8 -> G_SHARP_STANDARD;
					case -9 -> G_STANDARD;
					case -10 -> F_SHARP_STANDARD;
					case -11 -> F_STANDARD;
					default -> CUSTOM;
				};
				case 2 -> switch (lastStringTuning) {
					case 0 -> E_DROP_D;
					case -1 -> E_FLAT_DROP_D_FLAT;
					case -2 -> D_DROP_C;
					case -3 -> C_SHARP_DROP_B;
					default -> CUSTOM;
				};
				case 4 -> switch (lastStringTuning) {
					case 0 -> E_DROP_C;
					default -> CUSTOM;
				};
				default -> CUSTOM;
			};
		}

		private static boolean isOpenD(final int[] tuning) {
			if (tuning.length > 6) {
				return false;
			}

			for (int string = 0; string < tuning.length; string++) {
				if (tuning[string] != OPEN_D.tuning[3 + string]) {
					return false;
				}
			}

			return true;
		}

		private static boolean isOpenG(final int[] tuning) {
			if (tuning.length > 6) {
				return false;
			}

			for (int string = 0; string < tuning.length; string++) {
				if (tuning[string] != OPEN_G.tuning[3 + string]) {
					return false;
				}
			}

			return true;
		}

		public static TuningType fromTuning(final int[] tuning) {
			final TuningType type = fromStandardOrDropTuning(tuning);
			if (type != null) {
				return type;
			}
			if (isOpenD(tuning)) {
				return OPEN_D;
			}
			if (isOpenG(tuning)) {
				return OPEN_G;
			}

			return CUSTOM;
		}
	}

	public TuningType tuningType = TuningType.E_STANDARD;
	private int strings = 6;
	private int[] tuning = new int[strings];

	public int strings() {
		return strings;
	}

	public void strings(final int newStrings) {
		if (tuningType == TuningType.CUSTOM) {
			tuning = getTuningValues(tuning, newStrings);
		}

		strings = newStrings;
	}

	public Tuning() {
	}

	public Tuning(final TuningType tuningType, final int strings) {
		this.tuningType = tuningType;
		this.strings = strings;
		tuning = new int[strings];
	}

	public Tuning(final TuningType tuningType, final int strings, final int[] tuning) {
		this.tuningType = tuningType;
		this.strings = strings;
		this.tuning = getTuningValues(tuning, strings);
	}

	public Tuning(final Tuning other) {
		tuningType = other.tuningType;
		strings = other.strings;
		tuning = getTuningValues(other.tuning, strings);
	}

	public int[] getTuningRaw() {
		return tuning;
	}

	public int[] getTuning(final int strings) {
		return getTuningValues(tuningType == TuningType.CUSTOM ? tuning : tuningType.tuning, strings);
	}

	public int[] getTuning() {
		return getTuning(strings);
	}

	public void changeTuning(final int string, final int tuningValue) {
		tuning[string] = tuningValue;
		tuningType = TuningType.fromTuning(getTuningValues(tuning, strings));
	}

	public int getStringOffset(final int string) {
		final int[] tuning = getTuning();
		if (string < 0 || string >= tuning.length) {
			return 0;
		}

		return getStringDistance(string, strings) + tuning[string];
	}

	public String[] getStringNames(final boolean withScaleNumber, final boolean bass) {
		final String[] stringSoundNames = new String[strings];
		final int[] tuning = getTuning();

		for (int string = 0; string < strings; string++) {
			final int distanceFromC0 = getStringDistanceFromC0(string, strings, bass) + tuning[string];
			stringSoundNames[string] = withScaleNumber ? soundToFullName(distanceFromC0, false)
					: soundToSimpleName(distanceFromC0, false);
		}

		return stringSoundNames;
	}

	public String getFullName(final String format, final boolean bass) {
		final String[] stringSoundNames = getStringNames(true, bass);
		return format.formatted(tuningType.name, String.join(" ", stringSoundNames));
	}
}
