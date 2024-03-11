package log.charter.data.song.position;

public interface IConstantFractionalPosition extends Comparable<IConstantFractionalPosition> {
	public FractionalPosition fractionalPosition();

	@Override
	default int compareTo(final IConstantFractionalPosition o) {
		return fractionalPosition().compareTo(o.fractionalPosition());
	}
}
