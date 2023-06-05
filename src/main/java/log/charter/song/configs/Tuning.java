package log.charter.song.configs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import log.charter.io.rs.xml.song.ArrangementTuning;

public class Tuning {
	private static final int[] getTuningValues(final int[] tuning, final int strings) {
		final int[] tuningValues = Arrays.copyOf(tuning, strings);
		for (int i = tuning.length; i < strings; i++) {
			tuningValues[i] = tuning[tuning.length - 1];
		}

		return tuningValues;
	}

	private static final int[] standardStringDistances = { 0, 5, 10, 15, 19, 24 };

	public enum TuningType {
		E_STANDARD("E standard", new int[] { 0, 0, 0, 0, 0, 0 }), //
		E_DROP_D("E drop D", new int[] { -2, 0, 0, 0, 0, 0 }), //
		E_DROP_C("E drop C", new int[] { -4, 0, 0, 0, 0, 0 }), //
		E_FLAT_STANDARD("Eb standard", new int[] { -1, -1, -1, -1, -1, -1 }), //
		E_FLAT_DROP_D_FLAT("Eb drop Db", new int[] { -3, -1, -1, -1, -1, -1 }), //
		D_STANDARD("D standard", new int[] { -2, -2, -2, -2, -2, -2 }), //
		D_DROP_C("D drop C", new int[] { -4, -2, -2, -2, -2, -2 }), //
		C_SHARP_STANDARD("C# standard", new int[] { -3, -3, -3, -3, -3, -3 }), //
		C_SHARP_DROP_B("C# drop B", new int[] { -5, -3, -3, -3, -3, -3 }), //
		C_STANDARD("C standard", new int[] { -4, -4, -4, -4, -4, -4 }), //
		B_STANDARD("B standard", new int[] { -5, -5, -5, -5, -5, -5 }), //
		A_SHARP_STANDARD("A# standard", new int[] { -6, -6, -6, -6, -6, -6 }), //
		A_STANDARD("A standard", new int[] { -7, -7, -7, -7, -7, -7 }), //
		G_SHARP_STANDARD("G# standard", new int[] { -8, -8, -8, -8, -8, -8 }), //
		G_STANDARD("G standard", new int[] { -9, -9, -9, -9, -9, -9 }), //
		F_SHARP_STANDARD("F# standard", new int[] { -10, -10, -10, -10, -10, -10 }), //
		F_STANDARD("F# standard", new int[] { -11, -11, -11, -11, -11, -11 }), //
		OPEN_D("Open D", new int[] { -2, 0, 0, -1, -2, -2 }), //
		OPEN_G("Open G", new int[] { -2, -2, 0, 0, 0, -2 }), //
		CUSTOM("Custom", new int[] { 0, 0, 0, 0, 0, 0 });

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

		public boolean isTuning(final int[] tuning) {
			for (int i = 0; i < tuning.length; i++) {
				if (tuning[i] != this.tuning[i]) {
					return false;
				}
			}

			return true;
		}

		public static TuningType fromTuning(final int[] tuning) {
			if (tuning.length <= 6) {
				for (final TuningType type : values()) {
					if (type.isTuning(tuning)) {
						return type;
					}
				}
			}

			return CUSTOM;
		}
	}

	public TuningType tuningType = TuningType.E_STANDARD;
	public int strings = 6;
	public int[] tuning = new int[strings];

	public void strings(final int newStrings) {
		if (newStrings > 6) {
			tuningType = TuningType.CUSTOM;
		}

		if (tuningType == TuningType.CUSTOM) {
			final int[] oldTuning = tuning;
			tuning = new int[newStrings];
			for (int i = 0; i < strings && i < newStrings; i++) {
				tuning[i] = oldTuning[i];
			}
		}

		strings = newStrings;
	}

	public Tuning() {
	}

	public Tuning(final TuningType tuningType) {
		this.tuningType = tuningType;
	}

	public Tuning(final TuningType tuningType, final int strings) {
		this.tuningType = tuningType;
		this.strings = strings;
	}

	public Tuning(final int defaultStrings, final ArrangementTuning arrangementTuning) {
		tuning = new int[] { //
				arrangementTuning.string0, //
				arrangementTuning.string1, //
				arrangementTuning.string2, //
				arrangementTuning.string3, //
				arrangementTuning.string4, //
				arrangementTuning.string5, //
				arrangementTuning.string6, //
				arrangementTuning.string7, //
				arrangementTuning.string8 };
		tuning = Arrays.copyOf(tuning, strings);
		tuningType = TuningType.fromTuning(tuning);

		strings(arrangementTuning.strings == 0 ? defaultStrings : arrangementTuning.strings);
	}

	public Tuning(final Tuning other) {
		tuningType = other.tuningType;
		strings = other.strings;
		tuning = Arrays.copyOf(other.tuning, other.tuning.length);
	}

	public void tuning(final TuningType tuningType) {
		this.tuningType = tuningType;
	}

	public void tuning(final int[] tuning) {
		tuningType = TuningType.CUSTOM;
		this.tuning = tuning;
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

		return standardStringDistances[string] + tuning[string];
	}
}
