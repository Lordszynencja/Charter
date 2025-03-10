package log.charter.util;

import log.charter.data.config.Config;
import log.charter.data.config.ZoomUtils;
import log.charter.data.song.position.time.IPosition;

public class ScalingUtils {
	public static double xToPosition(final int x, final double t) {
		return (xToTimeLength(x - Config.markerOffset) + t);
	}

	public static double xToTimeLength(final int x) {
		return (x / ZoomUtils.zoom);
	}

	public static int positionToX(final double length) {
		return (int) (length * ZoomUtils.zoom);
	}

	public static int positionToX(final IPosition position, final double t) {
		return positionToX(position.position(), t);
	}

	public static int positionToX(final double pos, final double t) {
		return (int) (pos * ZoomUtils.zoom) - (int) (t * ZoomUtils.zoom) + Config.markerOffset;
	}

	public static int timeToXLength(final double position, final double length) {
		return positionToX(position + length) - positionToX(position);
	}
}
