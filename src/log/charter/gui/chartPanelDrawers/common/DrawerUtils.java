package log.charter.gui.chartPanelDrawers.common;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.util.Utils.getStringPosition;

public class DrawerUtils {
	public static final int sectionNamesY = 10;
	public static final int lyricLinesY = 30;

	public static final int beatTextY = 50;
	public static final int beatSizeTextY = beatTextY + 15;
	public static final int anchorY = beatSizeTextY + 15;
	public static final int anchorTextY = anchorY + 10;
	public static final int lanesTop = anchorTextY + 15;
	public static final int lanesBottom = lanesTop + 300;
	public static final int handShapesY = lanesBottom + 30;

	public static final int HEIGHT = handShapesY + 20;

	public static final int lanesHeight = lanesBottom - lanesTop;

	public static int getAsOdd(final int x) {
		return x % 2 == 0 ? x + 1 : x;
	}

	public static int getLaneSize(final int lanes) {
		return getAsOdd((int) (DrawerUtils.lanesHeight * 0.8 / lanes));
	}

	public static int getLaneY(final int lane, final int lanesNo) {
		final int lanePositionInLanes = (int) (lanesHeight * (getStringPosition(lane, lanesNo) + 0.5) / lanesNo);
		return lanesTop + lanePositionInLanes;
	}

	public static int yToLane(final double y, final int lanesNo) {
		final int lane = getStringPosition((int) ((y - lanesTop) * lanesNo / lanesHeight), lanesNo);
		return max(0, min(lanesNo - 1, lane));
	}

	public static boolean isInLanes(final int y) {
		return (y >= lanesTop) && (y <= lanesBottom);
	}

	public static boolean isInTempos(final int y) {
		return (y >= beatTextY) && (y < lanesTop);
	}
}
