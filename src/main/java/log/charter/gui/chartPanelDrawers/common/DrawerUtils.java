package log.charter.gui.chartPanelDrawers.common;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.data.config.GraphicalConfig.chartMapHeightMultiplier;
import static log.charter.data.config.GraphicalConfig.chartTextHeight;
import static log.charter.data.config.GraphicalConfig.noteHeight;
import static log.charter.data.config.GraphicalConfig.timingHeight;
import static log.charter.util.Utils.getStringPosition;

import log.charter.data.config.values.InstrumentConfig;
import log.charter.gui.chartPanelDrawers.instruments.ShowlightsDrawer;
import log.charter.gui.chartPanelDrawers.instruments.VocalsDrawer;
import log.charter.gui.chartPanelDrawers.instruments.guitar.GuitarDrawer;
import log.charter.services.editModes.EditMode;

public class DrawerUtils {
	public static int beatTextY;
	public static int beatSizeTextY;
	public static int lyricLinesY;
	public static int sectionNamesY;
	public static int phraseNamesY;
	public static int eventNamesY;
	public static int toneChangeY;
	public static int fhpY;
	public static int lanesTop;
	public static int laneHeight;
	public static int lanesHeight;
	public static int lanesBottom;
	public static int timingY;
	public static int editAreaHeight;

	public static int chartMapHeight = chartMapHeightMultiplier * 5;

	public static int tailHeight;

	static {
		updateEditAreaSizes(EditMode.TEMPO_MAP, false, 1);
	}

	private static void updateBaseSizes() {
		beatTextY = chartTextHeight * 5 / 4;
		beatSizeTextY = beatTextY + chartTextHeight * 5 / 4;
		lyricLinesY = beatSizeTextY + chartTextHeight * 5 / 4;
		sectionNamesY = lyricLinesY + chartTextHeight * 5 / 2;
		phraseNamesY = sectionNamesY + chartTextHeight * 9 / 5;
		eventNamesY = phraseNamesY + chartTextHeight * 9 / 5;
		toneChangeY = eventNamesY + chartTextHeight * 9 / 5;
		fhpY = toneChangeY + chartTextHeight * 2;
	}

	private static void setEditAreaSizesForGuitar(final boolean bass, final int strings) {
		updateBaseSizes();

		lanesTop = fhpY + chartTextHeight * 3 / 2;
		laneHeight = (int) (noteHeight * (bass ? 2 : 1.5));
		tailHeight = getAsOdd(noteHeight * 3 / 4);
		lanesHeight = laneHeight * strings;
		lanesBottom = lanesTop + lanesHeight + chartTextHeight;
		timingY = lanesBottom + chartTextHeight * 9 / 5;
		editAreaHeight = timingY + timingHeight;

		chartMapHeight = 2 * chartMapHeightMultiplier + 1 + InstrumentConfig.maxStrings * chartMapHeightMultiplier;
	}

	private static void setEditAreaSizesForShowlights() {
		updateBaseSizes();

		lanesTop = lyricLinesY;
		laneHeight = noteHeight * 2;
		tailHeight = noteHeight;
		lanesHeight = laneHeight * 3;
		lanesBottom = lanesTop + lanesHeight;
		timingY = lanesBottom;
		editAreaHeight = timingY + timingHeight;

		chartMapHeight = chartMapHeightMultiplier * 10;
	}

	private static void setEditAreaSizesForVocals() {
		updateBaseSizes();

		lanesTop = lyricLinesY;
		laneHeight = noteHeight * 5;
		tailHeight = noteHeight;
		lanesHeight = laneHeight;
		lanesBottom = lanesTop + lanesHeight;
		timingY = lanesBottom;
		editAreaHeight = timingY + timingHeight;

		chartMapHeight = chartMapHeightMultiplier * 10;
	}

	public static void updateEditAreaSizes(final EditMode editMode, final boolean bass, final int strings) {
		switch (editMode) {
			case GUITAR -> setEditAreaSizesForGuitar(bass, strings);
			case SHOWLIGHTS -> setEditAreaSizesForShowlights();
			case TEMPO_MAP -> setEditAreaSizesForGuitar(bass, strings);
			case VOCALS -> setEditAreaSizesForVocals();
			default -> setEditAreaSizesForGuitar(bass, strings);
		}

		BackgroundDrawer.reloadGraphics();
		BeatsDrawer.reloadGraphics();
		GuitarDrawer.reloadGraphics();
		LyricLinesDrawer.reloadGraphics();
		MarkerDrawer.reloadGraphics();
		ShowlightsDrawer.reloadGraphics();
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
