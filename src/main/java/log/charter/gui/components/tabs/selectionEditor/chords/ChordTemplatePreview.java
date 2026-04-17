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
import static log.charter.data.config.ChartPanelColors.getStringBasedColor;
import static log.charter.data.config.GraphicalConfig.inputSize;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledDiamond;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledOval;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledRectangle;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.lineVertical;
import static log.charter.util.Utils.getStringPosition;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import javax.swing.JComponent;

import log.charter.data.ChartData;
import log.charter.data.config.ChartPanelColors.ColorLabel;
import log.charter.data.config.ChartPanelColors.StringColorLabelType;
import log.charter.data.config.Config;
import log.charter.data.config.values.InstrumentConfig;
import log.charter.data.song.ChordTemplate;
import log.charter.gui.chartPanelDrawers.drawableShapes.CenteredText;
import log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShapeList;
import log.charter.gui.chartPanelDrawers.drawableShapes.FilledRectangle;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePositionWithSize;
import log.charter.gui.chartPanelDrawers.drawableShapes.Text;
import log.charter.gui.components.containers.RowedPanel;
import log.charter.services.mouseAndKeyboard.KeyboardHandler;
import log.charter.util.SoundUtils;
import log.charter.util.data.IntRange;
import log.charter.util.data.Position2D;

public class ChordTemplatePreview extends JComponent implements MouseListener, MouseMotionListener, KeyListener {
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

	private static final long serialVersionUID = 1L;

	private static final double fretsProportion = pow(2, -1.0 / 12);
	private static final int minFrets = 7;
	private static final Set<Integer> dotFrets = new HashSet<>(asList(3, 5, 7, 9));

	private static int fretStart = inputSize * 11 / 10;
	private static int diamondSize = max(5, inputSize / 2);
	private static int fretThin = max(1, inputSize / 15);
	private static int fretWide = max(2, inputSize / 10);
	private static int stringWidth = max(1, inputSize / 15);

	private static Font font = new Font(Font.DIALOG, Font.BOLD, inputSize * 3 / 5);

	public static void recalculateSizes() {
		fretStart = inputSize * 11 / 10;
		font = new Font(Font.DIALOG, Font.BOLD, inputSize * 3 / 5);
		diamondSize = max(5, inputSize / 2);
		fretThin = max(1, inputSize / 15);
		fretWide = max(2, inputSize / 10);
		stringWidth = max(1, inputSize / 15);
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

	private int getPositionWithLeftHandedFlip(final int x) {
		return InstrumentConfig.leftHanded ? getWidth() - x : x;
	}

	public int preferredHeight() {
		return fretStart + data.currentStrings() * inputSize * 5 / 4;
	}

	private IntRange getFretsRange() {
		int min = InstrumentConfig.frets;
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
		if (max < InstrumentConfig.frets) {
			max++;
		}
		while (max - min < minFrets) {
			if (min > 0) {
				min--;
			}
			if (max < InstrumentConfig.frets) {
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
		final int x = getPositionWithLeftHandedFlip(fretPosition.position - fretPosition.length / 2);
		final ShapePositionWithSize position = new ShapePositionWithSize(x, y, diamondSize, diamondSize).centered();
		frets.add(filledOval(position, ColorLabel.BASE_BG_4));
	}

	private void addFretDotLow(final DrawableShapeList frets, final FretPosition fretPosition) {
		addDot(frets, fretPosition, getHeight() - inputSize * 5 / 4);
	}

	private void addFretDotHigh(final DrawableShapeList frets, final FretPosition fretPosition) {
		addDot(frets, fretPosition, fretStart + inputSize * 5 / 4);
	}

	private void addFretDotDouble(final DrawableShapeList frets, final FretPosition fretPosition) {
		addFretDotLow(frets, fretPosition);
		addFretDotHigh(frets, fretPosition);
	}

	private void drawFrets(final Graphics2D g, final FretPosition[] fretPositions) {
		final DrawableShapeList frets = new DrawableShapeList();

		for (final FretPosition fretPosition : fretPositions) {
			frets.add(new CenteredText(new Position2D(getPositionWithLeftHandedFlip(fretPosition.position), 10),
					g.getFont(), fretPosition.fret + "", ColorLabel.BASE_DARK_TEXT));

			final int fretWidth = fretPosition.fret == 0 ? fretWide : fretThin;
			final IntRange fretX = new IntRange(getPositionWithLeftHandedFlip(fretPosition.position - fretWidth),
					getPositionWithLeftHandedFlip(fretPosition.position + fretWidth));
			final int x1 = fretX.min + 1;
			final int x2 = fretX.max - 1;
			frets.add(lineVertical(fretX.min, fretStart, getHeight(), ColorLabel.BASE_BG_2.color()));
			frets.add(new FilledRectangle(x1, fretStart, x2 - x1 + 1, getHeight() - fretStart, ColorLabel.BASE_BG_4));
			frets.add(lineVertical(fretX.max, fretStart, getHeight(), ColorLabel.BASE_BG_2.color()));

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
		final DrawableShapeList strings = new DrawableShapeList();

		for (int i = 0; i < stringPositions.length; i++) {
			final Color color = getStringBasedColor(StringColorLabelType.LANE, i, stringPositions.length);
			final ShapePositionWithSize stringTop = new ShapePositionWithSize(0, stringPositions[i] - stringWidth,
					width, stringWidth);
			strings.add(filledRectangle(stringTop, color));
			final Color color2 = getStringBasedColor(StringColorLabelType.LANE_BRIGHT, i, stringPositions.length);
			final ShapePositionWithSize stringBottom = new ShapePositionWithSize(0, stringPositions[i], width,
					stringWidth);
			strings.add(filledRectangle(stringBottom, color2));
		}

		strings.draw(g);
	}

	private void drawOpenString(final DrawableShapeList pressMarks, final FretPosition[] fretPositions,
			final int string, final int strings) {
		final int width = getWidth();
		final Color color = getStringBasedColor(StringColorLabelType.NOTE, string, strings);
		final Color colorDark = getStringBasedColor(StringColorLabelType.LANE, string, strings);
		final ShapePositionWithSize top = new ShapePositionWithSize(0, stringPositions[string] - stringWidth * 2, width,
				stringWidth);
		pressMarks.add(filledRectangle(top, colorDark));
		final ShapePositionWithSize middle = new ShapePositionWithSize(0, stringPositions[string] - stringWidth, width,
				stringWidth * 2);
		pressMarks.add(filledRectangle(middle, color));
		final ShapePositionWithSize bottom = new ShapePositionWithSize(0, stringPositions[string] + stringWidth, width,
				stringWidth);
		pressMarks.add(filledRectangle(bottom, colorDark));

		if (Config.showNoteNames) {
			final int tone = SoundUtils.getSound(data.currentArrangement().tuning, data.currentArrangement().isBass(),
					string, 0);
			final String toneName = SoundUtils.soundToSimpleName(tone, parentListenerAdded);
			final Color toneColor = getStringBasedColor(StringColorLabelType.NOTE, string, strings);
			pressMarks.add(new Text(new Position2D(fretPositions[0].position + diamondSize / 2,
					stringPositions[string] - diamondSize * 7 / 5), font, toneName, toneColor));
		}
	}

	private void drawFretPressMark(final DrawableShapeList pressMarks, final FretPosition[] fretPositions,
			final int string, final int strings, final int fret, final int baseFret) {
		final FretPosition fretPosition = fretPositions[fret - baseFret];
		int x = fretPosition.position - fretPosition.length / 2;
		x = getPositionWithLeftHandedFlip(x);
		final Position2D position = new Position2D(x, stringPositions[string]);
		pressMarks.add(filledDiamond(position, diamondSize,
				getStringBasedColor(StringColorLabelType.NOTE, string, strings).darker()));

		final Integer finger = chordTemplateSupplier.get().fingers.get(string);
		if (finger != null) {
			final String fingerText = finger == 0 ? "T" : finger.toString();
			pressMarks.add(new CenteredText(position, font, fingerText, ColorLabel.BASE_TEXT_INPUT));
		}

		if (Config.showNoteNames) {
			final int tone = SoundUtils.getSound(data.currentArrangement().tuning, data.currentArrangement().isBass(),
					string, fret);
			final String toneName = SoundUtils.soundToSimpleName(tone, parentListenerAdded);
			final Color toneColor = getStringBasedColor(StringColorLabelType.NOTE, string, strings);
			pressMarks.add(new Text(position.move(diamondSize, -diamondSize * 7 / 5), font, toneName, toneColor));
		}
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
				drawOpenString(pressMarks, fretPositions, i, strings);
				continue;
			}

			drawFretPressMark(pressMarks, fretPositions, i, strings, fret, baseFret);
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
		final int strings = data.currentArrangement().tuning.strings();

		mouseString = null;
		for (int i = 0; i < strings; i++) {
			final int stringPosition = stringPositions[i];
			if (y > stringPosition - inputSize / 2 && y <= stringPosition + inputSize / 2) {
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
			if (lastPosition.fret < InstrumentConfig.frets && x > lastPosition.position) {
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

		updateMouseStringAndFret(getPositionWithLeftHandedFlip(e.getX()), e.getY());

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
		stringPositions = new int[strings];
		for (int i = 0; i < strings; i++) {
			stringPositions[getStringPosition(i, strings)] = fretStart + (i * 2 + 1) * inputSize * 5 / 8;
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
