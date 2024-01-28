package log.charter.gui.chartPanelDrawers.common;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.data.config.Config.chartMapHeightMultiplier;
import static log.charter.data.config.Config.maxStrings;
import static log.charter.data.config.Config.noteHeight;
import static log.charter.util.Utils.getStringPosition;

import log.charter.data.config.Config;
import log.charter.data.managers.modes.EditMode;
import log.charter.io.rs.xml.song.ArrangementType;

public class DrawerUtils {
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
	public static int editAreaHeight;

	public static int chartMapHeight = 20;

	public static int tailHeight;

	static {
		updateEditAreaSizes(EditMode.TEMPO_MAP, null, 1);
	}

	private static void setEditAreaSizesForTempoMap() {
		laneHeight = 100;
		tailHeight = 100;
		lanesHeight = 100;
		lanesBottom = lanesTop + lanesHeight;
		handShapesY = lanesBottom;
		editAreaHeight = handShapesY;

		chartMapHeight = 15;
	}

	private static void setEditAreaSizesForVocals() {
		laneHeight = 100;
		tailHeight = 100;
		lanesHeight = 100;
		lanesBottom = lanesTop + lanesHeight;
		handShapesY = lanesBottom;
		editAreaHeight = handShapesY;

		chartMapHeight = Config.chartMapHeightMultiplier * 5;
	}

	private static void setEditAreaSizesForGuitar(final ArrangementType arrangementType, final int strings) {
		laneHeight = (int) (noteHeight * (arrangementType == ArrangementType.Bass ? 2 : 1.5));
		tailHeight = getAsOdd(noteHeight * 3 / 4);
		lanesHeight = laneHeight * strings;
		lanesBottom = lanesTop + lanesHeight;
		handShapesY = lanesBottom + 30;
		editAreaHeight = handShapesY + 20;

		chartMapHeight = 2 * chartMapHeightMultiplier + 1 + maxStrings * chartMapHeightMultiplier;
	}

	public static void updateEditAreaSizes(final EditMode editMode, final ArrangementType arrangementType,
			final int strings) {
		switch (editMode) {
		case GUITAR:
			setEditAreaSizesForGuitar(arrangementType, strings);
			break;
		case VOCALS:
			setEditAreaSizesForVocals();
			break;
		case TEMPO_MAP:
		default:
			setEditAreaSizesForTempoMap();
			break;

		}
	}

	public static int getAsOdd(final int x) {
		return x % 2 == 0 ? x + 1 : x;
	}

	public static int getLaneY(final int lane) {
		return lanesTop + (int) (laneHeight * (lane + 0.5));
	}

	public static int yToString(double y, final int lanesNo) {
		if (y > lanesBottom) {
			y = lanesBottom;
		}
		if (y < lanesTop) {
			y = lanesTop;
		}

		final int lane = getStringPosition((int) ((y - lanesTop) / laneHeight), lanesNo);
		return max(0, min(lanesNo - 1, lane));
	}

	public static boolean isInLanes(final int y) {
		return (y >= lanesTop) && (y <= lanesBottom);
	}

	public static boolean isInTempos(final int y) {
		return (y >= beatTextY) && (y < lanesTop);
	}
}
