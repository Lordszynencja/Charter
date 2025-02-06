package log.charter.services.data.files;

import static log.charter.gui.components.utils.ComponentUtils.showPopup;

import java.io.File;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.io.Logger;
import log.charter.io.gpa.GpaTrack;
import log.charter.io.gpa.GpaXmlXStreamHandler;

public class GpaXmlImporter {
	private ChartData chartData;
	private CharterFrame charterFrame;
	private FileDropHandler fileDropHandler;
	private GpaXmlXStreamHandler gpaXmlXStreamHandler;

	private void setMetadata(final GpaTrack gpaTrack) {
		if (chartData.songChart.artistName().isBlank()) {
			chartData.songChart.artistName(gpaTrack.artist);
		}
		if (chartData.songChart.title().isBlank()) {
			chartData.songChart.title(gpaTrack.title);
		}
	}

	public void importGpaXml(final File file) {
		try {
			final GpaTrack gpaTrack = GpaXmlXStreamHandler.readGpaTrack(file);
			System.out.println(gpaTrack.scoreUrl);
			setMetadata(gpaTrack);

			// TODO try to load audio from given url (ask user)
			// TODO try to load file from given tab url (ask user)
			// TODO set tempo map

		} catch (final Exception e) {
			Logger.error("Couldn't load arrangement", e);
			showPopup(charterFrame, Label.COULDNT_LOAD_ARRANGEMENT, e.getMessage());
		}
	}
}
