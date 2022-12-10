package log.charter.gui.modes;

import static java.lang.Math.min;

import java.util.List;

import log.charter.gui.ChartEventsHandler;
import log.charter.io.rs.xml.song.Chord;
import log.charter.song.Level;
import log.charter.song.Note;

public class GuitarModeHandler implements ModeHandler {

	private final ChartEventsHandler chartEventsHandler;

	public GuitarModeHandler(final ChartEventsHandler chartEventsHandler) {
		this.chartEventsHandler = chartEventsHandler;
	}

	@Override
	public void handleHome() {
		if (!chartEventsHandler.isCtrl()) {
			chartEventsHandler.setNextTime(0);
			return;
		}

		final Level currentLevel = chartEventsHandler.data.getCurrentArrangementLevel();
		final List<Chord> chords = currentLevel.chords;
		final List<Note> notes = currentLevel.notes;

		if (chords.isEmpty()) {
			if (notes.isEmpty()) {
				chartEventsHandler.setNextTime(0);
				return;
			}

			chartEventsHandler.setNextTime(notes.get(0).position);
			return;
		}

		if (notes.isEmpty()) {
			chartEventsHandler.setNextTime(chords.get(0).position);
			return;
		}

		chartEventsHandler.setNextTime(min(chords.get(0).position, notes.get(0).position));
	}

}
