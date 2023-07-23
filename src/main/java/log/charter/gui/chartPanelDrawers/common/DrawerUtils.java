package log.charter.gui.chartPanelDrawers.common;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.data.config.Config.maxStrings;
import static log.charter.data.config.Config.noteHeight;
import static log.charter.util.Utils.getStringPosition;

public class DrawerUtils {
	public static final int toolbarY = 0;
	public static final int editorY = toolbarY + 22;

	public static final int sectionNamesY = 5;
	public static final int phraseNamesY = sectionNamesY + 15;
	public static final int eventNamesY = phraseNamesY + 15;
	public static final int lyricLinesY = eventNamesY + 20;
	public static int beatTextY = lyricLinesY + 15;
	public static int beatSizeTextY = beatTextY + 15;
	public static int toneChangeY = beatSizeTextY + 10;
	public static int anchorY = toneChangeY + 15;
	public static int anchorTextY = anchorY + 11;
	public static int lanesTop = anchorTextY + 15;
	public static int laneHeight;
	public static int lanesHeight;
	public static int lanesBottom;
	public static int handShapesY;

	public static int editAreaBottom;
	public static int scrollBarBottom;

	public static int tailHeight;

	static {
		setSizesBasedOnNotesSizes();
	}

	public static void setSizesBasedOnNotesSizes() {
		laneHeight = noteHeight * 3 / 2;
		lanesHeight = laneHeight * maxStrings;
		lanesBottom = lanesTop + lanesHeight;
		handShapesY = lanesBottom + 30;

		editAreaBottom = editorY + handShapesY + 20;
		scrollBarBottom = editAreaBottom + 20;

		tailHeight = getAsOdd(noteHeight * 3 / 4);
	}

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
