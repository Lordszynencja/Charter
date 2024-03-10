package log.charter.services.data.files;

import static log.charter.gui.components.utils.ComponentUtils.showPopup;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.BeatsMap;
import log.charter.gui.CharterFrame;
import log.charter.io.Logger;
import log.charter.io.midi.MidiReader;
import log.charter.services.data.ChartTimeHandler;

public class MidiImporter {
	private ChartData chartData;
	private CharterFrame charterFrame;
	private ChartTimeHandler chartTimeHandler;

	private BeatsMap getBeatsMap(final String path) {
		try {
			final BeatsMap beatsMap = MidiReader.readBeatsMapFromMidi(path);
			beatsMap.makeBeatsUntilSongEnd(chartTimeHandler.audioTime());

			return beatsMap;
		} catch (InvalidMidiDataException | IOException e) {
			Logger.error("Couldn't load beats map for path " + path, e);
			return null;
		}
	}

	public void importMidiTempo(final File file) {
		final BeatsMap beatsMap = getBeatsMap(file.getAbsolutePath());
		if (beatsMap == null) {
			showPopup(charterFrame, Label.COULDNT_IMPORT_MIDI_TEMPO, file.getPath());
			return;
		}

		chartData.songChart.beatsMap = beatsMap;
	}
}
