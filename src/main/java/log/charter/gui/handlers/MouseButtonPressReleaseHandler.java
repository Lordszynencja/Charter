package log.charter.gui.handlers;

import static java.lang.Math.abs;

import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import log.charter.data.managers.HighlightManager;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.util.Position2D;

public class MouseButtonPressReleaseHandler {
	public enum MouseButton {
		LEFT_BUTTON, //
		RIGHT_BUTTON;

		public static MouseButton fromEvent(final MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				return LEFT_BUTTON;
			}
			if (e.getButton() == MouseEvent.BUTTON3) {
				return RIGHT_BUTTON;
			}

			return null;
		}
	}

	public static class MouseButtonPressData {
		public final MouseButton button;
		public final Position2D position;
		public final PositionWithIdAndType highlight;

		public MouseButtonPressData(final MouseButton button, final Position2D position,
				final PositionWithIdAndType highlight) {
			this.button = button;
			this.position = position;
			this.highlight = highlight;
		}
	}

	public static class MouseButtonPressReleaseData {
		public final MouseButton button;
		public final PositionWithIdAndType pressHighlight;
		public final PositionWithIdAndType releaseHighlight;
		public final Position2D pressPosition;
		public final Position2D releasePosition;

		public MouseButtonPressReleaseData(final MouseButtonPressData pressData,
				final PositionWithIdAndType releaseHighlight, final int releaseX, final int releaseY) {
			button = pressData.button;
			pressHighlight = pressData.highlight;
			this.releaseHighlight = releaseHighlight;
			pressPosition = pressData.position;
			releasePosition = new Position2D(releaseX, releaseY);
		}

		public boolean isXDrag() {
			return abs(releasePosition.x - pressPosition.x) > 5;
		}
	}

	private HighlightManager highlightManager;

	private final Map<MouseButton, MouseButtonPressData> pressedButtons = new HashMap<>();

	public void init(final HighlightManager highlightManager) {
		this.highlightManager = highlightManager;
	}

	public void press(final MouseEvent e) {
		final MouseButton button = MouseButton.fromEvent(e);
		if (button != null) {
			final Position2D position = new Position2D(e.getX(), e.getY());
			final PositionWithIdAndType highlight = highlightManager.getHighlight(position.x, position.y);
			final MouseButtonPressData pressData = new MouseButtonPressData(button, position, highlight);
			pressedButtons.put(button, pressData);
		}
	}

	public MouseButtonPressData getPressPosition(final MouseButton button) {
		return pressedButtons.get(button);
	}

	public MouseButtonPressReleaseData release(final MouseEvent e) {
		final MouseButton button = MouseButton.fromEvent(e);
		if (button == null) {
			return null;
		}

		final MouseButtonPressData pressData = pressedButtons.get(button);
		if (pressData == null) {
			return null;
		}

		final PositionWithIdAndType highlight = highlightManager.getHighlight(e.getX(), e.getY());
		return new MouseButtonPressReleaseData(pressData, highlight, e.getX(), e.getY());
	}

	public void remove(final MouseEvent e) {
		final MouseButton button = MouseButton.fromEvent(e);
		if (button != null) {
			pressedButtons.remove(button);
		}
	}

	public void clear() {
		pressedButtons.clear();
	}
}
