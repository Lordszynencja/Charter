package log.charter.gui.panes;

import static log.charter.gui.components.TextInputWithValidation.ValueValidator.createBigDecimalValidator;
import static log.charter.gui.components.TextInputWithValidation.ValueValidator.createIntValidator;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

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
	public BigDecimal crowdSpeed;

	public SongOptionsPane(final CharterFrame frame, final ChartData data) {
		super(frame, Label.SONG_OPTIONS_PANE.label(), 8, getSizes());

		songChart = data.songChart;

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
		addIntegerConfigValue(4, 20, 130, Label.SONG_OPTIONS_YEAR, albumYear, 80,
				createIntValidator(Integer.MIN_VALUE, Integer.MAX_VALUE, true), //
				val -> albumYear = val, false);
		addConfigValue(5, 20, 130, Label.SONG_OPTIONS_CROWD_SPEED, crowdSpeed.toString(), 80,
				createBigDecimalValidator(new BigDecimal("0"), new BigDecimal("9999"), false), //
				val -> crowdSpeed = new BigDecimal(val), false);

		addButtons(7, this::saveAndExit);

		getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		getRootPane().registerKeyboardAction(e -> saveAndExit(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		validate();
		setVisible(true);
	}

	private void saveAndExit() {
		dispose();

		songChart.title = title;
		songChart.artistName = artistName;
		songChart.artistNameSort = artistNameSort;
		songChart.albumName = albumName;
		songChart.albumYear = albumYear;
	}
}
