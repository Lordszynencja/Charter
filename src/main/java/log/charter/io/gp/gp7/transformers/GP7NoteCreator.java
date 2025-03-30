package log.charter.io.gp.gp7.transformers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.BendValue;
import log.charter.data.song.notes.NoteInterface;
import log.charter.data.song.position.FractionalPosition;
import log.charter.io.gp.gp7.data.GP7Note;

public class GP7NoteCreator {
	protected final ImmutableBeatsMap beats;

	public GP7NoteCreator(final ImmutableBeatsMap beats) {
		this.beats = beats;
	}

	private BendValue generateBendValue(final FractionalPosition position, final FractionalPosition endPosition,
			final double offset, final double value) {
		final double startTime = position.position(beats);
		final double endTime = endPosition.position(beats);
		final int bendTime = (int) (startTime * (100 - offset) + endTime * offset) / 100;
		final FractionalPosition bendPosition = FractionalPosition.fromTime(beats, bendTime);
		final BigDecimal bendValue = new BigDecimal(value / 25).setScale(2, RoundingMode.HALF_UP);

		return new BendValue(bendPosition, bendValue);
	}

	public void setNoteBend(final GP7Note gp7Note, final NoteInterface note, final FractionalPosition position,
			final FractionalPosition endPosition) {
		if (!gp7Note.bend) {
			return;
		}

		note.endPosition(endPosition);

		final List<BendValue> bendValues = note.bendValues();
		if (gp7Note.bendOriginValue != 0) {
			bendValues.add(generateBendValue(position, endPosition, 0, gp7Note.bendOriginValue));
		}
		if (gp7Note.bendOriginOffset > 0) {
			bendValues.add(generateBendValue(position, endPosition, gp7Note.bendOriginOffset, gp7Note.bendOriginValue));
		}

		bendValues.add(generateBendValue(position, endPosition, gp7Note.bendMiddleOffset1, gp7Note.bendMiddleValue));
		if (gp7Note.bendMiddleOffset2 != gp7Note.bendMiddleOffset1) {
			bendValues
					.add(generateBendValue(position, endPosition, gp7Note.bendMiddleOffset2, gp7Note.bendMiddleValue));
		}

		bendValues.add(
				generateBendValue(position, endPosition, gp7Note.bendDestinationOffset, gp7Note.bendDestinationValue));
	}
}
