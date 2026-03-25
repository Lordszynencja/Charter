package log.charter.services.data.files;

import static log.charter.gui.components.utils.ComponentUtils.showPopup;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.menuHandlers.CharterMenuBar;
import log.charter.gui.panes.imports.MidiImportPane;
import log.charter.io.Logger;
import log.charter.io.midi.MidiReader;
import log.charter.io.midi.MidiReader.MidiFileData;
import log.charter.services.data.ChartTimeHandler;

public class MidiImporter {
	private ChartData chartData;
	private CharterFrame charterFrame;
	private CharterMenuBar charterMenuBar;
	private ChartTimeHandler chartTimeHandler;
	private SongFilesBackuper songFilesBackuper;

	public void importMidi(final File file) {
		try {
			final MidiFileData midiFileData = MidiReader.readBeatsMapFromMidi(file.getAbsolutePath());
			if (midiFileData.isEmpty()) {
				showPopup(charterFrame, Label.COULDNT_IMPORT_MIDI, file.getPath());
				return;
			}

			if (midiFileData.beats != null) {
				midiFileData.beats.makeBeatsUntilSongEnd(chartTimeHandler.audioTime());
				midiFileData.beats.fixFirstBeatInMeasures();
			}

			new MidiImportPane(chartData, charterFrame, charterMenuBar, songFilesBackuper, midiFileData);
		} catch (InvalidMidiDataException | IOException e) {
			Logger.error("Couldn't load beats map for path " + file.getAbsolutePath(), e);
			return;
		}
	}
}
