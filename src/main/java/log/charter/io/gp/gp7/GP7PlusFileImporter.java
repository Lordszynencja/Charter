package log.charter.io.gp.gp7;

import static java.lang.Math.max;
import static log.charter.gui.components.utils.ComponentUtils.askYesNo;
import static log.charter.gui.components.utils.ComponentUtils.showPopup;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import log.charter.io.gp.gp7.data.GP7Automation.GP7SyncPointValue;
import log.charter.io.gp.gp7.data.GP7Automation.GP7TempoValue;
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
		public boolean linear;
		public int noteType = 2;
		public boolean noteTypeSet = false;
		public double tempo;
		public Double position = null;

		public TempoChangePoint(final int beatId) {
			this.beatId = beatId;
		}

		public void addValue(final GP7TempoValue value) {
			noteType = value.noteType;
			noteTypeSet = true;
			tempo = value.tempo;
		}

		public void addValue(final GP7SyncPointValue value) {
			tempo = value.modifiedTempo;
			position = value.frameOffset / 44.1;
		}

		public double getQuarterNoteTempo() {
			return tempo * noteTypeMultiplier(noteType);
		}

		public void setNoteType(final TempoChangePoint previous) {
			noteType = previous.noteType;
			noteTypeSet = true;
		}
	}

	private Map<Integer, TempoChangePoint> getTempoChangesMap(final GPIF gpif) {
		final Map<Integer, TempoChangePoint> tempoChangePoints = new HashMap<>();
		for (final GP7Automation automation : gpif.masterTrack.automations) {
			if (automation.type == null || (!automation.type.equals("Tempo") && !automation.type.equals("SyncPoint"))) {
				continue;
			}

			final int beatId = findBeatId(gpif, automation.bar, automation.position);
			if (!tempoChangePoints.containsKey(beatId)) {
				tempoChangePoints.put(beatId, new TempoChangePoint(beatId));
			}
			final TempoChangePoint tempoChangePoint = tempoChangePoints.get(beatId);
			tempoChangePoint.linear = automation.linear;

			switch (automation.type) {
				case "Tempo" -> tempoChangePoint.addValue(automation.value.asTempoValue());
				case "SyncPoint" -> tempoChangePoint.addValue(automation.value.asSyncPointValue());
			}
		}

		return tempoChangePoints;
	}

	private List<TempoChangePoint> compileTempoChanges(final Map<Integer, TempoChangePoint> tempoChangePoints) {
		final List<TempoChangePoint> tempoChanges = tempoChangePoints.values().stream()//
				.sorted((a, b) -> Integer.compare(a.beatId, b.beatId))//
				.collect(Collectors.toList());

		for (int i = 1; i < tempoChanges.size(); i++) {
			final TempoChangePoint tempoChangePoint = tempoChanges.get(i);
			if (!tempoChangePoint.noteTypeSet) {
				tempoChangePoint.setNoteType(tempoChanges.get(i - 1));
			}
		}

		return tempoChanges;
	}

	private double calculateBPM(final GP7MasterBar masterBar, final TempoChangePoint tempoChangePoint,
			final TempoChangePoint nextTempoChangePoint, final int beatId) {
		if (!tempoChangePoint.linear) {
			return tempoChangePoint.getQuarterNoteTempo() * masterBar.timeSignature.denominator / 4;
		}

		final double bpmA = tempoChangePoint.getQuarterNoteTempo() * masterBar.timeSignature.denominator / 4;
		final double bpmB = nextTempoChangePoint.getQuarterNoteTempo() * masterBar.timeSignature.denominator / 4;
		final int length = nextTempoChangePoint.beatId - tempoChangePoint.beatId;
		if (length <= 0) {
			return bpmB;
		}

		final int positionInChange = beatId - tempoChangePoint.beatId;
		return bpmA * (length - positionInChange) / length //
				+ bpmB * positionInChange / length;
	}

	private BeatsMap getTempoMap(final GPIF gpif, final boolean importBeatMap) {
		if (!importBeatMap) {
			return chartData.songChart.beatsMap;
		}

		final BeatsMap beatsMap = new BeatsMap(new ArrayList<>());

		final Map<Integer, TempoChangePoint> tempoChangePoints = getTempoChangesMap(gpif);
		final List<TempoChangePoint> tempoChanges = compileTempoChanges(tempoChangePoints);

		int tempoChangePointId = 0;
		int barBeat = 0;
		double position = 0;
		double offset = 0;
		if (gpif.backingTrack != null && gpif.backingTrack.framePadding != null) {
			offset = max(0, -gpif.backingTrack.framePadding / gpif.sampleRate * 1000);
			position = offset;
		}
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

				if (beatId == tempoChangePoint.beatId && tempoChangePoint.position != null) {
					position = offset + tempoChangePoint.position;
				}

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

	public GPIF importGPIF(final File file) {
		try {
			return GP7FileXStreamHandler.readGPIF(file);
		} catch (final Exception e) {
			Logger.error("Couldn't import GP7/8 file", e);
			showPopup(charterFrame, Label.COULDNT_IMPORT_GP7);
		}

		return null;
	}

	public void importGP7PlusFile(final File file) {
		final GPIF gpif = importGPIF(file);
		if (gpif == null) {
			return;
		}

		final boolean importBeatMap = askYesNo(charterFrame, Label.GP_IMPORT_TEMPO_MAP,
				Label.USE_TEMPO_MAP_FROM_IMPORT) == ConfirmAnswer.YES;

		final SongChart temporaryChart = transformGPIFToSongChart(gpif, importBeatMap);

		final List<String> trackNames = gpif.tracks.stream().map(t -> t.name).collect(Collectors.toList());
		new ArrangementImportOptions(charterFrame, arrangementFixer, charterMenuBar, chartData, temporaryChart,
				trackNames);
	}

	public SongChart transformGPIFToSongChart(final GPIF gpif, final boolean importBeatMap) {
		final BeatsMap beatsMap = getTempoMap(gpif, importBeatMap);
		return GP7FileToSongChart.transform(gpif, beatsMap);
	}
}
