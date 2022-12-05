package log.charter.gui;

import static java.util.Arrays.asList;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JPanel;

import log.charter.data.ChartData;
import log.charter.data.Config;
import log.charter.gui.chartPanelDrawers.AudioDrawer;
import log.charter.gui.chartPanelDrawers.BeatsDrawer;
import log.charter.gui.chartPanelDrawers.Drawer;
import log.charter.gui.chartPanelDrawers.HighlightDrawer;
import log.charter.gui.chartPanelDrawers.InstrumentDrawer;
import log.charter.gui.chartPanelDrawers.LanesDrawer;
import log.charter.gui.chartPanelDrawers.SectionsDrawer;
import log.charter.gui.chartPanelDrawers.SelectedNotesDrawer;
import log.charter.gui.handlers.CharterFrameMouseMotionListener;
import log.charter.io.Logger;
import log.charter.util.RW;

public class ChartPanel extends JPanel {
	private static final List<Drawer> DRAWERS = asList(//
			new SectionsDrawer(), //
			new AudioDrawer(), //
			new LanesDrawer(), //
			new BeatsDrawer(), //
			new InstrumentDrawer(), //
			new SelectedNotesDrawer(), //
			new HighlightDrawer());

	private static final long serialVersionUID = -3439446235287039031L;

	public static final int noteH = 30;
	public static final int noteW = 15;
	public static final int tailSize = 15;

	public static final int sectionNamesY = 10;
	public static final int lyricLinesY = sectionNamesY + 20;
	public static final int textY = lyricLinesY + 10;
	public static final int beatTextY = textY + 20;
	public static final int beatSizeTextY = beatTextY + 15;
	public static final int spY = beatSizeTextY + 20;
	public static final int tapY = spY + 5;
	public static final int drY = spY + 5;
	public static final int sdrY = drY + 5;
	public static final int lanesTop = sdrY + 5;
	public static final int lanesBottom = lanesTop + 250;
	public static final int HEIGHT = lanesBottom;

	public static final int lanesHeight = lanesBottom - lanesTop;
	public static final int noteH5 = (int) (lanesHeight * 0.8 / 5);
	public static final int noteH6 = (int) (lanesHeight * 0.8 / 6);

	public static int getLaneY(final int lane, final int lanesNo) {
		return lanesTop + (int) (ChartPanel.lanesHeight * (lane + 0.5) / lanesNo);
	}

	public static final Map<String, Color> colors = new HashMap<>();

	private static String[] hexes = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };

	static {
		// bg colors
		colors.put("BG", new Color(160, 160, 160));
		colors.put("NOTE_BG", new Color(16, 16, 16));
		colors.put("NOTE_ADD_LINE", new Color(0, 255, 0));
		colors.put("LANE", new Color(128, 128, 128));
		colors.put("MAIN_BEAT", new Color(255, 255, 255));
		colors.put("SECONDARY_BEAT", new Color(200, 200, 200));
		colors.put("SECTION_TEXT", new Color(255, 255, 255));
		colors.put("MARKER", new Color(255, 0, 0));
		colors.put("HIGHLIGHT", new Color(255, 0, 0));
		colors.put("SELECT", new Color(0, 255, 255));

		// notes colors
		colors.put("SP_SECTION", new Color(180, 200, 255));
		colors.put("TAP_SECTION", new Color(200, 0, 200));
		colors.put("SOLO_SECTION", new Color(100, 100, 210));
		colors.put("DRUM_ROLL_SECTION", new Color(255, 100, 100));
		colors.put("SPECIAL_DRUM_ROLL_SECTION", new Color(255, 200, 200));

		colors.put("OPEN_NOTE", new Color(230, 20, 230));
		colors.put("OPEN_NOTE_TAIL", new Color(200, 20, 200));
		colors.put("CRAZY", new Color(0, 0, 0));
		colors.put("HOPO", new Color(255, 255, 255));

		colors.put("NOTE_0", new Color(20, 230, 20));
		colors.put("NOTE_1", new Color(230, 20, 20));
		colors.put("NOTE_2", new Color(230, 230, 20));
		colors.put("NOTE_3", new Color(20, 20, 230));
		colors.put("NOTE_4", new Color(230, 115, 20));

		colors.put("NOTE_TAIL_0", new Color(20, 200, 20));
		colors.put("NOTE_TAIL_1", new Color(200, 20, 20));
		colors.put("NOTE_TAIL_2", new Color(200, 200, 20));
		colors.put("NOTE_TAIL_3", new Color(20, 20, 200));
		colors.put("NOTE_TAIL_4", new Color(200, 100, 20));

		colors.put("TAP_NOTE", new Color(155, 20, 155));
		colors.put("TAP_NOTE_TAIL", new Color(155, 20, 155));

		colors.put("NOTE_CYMBAL_TOM", new Color(64, 64, 64));

		// lyrics colors
		colors.put("LYRIC_LINE", new Color(100, 200, 200));
		colors.put("LYRIC_LINE_TEXT", new Color(0, 0, 128));
		colors.put("LYRIC_TEXT", new Color(255, 255, 255));
		colors.put("LYRIC", new Color(255, 0, 255));
		colors.put("LYRIC_NO_TONE", new Color(128, 128, 0));
		colors.put("LYRIC_WORD_PART", new Color(0, 0, 255));
		colors.put("LYRIC_CONNECTION", new Color(255, 128, 255));

		final Map<String, String> config = RW.readConfig("colors.txt");
		for (final Entry<String, String> configEntry : config.entrySet()) {
			try {
				final String[] rgb = configEntry.getValue().split(" ");
				colors.put(configEntry.getKey(), new Color(Integer.valueOf(rgb[0], 16), Integer.valueOf(rgb[1], 16),
						Integer.valueOf(rgb[2], 16)));
			} catch (final Exception e) {
				Logger.error("Couldn't load color " + configEntry.getKey() + "=" + configEntry.getValue(), e);
			}
		}

		for (final Entry<String, Color> colorEntry : colors.entrySet()) {
			final Color c = colorEntry.getValue();
			config.put(colorEntry.getKey(), hex2(c.getRed()) + " " + hex2(c.getGreen()) + " " + hex2(c.getBlue()));
		}
		RW.writeConfig("colors.txt", config);
	}

	public static int clamp(final double y, final int lanesNo) {
		return getLaneY(yToLane(y, lanesNo), lanesNo);
	}

	public static String hex2(final int v) {
		return hexes[(v / 16) % 16] + hexes[v % 16];
	}

	public static boolean isInLanes(final int y) {
		return (y >= lanesTop) && (y <= lanesBottom);
	}

	public static boolean isInTempos(final int y) {
		return (y >= beatTextY) && (y < lanesTop);
	}

	public static int yToLane(final double y, final int lanesNo) {
		return (int) ((y - lanesTop) * lanesNo / lanesHeight);
	}

	private final ChartData data;

	public ChartPanel(final ChartEventsHandler handler) {
		super();
		data = handler.data;
		addMouseListener(handler);
		addMouseMotionListener(new CharterFrameMouseMotionListener(data));
	}

	@Override
	public void paintComponent(final Graphics g) {
		data.t = (int) data.nextT;
		g.setColor(colors.get("BG"));
		g.fillRect(0, 0, getWidth(), getHeight());
		if (data.isEmpty) {
			return;
		}

		g.setColor(colors.get("NOTE_BG"));
		g.fillRect(0, lanesTop, getWidth(), lanesHeight);

		for (final Drawer drawer : DRAWERS) {
			drawer.draw(g, this, data);
		}

		g.setColor(colors.get("MARKER"));
		g.drawLine(Config.markerOffset, spY - 5, Config.markerOffset, lanesBottom);
		if (!data.currentInstrument.type.isVocalsType() && data.isNoteAdd) {
			g.setColor(colors.get("NOTE_ADD_LINE"));
			g.drawLine(data.mousePressX, data.mousePressY, data.mx, data.my);
		}
	}

	public int tempoX(final double lastPos, final int id, final int lastId, final int lastKBPM) {
		return data.timeToX(lastPos + (((id - lastId) * 60000000.0) / lastKBPM));
	}
}
