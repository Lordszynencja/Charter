package log.charter.gui.panes;

import java.math.BigDecimal;

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

	public SongOptionsPane(final CharterFrame frame) {
		super(frame, "Song options", 21);

		final SongChart songChart = frame.handler.data.songChart;
		title = songChart.title;
		artistName = songChart.artistName;
		artistNameSort = songChart.artistNameSort;
		albumName = songChart.albumName;
		albumYear = songChart.albumYear;
		crowdSpeed = songChart.crowdSpeed;

		addConfigValue(0, "Title", title, 300, null, //
				val -> title = val, false);
		addConfigValue(1, "Artist name", artistName, 300, null, //
				val -> artistName = val, false);
		addConfigValue(2, "Artist name (sorting)", artistNameSort, 300, null, //
				val -> artistNameSort = val, false);
		addConfigValue(3, "Album", albumName, 300, null, //
				val -> albumName = val, false);
		addConfigValue(4, "Year", albumYear, 80, createIntValidator(Integer.MIN_VALUE, Integer.MAX_VALUE, true), //
				val -> albumYear = (val == null) || val.isEmpty() ? null : Integer.valueOf(val), false);
		addConfigValue(5, "Crowd speed", crowdSpeed, 80,
				createBigDecimalValidator(new BigDecimal("0"), new BigDecimal("9999"), false), //
				val -> crowdSpeed = new BigDecimal(val), false);

		addButtons(20, e -> {
			songChart.title = title;
			songChart.artistName = artistName;
			songChart.artistNameSort = artistNameSort;
			songChart.albumName = albumName;
			songChart.albumYear = albumYear;

			frame.handler.songFileHandler.save();

			dispose();
		});

		validate();
		setVisible(true);
	}
}
