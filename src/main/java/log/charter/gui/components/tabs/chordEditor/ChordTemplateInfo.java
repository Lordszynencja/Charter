package log.charter.gui.components.tabs.chordEditor;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.gui.ChartPanelColors.getStringBasedColor;
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
import log.charter.data.config.values.InstrumentConfig;
import log.charter.data.song.ChordTemplate;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.ChartPanelColors.StringColorLabelType;
import log.charter.gui.chartPanelDrawers.drawableShapes.CenteredText;
import log.charter.gui.components.preview3D.glUtils.Point2D;
import log.charter.util.ColorUtils;
import log.charter.util.data.Position2D;

public class ChordTemplateInfo extends JComponent implements MouseListener {
	private static final int pictureNoteSize = 5;

	private static final Font chordNameFont = new Font(Font.DIALOG, Font.BOLD, 20);
	private static final Font fretsFont = new Font(Font.DIALOG, Font.BOLD, 14);

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

		final Dimension size = new Dimension(ChordTemplatesEditorTab.listWidth, 50);
		setMinimumSize(size);
		setMaximumSize(size);
		setPreferredSize(size);
		this.setSize(size);
	}

	private void paintBackground(final Graphics g) {
		if (parent.getSelectedChordTemplateId() != null && parent.getSelectedChordTemplateId() == chordTemplateId) {
			g.setColor(ColorLabel.BASE_BG_4.color());
		} else if (hover) {
			g.setColor(ColorUtils.mix(ColorLabel.BASE_BG_4.color(), ColorLabel.BASE_BG_3.color(), 0.5));
		} else {
			g.setColor(ColorLabel.BASE_BG_3.color());
		}
		g.fillRect(0, 0, getWidth(), getHeight());
	}

	private void drawChordShapeImage(final Graphics g, final ChordTemplate chordTemplate, final int strings) {
		final int[] frets = new int[strings];
		for (int i = 0; i < strings; i++) {
			frets[i] = -1;
		}
		int minUsedString = strings;
		int maxUsedString = 0;
		int minNonzeroFret = InstrumentConfig.frets;
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

		final int noteWidth = maxNonzeroFret > minNonzeroFret + 4
				? pictureNoteSize * 4 / (maxNonzeroFret - minNonzeroFret)
				: pictureNoteSize;

		final int width = noteWidth * max(4, maxNonzeroFret - minNonzeroFret + 1);
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
				final int x = 10 + (fret - minNonzeroFret) * noteWidth;
				g.fillRect(x, y, noteWidth, pictureNoteSize);
			}
		}
	}

	private void drawChordName(final Graphics2D g, final ChordTemplate chordTemplate, final int strings) {
		final Color lowestStringColor = getStringBasedColor(StringColorLabelType.NOTE, chordTemplate.getLowestString(),
				strings);
		final String chordName = "[%d] %s".formatted(chordTemplateId, chordTemplate.chordName);
		final Point2D expectedTextSize = CenteredText.getExpectedSize(g, chordNameFont, chordName);
		final int x = 60 + strings * 20;
		final float y = (float) (getHeight() / 2 + expectedTextSize.y / 2);

		g.setColor(mix(lowestStringColor, Color.WHITE, 0.5));
		g.setFont(chordNameFont);
		g.drawString(chordName, x, y);
	}

	private void drawDescription(final Graphics2D g, final ChordTemplate chordTemplate, final int strings) {
		for (int string = 0; string < strings; string++) {
			final int x = 50 + string * 20;
			final Color stringColor = getStringBasedColor(StringColorLabelType.NOTE, string, strings);
			g.setColor(mix(stringColor, Color.WHITE, 0.5));
			final String fret = chordTemplate.frets.containsKey(string) ? chordTemplate.frets.get(string) + "" : "X";
			final Integer fingerId = chordTemplate.fingers.get(string);
			final String finger = ChordTemplate.fingerNames.getOrDefault(fingerId, "-");

			new CenteredText(new Position2D(x, 18), fretsFont, fret, stringColor).draw(g);
			new CenteredText(new Position2D(x, 32), fretsFont, finger, stringColor).draw(g);
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
