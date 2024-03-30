package log.charter.gui.components.tabs.selectionEditor.bends;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;
import static log.charter.data.config.Config.gridSize;
import static log.charter.data.config.Config.maxBendValue;
import static log.charter.data.config.Config.maxStrings;
import static log.charter.gui.ChartPanelColors.getStringBasedColor;
import static log.charter.util.Utils.formatBendValue;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import javax.swing.JComponent;

import log.charter.data.song.BendValue;
import log.charter.data.song.notes.Chord;
import log.charter.data.song.notes.Note;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPosition;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.ChartPanelColors.StringColorLabelType;
import log.charter.util.data.Fraction;
import log.charter.util.data.Position2D;

public class BendEditorGraph extends JComponent implements MouseListener, MouseMotionListener {
	private static final int beatWidth = 100;
	private static final int labelsWidth = 30;
	private static final int bendValueDenominator = 2;
	private static final int maxBendInternalValue = maxBendValue * bendValueDenominator;
	public static final int height = 20 + 10 * maxBendInternalValue;

	private static int getYFromBendValue(final int value) {
		return height - 5 - (int) round(value * 10);
	}

	private static int getValueFromY(final int y) {
		final int y0 = getYFromBendValue(0);
		final int y1 = getYFromBendValue(1);
		int value = (int) round(1.0 * (y0 - y) / (y0 - y1));
		value = max(0, min(maxBendInternalValue, value));

		return value;
	}

	private static int getXFromBendPosition(final FractionalPosition position) {
		return labelsWidth + (int) round(position.doubleValue() * beatWidth);
	}

	private static FractionalPosition getPositionFromX(final int x) {
		final int x0 = getXFromBendPosition(new FractionalPosition(0));
		final int x1 = getXFromBendPosition(new FractionalPosition(1));
		return new FractionalPosition(new Fraction(x - x0, x1 - x0));
	}

	private static class EditorBendValue implements IConstantFractionalPosition {
		public FractionalPosition position;
		public int value;

		public EditorBendValue(final FractionalPosition position, final int value) {
			this.position = position;
			this.value = value;
		}

		@Override
		public FractionalPosition position() {
			return position;
		}
	}

	private static class BendPositionWithId {
		public final FractionalPosition position;
		public final int x;
		public int value;
		public final Integer id;

		public BendPositionWithId(final FractionalPosition position, final int value, final int id) {
			this.position = position;
			x = getXFromBendPosition(position);
			this.value = value;
			this.id = id;
		}

		public BendPositionWithId(final FractionalPosition position, final int value) {
			this.position = position;
			x = getXFromBendPosition(position);
			this.value = value;
			id = null;
		}
	}

	private static final long serialVersionUID = 5796481223743472294L;

	final BiConsumer<Integer, List<BendValue>> onChangeBends;

	private FractionalPosition notePosition = new FractionalPosition();
	private final FractionalPosition[] notesLengths = new FractionalPosition[maxStrings];
	private int firstBeatId = 0;
	private int lastBeatId = 1;
	private FractionalPosition noteStartPosition = new FractionalPosition();
	private FractionalPosition noteEndPosition = new FractionalPosition();

	private int string;
	private int strings;
	private final List<EditorBendValue> bendValues = new ArrayList<>();

	private int mouseX;
	private BendPositionWithId selectedBend = null;

	private int initialDragBendValue;

	public BendEditorGraph(final BiConsumer<Integer, List<BendValue>> onChangeBends) {
		super();
		strings = maxStrings;

		this.onChangeBends = onChangeBends;

		setBackground(ColorLabel.BASE_BG_3.color());

		calculateSize();

		addMouseListener(this);
		addMouseMotionListener(this);
	}

	private void calculateSize() {
		final int width = max(labelsWidth + 21 + beatWidth * 2,
				labelsWidth + 21 + (lastBeatId - firstBeatId) * beatWidth);
		final Dimension size = new Dimension(width, height);
		setMinimumSize(size);
		setMaximumSize(size);
		setPreferredSize(size);
		setSize(size);

		revalidate();
	}

	private void calculateBeatPositions() {
		firstBeatId = notePosition.beatId;

		final FractionalPosition endPosition = notePosition.add(notesLengths[string]);
		lastBeatId = endPosition.fraction.numerator > 0 ? endPosition.beatId + 1 : endPosition.beatId;

		noteStartPosition = notePosition.add(-firstBeatId);
		noteEndPosition = noteStartPosition.add(notesLengths[string]);

		calculateSize();
	}

	public void setNote(final Note note, final int strings) {
		string = note.string;
		this.strings = strings;
		notePosition = note.position();
		for (int i = 0; i < notesLengths.length; i++) {
			notesLengths[i] = new FractionalPosition();
		}
		notesLengths[string] = note.length();
		calculateBeatPositions();

		setBendValues(note.string, note.bendValues);
	}

	public void setChord(final Chord chord, final int string, final int strings) {
		this.string = string;
		this.strings = strings;
		notePosition = chord.position();
		for (int i = 0; i < notesLengths.length; i++) {
			notesLengths[i] = new FractionalPosition();
		}
		chord.chordNotes.forEach((chordNoteString, chordNote) -> notesLengths[chordNoteString] = chordNote.length());
		calculateBeatPositions();

		setBendValues(string, chord.chordNotes.get(string).bendValues);
	}

	public void setBendValues(final int string, final List<BendValue> bendValuesToEdit) {
		this.string = string;

		bendValues.clear();

		if (bendValuesToEdit != null) {
			for (final BendValue bendValueToEdit : bendValuesToEdit) {
				if (bendValueToEdit.position().add(notePosition.negate()).compareTo(notesLengths[string]) > 0) {
					continue;
				}

				final FractionalPosition inEditorPosition = bendValueToEdit.position()
						.add(new FractionalPosition(-firstBeatId));
				int value = (int) round(bendValueToEdit.bendValue.doubleValue() * bendValueDenominator);
				value = max(0, min(maxBendInternalValue, value));

				bendValues.add(new EditorBendValue(inEditorPosition, value));
			}
		}

		calculateBeatPositions();
		repaint();
	}

	private List<BendValue> getBendValues() {
		final List<BendValue> bendValuesForNote = new ArrayList<>();
		for (final EditorBendValue editorBendValue : bendValues) {
			final FractionalPosition actualPosition = editorBendValue.position.add(firstBeatId);
			final BigDecimal value = new BigDecimal(editorBendValue.value / (double) bendValueDenominator).setScale(2,
					RoundingMode.HALF_UP);
			bendValuesForNote.add(new BendValue(actualPosition, value));
		}

		return bendValuesForNote;
	}

	private BendPositionWithId getBendPosition() {
		final Fraction gridLength = new Fraction(1, gridSize);
		final Fraction halfGridLength = gridLength.divide(2);
		final FractionalPosition mousePosition = getPositionFromX(mouseX);
		if (mousePosition.compareTo(noteEndPosition.add(halfGridLength)) > 0//
				|| mousePosition.compareTo(noteStartPosition.add(halfGridLength.negate())) < 0) {
			return null;
		}

		final FractionalPosition gridPosition = mousePosition.round(gridLength);
		if (gridPosition.beatId < 0 || gridPosition.beatId > lastBeatId - firstBeatId) {
			return null;
		}

		Integer closestGridId = null;
		EditorBendValue closestGridBendValue = null;
		Integer closestId = null;
		EditorBendValue closestBendValue = null;
		for (int i = 0; i < bendValues.size(); i++) {
			final EditorBendValue bendValue = bendValues.get(i);
			if (closestId == null || bendValue.position.distance(mousePosition)
					.compareTo(bendValues.get(closestId).position.distance(mousePosition)) < 0) {
				closestId = i;
				closestBendValue = bendValue;
			}

			if (gridPosition.distance(bendValue.position).compareTo(halfGridLength) < 0) {
				closestGridId = i;
				closestGridBendValue = bendValue;
			}
		}

		if (closestBendValue != null) {
			final FractionalPosition closestBendValueDistance = closestBendValue.position.distance(mousePosition);
			final FractionalPosition gridPositionDistance = gridPosition.distance(mousePosition);

			if (closestBendValueDistance.compareTo(gridPositionDistance) < 0) {
				return new BendPositionWithId(closestBendValue.position, closestBendValue.value, closestId);
			}
		}
		if (closestGridBendValue != null) {
			return new BendPositionWithId(closestGridBendValue.position, closestBendValue.value, closestGridId);
		}
		if (noteStartPosition.distance(gridPosition).compareTo(halfGridLength) < 0) {
			return new BendPositionWithId(noteStartPosition, 0);
		}
		if (noteEndPosition.distance(gridPosition).compareTo(halfGridLength) < 0) {
			return new BendPositionWithId(noteEndPosition,
					bendValues.isEmpty() ? 0 : bendValues.get(bendValues.size() - 1).value);
		}

		return new BendPositionWithId(gridPosition, -1);
	}

	private void drawBackground(final Graphics g) {
		g.setColor(ColorLabel.BASE_BG_1.color());
		g.fillRect(0, 0, getWidth(), getHeight());
	}

	private void drawNoteLength(final Graphics g) {
		g.setColor(getStringBasedColor(StringColorLabelType.NOTE, string, strings));
		final int noteX0 = getXFromBendPosition(noteStartPosition);
		final int noteX1 = getXFromBendPosition(noteEndPosition);
		g.fillRect(noteX0, getYFromBendValue(maxBendInternalValue + 1), noteX1 - noteX0,
				getYFromBendValue(maxBendInternalValue) - getYFromBendValue(maxBendInternalValue + 1));
	}

	private void drawGrid(final Graphics g) {
		g.setColor(ColorLabel.GRID.color());

		final int y0 = getYFromBendValue(0);
		final int y1 = getYFromBendValue(maxBendInternalValue);
		FractionalPosition gridPosition = new FractionalPosition();
		final Fraction gridLength = new Fraction(1, gridSize);

		while (gridPosition.beatId <= lastBeatId - firstBeatId) {
			final int x = getXFromBendPosition(gridPosition);
			g.drawLine(x, y0, x, y1);
			gridPosition = gridPosition.add(gridLength);
		}
	}

	private void drawBeats(final Graphics g) {
		g.setColor(ColorLabel.MAIN_BEAT.color().brighter().brighter());
		final int y0 = getYFromBendValue(-1);
		final int y1 = getYFromBendValue(maxBendInternalValue + 1);
		for (int i = 0; i <= lastBeatId - firstBeatId; i++) {
			final int x = getXFromBendPosition(new FractionalPosition(i));
			g.drawLine(x, y0, x, y1);
		}
	}

	private void drawBendValues(final Graphics g) {
		for (int i = 0; i <= maxBendInternalValue; i++) {
			g.setColor((i % 4 == 0 ? ColorLabel.MAIN_BEAT.color() : ColorLabel.GRID.color()));
			final int y = getYFromBendValue(i);
			g.drawLine(labelsWidth, y, getWidth(), y);
		}

		g.setColor(ColorLabel.BASE_TEXT_INPUT.color());
		g.setFont(new Font(Font.DIALOG, Font.PLAIN, 10));
		for (int halfSteps = 0; halfSteps <= maxBendValue; halfSteps++) {
			final int bendValue = halfSteps * 2;
			final int y = getYFromBendValue(bendValue) + 4;
			g.drawString(formatBendValue(bendValue), 2, y);
		}
	}

	private void paintBackground(final Graphics g) {
		drawBackground(g);
		drawNoteLength(g);
		drawBeats(g);
		drawGrid(g);
		drawBendValues(g);
	}

	private void paintHighlightedBend(final Graphics g) {
		final BendPositionWithId highlightedBend = selectedBend == null ? getBendPosition() : selectedBend;
		if (highlightedBend != null) {
			g.setColor(ColorLabel.HIGHLIGHT.color());
			if (highlightedBend.value == -1) {
				final int x = highlightedBend.x;
				g.drawLine(x, getYFromBendValue(maxBendInternalValue), x, getYFromBendValue(0));
			} else {
				final int x = highlightedBend.x - 2;
				final int y = getYFromBendValue(highlightedBend.value) - 2;
				g.fillRect(x, y, 5, 5);
			}
		}
	}

	private List<Position2D> getBendPointsToDraw() {
		final List<Position2D> pointsToDraw = new ArrayList<>();
		int lastBendX = getXFromBendPosition(noteStartPosition);
		int lastBendY = getYFromBendValue(0);
		pointsToDraw.add(new Position2D(lastBendX, lastBendY));

		for (final EditorBendValue bendValue : bendValues) {
			final int x = getXFromBendPosition(bendValue.position);
			final int y = getYFromBendValue(bendValue.value);

			if (x <= lastBendX) {
				pointsToDraw.remove(pointsToDraw.size() - 1);
			}
			pointsToDraw.add(new Position2D(x, y));

			lastBendX = x;
			lastBendY = y;
		}

		final int endX = getXFromBendPosition(noteEndPosition);
		pointsToDraw.add(new Position2D(endX, lastBendY));

		return pointsToDraw;
	}

	private void paintBends(final Graphics g) {
		paintHighlightedBend(g);

		final List<Position2D> pointsToDraw = getBendPointsToDraw();

		g.setColor(ColorLabel.BASE_TEXT_INPUT.color());
		Position2D previousPoint = null;
		for (final Position2D point : pointsToDraw) {
			if (previousPoint != null) {
				g.drawLine(previousPoint.x, previousPoint.y, point.x, point.y);
			}
			g.fillRect(point.x - 1, point.y - 1, 3, 3);

			previousPoint = point;
		}
	}

	@Override
	public void paint(final Graphics g) {
		paintBackground(g);
		paintBends(g);
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		mouseX = e.getX();

		if (selectedBend != null) {
			final int value = getValueFromY(e.getY());
			bendValues.get(selectedBend.id).value = value;
			selectedBend.value = value;
		}

		repaint();
	}

	@Override
	public void mouseMoved(final MouseEvent e) {
		mouseX = e.getX();

		repaint();
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			selectedBend = getBendPosition();
			initialDragBendValue = selectedBend.value;

			final int value = getValueFromY(e.getY());
			if (selectedBend == null || selectedBend.id == null) {
				final FractionalPosition position = selectedBend.position;
				final EditorBendValue newBendValue = new EditorBendValue(position, value);

				bendValues.add(newBendValue);
				bendValues.sort(IConstantFractionalPosition::compareTo);

				selectedBend = new BendPositionWithId(position, value, bendValues.indexOf(newBendValue));
			} else {
				bendValues.get(selectedBend.id).value = value;
				selectedBend.value = value;
			}

			repaint();
			return;
		}

		if (e.getButton() == MouseEvent.BUTTON3) {
			final BendPositionWithId pressedBend = getBendPosition();
			if (pressedBend.id != null) {
				bendValues.remove((int) pressedBend.id);
				onChangeBends.accept(string, getBendValues());

				repaint();
			}

			return;
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (selectedBend != null && selectedBend.value != initialDragBendValue) {
				onChangeBends.accept(string, getBendValues());
			}
			initialDragBendValue = -1;
			selectedBend = null;
			return;
		}
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
	}

	@Override
	public void mouseExited(final MouseEvent e) {
	}
}
