package log.charter.gui.components.tabs.chordEditor;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.data.config.Config.maxStrings;
import static log.charter.gui.ChartPanelColors.getStringBasedColor;
import static log.charter.util.Utils.getStringPosition;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Map.Entry;

import javax.swing.JComponent;

import log.charter.data.ChartData;
import log.charter.data.song.ChordTemplate;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.ChartPanelColors.StringColorLabelType;
import log.charter.util.ColorUtils;

public class ChordTemplateInfo extends JComponent implements MouseListener {
	private static final int pictureNoteSize = 5;

	private static final long serialVersionUID = 1L;

	private final ChartData chartData;
	private final ChordTemplatesEditorTab parent;

	private final int chordTemplateId;

	private boolean hover = false;

	public ChordTemplateInfo(final ChartData chartData, final ChordTemplatesEditorTab parent,
			final int chordTemplateId) {
		this.chartData = chartData;
		this.parent = parent;

		this.chordTemplateId = chordTemplateId;

		addMouseListener(this);

		final Dimension size = new Dimension(250, 50);
		setMinimumSize(size);
		setMaximumSize(size);
		setPreferredSize(size);
		this.setSize(size);
	}

	private void drawChordShapeImage(final Graphics g, final ChordTemplate chordTemplate, final int strings) {
		final int[] frets = new int[strings];
		for (int i = 0; i < strings; i++) {
			frets[i] = -1;
		}
		int minUsedString = strings;
		int maxUsedString = 0;
		int minNonzeroFret = maxStrings;
		int maxNonzeroFret = 0;

		for (final Entry<Integer, Integer> entry : chordTemplate.frets.entrySet()) {
			final int string = entry.getKey();
			final int fret = entry.getValue();

			frets[string] = fret;
			minUsedString = min(minUsedString, string);
			maxUsedString = max(maxUsedString, string);
			if (fret == 0) {
				continue;
			}

			minNonzeroFret = min(minNonzeroFret, fret);
			maxNonzeroFret = max(maxNonzeroFret, fret);
		}
		if (maxNonzeroFret < minNonzeroFret) {
			minNonzeroFret = maxNonzeroFret;
		}

		final int width = pictureNoteSize * max(4, maxNonzeroFret - minNonzeroFret);
		final int yOffset = (getHeight() - pictureNoteSize * strings) / 2;
		for (int string = 0; string < strings; string++) {
			final int fret = frets[string];
			if (fret == -1) {
				continue;
			}

			g.setColor(getStringBasedColor(StringColorLabelType.NOTE, string, strings));

			final int y = yOffset + getStringPosition(string, strings) * pictureNoteSize;
			if (fret == 0) {
				final int height = (int) (pictureNoteSize * 0.6);
				g.fillRect(10, y + (pictureNoteSize - height) / 2, width, height);
			} else {
				final int x = 10 + min(4, fret - minNonzeroFret) * pictureNoteSize;
				g.fillRect(x, y, pictureNoteSize, pictureNoteSize);
			}
		}
	}

	@Override
	public void paint(final Graphics g) {
		if (parent.getSelectedChordTemplateId() != null && parent.getSelectedChordTemplateId() == chordTemplateId) {
			g.setColor(ColorLabel.BASE_BG_4.color());
		} else if (hover) {
			g.setColor(ColorUtils.mix(ColorLabel.BASE_BG_4.color(), ColorLabel.BASE_BG_3.color(), 0.5));
		} else {
			g.setColor(ColorLabel.BASE_BG_3.color());
		}
		g.fillRect(0, 0, getWidth(), getHeight());

		final ChordTemplate chordTemplate = chartData.currentChordTemplates().get(chordTemplateId);
		final int strings = chartData.currentStrings();

		drawChordShapeImage(g, chordTemplate, strings);

		g.setColor(getStringBasedColor(StringColorLabelType.NOTE, chordTemplate.getStringRange().min, strings));
		final String chordName = "[%d] %s".formatted(chordTemplateId, chordTemplate.chordName);
		g.drawString(chordName, 50, 18);
		final String chordFrets = chordTemplate.getTemplateFrets(strings);
		g.drawString(chordFrets, 50, 38);
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		parent.selectChordTemplate(chordTemplateId);
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
		hover = true;
		repaint();
	}

	@Override
	public void mouseExited(final MouseEvent e) {
		hover = false;
		repaint();
	}
}
