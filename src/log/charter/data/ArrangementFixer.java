package log.charter.data;

import log.charter.data.config.Config;
import log.charter.song.ArrangementChart;
import log.charter.song.Level;

public class ArrangementFixer {
	private ChartData data;

	public void init(final ChartData data) {
		this.data = data;
	}

	private void fixLevel(final Level level) {
		level.chordsAndNotes//
				.stream().filter(chordOrNote -> chordOrNote.note != null)//
				.map(chordOrNote -> chordOrNote.note)//
				.forEach(note -> note.length = note.length >= Config.minTailLength ? note.length : 0);
	}

	public void fixArrangement() {
		for (final ArrangementChart arrangementChart : data.songChart.arrangements) {
			for (final Level level : arrangementChart.levels.values()) {
				fixLevel(level);
			}
		}
	}
}
