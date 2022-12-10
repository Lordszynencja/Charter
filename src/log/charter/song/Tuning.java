package log.charter.song;

import java.util.Arrays;

import log.charter.io.rs.xml.song.ArrangementTuning;

public class Tuning {
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

		public boolean isTuning(final int[] tuning) {
			for (int i = 0; i < tuning.length; i++) {
				if (tuning[i] != this.tuning[i]) {
					return false;
				}
			}

			return true;
		}

		public static TuningType fromTuning(final int[] tuning) {
			for (final TuningType type : values()) {
				if (type.isTuning(tuning)) {
					return type;
				}
			}

			return CUSTOM;
		}

	}

	public TuningType tuningType = TuningType.E_STANDARD;
	public int strings = 6;
	public int[] tuning = { 0, 0, 0, 0, 0, 0 };

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

	public Tuning(final int strings, final ArrangementTuning arrangementTuning) {
		tuning = new int[] { //
				arrangementTuning.string0, //
				arrangementTuning.string1, //
				arrangementTuning.string2, //
				arrangementTuning.string3, //
				arrangementTuning.string4, //
				arrangementTuning.string5 };
		tuning = Arrays.copyOf(tuning, strings);
		tuningType = TuningType.fromTuning(tuning);

		strings(strings);
	}

	public void tuning(final TuningType tuningType) {
		this.tuningType = tuningType;
	}

	public void tuning(final int[] tuning) {
		tuningType = TuningType.CUSTOM;
		this.tuning = tuning;
	}

	public int[] getTuning(final int strings) {
		final int[] tuning = Arrays.copyOf(tuningType == TuningType.CUSTOM ? this.tuning : tuningType.tuning, strings);
		for (int i = this.strings; i < strings; i++) {
			tuning[i] = tuning[this.strings - 1];
		}
		return tuning;
	}
}
