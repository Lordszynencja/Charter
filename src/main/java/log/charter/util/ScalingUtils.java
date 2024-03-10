package log.charter.util;

import log.charter.data.config.Config;
import log.charter.data.config.Zoom;
import log.charter.data.song.position.IPosition;

public class ScalingUtils {
	public static int xToTime(final int x, final int t) {
		return (int) (xToTimeLength(x - Config.markerOffset) + t);
	}

	public static int xToTimeLength(final int x) {
		return (int) (x * pixelTimeLength());
	}

	public static double pixelTimeLength() {
		return 1 / Zoom.zoom;
	}

	public static int timeToX(final int pos, final int t) {
		return timeToXLength(pos) - timeToXLength(t) + Config.markerOffset;
	}

	public static int timeToX(final IPosition position, final int t) {
		return timeToX(position.position(), t);
	}

	public static int timeToX(final double pos, final int t) {
		return (int) (pos * Zoom.zoom - t * Zoom.zoom) + Config.markerOffset;
	}

	public static int timeToXLength(final int length) {
		return (int) (length * Zoom.zoom);
	}

	public static int timeToXLength(final int position, final int length) {
		return timeToXLength(position + length) - timeToXLength(position);
	}
}
