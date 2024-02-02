package log.charter.gui.components.preview3D.data;

import log.charter.song.enums.Mute;
import log.charter.song.notes.IConstantPosition;
import log.charter.util.IntRange;

public class ChordBoxDrawData implements IConstantPosition {
	public final int position;
	public final Mute mute;
	public final boolean onlyBox;
	public final IntRange frets;

	public ChordBoxDrawData(final int position, final Mute mute, final boolean onlyBox, final IntRange frets) {
		this.position = position;
		this.mute = mute;
		this.onlyBox = onlyBox;
		this.frets = frets;
	}

	public ChordBoxDrawData(final int position, final ChordBoxDrawData other) {
		this.position = position;
		mute = other.mute;
		onlyBox = other.onlyBox;
		frets = other.frets;
	}

	@Override
	public int position() {
		return position;
	}

}
