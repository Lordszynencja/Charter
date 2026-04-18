package log.charter.gui.components.simple;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.data.config.ChartPanelColors.getStringBasedColor;
import static log.charter.data.config.GraphicalConfig.chartMapHeightMultiplier;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.chartMapHeight;
import static log.charter.util.ScalingUtils.xToTimeLength;
import static log.charter.util.Utils.getStringPosition;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import log.charter.data.ChartData;
import log.charter.data.config.ChartPanelColors.ColorLabel;
import log.charter.data.config.ChartPanelColors.StringColorLabelType;
import log.charter.data.config.GraphicalConfig;
import log.charter.data.song.Beat;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.EventPoint;
import log.charter.data.song.Showlight;
import log.charter.data.song.Showlight.ShowlightType;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.position.virtual.IVirtualConstantPosition;
import log.charter.data.song.vocals.Vocal;
import log.charter.data.song.vocals.Vocal.VocalFlag;
import log.charter.data.types.PositionType;
import log.charter.gui.ChartPanel;
import log.charter.gui.CharterFrame;
import log.charter.io.Logger;
import log.charter.services.CharterContext.Initiable;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.data.selection.SelectionManager;
import log.charter.services.editModes.ModeManager;
import log.charter.util.ExitActions;

public class ChartMap extends Component implements Initiable, MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 1L;

	private ChartData chartData;
	private CharterFrame charterFrame;
	private ChartPanel chartPanel;
	private ChartTimeHandler chartTimeHandler;
	private ModeManager modeManager;
	private SelectionManager selectionManager;

	private BufferedImage background = null;
	private boolean redraw = false;

	private Thread imageMakerThread;

	private int positionToTime(int p) {
		p = max(0, min(getWidth() - 1, p));
		return (int) ((double) p * chartTimeHandler.maxTime() / (getWidth() - 1));
	}

	private int timeToPosition(final double t) {
		return (int) (t * (getWidth() - 1) / chartTimeHandler.maxTime());
	}

	private void drawBars(final Graphics g) {
		g.setColor(ColorLabel.MAIN_BEAT.color());

		final ImmutableBeatsMap beats = chartData.beats();

		for (int i = 0; i < beats.size(); i++) {
			final Beat beat = beats.get(i);
			if (!beat.firstInMeasure) {
				continue;
			}

			final int x = timeToPosition(beat.position());
			final Color color = (beat.anchor ? ColorLabel.MAIN_BEAT : ColorLabel.SECONDARY_BEAT).color();
			g.setColor(color);
			g.drawLine(x, 0, x, getHeight());
		}
	}

	private void drawShowlight(final Graphics g, final int x0, final int x1, final int y0, final int y1,
			final ShowlightType type) {
		if (type == ShowlightType.BEAMS_OFF || type == ShowlightType.LASERS_OFF) {
			return;
		}

		if (type.isFog) {
			g.setColor(type.color.darker().darker().darker());
			g.fillRect(x0, y0, x1 - x0, y1 - y0);
		} else if (type.isBeam) {
			if (type == ShowlightType.BEAMS_OFF) {
				return;
			}

			g.setColor(type.color.darker());
			g.fillRect(x0, y0, x1 - x0, y1 - y0);
		} else if (type.isLaser) {
			if (type == ShowlightType.LASERS_OFF) {
				return;
			}

			g.setColor(type.color);
			final int spacing = 4;
			for (int x = x0 + (x0 % spacing == 0 ? 0 : (spacing - x0 % spacing)); x < x1; x += spacing) {
				g.drawLine(x, y0, x, y1);
			}
		}
	}

	private void drawShowlights(final Graphics g, final int y0, final int y1, final ShowlightType defaultValue,
			final List<Showlight> showlights) {
		final ImmutableBeatsMap beats = chartData.beats();
		ShowlightType last = defaultValue;
		int lastX = timeToPosition(beats.get(0).position());

		for (final Showlight showlight : showlights) {
			final int x = timeToPosition(showlight.position(beats));

			drawShowlight(g, lastX, x, y0, y1, last);

			lastX = x;
			for (final ShowlightType type : showlight.types) {
				last = type;
			}
		}

		drawShowlight(g, lastX, getWidth(), y0, y1, last);
	}

	private void drawShowlightsSelect(final Graphics g) {
		final List<IVirtualConstantPosition> selected = selectionManager.getSelectedElements(PositionType.SHOWLIGHT);
		if (selected.isEmpty()) {
			return;
		}

		final ImmutableBeatsMap beats = chartData.beats();

		for (final IVirtualConstantPosition element : selected) {
			final int x = timeToPosition(element.toPosition(beats).position());
			g.setColor(ColorLabel.SELECT.color());
			g.drawLine(x, 0, x, getHeight());
		}
	}

	private void drawShowlights(final Graphics g) {
		final int y0 = 0;
		final int y2 = getHeight();
		final int y1 = y2 / 2;

		drawShowlights(g, y0, y1, ShowlightType.FOG_GREEN, chartData.showlightsFog());
		drawShowlights(g, y1, y2, ShowlightType.BEAMS_OFF, chartData.showlightsBeam());
		drawShowlights(g, y1, y2, ShowlightType.LASERS_OFF, chartData.showlightsLaser());
		drawShowlightsSelect(g);
	}

	private void drawVocalLines(final Graphics g) {
		final ImmutableBeatsMap beats = chartData.beats();
		final int y0 = chartMapHeightMultiplier;
		final int y2 = getHeight() - chartMapHeightMultiplier - 1;
		final int y1 = (y0 + y2) / 2;
		boolean started = false;
		int x = 0;

		for (final Vocal vocal : chartData.currentVocals().vocals) {
			if (!started) {
				started = true;
				x = timeToPosition(vocal.position(beats));
			}

			if (vocal.flag() == VocalFlag.PHRASE_END) {
				final int x1 = timeToPosition(vocal.endPosition(beats));

				g.setColor(ColorLabel.VOCAL_NOTE.color());
				g.fillRect(x, y1 - chartMapHeightMultiplier, x1 - x, chartMapHeightMultiplier * 2);
				g.drawLine(x, y0, x, y2);
				g.drawLine(x1, y0, x1, y2);
				started = false;
			}
		}

		final List<Vocal> selectedVocals = selectionManager.getSelectedElements(PositionType.VOCAL);
		for (final Vocal vocal : selectedVocals) {
			final int x0 = timeToPosition(vocal.position(beats));
			final int x1 = timeToPosition(vocal.endPosition(beats));
			g.setColor(ColorLabel.VOCAL_HIGHLIGHT.color());
			g.drawRect(x0, y1 - chartMapHeightMultiplier, x1 - x0, chartMapHeightMultiplier * 2);
		}
	}

	private double getMaxPhraseTime() {
		final List<EventPoint> endPhrases = chartData.currentArrangement()
				.getFilteredEventPoints(ep -> "END".equals(ep.phrase));

		if (endPhrases.isEmpty()) {
			return chartTimeHandler.maxTime();
		}

		final EventPoint endPhrase = endPhrases.get(0);
		return endPhrase.position(chartData.beats()) - 1;
	}

	private void drawEventPoints(final Graphics g, final int y, final ColorLabel color,
			final Predicate<EventPoint> filter) {
		final ImmutableBeatsMap beats = chartData.beats();
		final double maxPhraseTime = getMaxPhraseTime();
		final List<EventPoint> points = chartData.currentArrangement().getFilteredEventPoints(filter);

		for (int i = 0; i < points.size(); i++) {
			final double pointTime = points.get(i).position(beats);
			if (pointTime >= maxPhraseTime) {
				break;
			}

			final double nextPointTime = i + 1 < points.size() ? points.get(i + 1).position(beats) : maxPhraseTime;

			final int x0 = timeToPosition(pointTime);
			final int x1 = timeToPosition(nextPointTime);
			final int width = max(1, x1 - x0 - 2);

			g.setColor(color.color());
			g.fillRect(x0, y, width, chartMapHeightMultiplier);
			g.setColor(color.color().darker());
			g.fillRect(x1 - 2, y, 2, chartMapHeightMultiplier);
		}
	}

	private void drawSelectedEventPoints(final Graphics g) {
		final List<EventPoint> points = chartData.currentEventPoints();
		final List<Integer> selectedIds = selectionManager.getSelectedIds(PositionType.EVENT_POINT);
		final ImmutableBeatsMap beats = chartData.beats();
		final double maxTime = getMaxPhraseTime();
		g.setColor(ColorLabel.SELECT.color());

		for (final int id : selectedIds) {
			final double pointTime = points.get(id).position(beats);
			if (pointTime >= maxTime) {
				break;
			}

			final double nextPointTime = id + 1 < points.size() ? points.get(id + 1).position(beats) : maxTime;

			final int x0 = timeToPosition(pointTime);
			final int x1 = timeToPosition(nextPointTime);
			final int width = max(1, x1 - x0 - 2);

			g.drawRect(x0, 0, width, chartMapHeightMultiplier * 2 - 1);
		}
	}

	private void drawSections(final Graphics g) {
		drawEventPoints(g, 0, ColorLabel.SECTION_NAME_BG, ep -> ep.section != null);
	}

	private void drawPhrases(final Graphics g) {
		drawEventPoints(g, chartMapHeightMultiplier, ColorLabel.PHRASE_NAME_BG, ep -> ep.phrase != null);
	}

	private void drawNoteSelect(final Graphics g, final int string, final double position, final double length) {
		g.setColor(ColorLabel.SELECT.color());

		final int x0 = timeToPosition(position);
		final int x1 = timeToPosition(position + length);
		final int y0 = 2 * chartMapHeightMultiplier + 1
				+ getStringPosition(string, chartData.currentStrings()) * chartMapHeightMultiplier;
		g.drawRect(x0 - 1, y0 - 1, 2, chartMapHeightMultiplier + 1);

		if (x1 > x0 + 1) {
			final int y1 = y0 + chartMapHeightMultiplier - 1;
			g.drawRect(x0 + 1, y1 - 1, x1 - x0, 2);
		}
	}

	private void drawNote(final Graphics g, final int string, final double position, final double length) {
		g.setColor(getStringBasedColor(StringColorLabelType.NOTE, string, chartData.currentStrings()));

		final int x0 = timeToPosition(position);
		final int x1 = timeToPosition(position + length);
		final int y0 = 2 * chartMapHeightMultiplier + 1
				+ getStringPosition(string, chartData.currentStrings()) * chartMapHeightMultiplier;
		final int y1 = y0 + chartMapHeightMultiplier - 1;
		g.drawLine(x0, y0, x0, y1);
		if (x1 > x0) {
			g.drawLine(x0, y1, x1, y1);
		}
	}

	private void drawNotes(final Graphics g) {
		final ImmutableBeatsMap beats = chartData.beats();
		final List<ChordOrNote> sounds = chartData.currentArrangementLevel().sounds;
		final Set<Integer> selectedIds = new HashSet<>(selectionManager.getSelectedIds(PositionType.GUITAR_NOTE));
		final List<ChordOrNote> selectedSounds = new ArrayList<>(selectedIds.size());

		for (int i = 0; i < sounds.size(); i++) {
			final ChordOrNote sound = sounds.get(i);
			if (selectedIds.contains(i)) {
				selectedSounds.add(sound);
				continue;
			}

			sound.notes().forEach(note -> drawNote(g, note.string(), note.position(beats), note.length(beats)));
		}

		for (final ChordOrNote sound : selectedSounds) {
			sound.notes().forEach(note -> drawNoteSelect(g, note.string(), note.position(beats), note.length(beats)));
		}
		for (final ChordOrNote sound : selectedSounds) {
			sound.notes().forEach(note -> drawNote(g, note.string(), note.position(beats), note.length(beats)));
		}
	}

	private void drawBookmarks(final Graphics g) {
		g.setColor(ColorLabel.BOOKMARK.color());

		chartData.songChart.bookmarks.forEach((number, position) -> {
			final int x = timeToPosition(position);
			g.drawLine(x, 0, x, getHeight());
			g.drawString(number + "", x + 2, 10);
		});
	}

	private BufferedImage createBackground() {
		final BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		final Graphics g = img.getGraphics();

		g.setColor(ColorLabel.LANE.color());
		g.fillRect(0, 0, getWidth(), getHeight());
		if (chartData.isEmpty) {
			return img;
		}

		switch (modeManager.getMode()) {
			case TEMPO_MAP:
				drawBars(g);
				break;
			case SHOWLIGHTS:
				drawShowlights(g);
				break;
			case VOCALS:
				drawVocalLines(g);
				break;
			case GUITAR:
				drawSections(g);
				drawPhrases(g);
				drawSelectedEventPoints(g);
				drawNotes(g);
				break;
			default:
				break;
		}

		drawShowlightsSelect(g);
		drawBookmarks(g);

		return img;
	}

	@Override
	public void init() {
		setSize(charterFrame.getWidth(), chartMapHeight);

		setFocusable(false);
		addMouseListener(this);
		addMouseMotionListener(this);

		imageMakerThread = new Thread(() -> {
			long lastRedraw = System.currentTimeMillis();
			while (!imageMakerThread.isInterrupted()) {
				while (System.currentTimeMillis() - lastRedraw < 10_000 && !redraw) {
					try {
						Thread.sleep(1);
					} catch (final InterruptedException e) {
						return;
					}
				}
				try {
					background = createBackground();
					lastRedraw = System.currentTimeMillis();
					redraw = false;
					repaint();
				} catch (final Exception e) {
					Logger.error("Couldn't create background for chart map", e);
				}
			}
		});
		imageMakerThread.setName("Chart map painter");
		imageMakerThread.start();

		ExitActions.addOnExit(() -> imageMakerThread.interrupt());
	}

	private void drawMarkerAndViewArea(final Graphics g) {
		final int markerPosition = timeToPosition(chartTimeHandler.time());

		final int x0 = markerPosition - timeToPosition(xToTimeLength(GraphicalConfig.markerOffset));
		final int x1 = markerPosition
				+ timeToPosition(xToTimeLength(chartPanel.getWidth() - GraphicalConfig.markerOffset));
		g.setColor(ColorLabel.MARKER_VIEW_AREA.color());
		g.drawRect(x0, 0, x1 - x0, getHeight() - 1);

		g.setColor(ColorLabel.MARKER.color());
		g.drawLine(markerPosition, 0, markerPosition, getHeight() - 1);
		g.setColor(ColorLabel.MARKER.color().darker());
		g.drawLine(markerPosition + 1, 0, markerPosition + 1, getHeight() - 1);
	}

	@Override
	public void paint(final Graphics g) {
		if (background != null) {
			g.drawImage(background, 0, 0, null);
			drawMarkerAndViewArea(g);
		} else {
			g.setColor(ColorLabel.BASE_BG_0.color());
			g.fillRect(0, 0, getWidth(), getHeight());
		}
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		chartTimeHandler.nextTime(positionToTime(e.getX()));
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
		this.requestFocusInWindow();
	}

	@Override
	public void mouseExited(final MouseEvent e) {
		charterFrame.requestFocusInWindow();
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		chartTimeHandler.nextTime(positionToTime(e.getX()));
	}

	@Override
	public void mouseMoved(final MouseEvent e) {
	}

	public void redraw() {
		redraw = true;
	}
}
