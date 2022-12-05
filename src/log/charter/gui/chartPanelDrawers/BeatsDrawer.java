package log.charter.gui.chartPanelDrawers;

import static log.charter.gui.ChartPanel.beatTextY;
import static log.charter.gui.ChartPanel.lanesBottom;

import java.awt.Font;
import java.awt.Graphics;
import java.util.List;

import log.charter.data.ChartData;
import log.charter.gui.ChartPanel;
import log.charter.song.Tempo;

public class BeatsDrawer implements Drawer {
	private static final int MAX_DISTANCE_BEHIND = 100;

	private static int tempoX(final ChartData data, final double lastPos, final int id, final int lastId,
			final int lastKBPM) {
		return data.timeToX(lastPos + (((id - lastId) * 60000000.0) / lastKBPM));
	}

	private static void drawBeat(final Graphics g, final ChartData data, final Tempo tmp, final int id, final int x,
			final int beatInMeasure) {
		if (x > -MAX_DISTANCE_BEHIND) {
			g.setColor(ChartPanel.colors.get(beatInMeasure == 0 ? "MAIN_BEAT" : "SECONDARY_BEAT"));
			g.drawLine(x, beatTextY, x, lanesBottom);
			g.drawString("" + id, x + 3, ChartPanel.beatTextY + 11);
			if (id == tmp.id) {
				g.drawString("" + tmp.beats, x + 3, ChartPanel.beatSizeTextY + 11);
			}
			final String sectionName = data.s.sections.get(id);
			if (sectionName != null) {
				g.setColor(ChartPanel.colors.get("SECTION_TEXT"));
				g.drawString("[" + sectionName + "]", x, ChartPanel.sectionNamesY + 11);
			}
		}
	}

	@Override
	public void draw(final Graphics g, final ChartPanel panel, final ChartData data) {
		final List<Tempo> tempos = data.s.tempoMap.tempos;
		int lastKBPM = 120000;
		int beatInMeasure = 0;
		int beatsPerMeasure = 1;

		g.setFont(new Font(Font.DIALOG, Font.PLAIN, 15));
		for (int i = 0; i < (tempos.size() - 1); i++) {
			final Tempo tmp = tempos.get(i);
			final Tempo nextTempo = tempos.get(i + 1);
			if (beatsPerMeasure != tmp.beats) {
				beatInMeasure = 0;
				beatsPerMeasure = tmp.beats;
			}
			lastKBPM = tmp.kbpm;
			final boolean drawing = data.timeToX(nextTempo.pos) >= -20;

			for (int id = tmp.id; id < nextTempo.id; id++) {
				if (drawing) {
					final int x = tempoX(data, tmp.pos, id, tmp.id, lastKBPM);
					if (x > panel.getWidth()) {
						return;
					}
					drawBeat(g, data, tmp, id, x, beatInMeasure);
				}
				beatInMeasure = (beatInMeasure + 1) % beatsPerMeasure;
			}
		}
		final Tempo tmp = tempos.get(tempos.size() - 1);
		lastKBPM = tmp.kbpm;
		beatsPerMeasure = tmp.beats;
		if (beatsPerMeasure != tmp.beats) {
			beatInMeasure = 0;
		}

		int id = tmp.id;
		while (lastKBPM < 10000000) {
			final int x = tempoX(data, tmp.pos, id, tmp.id, lastKBPM);
			if (x > panel.getWidth()) {
				return;
			}
			drawBeat(g, data, tmp, id, x, beatInMeasure);
			beatInMeasure = (beatInMeasure + 1) % beatsPerMeasure;
			id++;
		}
	}
}
