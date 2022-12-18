package log.charter.gui.panes;

import java.math.BigDecimal;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.song.SongChart;

public final class SongOptionsPane extends ParamsPane {
	private static final long serialVersionUID = -3193534671039163160L;;

	private String title;
	private String artistName;
	private String artistNameSort;
	private String albumName;
	private Integer albumYear;
	public BigDecimal crowdSpeed;

	public SongOptionsPane(final CharterFrame frame, final ChartData data) {
		super(frame, Label.SONG_OPTIONS_PANE.label(), 21);

		final SongChart songChart = data.songChart;
		title = songChart.title;
		artistName = songChart.artistName;
		artistNameSort = songChart.artistNameSort;
		albumName = songChart.albumName;
		albumYear = songChart.albumYear;
		crowdSpeed = songChart.crowdSpeed;

		addConfigValue(0, Label.SONG_OPTIONS_TITLE, title, 300, null, //
				val -> title = val, false);
		addConfigValue(1, Label.SONG_OPTIONS_ARTIST_NAME, artistName, 300, null, //
				val -> artistName = val, false);
		addConfigValue(2, Label.SONG_OPTIONS_ARTIST_NAME_SORTING, artistNameSort, 300, null, //
				val -> artistNameSort = val, false);
		addConfigValue(3, Label.SONG_OPTIONS_ALBUM, albumName, 300, null, //
				val -> albumName = val, false);
		addConfigValue(4, Label.SONG_OPTIONS_YEAR, albumYear, 80,
				createIntValidator(Integer.MIN_VALUE, Integer.MAX_VALUE, true), //
				val -> albumYear = (val == null) || val.isEmpty() ? null : Integer.valueOf(val), false);
		addConfigValue(5, Label.SONG_OPTIONS_CROWD_SPEED, crowdSpeed, 80,
				createBigDecimalValidator(new BigDecimal("0"), new BigDecimal("9999"), false), //
				val -> crowdSpeed = new BigDecimal(val), false);

		addButtons(20, e -> {
			songChart.title = title;
			songChart.artistName = artistName;
			songChart.artistNameSort = artistNameSort;
			songChart.albumName = albumName;
			songChart.albumYear = albumYear;

			dispose();
		});

		validate();
		setVisible(true);
	}
}
