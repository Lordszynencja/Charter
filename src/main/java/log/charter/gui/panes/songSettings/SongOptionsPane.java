package log.charter.gui.panes.songSettings;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.SongChart;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.ParamsPane;
import log.charter.gui.components.utils.validators.IntegerValueValidator;

public final class SongOptionsPane extends ParamsPane {
	private static final long serialVersionUID = -3193534671039163160L;

	private static final IntegerValueValidator yearValidator = new IntegerValueValidator(Integer.MIN_VALUE,
			Integer.MAX_VALUE, true);

	private final SongChart songChart;

	private String title;
	private String artistName;
	private String artistNameSort;
	private String albumName;
	private Integer albumYear;

	public SongOptionsPane(final CharterFrame frame, final ChartData data) {
		super(frame, Label.SONG_OPTIONS_PANE, 500);

		songChart = data.songChart;

		title = songChart.title();
		artistName = songChart.artistName();
		artistNameSort = songChart.artistNameSort();
		albumName = songChart.albumName();
		albumYear = songChart.albumYear;

		addStringConfigValue(0, 20, 130, Label.SONG_OPTIONS_TITLE, title, 300, //
				null, v -> title = v, false);
		addStringConfigValue(1, 20, 130, Label.SONG_OPTIONS_ARTIST_NAME, artistName, 300, //
				null, v -> artistName = v, false);
		addStringConfigValue(2, 20, 130, Label.SONG_OPTIONS_ARTIST_NAME_SORTING, artistNameSort, 300, //
				null, v -> artistNameSort = v, false);
		addStringConfigValue(3, 20, 130, Label.SONG_OPTIONS_ALBUM, albumName, 300, //
				null, v -> albumName = v, false);
		addIntegerConfigValue(4, 20, 130, Label.SONG_OPTIONS_YEAR, albumYear, 80, //
				yearValidator, v -> albumYear = v, false);

		this.setOnFinish(this::saveAndExit, null);
		this.addDefaultFinish(6);
	}

	private void saveAndExit() {
		songChart.title(title);
		songChart.artistName(artistName);
		songChart.artistNameSort(artistNameSort);
		songChart.albumName(albumName);
		songChart.albumYear = albumYear;
	}
}
