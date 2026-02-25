package log.charter.services.data.files;

import static java.util.Arrays.asList;
import static log.charter.gui.components.utils.ComponentUtils.showPopup;

import java.io.File;
import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.Arrangement;
import log.charter.data.song.Showlight;
import log.charter.data.song.Showlight.ShowlightType;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.vocals.VocalPath;
import log.charter.gui.CharterFrame;
import log.charter.gui.menuHandlers.CharterMenuBar;
import log.charter.io.Logger;
import log.charter.io.rs.xml.RSXMLToArrangement;
import log.charter.io.rs.xml.showlights.RsXmlShowlights;
import log.charter.io.rs.xml.showlights.RsXmlShowlightsXStreamHandler;
import log.charter.io.rs.xml.song.SongArrangement;
import log.charter.io.rs.xml.song.SongArrangementXStreamHandler;
import log.charter.io.rs.xml.vocals.ArrangementVocals;
import log.charter.io.rs.xml.vocals.VocalsXStreamHandler;
import log.charter.services.data.fixers.ArrangementFixer;
import log.charter.services.editModes.ModeManager;
import log.charter.util.CollectionUtils;
import log.charter.util.RW;

public class RSXMLImporter {
	private ChartData chartData;
	private CharterFrame charterFrame;
	private CharterMenuBar charterMenuBar;
	private ModeManager modeManager;
	private SongFileHandler songFileHandler;

	public void importAndAddRsShowlightsXml(final File file) {
		try {
			final RsXmlShowlights rsShowlights = RsXmlShowlightsXStreamHandler.readShowlights(RW.read(file, "UTF-8"));
			final List<Showlight> showlights = CollectionUtils.map(rsShowlights.showlights, showlight -> {
				final FractionalPosition position = FractionalPosition.fromTime(chartData.beats(), showlight.time);
				final ShowlightType type = ShowlightType.fromNote(showlight.note);
				return new Showlight(position, asList(type));
			});

			chartData.songChart.showlights(ArrangementFixer.fixShowlights(showlights));

			songFileHandler.save();
		} catch (final Exception e) {
			Logger.error("Couldn't load arrangement", e);
			showPopup(charterFrame, Label.COULDNT_LOAD_ARRANGEMENT, e.getMessage());
		}
	}

	public void importAndAddRsVocalsXML(final File file) {
		try {
			final ArrangementVocals vocals = VocalsXStreamHandler.readVocals(RW.read(file, "UTF-8"));
			final VocalPath newVocals = new VocalPath(chartData.beats(), vocals);

			chartData.addVocals(newVocals);
			charterMenuBar.refreshMenus();

			songFileHandler.save();
		} catch (final Exception e) {
			Logger.error("Couldn't load arrangement", e);
			showPopup(charterFrame, Label.COULDNT_LOAD_ARRANGEMENT, e.getMessage());
		}
	}

	public void importAndAddRSArrangementXML(final File file) {
		try {
			final SongArrangement songArrangement = SongArrangementXStreamHandler.readSong(file);
			final Arrangement arrangementChart = RSXMLToArrangement.toArrangement(songArrangement,
					chartData.songChart.beatsMap.immutable);

			chartData.songChart.arrangements.add(arrangementChart);
			modeManager.setArrangement(chartData.songChart.arrangements.size() - 1);
			charterMenuBar.refreshMenus();

			songFileHandler.save();
		} catch (final Exception e) {
			Logger.error("Couldn't load arrangement", e);
			showPopup(charterFrame, Label.COULDNT_LOAD_ARRANGEMENT, e.getMessage());
		}
	}
}
