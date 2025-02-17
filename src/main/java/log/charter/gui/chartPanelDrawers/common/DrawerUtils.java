package log.charter.gui.chartPanelDrawers.common;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.data.config.GraphicalConfig.chartMapHeightMultiplier;
import static log.charter.data.config.GraphicalConfig.chordHeight;
import static log.charter.data.config.GraphicalConfig.eventsChangeHeight;
import static log.charter.data.config.GraphicalConfig.fhpInfoHeight;
import static log.charter.data.config.GraphicalConfig.handShapesHeight;
import static log.charter.data.config.GraphicalConfig.noteHeight;
import static log.charter.data.config.GraphicalConfig.timingHeight;
import static log.charter.data.config.GraphicalConfig.toneChangeHeight;
import static log.charter.util.Utils.getStringPosition;

import log.charter.data.config.Config;
import log.charter.gui.chartPanelDrawers.instruments.VocalsDrawer;
import log.charter.gui.chartPanelDrawers.instruments.guitar.GuitarDrawer;
import log.charter.services.editModes.EditMode;

public class DrawerUtils {
	public static int beatTextY = 15;
	public static int beatSizeTextY = beatTextY + 15;
	public static final int lyricLinesY = beatSizeTextY + 15;
	public static final int sectionNamesY = lyricLinesY + eventsChangeHeight + 15;
	public static final int phraseNamesY = sectionNamesY + eventsChangeHeight + 10;
	public static final int eventNamesY = phraseNamesY + eventsChangeHeight + 10;
	public static int toneChangeY = eventNamesY + toneChangeHeight + 10;
	public static int fhpY = toneChangeY + toneChangeHeight + 10;
	public static int lanesTop = fhpY + fhpInfoHeight + chordHeight + 2;
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

	private static void setEditAreaSizesForVocals() {
		lanesTop = beatSizeTextY + 15;
		laneHeight = (int) (noteHeight * 7.5);
		tailHeight = noteHeight;
		lanesHeight = laneHeight;
		lanesBottom = lanesTop + lanesHeight;
		timingY = lanesBottom;
		editAreaHeight = timingY + timingHeight;

		chartMapHeight = chartMapHeightMultiplier * 5;
	}

	private static void setEditAreaSizesForGuitar(final boolean bass, final int strings) {
		fhpY = toneChangeY + toneChangeHeight + 10;
		lanesTop = fhpY + fhpInfoHeight + chordHeight + 2;
		laneHeight = (int) (noteHeight * (bass ? 2 : 1.5));
		tailHeight = getAsOdd(noteHeight * 3 / 4);
		lanesHeight = laneHeight * strings;
		lanesBottom = lanesTop + lanesHeight;
		timingY = lanesBottom + handShapesHeight;
		editAreaHeight = timingY + timingHeight;

		chartMapHeight = 2 * chartMapHeightMultiplier + 1 + Config.instrument.maxStrings * chartMapHeightMultiplier;
	}

	public static void updateEditAreaSizes(final EditMode editMode, final boolean bass, final int strings) {
		switch (editMode) {
			case GUITAR -> setEditAreaSizesForGuitar(bass, strings);
			case TEMPO_MAP -> setEditAreaSizesForGuitar(bass, strings);
			default -> setEditAreaSizesForVocals();
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
