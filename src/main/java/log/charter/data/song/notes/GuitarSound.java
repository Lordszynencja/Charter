package log.charter.data.song.notes;

import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPositionWithEnd;
import log.charter.data.song.position.fractional.IFractionalPosition;

public abstract class GuitarSound implements IFractionalPosition, IConstantFractionalPositionWithEnd {
	private FractionalPosition position;
	public boolean accent = false;
	public boolean ignore = false;
	public boolean passOtherNotes = false;

	public GuitarSound() {
	}

	public GuitarSound(final FractionalPosition position) {
		this.position = position;
	}

	public GuitarSound(final FractionalPosition position, final boolean accent, final boolean ignore) {
		this.position = position;
		this.accent = accent;
		this.ignore = ignore;
	}

	public GuitarSound(final GuitarSound other) {
		position = other.position;
		accent = other.accent;
		ignore = other.ignore;
		passOtherNotes = other.passOtherNotes;
	}

	public boolean accent() {
		return accent;
	}

	public void accent(final boolean value) {
		accent = value;
	}

	public boolean ignore() {
		return ignore;
	}

	public void ignore(final boolean value) {
		ignore = value;
	}

	public boolean passOtherNotes() {
		return passOtherNotes;
	}

	public void passOtherNotes(final boolean value) {
		passOtherNotes = value;
	}

	@Override
	public FractionalPosition position() {
		return position;
	}

	@Override
	public void position(final FractionalPosition newPosition) {
		position = newPosition;
	}

	@Override
	public boolean isFraction() {
		return true;
	}

	@Override
	public boolean isPosition() {
		return false;
	}
}
