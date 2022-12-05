package log.charter.gui.chartPanelDrawers;

import static log.charter.gui.ChartPanel.colors;
import static log.charter.gui.ChartPanel.drY;
import static log.charter.gui.ChartPanel.lanesHeight;
import static log.charter.gui.ChartPanel.lanesTop;
import static log.charter.gui.ChartPanel.lyricLinesY;
import static log.charter.gui.ChartPanel.sdrY;
import static log.charter.gui.ChartPanel.spY;
import static log.charter.gui.ChartPanel.tapY;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import log.charter.data.ChartData;
import log.charter.gui.ChartPanel;
import log.charter.gui.chartPanelDrawers.lists.FillList;
import log.charter.gui.chartPanelDrawers.lists.TextDrawList;
import log.charter.song.Event;
import log.charter.song.Instrument;
import log.charter.song.Lyric;

public class SectionsDrawer implements Drawer {
	private void drawEvents(final Graphics g, final ChartPanel panel, final ChartData data, final List<Event> events,
			final int y, final int h, final String colorName) {
		final FillList list = new FillList();
		for (final Event e : events) {
			final int x = data.timeToX(e.pos);
			final int l = data.timeToXLength(e.getLength());
			if ((x + l) < 0) {
				continue;
			}
			if (x >= panel.getWidth()) {
				break;
			}
			list.addPositions(x, y, l, h);
		}
		list.draw(g, colors.get(colorName));
	}

	private void drawLyrics(final Graphics g, final ChartPanel panel, final ChartData data) {
		final TextDrawList lyricLines = new TextDrawList();
		final FillList lines = new FillList();
		for (final Event e : data.s.v.lyricLines) {
			final int x = data.timeToX(e.pos);
			final int l = data.timeToXLength(e.getLength());
			if ((x + l) < 0) {
				continue;
			}
			if (x >= panel.getWidth()) {
				break;
			}
			lines.addPositions(x, lyricLinesY, l, 13);

			final List<Lyric> lyricsInLine = new ArrayList<>(20);
			for (final Lyric lyric : data.s.v.lyrics) {
				if (lyric.pos < e.pos) {
					continue;
				}
				if (lyric.pos > (e.pos + e.getLength())) {
					break;
				}
				lyricsInLine.add(lyric);
			}
			lyricLines.addString(Lyric.joinLyrics(lyricsInLine), x, lyricLinesY + 11);
		}
		lines.draw(g, colors.get("LYRIC_LINE"));
		lyricLines.draw(g, colors.get("LYRIC_LINE_TEXT"));
	}

	@Override
	public void draw(final Graphics g, final ChartPanel panel, final ChartData data) {
		drawLyrics(g, panel, data);

		final Instrument instr = data.currentInstrument;
		if (instr.type.isGuitarType()) {
			drawEvents(g, panel, data, instr.sp, spY, 5, "SP_SECTION");
			drawEvents(g, panel, data, instr.tap, tapY, 5, "TAP_SECTION");
			drawEvents(g, panel, data, instr.solo, lanesTop, lanesHeight, "SOLO_SECTION");
		} else if (instr.type.isKeysType()) {
			drawEvents(g, panel, data, instr.sp, spY, 5, "SP_SECTION");
			drawEvents(g, panel, data, instr.solo, lanesTop, lanesHeight, "SOLO_SECTION");
		} else if (instr.type.isDrumsType()) {
			drawEvents(g, panel, data, instr.sp, spY, 5, "SP_SECTION");
			drawEvents(g, panel, data, instr.solo, lanesTop, lanesHeight, "SOLO_SECTION");
			drawEvents(g, panel, data, instr.drumRoll, drY, 5, "DRUM_ROLL_SECTION");
			drawEvents(g, panel, data, instr.specialDrumRoll, sdrY, 5, "SPECIAL_DRUM_ROLL_SECTION");
		}
	}
}
