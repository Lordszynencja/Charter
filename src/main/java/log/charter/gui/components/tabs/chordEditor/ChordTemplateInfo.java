package log.charter.gui.components.tabs.chordEditor;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.data.config.ChartPanelColors.getStringBasedColor;
import static log.charter.data.config.GraphicalConfig.inputSize;
import static log.charter.util.ColorUtils.mix;
import static log.charter.util.Utils.getStringPosition;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Map.Entry;

import javax.swing.JComponent;

import log.charter.data.ChartData;
import log.charter.data.config.ChartPanelColors.ColorLabel;
import log.charter.data.config.ChartPanelColors.StringColorLabelType;
import log.charter.data.config.values.InstrumentConfig;
import log.charter.data.song.ChordTemplate;
import log.charter.gui.chartPanelDrawers.drawableShapes.CenteredText;
import log.charter.gui.components.containers.ScrollableRowedPanel;
import log.charter.gui.components.preview3D.glUtils.Point2D;
import log.charter.util.ColorUtils;
import log.charter.util.data.Position2D;

public class ChordTemplateInfo extends JComponent implements MouseListener {
	private static class UsedStringsAndFrets {
		final int[] frets;
		public int minUsedString;
		public int maxUsedString = 0;
		public int minNonzeroFret = InstrumentConfig.frets;
		public int maxNonzeroFret = 0;

		public UsedStringsAndFrets(final int strings, final ChordTemplate chordTemplate) {
			frets = new int[strings];
			for (int i = 0; i < strings; i++) {
				frets[i] = -1;
			}
			minUsedString = strings;

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
		}
	}

	public static int maxFretsVisible = 10;

	private static int pictureNoteSize = inputSize / 4;
	private static int noteX0 = inputSize / 2;
	private static int descriptionX = maxFretsVisible * pictureNoteSize + inputSize * 3 / 2;
	private static int descriptionY0 = inputSize * 9 / 10;
	private static int descriptionY1 = inputSize * 8 / 5;
	private static int textStringWidth = inputSize;

	private static Font chordNameFont = new Font(Font.DIALOG, Font.BOLD, inputSize);
	private static Font fretsFont = new Font(Font.DIALOG, Font.BOLD, inputSize * 7 / 10);

	private static final long serialVersionUID = 1L;

	public static void recalculateSizes() {
		pictureNoteSize = inputSize / 4;
		noteX0 = inputSize / 2;
		descriptionX = maxFretsVisible * pictureNoteSize + inputSize * 3 / 2;
		descriptionY0 = inputSize * 9 / 10;
		descriptionY1 = inputSize * 8 / 5;
		textStringWidth = inputSize;

		chordNameFont = new Font(Font.DIALOG, Font.BOLD, inputSize);
		fretsFont = new Font(Font.DIALOG, Font.BOLD, inputSize * 7 / 10);
	}

	private final ChartData chartData;
	private final ChordTemplatesEditorTab tab;

	private final int chordTemplateId;

	private boolean hover = false;

	public ChordTemplateInfo(final ChartData chartData, final ScrollableRowedPanel parent,
			final ChordTemplatesEditorTab tab, final int chordTemplateId) {
		this.chartData = chartData;
		this.tab = tab;

		this.chordTemplateId = chordTemplateId;

		addMouseListener(this);

		final Dimension size = new Dimension(parent.sizes.width, parent.sizes.rowHeight);
		setMinimumSize(size);
		setMaximumSize(size);
		setPreferredSize(size);
		this.setSize(size);
	}

	private void paintBackground(final Graphics g) {
		if (tab.getSelectedChordTemplateId() != null && tab.getSelectedChordTemplateId() == chordTemplateId) {
			g.setColor(ColorLabel.BASE_BG_4.color());
		} else if (hover) {
			g.setColor(ColorUtils.mix(ColorLabel.BASE_BG_4.color(), ColorLabel.BASE_BG_3.color(), 0.5));
		} else {
			g.setColor(ColorLabel.BASE_BG_3.color());
		}
		g.fillRect(0, 0, getWidth(), getHeight());
	}

	private void drawChordShapeImage(final Graphics g, final ChordTemplate chordTemplate, final int strings) {
		final UsedStringsAndFrets used = new UsedStringsAndFrets(strings, chordTemplate);

		final int noteWidth = used.maxNonzeroFret > used.minNonzeroFret + maxFretsVisible
				? pictureNoteSize * maxFretsVisible / (used.maxNonzeroFret - used.minNonzeroFret)
				: pictureNoteSize;

		final int width = noteWidth * max(maxFretsVisible, used.maxNonzeroFret - used.minNonzeroFret + 1);
		final int yOffset = (getHeight() - pictureNoteSize * strings) / 2;
		for (int string = 0; string < strings; string++) {
			final int fret = used.frets[string];
			if (fret == -1) {
				continue;
			}

			g.setColor(getStringBasedColor(StringColorLabelType.NOTE, string, strings));

			final int y = yOffset + getStringPosition(string, strings) * pictureNoteSize;
			if (fret == 0) {
				final int height = (int) (pictureNoteSize * 0.6);
				g.fillRect(noteX0, y + (pictureNoteSize - height) / 2, width, height);
			} else {
				final int x = noteX0 + (fret - used.minNonzeroFret) * noteWidth;
				g.fillRect(x, y, noteWidth, pictureNoteSize);
			}
		}
	}

	private void drawChordName(final Graphics2D g, final ChordTemplate chordTemplate, final int strings) {
		final Color lowestStringColor = getStringBasedColor(StringColorLabelType.NOTE, chordTemplate.getLowestString(),
				strings);
		final String chordName = "[%d] %s".formatted(chordTemplateId, chordTemplate.chordName);
		final Point2D expectedTextSize = CenteredText.getExpectedSize(g, chordNameFont, chordName);
		final int x = descriptionX + strings * textStringWidth;
		final float y = (float) (getHeight() / 2 + expectedTextSize.y / 2);

		g.setColor(mix(lowestStringColor, Color.WHITE, 0.5));
		g.setFont(chordNameFont);
		g.drawString(chordName, x, y);
	}

	private void drawDescription(final Graphics2D g, final ChordTemplate chordTemplate, final int strings) {
		for (int string = 0; string < strings; string++) {
			final int x = descriptionX + string * textStringWidth;
			final Color stringColor = getStringBasedColor(StringColorLabelType.NOTE, string, strings);
			g.setColor(mix(stringColor, Color.WHITE, 0.5));
			final String fret = chordTemplate.frets.containsKey(string) ? chordTemplate.frets.get(string) + "" : "X";
			final Integer fingerId = chordTemplate.fingers.get(string);
			final String finger = ChordTemplate.fingerNames.getOrDefault(fingerId, "-");

			new CenteredText(new Position2D(x, descriptionY0), fretsFont, fret, stringColor).draw(g);
			new CenteredText(new Position2D(x, descriptionY1), fretsFont, finger, stringColor).draw(g);
		}
	}

	@Override
	public void paint(final Graphics g) {
		paintBackground(g);

		final ChordTemplate chordTemplate = chartData.currentChordTemplates().get(chordTemplateId);
		final int strings = chartData.currentStrings();

		drawChordShapeImage(g, chordTemplate, strings);
		drawDescription((Graphics2D) g, chordTemplate, strings);
		drawChordName((Graphics2D) g, chordTemplate, strings);
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		tab.selectChordTemplate(chordTemplateId);
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
