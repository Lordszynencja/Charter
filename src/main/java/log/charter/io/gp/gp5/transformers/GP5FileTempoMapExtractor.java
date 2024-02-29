package log.charter.io.gp.gp5.transformers;

import java.util.List;

import log.charter.io.gp.gp5.data.GP5File;
import log.charter.io.gp.gp5.data.GPBar;
import log.charter.io.gp.gp5.data.GPBeat;
import log.charter.io.gp.gp5.data.GPMasterBar;
import log.charter.song.Beat;
import log.charter.song.BeatsMap;
import log.charter.util.CollectionUtils.ArrayList2;

public class GP5FileTempoMapExtractor {
	public static BeatsMap getTempoMap(final GP5File gp5File, final int start, final int musicLength,
			final List<Integer> barsOrder) {
		final List<GPBar> track = gp5File.trackBars.get(0);
		final ArrayList2<Beat> beats = new ArrayList2<>();

		double position = start;
		int tempo = gp5File.tempo;
		boolean anchor = true;
		for (final int barId : barsOrder) {
			final GPBar bar = track.get(barId - 1);
			for (final GPBeat gpBeat : bar.voices.get(0)) {
				if (tempo != gpBeat.tempo) {
					tempo = gpBeat.tempo;
					anchor = true;
				}
			}

			final GPMasterBar masterBar = gp5File.masterBars.get(barId - 1);
			final int beatsInMeasure = masterBar.timeSignatureNumerator;
			final int noteDenominator = masterBar.timeSignatureDenominator;
			for (int i = 0; i < beatsInMeasure; i++) {
				beats.add(new Beat((int) position, beatsInMeasure, noteDenominator, i == 0, anchor));
				position += 60_000.0 * 4 / noteDenominator / tempo;
				anchor = false;
			}
		}
		beats.getLast().anchor = true;

		final BeatsMap beatsMap = new BeatsMap(musicLength, beats);
		beatsMap.makeBeatsUntilSongEnd();
		beatsMap.fixFirstBeatInMeasures();

		return beatsMap;
	}
}
