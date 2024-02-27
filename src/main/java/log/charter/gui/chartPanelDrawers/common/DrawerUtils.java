package log.charter.gui.chartPanelDrawers.common;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.data.config.Config.maxStrings;
import static log.charter.data.config.GraphicalConfig.*;
import static log.charter.util.Utils.getStringPosition;

import log.charter.data.managers.modes.EditMode;
import log.charter.gui.chartPanelDrawers.instruments.VocalsDrawer;
import log.charter.gui.chartPanelDrawers.instruments.guitar.GuitarDrawer;
import log.charter.io.rs.xml.song.ArrangementType;

public class DrawerUtils {
	public static int beatTextY = 15;
	public static int beatSizeTextY = beatTextY + 15;
	public static final int lyricLinesY = beatSizeTextY + 15;
	public static final int sectionNamesY = lyricLinesY + 25;
	public static final int phraseNamesY = sectionNamesY + 15;
	public static final int eventNamesY = phraseNamesY + 15;
	public static int toneChangeY = eventNamesY + 15;
	public static int anchorY = toneChangeY + toneChangeHeight + 15;
	public static int lanesTop = anchorY + anchorInfoHeight + 15;
	public static int laneHeight;
	public static int lanesHeight;
	public static int lanesBottom;
	public static int timingY;
	public static int editAreaHeight;

	public static int chartMapHeight = chartMapHeightMultiplier * 5;

	public static int tailHeight;

	static {
		updateEditAreaSizes(EditMode.TEMPO_MAP, null, 0);
	}

	private static void setEditAreaSizesForTempoMap() {
		lanesTop = beatSizeTextY + 15;
		laneHeight = 100;
		tailHeight = 100;
		lanesHeight = 100;
		lanesBottom = lanesTop + lanesHeight;
		timingY = lanesBottom;
		editAreaHeight = timingY + timingHeight;

		chartMapHeight = chartMapHeightMultiplier * 5;
	}

	private static void setEditAreaSizesForVocals() {
		lanesTop = beatSizeTextY + 15;
		laneHeight = 100;
		tailHeight = 100;
		lanesHeight = 100;
		lanesBottom = lanesTop + lanesHeight;
		timingY = lanesBottom;
		editAreaHeight = timingY + timingHeight;

		chartMapHeight = chartMapHeightMultiplier * 5;
	}

	private static void setEditAreaSizesForGuitar(final ArrangementType arrangementType, final int strings) {
		anchorY = toneChangeY + toneChangeHeight + 15;
		lanesTop = anchorY + anchorInfoHeight + 15;
		laneHeight = (int) (noteHeight * (arrangementType == ArrangementType.Bass ? 2 : 1.5));
		tailHeight = getAsOdd(noteHeight * 3 / 4);
		lanesHeight = laneHeight * strings;
		lanesBottom = lanesTop + lanesHeight;
		timingY = lanesBottom + handShapesHeight;
		editAreaHeight = timingY + timingHeight;

		chartMapHeight = 2 * chartMapHeightMultiplier + 1 + maxStrings * chartMapHeightMultiplier;
	}

	public static void updateEditAreaSizes(final EditMode editMode, final ArrangementType arrangementType,
			final int strings) {
		switch (editMode) {
			case GUITAR -> setEditAreaSizesForGuitar(arrangementType, strings);
			case VOCALS -> setEditAreaSizesForVocals();
			default -> setEditAreaSizesForTempoMap();
		}

		BackgroundDrawer.reloadGraphics();
		GuitarDrawer.reloadGraphics();
		LyricLinesDrawer.reloadGraphics();
		VocalsDrawer.reloadGraphics();
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
