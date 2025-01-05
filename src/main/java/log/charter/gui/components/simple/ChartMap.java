package log.charter.gui.components.simple;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.data.config.GraphicalConfig.chartMapHeightMultiplier;
import static log.charter.gui.ChartPanelColors.getStringBasedColor;
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
import java.util.List;
import java.util.function.Predicate;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.EventPoint;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.vocals.Vocal;
import log.charter.data.song.vocals.Vocal.VocalFlag;
import log.charter.gui.ChartPanel;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.ChartPanelColors.StringColorLabelType;
import log.charter.gui.CharterFrame;
import log.charter.io.Logger;
import log.charter.services.CharterContext.Initiable;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;
import log.charter.util.ExitActions;

public class ChartMap extends Component implements Initiable, MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 1L;

	private ChartData chartData;
	private CharterFrame charterFrame;
	private ChartPanel chartPanel;
	private ChartTimeHandler chartTimeHandler;
	private ModeManager modeManager;

	private BufferedImage background = null;

	private Thread imageMakerThread;

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
			case VOCALS:
				drawVocalLines(g);
				break;
			case GUITAR:
				drawPhrases(g);
				drawSections(g);
				drawNotes(g);
				break;
			default:
				break;
		}

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
			while (!imageMakerThread.isInterrupted()) {
				try {
					background = createBackground();
				} catch (final Exception e) {
					Logger.error("Couldn't create background for chart map", e);
				}

				try {
					Thread.sleep(1000);
				} catch (final InterruptedException e) {
					return;
				}
			}
		});
		imageMakerThread.setName("Chart map painter");
		imageMakerThread.start();

		ExitActions.addOnExit(() -> imageMakerThread.interrupt());
	}

	private int positionToTime(int p) {
		p = max(0, min(getWidth() - 1, p));
		return (int) ((double) p * chartTimeHandler.maxTime() / (getWidth() - 1));
	}

	private int timeToPosition(final int t) {
		return (int) ((long) t * (getWidth() - 1) / chartTimeHandler.maxTime());
	}

	private void drawBars(final Graphics g) {
		g.setColor(ColorLabel.MAIN_BEAT.color());

		chartData.beats().stream()//
				.filter(beat -> beat.firstInMeasure)//
				.forEach(beat -> {
					final int x = timeToPosition(beat.position());
					final Color color = (beat.anchor ? ColorLabel.MAIN_BEAT : ColorLabel.SECONDARY_BEAT).color();
					g.setColor(color);
					g.drawLine(x, 0, x, getHeight());
				});
	}

	private void drawVocalLines(final Graphics g) {
		g.setColor(ColorLabel.VOCAL_NOTE.color());

		final ImmutableBeatsMap beats = chartData.beats();
		final int y0 = chartMapHeightMultiplier;
		final int y2 = getHeight() - chartMapHeightMultiplier - 1;
		final int y1 = y0 + chartMapHeightMultiplier;
		boolean started = false;
		int x = 0;

		for (final Vocal vocal : chartData.currentVocals().vocals) {
			if (!started) {
				started = true;
				x = timeToPosition(vocal.position(beats));
			}

			if (vocal.flag() == VocalFlag.PHRASE_END) {
				final int x1 = timeToPosition(vocal.endPosition(beats));

				g.fillRect(x, y1, x1 - x, chartMapHeightMultiplier);
				g.drawLine(x, y0, x, y2);
				g.drawLine(x1, y0, x1, y2);
				started = false;
			}
		}
	}

	private void drawEventPoints(final Graphics g, final int y, final ColorLabel color,
			final Predicate<EventPoint> filter) {
		final ImmutableBeatsMap beats = chartData.beats();
		final List<EventPoint> points = chartData.currentArrangement().getFilteredEventPoints(filter);

		for (int i = 0; i < points.size(); i++) {
			final int pointTime = points.get(i).position(beats);
			final int nextPointTime = i + 1 < points.size() ? points.get(i + 1).position(beats)
					: chartTimeHandler.maxTime();

			final int x0 = timeToPosition(pointTime);
			final int x1 = timeToPosition(nextPointTime);
			final int width = max(1, x1 - x0 - 2);

			g.setColor(color.color());
			g.fillRect(x0, y, width, chartMapHeightMultiplier);
			g.setColor(color.color().darker());
			g.fillRect(x1 - 2, y, 2, chartMapHeightMultiplier);
		}
	}

	private void drawPhrases(final Graphics g) {
		drawEventPoints(g, chartMapHeightMultiplier, ColorLabel.PHRASE_NAME_BG, ep -> ep.phrase != null);
	}

	private void drawSections(final Graphics g) {
		drawEventPoints(g, 0, ColorLabel.SECTION_NAME_BG, ep -> ep.section != null);
	}

	private void drawNote(final Graphics g, final int string, final int position, final int length) {
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

		chartData.currentArrangementLevel().sounds.stream()//
				.flatMap(ChordOrNote::notes)//
				.forEach(note -> drawNote(g, note.string(), note.position(beats), note.length(beats)));
	}

	private void drawBookmarks(final Graphics g) {
		g.setColor(ColorLabel.BOOKMARK.color());

		chartData.songChart.bookmarks.forEach((number, position) -> {
			final int x = timeToPosition(position);
			g.drawLine(x, 0, x, getHeight());
			g.drawString(number + "", x + 2, 10);
		});
	}

	private void drawMarkerAndViewArea(final Graphics g) {
		final int markerPosition = timeToPosition(chartTimeHandler.time());

		final int x0 = markerPosition - timeToPosition(xToTimeLength(Config.markerOffset));
		final int x1 = markerPosition + timeToPosition(xToTimeLength(chartPanel.getWidth() - Config.markerOffset));
		g.setColor(ColorLabel.MARKER_VIEW_AREA.color());
		g.drawRect(x0, 0, x1 - x0, getHeight() - 1);

		g.setColor(ColorLabel.MARKER.color());
		g.drawLine(markerPosition, 0, markerPosition, getHeight() - 1);
		g.setColor(ColorLabel.MARKER.color().darker());
		g.drawLine(markerPosition + 1, 0, markerPosition + 1, getHeight() - 1);
	}

	@Override
	public void paint(final Graphics g) {
		if (modeManager.getMode() == EditMode.EMPTY) {
			g.setColor(ColorLabel.BASE_BG_0.color());
			g.fillRect(0, 0, getWidth(), getHeight());
			return;
		}

		if (background != null) {
			g.drawImage(background, 0, 0, null);
		} else {
			g.setColor(ColorLabel.BASE_BG_0.color());
			g.fillRect(0, 0, getWidth(), getHeight());
		}

		drawMarkerAndViewArea(g);
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

	public void triggerRedraw() {
		new Thread(() -> {
			try {
				background = createBackground();
			} catch (final Exception e) {
				Logger.error("Couldn't create background for chart map", e);
			}
		}).start();
	}
}
