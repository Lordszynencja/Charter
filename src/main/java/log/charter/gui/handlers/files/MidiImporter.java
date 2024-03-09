package log.charter.gui.handlers.files;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.utils.ComponentUtils;
import log.charter.gui.handlers.data.ChartTimeHandler;
import log.charter.io.Logger;
import log.charter.io.midi.MidiReader;
import log.charter.io.midi.Tempo;
import log.charter.io.midi.TempoMap;
import log.charter.song.Beat;
import log.charter.song.BeatsMap;
import log.charter.util.CollectionUtils.ArrayList2;

public class MidiImporter {
	private ChartData chartData;
	private CharterFrame charterFrame;
	private ChartTimeHandler chartTimeHandler;

	private BeatsMap getBeatsMap(final String path) {
		try {
			final TempoMap tempoMap = MidiReader.readMidi(path);

			final ArrayList2<Beat> beats = new ArrayList2<>();
			int kbpm = -1;
			int id = -1;

			for (final Tempo tempo : tempoMap.tempos) {
				while (tempo.id > id + 1) {
					final Beat lastBeat = beats.getLast();
					final int nextPosition = lastBeat.position() + 60_000_000 / kbpm;
					final Beat intermediateBeat = new Beat(lastBeat);
					intermediateBeat.position(nextPosition);
					beats.add(intermediateBeat);
					id++;
				}
				final Beat beat = new Beat((int) tempo.pos, tempo.numerator, tempo.denominator, false,
						tempo.kbpm != kbpm);
				beats.add(beat);
				kbpm = tempo.kbpm;
				id = tempo.id;
			}

			final BeatsMap beatsMap = new BeatsMap(beats);
			beatsMap.makeBeatsUntilSongEnd(chartTimeHandler.audioTime());
			beatsMap.fixFirstBeatInMeasures();

			return beatsMap;
		} catch (InvalidMidiDataException | IOException e) {
			Logger.error("Couldn't load beats map for path " + path, e);
			return null;
		}
	}

	public void importMidiTempo(final File file) {
		final BeatsMap beatsMap = getBeatsMap(file.getAbsolutePath());
		if (beatsMap == null) {
			Logger.error("Couldn't import tempo from midi file " + file.getAbsolutePath());
			ComponentUtils.showPopup(charterFrame, Label.COULDNT_IMPORT_MIDI_TEMPO, file.getPath());
			return;
		}

		chartData.songChart.beatsMap = beatsMap;
	}
}
