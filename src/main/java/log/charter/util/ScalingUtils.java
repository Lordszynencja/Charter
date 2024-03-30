package log.charter.util;

import log.charter.data.config.Config;
import log.charter.data.config.Zoom;
import log.charter.data.song.position.time.IPosition;

public class ScalingUtils {
	public static int xToPosition(final int x, final int t) {
		return (int) (xToTimeLength(x - Config.markerOffset) + t);
	}

	public static int xToTimeLength(final int x) {
		return (int) (x / Zoom.zoom);
	}

	public static int positionToX(final int length) {
		return (int) (length * Zoom.zoom);
	}

	public static int positionToX(final int pos, final int t) {
		return positionToX(pos) - positionToX(t) + Config.markerOffset;
	}

	public static int positionToX(final IPosition position, final int t) {
		return positionToX(position.position(), t);
	}

	public static int positionToX(final double pos, final int t) {
		return (int) (pos * Zoom.zoom - t * Zoom.zoom) + Config.markerOffset;
	}

	public static int timeToXLength(final int position, final int length) {
		return positionToX(position + length) - positionToX(position);
	}
}
