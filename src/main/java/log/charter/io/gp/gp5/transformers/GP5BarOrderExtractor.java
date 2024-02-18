package log.charter.io.gp.gp5.transformers;

import static java.lang.Math.max;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import log.charter.io.gp.gp5.data.Directions;
import log.charter.io.gp.gp5.data.GPMasterBar;

public class GP5BarOrderExtractor {
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

	public static List<Integer> getBarsOrder(final Directions directions, final List<GPMasterBar> bars) {
		return new GP5BarOrderExtractor(directions).getBarsOrder(bars);
	}

	private final Directions directions;

	private boolean isInRepeatSection = false;
	private int barToCheckIfDirectionsAreOk = 0;

	private GPMasterBar masterBar;
	private int barId;
	private int nextBarId;
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

	private final List<Integer> barsOrder = new ArrayList<>();

	private GP5BarOrderExtractor(final Directions directions) {
		this.directions = new Directions(directions);
	}

	private void recalculateRepeatSectionData(final List<GPMasterBar> bars, final Directions directions,
			final HashMap<Integer, Integer> repeatTracker, final int start) {
		for (int j = start; j < bars.size(); j++) {
			if (bars.get(j).isRepeatStart || directions.coda == j || directions.doubleCoda == j) {
				isInRepeatSection = false;
				break;
			} else if (bars.get(j).repeatCount != 0) {
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

	private boolean daCapoBar() {
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

	private int daSegnoBar() {
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

	private int daSegnoSegnoBar() {
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

	private int daCodaBar() {
		int directionBarId = 0;
		if (barId == directions.daCoda) {
			directionBarId = directions.coda;
			directions.daCoda = -1;
		}
		return directionBarId;
	}

	private int daDoubleCodaBar() {
		int directionBarId = 0;
		if (barId == directions.daDoubleCoda) {
			directionBarId = directions.doubleCoda;
			directions.daDoubleCoda = -1;
		}

		return directionBarId;
	}

	private boolean fineBar() {
		if (barId == directions.fine) {
			directions.fine = -1;
			return true;
		}

		return false;
	}

	private boolean codaExists() {
		return directions.coda != -1;
	}

	private boolean doubleCodaExists() {
		return directions.doubleCoda != -1;
	}

	private boolean fineExists() {
		return directions.fine != -1;
	}

	private boolean segnoExists() {
		return directions.segno != -1;
	}

	private boolean segnoSegnoExists() {
		return directions.segnoSegno != -1;
	}

	private boolean alCodaBar() {
		return (barId == directions.daCapoAlCoda //
				|| barId == directions.daSegnoAlCoda//
				|| barId == directions.daSegnoSegnoAlCoda
				// If there are no "al coda", da coda will trigger by itself
				|| (directions.daCapoAlCoda == -1//
						&& directions.daSegnoAlCoda == -1 //
						&& directions.daSegnoSegnoAlCoda == -1));
	}

	private boolean alDoubleCodaBar() {
		return barId == directions.daCapoAlDoubleCoda//
				|| barId == directions.daSegnoAlDoubleCoda//
				|| barId == directions.daSegnoSegnoAlDoubleCoda
				// If there are no "al double coda", da double coda will trigger by itself
				|| (directions.daCapoAlDoubleCoda == -1 //
						&& directions.daSegnoAlDoubleCoda == -1 //
						&& directions.daSegnoSegnoAlDoubleCoda == -1);
	}

	private boolean alFineBar() {
		return barId == directions.daCapoAlFine//
				|| barId == directions.daSegnoAlFine//
				|| barId == directions.daSegnoSegnoAlFine//
				// If there are no "al fine", fine will trigger by itself
				|| (directions.daCapoAlFine == -1//
						&& directions.daSegnoAlFine == -1//
						&& directions.daSegnoSegnoAlFine == -1);
	}

	private boolean skipBarOnAlternateEnding() {
		if (masterBar.alternateEndings != 0) {
			barToProgressPastToDisableAltEnding = barId > barToProgressPastToDisableAltEnding ? barId
					: barToProgressPastToDisableAltEnding;
			latestAlternateEnding = readAlternateEndingBit(masterBar.alternateEndings,
					nextAlternateEndingToProcess - 1);

			// If this is a different alternate ending skip it
			if (nextAlternateEndingToProcess != latestAlternateEnding) {
				return true;
			}

			// If the right ending, progress to the next one
			if (nextAlternateEndingToProcess == latestAlternateEnding) {
				storedNextAlternateEnding = nextAlternateEndingToProcess + 1; // Read when repeating
			}

			return false;
		}

		// If there are alt endings, skip the ones we have processed
		if (latestAlternateEnding != 0 && latestAlternateEnding == nextAlternateEndingToProcess - 1) {
			barToProgressPastToDisableAltEnding = max(barId, barToProgressPastToDisableAltEnding);
			return true;
		}

		return false;
	}

	private void addBar() {
		barsOrder.add(barId);
	}

	// Check early if this is the fine bar and if so, end the algorithm
	private boolean checkFine() {
		return fineActivated && fineBar();
	}

	// If this is a repeat bar, initialize its entry to keep track of potential
	// nested repeats
	private void initializeRepeatForCurrentBar() {
		if (masterBar.repeatCount != 0) {
			repeatTracker.putIfAbsent(barId, -1);
		}
	}

	private void storeRepeatStart() {
		if (masterBar.isRepeatStart) {
			// When passing a new repeat bar we can reset alternate ending variables
			if (startOfRepeatBar != barId) {
				nextAlternateEndingToProcess = 1;
				storedNextAlternateEnding = 0;
				latestAlternateEnding = 1;
			}
			startOfRepeatBar = barId;
		}
	}

	private boolean handleRepeatBar() {
		if (!repeatTracker.containsKey(barId)) {
			return false;
		}

		int repeatNTimes = repeatTracker.get(barId);
		if (repeatNTimes == -1 && masterBar.repeatCount != 0) {
			repeatNTimes = masterBar.repeatCount - 1;
			nextBarId = startOfRepeatBar;
		} else if (repeatNTimes > 0) {
			nextBarId = startOfRepeatBar;
		}

		repeatNTimes--; // We are sending it to repeat
		repeatTracker.put(barId, repeatNTimes);
		nextAlternateEndingToProcess = storedNextAlternateEnding;
		storedNextAlternateEnding = 0;
		latestAlternateEnding = 0;
		return repeatNTimes >= 0; // Don't continue to check directions if we are repeating
	}

	private void activateFineCodaDoubleCoda() {
		if (fineExists() && alFineBar()) {
			fineActivated = true;
		}
		if (codaExists() && alCodaBar()) {
			codaActivated = true;
		}
		if (doubleCodaExists() && alDoubleCodaBar()) {
			doubleCodaActivated = true;
		}
	}

	private boolean checkDaCapoBar() {
		if (daCapoBar()) {
			nextBarId = 1; // Return to first bar
			return true;
		}

		return false;
	}

	private void goToSegnoBar(final int directionBarId) {
		nextBarId = directionBarId; // Go to segno/segno segno bar
		if (repeatTracker.containsKey(barToCheckIfDirectionsAreOk)) {
			repeatTracker.put(barToCheckIfDirectionsAreOk, -1);
			// Reset repeats when using direction
		}
		for (final Entry<Integer, Integer> entry : repeatTracker.entrySet()) {
			entry.setValue(-1);
		}

		nextAlternateEndingToProcess = 1; // Reset alt ending variables
	}

	private boolean checkSegnoBar() {
		int directionBarId = 0;
		if (segnoExists()) {
			directionBarId = daSegnoBar();
			if (directionBarId != 0) {
				goToSegnoBar(directionBarId);
				return true;
			}
		}
		if (segnoSegnoExists()) {
			directionBarId = daSegnoSegnoBar();
			if (directionBarId != 0) {
				goToSegnoBar(directionBarId);
				return true;
			}
		}

		return false;
	}

	private boolean checkCodaBar() {
		if (!codaActivated) {
			return false;
		}

		final int directionBarId = daCodaBar();
		if (directionBarId == 0) {
			return false;
		}
		codaActivated = false;
		nextBarId = directionBarId; // Go to coda bar
		for (final Entry<Integer, Integer> entry : repeatTracker.entrySet()) {
			entry.setValue(-1);
		}

		return true;
	}

	private boolean checkDoubleCodaBar() {
		if (!doubleCodaActivated) {
			return false;
		}

		final int directionBarId = daDoubleCodaBar();
		if (directionBarId == 0) {
			return false;
		}

		doubleCodaActivated = false;
		nextBarId = directionBarId; // Go to double coda bar
		for (final Entry<Integer, Integer> entry : repeatTracker.entrySet()) {
			entry.setValue(-1);
		}

		return true;
	}

	private void setBarsOrder(final List<GPMasterBar> masterBars) {
		barId = 1;
		nextBarId = 1;
		while (nextBarId <= masterBars.size()) {
			barId = nextBarId;
			nextBarId = barId + 1;
			masterBar = masterBars.get(barId - 1);

			recalculateRepeatSectionData(masterBars, directions, repeatTracker, barId - 1);

			if (skipBarOnAlternateEnding()) {
				continue;
			}

			addBar();

			if (checkFine()) {
				return;
			}

			initializeRepeatForCurrentBar();
			storeRepeatStart();

			if (handleRepeatBar()) {
				continue;
			}

			activateFineCodaDoubleCoda();

			if (checkDaCapoBar()) {
				continue;
			}

			if (isInRepeatSection) {
				continue;
			}
			if (checkSegnoBar()) {
				continue;
			}
			if (checkCodaBar()) {
				continue;
			}
			if (checkDoubleCodaBar()) {
				continue;
			}
		}
	}

	private List<Integer> getBarsOrder(final List<GPMasterBar> masterBars) {
		if (barsOrder.isEmpty()) {
			setBarsOrder(masterBars);
		}

		return barsOrder;
	}
}
