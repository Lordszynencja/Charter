package log.charter.song.notes;

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
}
