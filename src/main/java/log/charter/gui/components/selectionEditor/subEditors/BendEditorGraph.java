package log.charter.gui.components.selectionEditor.subEditors;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;
import static log.charter.data.config.Config.gridSize;
import static log.charter.data.config.Config.maxBendValue;
import static log.charter.song.notes.IPosition.findFirstIdAfterEqual;
import static log.charter.song.notes.IPosition.findLastIdBeforeEqual;

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

import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.song.BeatsMap;
import log.charter.song.BendValue;
import log.charter.song.notes.Chord;
import log.charter.song.notes.Note;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.Position2D;

public class BendEditorGraph extends JComponent implements MouseListener, MouseMotionListener {
	private static final int beatWidth = 200;
	public static final int height = 40 + 40 * maxBendValue;
	private static final int maxBendInternalValue = maxBendValue * 4;

	private static int getXFromBendPosition(final double position) {
		return 20 + (int) round(position * beatWidth);
	}

	private static int getYFromBendValue(final int value) {
		return height - 20 - (int) round(value * 10);
	}

	private static double getPositionFromX(final int x) {
		final int x0 = getXFromBendPosition(0);
		final int x1 = getXFromBendPosition(1);
		return 1.0 * (x - x0) / (x1 - x0);
	}

	private static int getValueFromY(final int y) {
		final int y0 = getYFromBendValue(0);
		final int y1 = getYFromBendValue(1);
		int value = (int) round(1.0 * (y0 - y) / (y0 - y1));
		value = max(0, min(maxBendInternalValue, value));

		return value;
	}

	private class EditorBendValue {
		public double position;
		/**
		 * in quarter steps, maximum is 16 = 4 full steps
		 */
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
	private int noteLength = 1;
	private int firstBeatId = 0;
	private int lastBeatId = 1;
	private double noteStartPosition = 0;
	private double noteEndPosition = 1;

	private int string;
	private final ArrayList2<EditorBendValue> bendValues = new ArrayList2<>();

	private int mouseX;
	private BendPositionWithId selectedBend = null;

	private int initialDragBendValue;

	public BendEditorGraph(final BeatsMap beatsMap, final BiConsumer<Integer, ArrayList2<BendValue>> onChangeBends) {
		super();
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

	private void calculateSize() {
		final int width = max(141, 41 + (lastBeatId - firstBeatId) * beatWidth);
		final Dimension size = new Dimension(width, height);
		setMinimumSize(size);
		setMaximumSize(size);
		setPreferredSize(size);
		setSize(size);

		revalidate();
	}

	private void calculateBeatPositions() {
		firstBeatId = findLastIdBeforeEqual(beatsMap.beats, notePosition);
		lastBeatId = findFirstIdAfterEqual(beatsMap.beats, notePosition + noteLength);

		noteStartPosition = beatsMap.getPositionInBeats(notePosition) - firstBeatId;
		noteEndPosition = beatsMap.getPositionInBeats(notePosition + noteLength) - firstBeatId;

		calculateSize();
	}

	public void setNote(final Note note) {
		string = note.string;
		notePosition = note.position();
		noteLength = note.length();
		calculateBeatPositions();

		setBendValues(note.string, note.bendValues);
	}

	public void setChord(final Chord chord, final int string) {
		this.string = string;
		notePosition = chord.position();
		noteLength = chord.length();
		calculateBeatPositions();

		setBendValues(string, chord.bendValues.get(string));
	}

	public void setBendValues(final int string, final ArrayList2<BendValue> bendValuesToEdit) {
		this.string = string;

		bendValues.clear();

		if (bendValuesToEdit != null) {
			for (final BendValue bendValueToEdit : bendValuesToEdit) {
				if (bendValueToEdit.position() > noteLength) {
					continue;
				}
				final int fullPosition = notePosition + bendValueToEdit.position();

				final double positionInBeats = beatsMap.getPositionInBeats(fullPosition);
				final double position = positionInBeats - firstBeatId;
				int value = (int) round(bendValueToEdit.bendValue.doubleValue() * 4);
				value = max(0, min(maxBendInternalValue, value));

				bendValues.add(new EditorBendValue(position, value));
			}
		}

		repaint();
	}

	private ArrayList2<BendValue> getBendValues() {
		final ArrayList2<BendValue> bendValuesForNote = new ArrayList2<>();
		for (final EditorBendValue bendValue : bendValues) {
			final int position = beatsMap.getPositionForPositionInBeats(bendValue.position + firstBeatId)
					- notePosition;
			final BigDecimal value = new BigDecimal(bendValue.value / 4.0).setScale(2, RoundingMode.HALF_UP);
			bendValuesForNote.add(new BendValue(position, value));
		}

		return bendValuesForNote;
	}

	private BendPositionWithId getBendPosition() {
		final double gridLength = 1.0 / gridSize;
		final double mousePosition = (mouseX - getXFromBendPosition(0)) * 1.0 / beatWidth;
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

	private void paintBackground(final Graphics g) {
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());

		g.setColor(ColorLabel.valueOf("NOTE_" + string).color());
		final int noteX0 = getXFromBendPosition(noteStartPosition);
		final int noteX1 = getXFromBendPosition(noteEndPosition);
		g.fillRect(noteX0, getYFromBendValue(maxBendInternalValue + 1), noteX1 - noteX0,
				getYFromBendValue(maxBendInternalValue) - getYFromBendValue(maxBendInternalValue + 1));

		g.setColor(ColorLabel.BASE_2.color());
		for (int i = 0; i <= lastBeatId - firstBeatId; i++) {
			final int x = getXFromBendPosition(i);
			g.drawLine(x, 10, x, height - 10);
		}

		for (int i = 0; i <= maxBendInternalValue; i++) {
			g.setColor((i % 4 == 0 ? ColorLabel.BASE_2 : ColorLabel.BASE_1).color());
			final int y = getYFromBendValue(i);
			g.drawLine(10, y, getWidth(), y);
		}

		g.setColor(ColorLabel.BASE_TEXT.color());
		g.setFont(new Font(Font.DIALOG, Font.PLAIN, 10));
		for (int i = 0; i <= maxBendValue; i++) {
			final int y = getYFromBendValue(i * 4) + 4;
			g.drawString(i + "", 2, y);
		}
	}

	private void paintBends(final Graphics g) {
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

		g.setColor(ColorLabel.BASE_TEXT.color());
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
