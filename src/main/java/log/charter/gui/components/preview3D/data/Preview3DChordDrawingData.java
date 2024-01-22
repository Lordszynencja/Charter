package log.charter.gui.components.preview3D.data;

import log.charter.song.enums.Mute;
import log.charter.song.notes.Position;

public class Preview3DChordDrawingData extends Position {
	public final Mute mute;

	public Preview3DChordDrawingData(final int position, final Mute mute) {
		super(position);
		this.mute = mute;
	}

}
