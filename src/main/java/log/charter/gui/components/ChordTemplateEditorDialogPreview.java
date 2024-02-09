package log.charter.gui.components;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static log.charter.gui.ChartPanelColors.getStringBasedColor;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledDiamond;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledOval;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.lineHorizontal;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.lineVertical;
import static log.charter.util.Utils.getStringPosition;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Set;

import javax.swing.JComponent;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.ChartPanelColors.StringColorLabelType;
import log.charter.gui.chartPanelDrawers.drawableShapes.CenteredText;
import log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShapeList;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePositionWithSize;
import log.charter.song.ChordTemplate;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashSet2;
import log.charter.util.IntRange;
import log.charter.util.Position2D;

public class ChordTemplateEditorDialogPreview extends JComponent
		implements MouseListener, MouseMotionListener, KeyListener {
	private static final long serialVersionUID = 1L;
	private static final double fretsProportion = pow(2, -1.0 / 12);
	private static final int fretStart = 22;
	private static final Set<Integer> dotFrets = new HashSet2<>(new ArrayList2<>(3, 5, 7, 9));

	private static class FretPosition {
		public final int fret;
		public final int position;
		public final int length;

		private FretPosition(final int fret, final int position, final int length) {
			this.fret = fret;
			this.position = position;
			this.length = length;
		}
	}

	private static int[] getStringPositions(final int strings, final int height) {
		final int[] stringPositions = new int[strings];
		final int stringSpace = (height - fretStart) / strings;
		int y = fretStart + stringSpace / 2;
		for (int i = 0; i < strings; i++) {
			stringPositions[getStringPosition(i, strings)] = y;
			y += stringSpace;
		}

		return stringPositions;
	}

	private final int[] stringPositions;

	private final ChordTemplateEditorDialog parent;

	private final ChartData data;
	private final ChordTemplate chordTemplate;

	private Integer mouseString;
	private Integer mouseFret;

	public ChordTemplateEditorDialogPreview(final ChordTemplateEditorDialog parent, final ChartData data,
			final ChordTemplate chordTemplate, final int height) {
		super();
		this.parent = parent;
		this.data = data;
		this.chordTemplate = chordTemplate;

		stringPositions = getStringPositions(data.getCurrentArrangement().tuning.strings, height);

		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		parent.addKeyListener(this);

		setFocusable(true);
		setRequestFocusEnabled(true);
	}

	private IntRange getFretsRange() {
		int min = Config.frets;
		int max = 0;
		for (final int fret : chordTemplate.frets.values()) {
			if (fret != 0) {
				min = min(min, fret);
				max = max(max, fret);
			}
		}
		if (min > max) {
			min = max;
		}

		if (min > 0) {
			min--;
		}
		if (min > 0) {
			min--;
		}
		if (max < Config.frets) {
			max++;
		}
		while (max - min < 5) {
			if (min > 0) {
				min--;
			}
			if (max < Config.frets) {
				max++;
			}
		}

		return new IntRange(min, max);
	}

	private FretPosition[] getFretPositions() {
		final IntRange fretsRange = getFretsRange();
		final int fretsAmount = fretsRange.max - fretsRange.min + 1;

		double scaleLength;
		final double[] fretScales = new double[fretsAmount];
		if (fretsRange.min == 0) {
			scaleLength = 1;
			fretScales[0] = 1;
		} else {
			scaleLength = 0.5;
			fretScales[0] = 0.5;
		}

		double fretScale = 1;
		for (int i = 1; i < fretsAmount; i++) {
			fretScales[i] = fretScale;
			scaleLength += fretScale;
			fretScale *= fretsProportion;
		}
		scaleLength += 0.5 * fretScale;

		final FretPosition[] fretPositions = new FretPosition[fretsAmount];
		final double multiplier = getWidth() / scaleLength;
		int fretPosition = 0;
		for (int i = 0; i < fretsAmount; i++) {
			final int fretLength = (int) (fretScales[i] * multiplier);
			fretPosition += fretLength;
			fretPositions[i] = new FretPosition(fretsRange.min + i, fretPosition, fretLength);
		}

		return fretPositions;
	}

	private void drawBackground(final Graphics g) {
		g.setColor(ColorLabel.BASE_BG_1.color());
		g.fillRect(0, 0, getWidth(), getHeight());
	}

	private void addDot(final DrawableShapeList frets, final FretPosition fretPosition, final int y) {
		final int x = fretPosition.position - fretPosition.length / 2;
		final ShapePositionWithSize position = new ShapePositionWithSize(x, y, 10, 10).centered();
		frets.add(filledOval(position, ColorLabel.BASE_BG_4));
	}

	private void addFretDotLow(final DrawableShapeList frets, final FretPosition fretPosition) {
		addDot(frets, fretPosition, getHeight() - 25);
	}

	private void addFretDotHigh(final DrawableShapeList frets, final FretPosition fretPosition) {
		addDot(frets, fretPosition, fretStart + 25);
	}

	private void addFretDotDouble(final DrawableShapeList frets, final FretPosition fretPosition) {
		addFretDotLow(frets, fretPosition);
		addFretDotHigh(frets, fretPosition);
	}

	private void drawFrets(final Graphics g, final FretPosition[] fretPositions) {
		final DrawableShapeList frets = new DrawableShapeList();

		for (final FretPosition fretPosition : fretPositions) {
			frets.add(lineVertical(fretPosition.position - 1, fretStart, getHeight(), ColorLabel.BASE_BG_2.color()));
			frets.add(lineVertical(fretPosition.position, fretStart, getHeight(), ColorLabel.BASE_BG_4.color()));
			frets.add(lineVertical(fretPosition.position + 1, fretStart, getHeight(), ColorLabel.BASE_BG_2.color()));
			frets.add(new CenteredText(new Position2D(fretPosition.position, 10), g.getFont(), fretPosition.fret + "",
					ColorLabel.BASE_DARK_TEXT.color()));

			if (fretPosition.fret % 12 == 0 && fretPosition.fret >= 12) {
				addFretDotDouble(frets, fretPosition);
			} else if (dotFrets.contains(fretPosition.fret % 12)) {
				if ((fretPosition.fret / 12) % 2 == 0) {
					addFretDotLow(frets, fretPosition);
				} else {
					addFretDotHigh(frets, fretPosition);
				}
			}
		}

		frets.draw(g);
	}

	private void drawStrings(final Graphics g) {
		final int strings = data.currentStrings();
		final int width = getWidth();
		final DrawableShapeList stringLines = new DrawableShapeList();

		for (int i = 0; i < strings; i++) {
			final Color color = getStringBasedColor(StringColorLabelType.LANE, i, strings);
			stringLines.add(lineHorizontal(0, width, stringPositions[i], color));
			final Color color2 = getStringBasedColor(StringColorLabelType.LANE_BRIGHT, i, strings);
			stringLines.add(lineHorizontal(0, width, stringPositions[i] + 1, color2));
		}

		stringLines.draw(g);
	}

	private void drawFretPressMarks(final Graphics g, final FretPosition[] fretPositions) {
		final int strings = data.currentStrings();
		final int baseFret = fretPositions[0].fret;
		final DrawableShapeList pressMarks = new DrawableShapeList();

		for (int i = 0; i < strings; i++) {
			final Integer fret = chordTemplate.frets.get(i);
			if (fret == null) {
				continue;
			}
			if (fret == 0) {
				for (int j = 0; j < 8; j++) {
					final StringColorLabelType type = j < 2 || j >= 6 ? StringColorLabelType.LANE_BRIGHT
							: StringColorLabelType.NOTE;
					final Color color = getStringBasedColor(type, i, strings);
					pressMarks.add(lineHorizontal(0, getWidth(), stringPositions[i] - 3 + j, color));
				}
				continue;
			}

			final FretPosition fretPosition = fretPositions[fret - baseFret];
			final Position2D position = new Position2D(fretPosition.position - fretPosition.length / 2,
					stringPositions[i]);
			pressMarks.add(
					filledDiamond(position.move(1, 0), 10, getStringBasedColor(StringColorLabelType.NOTE, i, strings)));

			final Integer finger = chordTemplate.fingers.get(i);
			final String fingerText = finger == null ? "" : finger == 0 ? "T" : finger.toString();
			pressMarks.add(new CenteredText(position, g.getFont(), fingerText, ColorLabel.BASE_TEXT.color()));
		}

		pressMarks.draw(g);
	}

	@Override
	protected void paintComponent(final Graphics g) {
		final FretPosition[] fretPositions = getFretPositions();
		drawBackground(g);
		drawFrets(g, fretPositions);
		drawStrings(g);
		drawFretPressMarks(g, fretPositions);
	}

	@Override
	public void keyTyped(final KeyEvent e) {
	}

	@Override
	public void keyPressed(final KeyEvent e) {
		if (mouseString == null || chordTemplate.frets.get(mouseString) == null) {
			return;
		}

		Integer fingerId;
		switch (e.getKeyCode()) {
		case KeyEvent.VK_T:
			fingerId = 0;
			break;
		case KeyEvent.VK_1:
			fingerId = 1;
			break;
		case KeyEvent.VK_2:
			fingerId = 2;
			break;
		case KeyEvent.VK_3:
			fingerId = 3;
			break;
		case KeyEvent.VK_4:
			fingerId = 4;
			break;
		default:
			return;
		}

		chordTemplate.fingers.put(mouseString, fingerId);
		parent.fingerUpdated(mouseString, fingerId);
	}

	@Override
	public void keyReleased(final KeyEvent e) {
	}

	private void updateMouseStringAndFret(final int x, final int y) {
		final int strings = data.currentStrings();

		mouseString = null;
		for (int i = 0; i < strings; i++) {
			final int stringPosition = stringPositions[i];
			if (y > stringPosition - 10 && y <= stringPosition + 10) {
				mouseString = i;
			}
		}

		mouseFret = null;
		final FretPosition[] fretPositions = getFretPositions();
		for (final FretPosition fretPosition : fretPositions) {
			if (x > fretPosition.position - fretPosition.length && x < fretPosition.position) {
				mouseFret = fretPosition.fret;
			}
		}

		if (mouseFret == null) {
			final FretPosition lastPosition = fretPositions[fretPositions.length - 1];
			if (lastPosition.fret < Config.frets && x > lastPosition.position) {
				mouseFret = lastPosition.fret + 1;
			}
		}
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		this.requestFocus();

		updateMouseStringAndFret(e.getX(), e.getY());

		if (mouseString == null || mouseFret == null) {
			return;
		}

		final Integer currentFret = chordTemplate.frets.get(mouseString);
		if (currentFret != null && currentFret == mouseFret) {
			chordTemplate.frets.remove(mouseString);
			chordTemplate.fingers.remove(mouseString);
			parent.fretUpdated(mouseString, null);
			parent.fingerUpdated(mouseString, null);
		} else {
			chordTemplate.frets.put(mouseString, mouseFret);
			parent.fretUpdated(mouseString, mouseFret);
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
	}

	@Override
	public void mouseExited(final MouseEvent e) {
		mouseFret = null;
		mouseString = null;
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		mouseMoved(e);
	}

	@Override
	public void mouseMoved(final MouseEvent e) {
		updateMouseStringAndFret(e.getX(), e.getY());
	}
}
