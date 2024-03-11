package log.charter.gui.components.tabs.selectionEditor.chords;

import static java.awt.event.KeyEvent.VK_1;
import static java.awt.event.KeyEvent.VK_2;
import static java.awt.event.KeyEvent.VK_3;
import static java.awt.event.KeyEvent.VK_4;
import static java.awt.event.KeyEvent.VK_T;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.util.Arrays.asList;
import static log.charter.gui.ChartPanelColors.getStringBasedColor;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledDiamond;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledOval;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.lineHorizontal;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.lineVertical;
import static log.charter.util.Utils.getStringPosition;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Set;
import java.util.function.Supplier;

import javax.swing.JComponent;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.song.ChordTemplate;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.ChartPanelColors.StringColorLabelType;
import log.charter.gui.chartPanelDrawers.drawableShapes.CenteredText;
import log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShapeList;
import log.charter.gui.chartPanelDrawers.drawableShapes.FilledRectangle;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePositionWithSize;
import log.charter.gui.components.containers.RowedPanel;
import log.charter.services.mouseAndKeyboard.KeyboardHandler;
import log.charter.util.collections.ArrayList2;
import log.charter.util.collections.HashSet2;
import log.charter.util.data.IntRange;
import log.charter.util.data.Position2D;

public class ChordTemplatePreview extends JComponent implements MouseListener, MouseMotionListener, KeyListener {
	private static final long serialVersionUID = 1L;
	private static final double fretsProportion = pow(2, -1.0 / 12);
	private static final int minFrets = 7;
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

	private int[] stringPositions;

	private final RowedPanel parent;
	private final ChordTemplateEditorInterface chordEditor;
	private boolean parentListenerAdded = false;

	private final ChartData data;
	private final Runnable focusRequester;
	private final KeyboardHandler keyboardHandler;

	private final Supplier<ChordTemplate> chordTemplateSupplier;

	private Integer mouseString;
	private Integer mouseFret;

	public ChordTemplatePreview(final RowedPanel parent, final ChordTemplateEditorInterface chordEditor,
			final ChartData data, final Runnable focusRequester, final KeyboardHandler keyboardHandler,
			final Supplier<ChordTemplate> chordTemplateSupplier) {
		super();
		this.parent = parent;
		this.chordEditor = chordEditor;

		this.data = data;
		this.focusRequester = focusRequester;
		this.keyboardHandler = keyboardHandler;

		this.chordTemplateSupplier = chordTemplateSupplier;

		changeStringsAmount();

		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);

		setFocusable(true);
		setRequestFocusEnabled(true);

		hideFields();
	}

	public int preferredHeight() {
		return fretStart + data.currentStrings() * parent.sizes.rowDistance;
	}

	private IntRange getFretsRange() {
		int min = Config.frets;
		int max = 0;
		for (final int fret : chordTemplateSupplier.get().frets.values()) {
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
		while (max - min < minFrets) {
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

	private void drawBackground(final Graphics2D g) {
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

	private void drawFrets(final Graphics2D g, final FretPosition[] fretPositions) {
		final DrawableShapeList frets = new DrawableShapeList();

		for (final FretPosition fretPosition : fretPositions) {
			frets.add(new CenteredText(new Position2D(fretPosition.position, 10), g.getFont(), fretPosition.fret + "",
					ColorLabel.BASE_DARK_TEXT));

			final int fretWidth = fretPosition.fret == 0 ? 3 : 1;
			final int x0 = fretPosition.position - fretWidth;
			final int x1 = x0 + 1;
			final int x3 = fretPosition.position + fretWidth;
			final int x2 = x3 - 1;
			frets.add(lineVertical(x0, fretStart, getHeight(), ColorLabel.BASE_BG_2.color()));
			frets.add(new FilledRectangle(x1, fretStart, x2 - x1 + 1, getHeight() - fretStart, ColorLabel.BASE_BG_4));
			frets.add(lineVertical(x3, fretStart, getHeight(), ColorLabel.BASE_BG_2.color()));

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

	private void drawStrings(final Graphics2D g) {
		final int width = getWidth();
		final DrawableShapeList stringLines = new DrawableShapeList();

		for (int i = 0; i < stringPositions.length; i++) {
			final Color color = getStringBasedColor(StringColorLabelType.LANE, i, stringPositions.length);
			stringLines.add(lineHorizontal(0, width, stringPositions[i], color));
			final Color color2 = getStringBasedColor(StringColorLabelType.LANE_BRIGHT, i, stringPositions.length);
			stringLines.add(lineHorizontal(0, width, stringPositions[i] + 1, color2));
		}

		stringLines.draw(g);
	}

	private void drawFretPressMarks(final Graphics2D g, final FretPosition[] fretPositions) {
		final int strings = data.currentStrings();
		final int baseFret = fretPositions[0].fret;
		final DrawableShapeList pressMarks = new DrawableShapeList();

		for (int i = 0; i < strings; i++) {
			final Integer fret = chordTemplateSupplier.get().frets.get(i);
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
			pressMarks.add(filledDiamond(position.move(1, 0), 10,
					getStringBasedColor(StringColorLabelType.NOTE, i, strings).darker()));

			final Integer finger = chordTemplateSupplier.get().fingers.get(i);
			final String fingerText = finger == null ? "" : finger == 0 ? "T" : finger.toString();
			pressMarks.add(new CenteredText(position, g.getFont(), fingerText, ColorLabel.BASE_TEXT_INPUT));
		}

		pressMarks.draw(g);
	}

	private void paintComponent2D(final Graphics2D g) {
		final FretPosition[] fretPositions = getFretPositions();
		drawBackground(g);
		drawFrets(g, fretPositions);
		drawStrings(g);
		drawFretPressMarks(g, fretPositions);
	}

	@Override
	protected void paintComponent(final Graphics g) {
		if (g instanceof Graphics2D) {
			paintComponent2D((Graphics2D) g);
		}
	}

	@Override
	public void keyTyped(final KeyEvent e) {
		if (!asList(VK_T, VK_1, VK_2, VK_3, VK_4).contains(e.getKeyCode())) {
			if (keyboardHandler != null) {
				keyboardHandler.keyTyped(e);
			}
			return;
		}
	}

	@Override
	public void keyPressed(final KeyEvent e) {
		if (!asList(VK_T, VK_1, VK_2, VK_3, VK_4).contains(e.getKeyCode())) {
			if (keyboardHandler != null) {
				keyboardHandler.keyPressed(e);
			}
			return;
		}

		if (mouseString == null || chordTemplateSupplier.get().frets.get(mouseString) == null) {
			return;
		}

		Integer fingerId;
		switch (e.getKeyCode()) {
			case VK_T:
				fingerId = 0;
				break;
			case VK_1:
				fingerId = 1;
				break;
			case VK_2:
				fingerId = 2;
				break;
			case VK_3:
				fingerId = 3;
				break;
			case VK_4:
				fingerId = 4;
				break;
			default:
				return;
		}

		chordTemplateSupplier.get().fingers.put(mouseString, fingerId);
		chordEditor.fingerUpdated(mouseString, fingerId);
		e.consume();
	}

	@Override
	public void keyReleased(final KeyEvent e) {
		if (!asList(VK_T, VK_1, VK_2, VK_3, VK_4).contains(e.getKeyCode())) {
			if (keyboardHandler != null) {
				keyboardHandler.keyReleased(e);
			}
			return;
		}
	}

	private void updateMouseStringAndFret(final int x, final int y) {
		final int strings = data.getCurrentArrangement().tuning.strings();

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

		final Integer currentFret = chordTemplateSupplier.get().frets.get(mouseString);
		if (currentFret != null && currentFret == mouseFret) {
			chordTemplateSupplier.get().frets.remove(mouseString);
			chordTemplateSupplier.get().fingers.remove(mouseString);
			chordEditor.fretUpdated(mouseString, null);
			chordEditor.fingerUpdated(mouseString, null);
		} else {
			chordTemplateSupplier.get().frets.put(mouseString, mouseFret);
			chordEditor.fretUpdated(mouseString, mouseFret);
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
		grabFocus();
	}

	@Override
	public void mouseExited(final MouseEvent e) {
		mouseFret = null;
		mouseString = null;

		focusRequester.run();
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		mouseMoved(e);
	}

	@Override
	public void mouseMoved(final MouseEvent e) {
		updateMouseStringAndFret(e.getX(), e.getY());
	}

	public void changeStringsAmount() {
		final int strings = data.currentStrings();
		final int stringSpace = parent.sizes.rowDistance;
		stringPositions = new int[strings];

		int y = fretStart + stringSpace / 2;
		for (int i = 0; i < strings; i++) {
			stringPositions[getStringPosition(i, strings)] = y;
			y += stringSpace;
		}
	}

	public void showFields() {
		setVisible(true);

		if (!parentListenerAdded) {
			parent.addKeyListener(this);
			parentListenerAdded = true;
		}
	}

	public void hideFields() {
		setVisible(false);

		if (parentListenerAdded) {
			parent.removeKeyListener(this);
			parentListenerAdded = false;
		}
	}
}
