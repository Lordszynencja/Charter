package log.charter.gui.components.tabs.selectionEditor.bends;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;
import static log.charter.data.config.Config.gridSize;
import static log.charter.data.config.Config.maxBendValue;
import static log.charter.data.config.Config.maxStrings;
import static log.charter.data.song.notes.IConstantPosition.findFirstIdAfterEqual;
import static log.charter.data.song.notes.IConstantPosition.findLastIdBeforeEqual;
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

import log.charter.data.song.BeatsMap;
import log.charter.data.song.BendValue;
import log.charter.data.song.notes.Chord;
import log.charter.data.song.notes.Note;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.ChartPanelColors.StringColorLabelType;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.Position2D;

public class BendEditorGraph extends JComponent implements MouseListener, MouseMotionListener {
	private static final int beatWidth = 100;
	private static final int labelsWidth = 30;
	private static final int bendValueDenominator = 2;
	private static final int maxBendInternalValue = maxBendValue * bendValueDenominator;
	public static final int height = 20 + 10 * maxBendInternalValue;

	private static int getDefaultXFromBendPosition(final double position) {
		return labelsWidth + (int) round(position * beatWidth);
	}

	private static int getZoomedXFromBendPosition(final double position) {
		return labelsWidth + (int) round(2 * position * beatWidth);
	}

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

	private int getXFromBendPosition(final double position) {
		if (isZoomed()) {
			return getZoomedXFromBendPosition(position);
		}
		return getDefaultXFromBendPosition(position);
	}

	private double getPositionFromX(final int x) {
		final int x0 = getXFromBendPosition(0);
		final int x1 = getXFromBendPosition(1);
		return 1.0 * (x - x0) / (x1 - x0);
	}

	private class EditorBendValue {
		public double position;
		public int value;

		public EditorBendValue(final double position, final int value) {
			this.position = position;
			this.value = value;
		}
	}

	private static class BendPositionWithId {
		public final int x;
		public int value;
		public final Integer id;

		public BendPositionWithId(final int x, final int value, final int id) {
			this.x = x;
			this.value = value;
			this.id = id;
		}

		public BendPositionWithId(final int x, final int value) {
			this.x = x;
			this.value = value;
			id = null;
		}
	}

	private static final long serialVersionUID = 5796481223743472294L;

	final BiConsumer<Integer, ArrayList2<BendValue>> onChangeBends;

	private BeatsMap beatsMap;

	private int notePosition = 0;
	private final int[] notesLengths = new int[maxStrings];
	private int firstBeatId = 0;
	private int lastBeatId = 1;
	private double noteStartPosition = 0;
	private double noteEndPosition = 1;

	private int string;
	private int strings;
	private final ArrayList2<EditorBendValue> bendValues = new ArrayList2<>();

	private int mouseX;
	private BendPositionWithId selectedBend = null;

	private int initialDragBendValue;

	public BendEditorGraph(final BeatsMap beatsMap, final BiConsumer<Integer, ArrayList2<BendValue>> onChangeBends) {
		super();
		strings = maxStrings;

		this.onChangeBends = onChangeBends;

		this.beatsMap = beatsMap;

		setBackground(ColorLabel.BASE_BG_3.color());

		calculateSize();

		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public void setBeatsMap(final BeatsMap beatsMap) {
		this.beatsMap = beatsMap;
	}

	private boolean isZoomed() {
		return noteEndPosition - noteStartPosition < 1;
	}

	private void calculateSize() {
		final int width = max(labelsWidth + 21 + beatWidth * 2,
				labelsWidth + 21 + (lastBeatId - firstBeatId) * beatWidth * (isZoomed() ? 2 : 1));
		final Dimension size = new Dimension(width, height);
		setMinimumSize(size);
		setMaximumSize(size);
		setPreferredSize(size);
		setSize(size);

		revalidate();
	}

	private void calculateBeatPositions() {
		firstBeatId = findLastIdBeforeEqual(beatsMap.beats, notePosition);
		lastBeatId = findFirstIdAfterEqual(beatsMap.beats, notePosition + notesLengths[string]);

		noteStartPosition = beatsMap.getPositionInBeats(notePosition) - firstBeatId;
		noteEndPosition = beatsMap.getPositionInBeats(notePosition + notesLengths[string]) - firstBeatId;

		calculateSize();
	}

	public void setNote(final Note note, final int strings) {
		string = note.string;
		this.strings = strings;
		notePosition = note.position();
		for (int i = 0; i < notesLengths.length; i++) {
			notesLengths[i] = 1;
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
			notesLengths[i] = 1;
		}
		chord.chordNotes.forEach((chordNoteString, chordNote) -> notesLengths[chordNoteString] = chordNote.length);
		calculateBeatPositions();

		setBendValues(string, chord.chordNotes.get(string).bendValues);
	}

	public void setBendValues(final int string, final ArrayList2<BendValue> bendValuesToEdit) {
		this.string = string;

		bendValues.clear();

		if (bendValuesToEdit != null) {
			for (final BendValue bendValueToEdit : bendValuesToEdit) {
				if (bendValueToEdit.position() > notesLengths[string]) {
					continue;
				}
				final int fullPosition = notePosition + bendValueToEdit.position();

				final double positionInBeats = beatsMap.getPositionInBeats(fullPosition);
				final double position = positionInBeats - firstBeatId;
				int value = (int) round(bendValueToEdit.bendValue.doubleValue() * bendValueDenominator);
				value = max(0, min(maxBendInternalValue, value));

				bendValues.add(new EditorBendValue(position, value));
			}
		}

		calculateBeatPositions();
		repaint();
	}

	private ArrayList2<BendValue> getBendValues() {
		final ArrayList2<BendValue> bendValuesForNote = new ArrayList2<>();
		for (final EditorBendValue bendValue : bendValues) {
			final int position = min(notesLengths[string],
					beatsMap.getPositionForPositionInBeats(bendValue.position + firstBeatId) - notePosition);
			final BigDecimal value = new BigDecimal(bendValue.value / (double) bendValueDenominator).setScale(2,
					RoundingMode.HALF_UP);
			bendValuesForNote.add(new BendValue(position, value));
		}

		return bendValuesForNote;
	}

	private BendPositionWithId getBendPosition() {
		final double gridLength = 1.0 / gridSize;
		final double mousePosition = getPositionFromX(mouseX);
		if (mousePosition > noteEndPosition + gridLength / 2 || mousePosition < noteStartPosition - gridLength / 2) {
			return null;
		}

		final int closestGrid = (int) round(mousePosition * gridSize);
		if (closestGrid < 0 || closestGrid > (lastBeatId - firstBeatId) * gridSize) {
			return null;
		}

		final double closestGridPosition = closestGrid * gridLength;
		Integer closestGridId = null;
		EditorBendValue closestGridBendValue = null;
		Integer closestId = null;
		EditorBendValue closestBendValue = null;
		for (int i = 0; i < bendValues.size(); i++) {
			final EditorBendValue bendValue = bendValues.get(i);
			if (closestId == null || abs(bendValue.position - mousePosition) < abs(
					bendValues.get(closestId).position - mousePosition)) {
				closestId = i;
				closestBendValue = bendValue;
			}

			if (abs(closestGridPosition - bendValue.position) < gridLength / 2) {
				closestGridId = i;
				closestGridBendValue = bendValue;
			}
		}

		final double distanceToClosestBendValue = closestBendValue == null ? 2
				: abs(closestBendValue.position - mousePosition);
		final double distanceToClosestGrid = abs(1.0 * closestGrid / gridSize - mousePosition);

		if (distanceToClosestBendValue < distanceToClosestGrid) {
			return new BendPositionWithId(getXFromBendPosition(closestBendValue.position), closestBendValue.value,
					closestId);
		}

		if (closestGridBendValue != null) {
			return new BendPositionWithId(getXFromBendPosition(closestGridBendValue.position), closestBendValue.value,
					closestGridId);
		}

		if (abs(noteStartPosition - closestGridPosition) < gridLength / 2) {
			return new BendPositionWithId(getXFromBendPosition(noteStartPosition), 0);
		}
		if (abs(noteEndPosition - closestGridPosition) < gridLength / 2) {
			return new BendPositionWithId(getXFromBendPosition(noteEndPosition),
					bendValues.isEmpty() ? 0 : bendValues.getLast().value);
		}

		return new BendPositionWithId(getXFromBendPosition(closestGrid * gridLength), -1);
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
		for (int i = 0; i <= lastBeatId - firstBeatId; i++) {
			for (int j = 1; j < gridSize; j++) {
				final int x = getXFromBendPosition(i + (double) j / gridSize);
				g.drawLine(x, y0, x, y1);
			}
		}
	}

	private void drawBeats(final Graphics g) {
		g.setColor(ColorLabel.MAIN_BEAT.color().brighter().brighter());
		final int y0 = getYFromBendValue(-1);
		final int y1 = getYFromBendValue(maxBendInternalValue + 1);
		for (int i = 0; i <= lastBeatId - firstBeatId; i++) {
			final int x = getXFromBendPosition(i);
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
				final double position = getPositionFromX(selectedBend.x);
				final EditorBendValue newBendValue = new EditorBendValue(position, value);

				bendValues.add(newBendValue);
				bendValues.sort((v0, v1) -> Double.compare(v0.position, v1.position));

				selectedBend = new BendPositionWithId(selectedBend.x, value, bendValues.indexOf(newBendValue));
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
