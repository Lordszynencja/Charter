package log.charter.gui.chartPanelDrawers.common;

import log.charter.data.Config;

public class DrawerUtils {
	public static final int sectionNamesY = 10;
	public static final int lyricLinesY = 30;
	public static final int beatTextY = 50;
	public static final int beatSizeTextY = 65;
	public static final int lanesTop = 80;
	public static final int lanesBottom = lanesTop + 250;
	public static final int handShapesY = lanesBottom + 30;
	public static final int HEIGHT = handShapesY + 20;

	public static final int lanesHeight = lanesBottom - lanesTop;

	public static int applyInvertion(final int lane, final int lanesNo) {
		return Config.invertStrings ? lane : lanesNo - lane - 1;
	}

	public static int getAsOdd(final int x) {
		return x % 2 == 0 ? x + 1 : x;
	}

	public static int getLaneSize(final int lanes) {
		return getAsOdd((int) (DrawerUtils.lanesHeight * 0.8 / lanes));
	}

	public static int getLaneY(final int lane, final int lanesNo) {
		return lanesTop + (int) (lanesHeight * (applyInvertion(lane, lanesNo) + 0.5) / lanesNo);
	}

	public static int yToLane(final double y, final int lanesNo) {
		return applyInvertion((int) ((y - lanesTop) * lanesNo / lanesHeight), lanesNo);
	}

	public static boolean isInLanes(final int y) {
		return (y >= lanesTop) && (y <= lanesBottom);
	}

	public static boolean isInTempos(final int y) {
		return (y >= beatTextY) && (y < lanesTop);
	}
}
