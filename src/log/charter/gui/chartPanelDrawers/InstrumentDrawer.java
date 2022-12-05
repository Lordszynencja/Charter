package log.charter.gui.chartPanelDrawers;

import static log.charter.gui.ChartPanel.getLaneY;
import static log.charter.gui.ChartPanel.noteH5;
import static log.charter.gui.ChartPanel.noteH6;
import static log.charter.gui.ChartPanel.noteW;
import static log.charter.util.ByteUtils.getBit;

import java.awt.Font;
import java.awt.Graphics;
import java.util.List;

import log.charter.data.ChartData;
import log.charter.gui.ChartPanel;
import log.charter.gui.chartPanelDrawers.lists.ArcList;
import log.charter.gui.chartPanelDrawers.lists.FillList;
import log.charter.gui.chartPanelDrawers.lists.TextDrawList;
import log.charter.song.Lyric;
import log.charter.song.Note;

public class InstrumentDrawer implements Drawer {
	private void drawKeysNotes(final Graphics g, final ChartPanel panel, final ChartData data) {
		final FillList[] noteTails = new FillList[5];
		final FillList[] notes = new FillList[5];
		for (int i = 0; i < 5; i++) {
			noteTails[i] = new FillList();
			notes[i] = new FillList();
		}
		final int tailH = noteH5 / 2;
		final int w = panel.getWidth();

		for (final Note n : data.currentNotes) {
			final int x = data.timeToX(n.pos);
			final int length = data.timeToXLength(n.getLength());
			if (x > (w + (noteW / 2))) {
				break;
			}
			if ((x + length) > 0) {
				for (int c = 0; c < 5; c++) {
					if ((n.notes & (1 << c)) > 0) {
						final int y = getLaneY(c, 5);
						notes[c].addPositions(x - (noteW / 2), y - noteH5 / 2, noteW, noteH5);
						if (length > noteW / 2) {
							noteTails[c].addPositions(x, y - tailH / 2, length, tailH);
						}
					}
				}
			}
		}
		for (int i = 0; i < 5; i++) {
			noteTails[i].draw(g, ChartPanel.colors.get("NOTE_TAIL_" + i));
			notes[i].draw(g, ChartPanel.colors.get("NOTE_" + i));
		}
	}

	private void drawGuitarNotes(final Graphics g, final ChartPanel panel, final ChartData data) {
		final FillList[] notes = new FillList[6];
		final FillList[] noteTails = new FillList[6];
		final FillList tapNotes = new FillList();
		final FillList tapNoteTails = new FillList();
		for (int i = 0; i < 6; i++) {
			notes[i] = new FillList();
			noteTails[i] = new FillList();
		}
		final FillList crazy = new FillList();
		final FillList hopos = new FillList();

		final int tailH = noteH6 / 2;
		final int w = panel.getWidth();

		for (final Note n : data.currentNotes) {
			final int x = data.timeToX(n.pos);
			final int length = data.timeToXLength(n.getLength());
			if (x > (w + (noteW / 2))) {
				break;
			}
			if ((x + length) > 0) {
				if (n.notes == 0) {
					final int y = getLaneY(0, 6);
					if (n.tap) {
						tapNotes.addPositions(x - (noteW / 2), y - (noteH6 / 2), noteW, noteH6);
						tapNoteTails.addPositions(x, y - (tailH / 2), length, tailH);
					} else {
						notes[0].addPositions(x - (noteW / 2), y - (noteH6 / 2), noteW, noteH6);
						noteTails[0].addPositions(x, y - (tailH / 2), length, tailH);
						if (n.hopo) {
							hopos.addPositions(x - (noteW / 4), y - (noteH6 / 4), noteW / 2, noteH6 / 2);
						}
						if (n.crazy) {
							crazy.addPositions(x - (noteW / 4), y - (noteH6 / 3) - 1, noteW / 2,
									((3 * noteH6) / 4) + 1);
						}
					}
				} else {
					for (int c = 0; c < 5; c++) {
						if ((n.notes & (1 << c)) > 0) {
							final int y = getLaneY(c + 1, 6);
							if (n.tap) {
								tapNotes.addPositions(x - (noteW / 2), y - (noteH6 / 2), noteW, noteH6);
								if (length > noteW / 2) {
									tapNoteTails.addPositions(x, y - (tailH / 2), length, tailH);
								}
							} else {
								notes[c + 1].addPositions(x - (noteW / 2), y - (noteH6 / 2), noteW, noteH6);
								if (length > noteW / 2) {
									noteTails[c + 1].addPositions(x, y - (tailH / 2), length, tailH);
								}
								if (n.hopo) {
									hopos.addPositions(x - (noteW / 4), y - (noteH6 / 4), noteW / 2, noteH6 / 2);
								}
								if (n.crazy) {
									crazy.addPositions(x - (noteW / 4), y - (noteH6 / 3) - 1, noteW / 2,
											((3 * noteH6) / 4) + 1);
								}
							}
						}
					}
				}
			}
		}
		noteTails[0].draw(g, ChartPanel.colors.get("OPEN_NOTE_TAIL"));
		for (int i = 0; i < 5; i++) {
			noteTails[i + 1].draw(g, ChartPanel.colors.get("NOTE_TAIL_" + i));
		}
		tapNoteTails.draw(g, ChartPanel.colors.get("TAP_NOTE_TAIL"));

		notes[0].draw(g, ChartPanel.colors.get("OPEN_NOTE"));
		for (int i = 0; i < 5; i++) {
			notes[i + 1].draw(g, ChartPanel.colors.get("NOTE_" + i));
		}
		tapNotes.draw(g, ChartPanel.colors.get("TAP_NOTE"));

		crazy.draw(g, ChartPanel.colors.get("CRAZY"));
		hopos.draw(g, ChartPanel.colors.get("HOPO"));
	}

	private void drawDrumsNotes(final Graphics g, final ChartPanel panel, final ChartData data) {
		final FillList[] noteTails = new FillList[5];
		final FillList[] notes = new FillList[5];
		final ArcList[] cymbalNotes = new ArcList[3];
		for (int i = 0; i < 5; i++) {
			noteTails[i] = new FillList();
			notes[i] = new FillList();
		}
		for (int i = 0; i < 3; i++) {
			cymbalNotes[i] = new ArcList();
		}
		final FillList expertPlusNotes = new FillList();
		final FillList tomCymbalNotes = new FillList();

		final int tailH = noteH5 / 2;
		final int w = panel.getWidth();

		for (final Note n : data.currentNotes) {
			final int x = data.timeToX(n.pos);
			final int length = data.timeToXLength(n.getLength());
			if (x > (w + (noteW / 2))) {
				break;
			}
			if ((x + length) > 0) {
				for (int c = 0; c < 5; c++) {
					if (getBit(n.notes, c)) {
						final int y = getLaneY(c, 5);
						if ((c == 2 && !n.yellowTom) || (c == 3 && !n.blueTom) || (c == 4 && !n.greenTom)) {
							cymbalNotes[c - 2].addPositions(x - noteW * 3 / 2, y - noteH5 / 2, noteW * 2, noteH5, 270,
									180);
						} else {
							notes[c].addPositions(x - noteW / 2, y - noteH5 / 2, noteW, noteH5);
						}
						if ((c == 2 && n.yellowCymbal && n.yellowTom)//
								|| (c == 3 && n.blueCymbal && n.blueTom)//
								|| (c == 4 && n.greenCymbal && n.greenTom)) {
							tomCymbalNotes.addPositions(x - 3, y - 3, 7, 7);
						}

						if (length > noteW / 2) {
							noteTails[c].addPositions(x, y - tailH / 2, length, tailH);
						}
						if (c == 0 && n.expertPlus) {
							expertPlusNotes.addPositions(x - noteW / 3, y - noteH5 * 2 / 5, noteW * 2 / 3,
									noteH5 * 4 / 5);
						}
					}
				}
			}
		}

		final int[] colorIds = { 4, 1, 2, 3, 0 };
		for (int i = 0; i < 5; i++) {
			noteTails[i].draw(g, ChartPanel.colors.get("NOTE_TAIL_" + colorIds[i]));
			notes[i].draw(g, ChartPanel.colors.get("NOTE_" + colorIds[i]));
		}
		for (int i = 0; i < 3; i++) {
			cymbalNotes[i].draw(g, ChartPanel.colors.get("NOTE_" + colorIds[i + 2]));
		}
		expertPlusNotes.draw(g, ChartPanel.colors.get("NOTE_1"));
		tomCymbalNotes.draw(g, ChartPanel.colors.get("NOTE_CYMBAL_TOM"));
	}

	private void drawLyrics(final Graphics g, final ChartPanel panel, final ChartData data) {
		final TextDrawList texts = new TextDrawList();
		final FillList notes = new FillList();
		final FillList tonelessNotes = new FillList();
		final FillList connections = new FillList();
		final FillList wordConnections = new FillList();

		final List<Lyric> lyrics = data.s.v.lyrics;
		final int y = getLaneY(0, 1) - 3;
		final int w = panel.getWidth();

		for (int i = 0; i < lyrics.size(); i++) {
			final Lyric l = lyrics.get(i);
			final int x = data.timeToX(l.pos);
			int length = data.timeToXLength(l.getLength());
			if (length < 1) {
				length = 1;
			}
			if (((x > w) && !l.connected) //
					|| ((i > 0) && (data.timeToX(lyrics.get(i - 1).pos) > w))) {
				break;
			}
			if ((x + length) > 0) {
				(l.toneless ? tonelessNotes : notes).addPositions(x, y, length, 8);
			}
			if (l.connected && (i > 0)) {
				final Lyric prev = lyrics.get(i - 1);
				final int prevEnd = data.timeToX(prev.pos + prev.getLength());
				connections.addPositions(prevEnd, y, x - prevEnd, 8);
			}
			if ((x + g.getFontMetrics().stringWidth(l.lyric)) > 0) {
				texts.addString(l.lyric + (l.wordPart ? "-" : ""), x, ChartPanel.textY + 17);
			}
			if (l.wordPart && (i < (lyrics.size() - 1))) {
				final Lyric next = lyrics.get(i + 1);
				final int nextStart = data.timeToX(next.pos);
				wordConnections.addPositions(x + length, y + 2, nextStart - x - length, 4);
			}
		}
		texts.draw(g, ChartPanel.colors.get("LYRIC_TEXT"));
		notes.draw(g, ChartPanel.colors.get("LYRIC"));
		tonelessNotes.draw(g, ChartPanel.colors.get("LYRIC_NO_TONE"));
		connections.draw(g, ChartPanel.colors.get("LYRIC_CONNECTION"));
		wordConnections.draw(g, ChartPanel.colors.get("LYRIC_WORD_PART"));
	}

	private void drawDebugNoteId(final Graphics g, final ChartPanel panel, final ChartData data) {
		for (int i = 0; i < data.currentNotes.size(); i++) {
			final Note n = data.currentNotes.get(i);
			final int x = data.timeToX(n.pos);
			if (x >= 0 && x < panel.getWidth()) {
				g.setFont(new Font(Font.DIALOG, Font.PLAIN, 15));
				g.drawString("" + i, x - 5, ChartPanel.beatTextY - 10);
			}
		}
	}

	@Override
	public void draw(final Graphics g, final ChartPanel panel, final ChartData data) {
		if (data.s == null) {
			return;
		}

		if (data.currentInstrument.type.isGuitarType()) {
			drawGuitarNotes(g, panel, data);
		} else if (data.currentInstrument.type.isDrumsType()) {
			drawDrumsNotes(g, panel, data);
		} else if (data.currentInstrument.type.isVocalsType()) {
			drawLyrics(g, panel, data);
		} else if (data.currentInstrument.type.isKeysType()) {
			drawKeysNotes(g, panel, data);
		}

		if (data.drawDebug) {
			drawDebugNoteId(g, panel, data);
		}
	}
}
