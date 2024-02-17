package log.charter.io.gp.gp5.transformers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import log.charter.io.Logger;
import log.charter.io.gp.gp5.data.Directions;
import log.charter.song.Beat;
import log.charter.song.BeatsMap;
import log.charter.util.CollectionUtils.ArrayList2;

public class GPBarUnwrapper {
	private class RepeatSectionData {
		public boolean isInRepeatSection = false;
		public int barToCheckIfDirectionsAreOk = 0;

		public void recalculateRepeatSectionData(final HashMap<Integer, Integer> repeatTracker, final int start) {

			for (int j = start; j < combinedBars.size(); j++) {
				if (combinedBars.get(j).bar.isRepeatStart || directions.coda == j || directions.doubleCoda == j) {
					isInRepeatSection = false;
					break;
				} else if (combinedBars.get(j).bar.repeatCount != 0) {
					isInRepeatSection = true;
					barToCheckIfDirectionsAreOk = j + 1;
					break;
				} else {
					isInRepeatSection = false;
				}
			}

			if (isInRepeatSection && repeatTracker.containsKey(barToCheckIfDirectionsAreOk)
					&& repeatTracker.get(barToCheckIfDirectionsAreOk) == 0) {
				isInRepeatSection = false;
			}
		}
	}

	private static int readAlternateEndingBit(final int bitmask, final int startBit) {
		int alternateEnding = startBit;

		for (int i = startBit; i < 8; i++) {
			if (((bitmask >> i) & 1) != 0) {
				alternateEnding = i + 1;
				break;
			}
		}

		return alternateEnding;
	}

	private final Directions directions;
	public ArrayList2<CombinedGPBars> combinedBars;
	public ArrayList2<CombinedGPBars> unwrappedBars;
	private BeatsMap unwrappedBeatsMap;
	private final List<Integer> barOrder;

	public GPBarUnwrapper(final Directions directions) {
		this.directions = directions;
		combinedBars = new ArrayList2<CombinedGPBars>();
		unwrappedBars = new ArrayList2<CombinedGPBars>();
		unwrappedBeatsMap = new BeatsMap(0);
		barOrder = new ArrayList<>();
	}

	public void unwrap() {
		// Repeat handling
		int startOfRepeatBar = 1; // If no repeat starts are set, the first bar is used
		final HashMap<Integer, Integer> repeatTracker = new HashMap<>();

		// Alternate ending handling
		int nextAlternateEndingToProcess = 1;
		int storedNextAlternateEnding = 0;
		int latestAlternateEnding = 1;
		int barToProgressPastToDisableAltEnding = 0;

		// Direction handling
		boolean fineActivated = false;
		boolean codaActivated = false;
		boolean doubleCodaActivated = false;

		final RepeatSectionData repeatSectionData = new RepeatSectionData();

		for (int i = 0; i < combinedBars.size(); i++) {
			final CombinedGPBars comboBar = combinedBars.get(i);
			final int currentId = comboBar.gpBarId;

			repeatSectionData.recalculateRepeatSectionData(repeatTracker, i);

			if (comboBar.bar.alternateEndings != 0) {
				barToProgressPastToDisableAltEnding = currentId > barToProgressPastToDisableAltEnding ? currentId
						: barToProgressPastToDisableAltEnding;
				latestAlternateEnding = readAlternateEndingBit(comboBar.bar.alternateEndings,
						nextAlternateEndingToProcess - 1);

				// If this is a different alternate ending skip it
				if (nextAlternateEndingToProcess != latestAlternateEnding) {
					continue;
				}
				// If the right ending, progress to the next one
				else if (nextAlternateEndingToProcess == latestAlternateEnding) {
					storedNextAlternateEnding = nextAlternateEndingToProcess + 1; // Read when repeating
				}
			} else {
				// If there are alt endings, skip the ones we have processed
				if (latestAlternateEnding != 0 && latestAlternateEnding == nextAlternateEndingToProcess - 1) {
					barToProgressPastToDisableAltEnding = currentId > barToProgressPastToDisableAltEnding ? currentId
							: barToProgressPastToDisableAltEnding;
					continue;
				}
			}
			barOrder.add(currentId);

			// Check early if this is the fine bar and if so, end unwrapping
			if (fineActivated && fineBar(currentId)) {
				break;
			}
			// If this is a repeat bar, initialize its entry to keep track of potential
			// nested repeats
			if (comboBar.bar.repeatCount != 0) {
				repeatTracker.putIfAbsent(currentId, -1);
			}

			// Store repeat start
			if (comboBar.bar.isRepeatStart) {
				// When passing a new repeat bar we can reset alternate ending variables
				if (startOfRepeatBar != currentId) {
					nextAlternateEndingToProcess = 1;
					storedNextAlternateEnding = 0;
					latestAlternateEnding = 1;
				}
				startOfRepeatBar = currentId;
			}

			// Handle repeat bars
			if (repeatTracker.containsKey(currentId)) {
				int repeatNTimes = repeatTracker.get(currentId);

				if (repeatNTimes == -1 && comboBar.bar.repeatCount != 0) {
					repeatNTimes = comboBar.bar.repeatCount - 1; // Repeat: 2 means do it 1 more time (since we just did
																	// the first time)
					i = startOfRepeatBar - 1 - 1; // Example: Bar 1 being index 0, and after continue the for loop
													// increments i + 1
				} else if (repeatNTimes > 0) {
					i = startOfRepeatBar - 1 - 1; // Example: Bar 1 being index 0, and after continue the for loop
													// increments i + 1
				}

				repeatNTimes--; // We are sending it to repeat
				repeatTracker.put(currentId, repeatNTimes);
				nextAlternateEndingToProcess = storedNextAlternateEnding;
				storedNextAlternateEnding = 0;
				latestAlternateEnding = 0;
				if (repeatNTimes >= 0) {
					continue; // Don't continue to check directions if we are repeating
				}
			}

			if (fineExists() && alFineBar(currentId)) {
				fineActivated = true;
			}
			if (codaExists() && alCodaBar(currentId)) {
				codaActivated = true;
			}
			if (doubleCodaExists() && alDoubleCodaBar(currentId)) {
				doubleCodaActivated = true;
			}

			int directionBarId = 0;

			if (daCapoBar(currentId)) {
				i = -1; // Return to first bar
			} else if (repeatSectionData.isInRepeatSection == false
					&& ((segnoExists() && (directionBarId = daSegnoBar(currentId)) != 0)
							|| (segnoSegnoExists() && (directionBarId = daSegnoSegnoBar(currentId)) != 0))) {
				i = directionBarId - 1 - 1; // Go to segno/segno segno bar
				if (repeatTracker.containsKey(repeatSectionData.barToCheckIfDirectionsAreOk)) {
					repeatTracker.put(repeatSectionData.barToCheckIfDirectionsAreOk, -1);
					// Reset repeats when using direction
				}
				for (final Entry<Integer, Integer> entry : repeatTracker.entrySet()) {
					entry.setValue(-1);
				}
				nextAlternateEndingToProcess = 1; // Reset alt ending variables

			} else if (codaActivated && repeatSectionData.isInRepeatSection == false
					&& (directionBarId = daCodaBar(currentId)) != 0) {
				codaActivated = false;
				i = directionBarId - 1 - 1; // Go to coda bar
				for (final Entry<Integer, Integer> entry : repeatTracker.entrySet()) {
					entry.setValue(-1);
				}
			} else if (doubleCodaActivated && repeatSectionData.isInRepeatSection == false
					&& (directionBarId = daDoubleCodaBar(currentId)) != 0) {
				doubleCodaActivated = false;
				i = directionBarId - 1 - 1; // Go to double coda bar
				for (final Entry<Integer, Integer> entry : repeatTracker.entrySet()) {
					entry.setValue(-1);
				}
			}
		}

		// Now that we know the bar order, start unwrapping
		for (final int bar : barOrder) {
			unwrappedBars.add(new CombinedGPBars(combinedBars.get(bar - 1)));
		}

		// With all bars in correct order, update the positions of them (reduce
		// truncated positions)
		double sumOfBarWidths = 0;
		for (int i = 0; i < unwrappedBars.size(); i++) {
			for (int j = 0; j < unwrappedBars.get(i).barBeats.size(); j++) {
				unwrappedBars.get(i).barBeats.get(j).position((int) sumOfBarWidths);
				sumOfBarWidths += unwrappedBars.get(i).barBeats.get(j).barWidthInTimeMs;
			}
		}
	}

	private boolean daCapoBar(final int barId) {
		if (barId == directions.daCapo) {
			directions.daCapo = -1;
			return true;
		}
		if (barId == directions.daCapoAlCoda) {
			directions.daCapoAlCoda = -1;
			return true;
		}
		if (barId == directions.daCapoAlDoubleCoda) {
			directions.daCapoAlDoubleCoda = -1;
			return true;
		}
		if (barId == directions.daCapoAlFine) {
			directions.daCapoAlFine = -1;
			return true;
		}

		return false;
	}

	private int daSegnoBar(final int barId) {
		int directionBarId = 0;
		final int symbolBarId = directions.segno;

		if (barId == directions.daSegno) {
			directionBarId = symbolBarId;
			directions.daSegno = -1;
		} else if (barId == directions.daSegnoAlCoda) {
			directionBarId = symbolBarId;
			directions.daSegnoAlCoda = -1;
		} else if (barId == directions.daSegnoAlDoubleCoda) {
			directionBarId = symbolBarId;
			directions.daSegnoAlDoubleCoda = -1;
		} else if (barId == directions.daSegnoAlFine) {
			directionBarId = symbolBarId;
			directions.daSegnoAlFine = -1;
		}
		return directionBarId;
	}

	private int daSegnoSegnoBar(final int barId) {
		int directionBarId = 0;
		final int symbolBarId = directions.segnoSegno;
		if (barId == directions.daSegnoSegno) {
			directionBarId = symbolBarId;
			directions.daSegnoSegno = -1;
		} else if (barId == directions.daSegnoSegnoAlCoda) {
			directionBarId = symbolBarId;
			directions.daSegnoSegnoAlCoda = -1;
		} else if (barId == directions.daSegnoSegnoAlDoubleCoda) {
			directionBarId = symbolBarId;
			directions.daSegnoSegnoAlDoubleCoda = -1;
		} else if (barId == directions.daSegnoSegnoAlFine) {
			directionBarId = symbolBarId;
			directions.daSegnoSegnoAlFine = -1;
		}
		return directionBarId;
	}

	private int daCodaBar(final int barId) {
		int directionBarId = 0;
		if (barId == directions.daCoda) {
			directionBarId = directions.coda;
			directions.daCoda = -1;
		}
		return directionBarId;
	}

	private int daDoubleCodaBar(final int barId) {
		int directionBarId = 0;
		if (barId == directions.daDoubleCoda) {
			directionBarId = directions.doubleCoda;
			directions.daDoubleCoda = -1;
		}
		return directionBarId;
	}

	private boolean fineBar(final int barId) {
		if (barId == directions.fine) {
			directions.fine = -1;
			return true;
		}
		return false;
	}

	private boolean codaExists() {
		return (directions.coda != -1);
	}

	private boolean doubleCodaExists() {
		return (directions.doubleCoda != -1);
	}

	private boolean fineExists() {
		return (directions.fine != -1);
	}

	private boolean segnoExists() {
		return (directions.segno != -1);
	}

	private boolean segnoSegnoExists() {
		return (directions.segnoSegno != -1);
	}

	private boolean alCodaBar(final int barId) {
		return (barId == directions.daCapoAlCoda || barId == directions.daSegnoAlCoda
				|| barId == directions.daSegnoSegnoAlCoda || (directions.daCapoAlCoda == -1 &&
				// If there are no "al
				// coda", da coda
				// will trigger by
				// itself
						directions.daSegnoAlCoda == -1 && directions.daSegnoSegnoAlCoda == -1));
	}

	private boolean alDoubleCodaBar(final int barId) {
		return barId == directions.daCapoAlDoubleCoda//
				|| barId == directions.daSegnoAlDoubleCoda//
				|| barId == directions.daSegnoSegnoAlDoubleCoda
				// If there are no "al double coda", da double coda will trigger by itself
				|| (directions.daCapoAlDoubleCoda == -1 //
						&& directions.daSegnoAlDoubleCoda == -1 //
						&& directions.daSegnoSegnoAlDoubleCoda == -1);
	}

	private boolean alFineBar(final int barId) {
		return barId == directions.daCapoAlFine//
				|| barId == directions.daSegnoAlFine//
				|| barId == directions.daSegnoSegnoAlFine//
				// If there are no "al fine", fine will trigger by itself
				|| (directions.daCapoAlFine == -1//
						&& directions.daSegnoAlFine == -1//
						&& directions.daSegnoSegnoAlFine == -1);
	}

	public final BeatsMap getUnwrappedBeatsMap(final int songLengthMs) {
		if (unwrappedBeatsMap.songLengthMs == 0) {
			// Initialize a beat map first time
			final BeatsMap beatsMap = new BeatsMap(songLengthMs, false);

			for (final CombinedGPBars comboBeat : unwrappedBars) {
				for (final Beat beat : comboBeat.barBeats) {
					beatsMap.beats.add(beat);
				}
			}
			unwrappedBeatsMap = new BeatsMap(beatsMap);
		}
		final int numberOfBars = unwrappedBars.size();
		int numberOfFirstBeatsInBars = 0;
		int previousPos = 0;
		for (final Beat beat : unwrappedBeatsMap.beats) {
			if (beat.firstInMeasure) {
				numberOfFirstBeatsInBars++;
			}
			if (previousPos > beat.position()) {
				Logger.error("Unwrapping issue detected: earlier beats were placed ahead.");
			}
			previousPos = beat.position();
		}

		if (numberOfBars != numberOfFirstBeatsInBars) {
			Logger.error("Unwrapping issue detected: incorrect number of first beats in bars.");
		}

		return unwrappedBeatsMap;
	}

	public final BeatsMap getUnwrappedBeatsMap() {
		return unwrappedBeatsMap;
	}

	public final int getLastBarStartPosition() {
		for (int i = unwrappedBeatsMap.beats.size() - 1; i > 0; i--) {
			final Beat beat = unwrappedBeatsMap.beats.get(i);
			if (beat.firstInMeasure) {
				return beat.position();
			}
		}
		return 0;
	}
}
