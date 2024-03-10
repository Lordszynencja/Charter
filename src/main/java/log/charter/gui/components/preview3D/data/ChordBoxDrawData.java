package log.charter.gui.components.preview3D.data;

import log.charter.data.song.enums.Mute;
import log.charter.data.song.position.IConstantPosition;

public class ChordBoxDrawData implements IConstantPosition {
	public final int originalPosition;
	public final int position;
	public final Mute mute;
	public final boolean onlyBox;

	public ChordBoxDrawData(final int position, final Mute mute, final boolean onlyBox) {
		originalPosition = position;
		this.position = position;
		this.mute = mute;
		this.onlyBox = onlyBox;
	}

	public ChordBoxDrawData(final int position, final ChordBoxDrawData other) {
		originalPosition = other.position;
		this.position = position;
		mute = other.mute;
		onlyBox = other.onlyBox;
	}

	@Override
	public int position() {
		return position;
	}

}
