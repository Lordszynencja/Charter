package log.charter.gui.components.preview3D.data;

import log.charter.data.song.enums.Mute;

public class ChordBoxDrawData {
	public final double originalPosition;
	public final double position;
	public final Mute mute;
	public final boolean onlyBox;
	public final boolean withTop;

	public ChordBoxDrawData(final double position, final Mute mute, final boolean onlyBox, final boolean withTop) {
		originalPosition = position;
		this.position = position;
		this.mute = mute;
		this.onlyBox = onlyBox;
		this.withTop = withTop;
	}

	public ChordBoxDrawData(final double position, final ChordBoxDrawData other) {
		originalPosition = other.originalPosition;
		this.position = position;
		mute = other.mute;
		onlyBox = other.onlyBox;
		withTop = other.withTop;
	}
}
