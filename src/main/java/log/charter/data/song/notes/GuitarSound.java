package log.charter.data.song.notes;

import log.charter.data.song.position.PositionWithLength;

public abstract class GuitarSound extends PositionWithLength {
	public boolean accent = false;
	public boolean ignore = false;
	public boolean passOtherNotes = false;

	public GuitarSound(final int position) {
		super(position);
	}

	public GuitarSound(final int position, final boolean accent, final boolean ignore) {
		super(position);
		this.accent = accent;
		this.ignore = ignore;
	}

	public GuitarSound(final int position, final int length, final boolean accent, final boolean ignore) {
		super(position, length);
		this.accent = accent;
		this.ignore = ignore;
	}

	public GuitarSound(final GuitarSound other) {
		super(other);
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
}
