package log.charter.io.rsc.xml.converters;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.BendValue;
import log.charter.data.song.position.FractionalPosition;

public class BendValuesConverter {
	public static class TemporaryBendValue extends BendValue {
		private final int positionInNote;

		public TemporaryBendValue(final int positionInNote) {
			this.positionInNote = positionInNote;
		}

		public BendValue transform(final ImmutableBeatsMap beats, final int notePosition) {
			position(FractionalPosition.fromTimeRounded(beats, notePosition + positionInNote));

			return new BendValue(this);
		}
	}

	public static String convertToString(final List<BendValue> bendValues) {
		if (bendValues.isEmpty()) {
			return null;
		}

		return bendValues.stream()//
				.map(bendValue -> bendValue.position().asString() + "=" + bendValue.bendValue.toString())//
				.collect(Collectors.joining(";"));
	}

	private static BendValue generateBendValueFromPosition(final String positionString) {
		if (positionString.matches("[0-9]* [0-9]*/[0-9]*")) {
			return new BendValue(FractionalPosition.fromString(positionString));
		}

		return new TemporaryBendValue(Integer.valueOf(positionString));
	}

	public static List<BendValue> convertFromString(final String bendValuesString) {
		if (bendValuesString == null) {
			return new ArrayList<>();
		}

		final String[] bendValueStrings = bendValuesString.split(";");
		final List<BendValue> bendValues = new ArrayList<>(bendValueStrings.length);

		for (final String bendValueString : bendValueStrings) {
			final String[] pairValues = bendValueString.split("=");
			final BendValue bendValue = generateBendValueFromPosition(pairValues[0]);
			bendValue.bendValue = new BigDecimal(pairValues[1]);

			bendValues.add(new BendValue(bendValue));
		}

		return bendValues;
	}
}
