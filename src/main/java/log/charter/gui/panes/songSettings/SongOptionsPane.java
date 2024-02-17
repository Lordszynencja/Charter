package log.charter.gui.panes.songSettings;

import static log.charter.gui.components.TextInputWithValidation.ValueValidator.createIntValidator;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.ParamsPane;
import log.charter.song.SongChart;

public final class SongOptionsPane extends ParamsPane {
	private static final long serialVersionUID = -3193534671039163160L;

	private static PaneSizes getSizes() {
		final PaneSizes sizes = new PaneSizes();
		sizes.lSpace = 20;
		sizes.labelWidth = 130;
		sizes.width = 500;

		return sizes;
	}

	private final SongChart songChart;

	private String title;
	private String artistName;
	private String artistNameSort;
	private String albumName;
	private Integer albumYear;

	public SongOptionsPane(final CharterFrame frame, final ChartData data) {
		super(frame, Label.SONG_OPTIONS_PANE, getSizes());

		songChart = data.songChart;

		title = songChart.title;
		artistName = songChart.artistName;
		artistNameSort = songChart.artistNameSort;
		albumName = songChart.albumName;
		albumYear = songChart.albumYear;

		addConfigValue(0, Label.SONG_OPTIONS_TITLE, title, 300, null, //
				val -> title = val, false);
		addConfigValue(1, Label.SONG_OPTIONS_ARTIST_NAME, artistName, 300, null, //
				val -> artistName = val, false);
		addConfigValue(2, Label.SONG_OPTIONS_ARTIST_NAME_SORTING, artistNameSort, 300, null, //
				val -> artistNameSort = val, false);
		addConfigValue(3, Label.SONG_OPTIONS_ALBUM, albumName, 300, null, //
				val -> albumName = val, false);
		addIntegerConfigValue(4, 20, 130, Label.SONG_OPTIONS_YEAR, albumYear, 80,
				createIntValidator(Integer.MIN_VALUE, Integer.MAX_VALUE, true), //
				val -> albumYear = val, false);

		this.addDefaultFinish(6, this::saveAndExit);
	}

	private void saveAndExit() {
		songChart.title = title;
		songChart.artistName = artistName;
		songChart.artistNameSort = artistNameSort;
		songChart.albumName = albumName;
		songChart.albumYear = albumYear;
	}
}
