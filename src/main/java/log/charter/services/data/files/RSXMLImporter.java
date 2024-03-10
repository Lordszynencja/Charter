package log.charter.services.data.files;

import static log.charter.gui.components.utils.ComponentUtils.showPopup;

import java.io.File;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.menuHandlers.CharterMenuBar;
import log.charter.io.Logger;
import log.charter.io.rs.xml.RSXMLToArrangement;
import log.charter.io.rs.xml.song.SongArrangement;
import log.charter.io.rs.xml.song.SongArrangementXStreamHandler;
import log.charter.io.rs.xml.vocals.ArrangementVocals;
import log.charter.io.rs.xml.vocals.VocalsXStreamHandler;
import log.charter.services.editModes.ModeManager;
import log.charter.song.Arrangement;
import log.charter.song.vocals.Vocals;
import log.charter.util.RW;

public class RSXMLImporter {
	private ChartData chartData;
	private CharterFrame charterFrame;
	private CharterMenuBar charterMenuBar;
	private ModeManager modeManager;
	private SongFileHandler songFileHandler;

	public void importAndAddRSArrangementXML(final File file) {
		try {
			final SongArrangement songArrangement = SongArrangementXStreamHandler.readSong(RW.read(file));
			final Arrangement arrangementChart = RSXMLToArrangement.toArrangement(songArrangement,
					chartData.songChart.beatsMap.beats);
			chartData.songChart.arrangements.add(arrangementChart);

			charterMenuBar.refreshMenus();

			modeManager.setArrangement(chartData.songChart.arrangements.size() - 1);
			songFileHandler.save();
		} catch (final Exception e) {
			Logger.error("Couldn't load arrangement", e);
			showPopup(charterFrame, Label.COULDNT_LOAD_ARRANGEMENT, e.getMessage());
		}
	}

	public void importRSVocalsXML(final File file) {
		try {
			final ArrangementVocals vocals = VocalsXStreamHandler.readVocals(RW.read(file));
			chartData.songChart.vocals = new Vocals(vocals);
			songFileHandler.save();
		} catch (final Exception e) {
			Logger.error("Couldn't load arrangement", e);
			showPopup(charterFrame, Label.COULDNT_LOAD_ARRANGEMENT, e.getMessage());
		}
	}
}
