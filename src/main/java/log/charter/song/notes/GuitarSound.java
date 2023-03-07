package log.charter.song.notes;

import log.charter.song.enums.HOPO;
import log.charter.song.enums.Harmonic;
import log.charter.song.enums.Mute;

public class GuitarSound extends PositionWithLength {
	public Mute mute = Mute.NONE;
	public HOPO hopo = HOPO.NONE;
	public Harmonic harmonic = Harmonic.NONE;
	public boolean accent = false;
	public boolean tremolo = false;
	public boolean linkNext = false;
	public Integer slideTo = null;
	public boolean unpitchedSlide = false;
	public boolean ignore = false;

	public GuitarSound(final int position) {
		super(position);
	}

	public GuitarSound(final int position, final int length) {
		super(position, length);
	}

	public GuitarSound(final int position, final int length, final Mute mute, final HOPO hopo, final Harmonic harmonic,
			final boolean accent, final boolean tremolo, final boolean linkNext, final Integer slideTo,
			final boolean unpitchedSlide, final boolean ignore) {
		super(position, length);
		this.mute = mute;
		this.hopo = hopo;
		this.harmonic = harmonic;
		this.accent = accent;
		this.tremolo = tremolo;
		this.linkNext = linkNext;
		this.slideTo = slideTo;
		this.unpitchedSlide = unpitchedSlide;
		this.ignore = ignore;
	}

	public GuitarSound(final GuitarSound other) {
		super(other);
		mute = other.mute;
		hopo = other.hopo;
		harmonic = other.harmonic;
		accent = other.accent;
		tremolo = other.tremolo;
		linkNext = other.linkNext;
		slideTo = other.slideTo;
		unpitchedSlide = other.unpitchedSlide;
		ignore = other.ignore;
	}

}
