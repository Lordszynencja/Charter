package log.charter.io.gp.gp7;

import static log.charter.gui.components.utils.ComponentUtils.askYesNo;
import static log.charter.gui.components.utils.ComponentUtils.showPopup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.Beat;
import log.charter.data.song.BeatsMap;
import log.charter.data.song.SongChart;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.utils.ComponentUtils.ConfirmAnswer;
import log.charter.gui.menuHandlers.CharterMenuBar;
import log.charter.gui.panes.imports.ArrangementImportOptions;
import log.charter.io.Logger;
import log.charter.io.gp.gp7.data.GP7Automation;
import log.charter.io.gp.gp7.data.GP7MasterBar;
import log.charter.io.gp.gp7.data.GPIF;
import log.charter.io.gp.gp7.transformers.GP7FileToSongChart;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.data.fixers.ArrangementFixer;

public class GP7PlusFileImporter {
	private ArrangementFixer arrangementFixer;
	private ChartData chartData;
	private CharterFrame charterFrame;
	private CharterMenuBar charterMenuBar;
	private ChartTimeHandler chartTimeHandler;

	private int findBeatId(final GPIF gpif, final int bar, final double position) {
		int currentBar = 0;
		int currentBeat = 0;
		for (final GP7MasterBar masterBar : gpif.masterBars) {
			if (currentBar >= bar) {
				return currentBeat + (int) Math.round(position * masterBar.timeSignature.numerator);
			}

			currentBeat += masterBar.timeSignature.numerator;
			currentBar++;
		}

		final GP7MasterBar lastMasterBar = gpif.masterBars.get(gpif.masterBars.size() - 1);
		return currentBeat + lastMasterBar.timeSignature.numerator * (bar - currentBar)
				+ (int) Math.round(position * lastMasterBar.timeSignature.numerator);
	}

	private static double noteTypeMultiplier(final int noteType) {
		return switch (noteType) {
			case 1 -> 0.5;
			case 2 -> 1;
			case 3 -> 1.5;
			case 4 -> 2;
			case 5 -> 3;
			default -> {
				Logger.error("Unknown gp7 note type for tempo: " + noteType);
				yield 1;
			}
		};
	}

	private class TempoChangePoint {
		public final int beatId;
		public final double quarterNoteTempo;
		public final boolean linear;

		public TempoChangePoint(final GPIF gpif, final GP7Automation automation) {
			beatId = findBeatId(gpif, automation.bar, automation.position);

			final String[] tempoValues = automation.value.split(" ");
			final double tempo = Double.valueOf(tempoValues[0]);
			final int noteType = Integer.valueOf(tempoValues[1]);
			quarterNoteTempo = tempo * noteTypeMultiplier(noteType);
			linear = automation.linear;
		}
	}

	private double calculateBPM(final GP7MasterBar masterBar, final TempoChangePoint tempoChangePoint,
			final TempoChangePoint nextTempoChangePoint, final int beatId) {
		if (!tempoChangePoint.linear) {
			return tempoChangePoint.quarterNoteTempo * masterBar.timeSignature.denominator / 4;
		}

		final double bpmA = tempoChangePoint.quarterNoteTempo * masterBar.timeSignature.denominator / 4;
		final double bpmB = nextTempoChangePoint.quarterNoteTempo * masterBar.timeSignature.denominator / 4;
		final int length = nextTempoChangePoint.beatId - tempoChangePoint.beatId;
		if (length <= 0) {
			return bpmB;
		}

		final int positionInChange = beatId - tempoChangePoint.beatId;
		return bpmA * (length - positionInChange) / length //
				+ bpmB * positionInChange / length;
	}

	private BeatsMap replaceTempoMap(final GPIF gpif) {
		if (askYesNo(charterFrame, Label.GP5_IMPORT_TEMPO_MAP, Label.USE_TEMPO_MAP_FROM_IMPORT) != ConfirmAnswer.YES) {
			return chartData.songChart.beatsMap;
		}

		final BeatsMap beatsMap = new BeatsMap(new ArrayList<>());
		final List<TempoChangePoint> tempoChanges = gpif.masterTrack.automations.stream()//
				.filter(a -> a.type != null && a.type.equals("Tempo"))//
				.map(a -> new TempoChangePoint(gpif, a))//
				.collect(Collectors.toList());

		int tempoChangePointId = 0;
		int barBeat = 0;
		double position = 0;
		boolean firstInMeasure = true;
		for (final GP7MasterBar masterBar : gpif.masterBars) {
			for (int beatId = barBeat; beatId < barBeat + masterBar.timeSignature.numerator; beatId++) {
				while (tempoChangePointId < tempoChanges.size() - 1
						&& beatId >= tempoChanges.get(tempoChangePointId + 1).beatId) {
					tempoChangePointId++;
				}

				final TempoChangePoint tempoChangePoint = tempoChanges.get(tempoChangePointId);
				final TempoChangePoint nextTempoChangePoint = tempoChangePointId < tempoChanges.size() - 1
						? tempoChanges.get(tempoChangePointId + 1)
						: tempoChangePoint;

				final double bpm = calculateBPM(masterBar, tempoChangePoint, nextTempoChangePoint, beatId);

				final int beatsInMeasure = masterBar.timeSignature.numerator;
				final int noteDenominator = masterBar.timeSignature.denominator;
				final boolean anchor = tempoChangePoint.beatId == beatId || tempoChangePoint.linear;
				final Beat beat = new Beat(position, beatsInMeasure, noteDenominator, firstInMeasure, anchor);

				beatsMap.beats.add(beat);

				position += 60_000 / bpm;
				firstInMeasure = false;
			}

			barBeat += masterBar.timeSignature.numerator;
			firstInMeasure = true;
		}

		beatsMap.makeBeatsUntilSongEnd(chartTimeHandler.audioTime());

		return beatsMap;
	}

	public void importGP7PlusFile(final File file) {
		final GPIF gpif;
		try {
			gpif = GP7FileXStreamHandler.readGPIF(file);
		} catch (final Exception e) {
			Logger.error("Couldn't import GP7/8 file", e);
			showPopup(charterFrame, Label.COULDNT_IMPORT_GP7);

			return;
		}

		final BeatsMap beatsMap = replaceTempoMap(gpif);
		final SongChart temporaryChart = GP7FileToSongChart.transform(gpif, beatsMap);

		new ArrangementImportOptions(charterFrame, arrangementFixer, charterMenuBar, chartData, temporaryChart);
	}
}
