package log.charter.song;

import java.util.List;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.HashMap;

import log.charter.io.gp.gp5.Directions;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.io.Logger;

public class GPBarUnwrapper {
	Directions directions;
	ArrayList2<CombinedGPBars> bars;
	ArrayList2<CombinedGPBars> unwrappedBars;
	BeatsMap unwrappedBeatsMap;
	List<Integer> barOrder;

	public GPBarUnwrapper(Directions directions) {
		this.directions = directions;
		this.bars = new ArrayList2<CombinedGPBars>();
		this.unwrappedBars = new ArrayList2<CombinedGPBars>();
		this.unwrappedBeatsMap = new BeatsMap(0);
		this.barOrder = new ArrayList<>();
	}

	public boolean addBar(final CombinedGPBars bar) {
		return this.bars.add(bar);
	}

	public final CombinedGPBars get(final int index) {
		return this.bars.get(index);
	}

	public final CombinedGPBars getLast() {
		return this.bars.getLast();
	}

	private final int readAlternateEndingBit(final int bitmask, final int startBit)
	{
		int alternateEnding = startBit;

		for (int i = startBit; i < 8; i++) {
			if (((bitmask >> i) & 1) != 0) {
				alternateEnding = i+1;
				break;
			}
		}
		return alternateEnding;
	}

	public void unwrap() {
		// Repeat handling
		int startOfRepeatBar = 1; // If no repeat starts are set, the first bar is used
        HashMap<Integer, Integer> repeatTracker = new HashMap<>();
		
		// Alternate ending handling
		int nextAlternateEndingToProcess = 1;
		int storedNextAlternateEnding = 0;
		int latestAlternateEnding = 1;
		int barToProgressPastToDisableAltEnding = 0;
		
		// Direction handling
		boolean fineActivated = false;
		boolean codaActivated = false;
		boolean doubleCodaActivated = false;

		boolean isInRepeatSection = false;
		int barToCheckIfDirectionsAreOk = 0;

		for (int i = 0; i < this.bars.size(); i++) {
			final CombinedGPBars comboBar = this.bars.get(i);
			final int currentId = comboBar.gpBarId;
			for (int j = i; j < this.bars.size(); j++) {
				if (this.bars.get(j).bar.isRepeatStart ||
					this.directions.coda == j ||
					this.directions.doubleCoda == j) {
					isInRepeatSection = false;
					break;
				}
				else if (this.bars.get(j).bar.repeatCount != 0) {
					isInRepeatSection = true;
					barToCheckIfDirectionsAreOk = j+1;
					break;
				}
				else {
					isInRepeatSection = false;
				}
			}

			if (isInRepeatSection) {
				if (repeatTracker.containsKey(barToCheckIfDirectionsAreOk)) {
					if (repeatTracker.get(barToCheckIfDirectionsAreOk) == 0) {
						isInRepeatSection = false;
					}
				}
			}

			if (comboBar.bar.alternateEndings != 0) {
				barToProgressPastToDisableAltEnding = currentId > barToProgressPastToDisableAltEnding ? currentId : barToProgressPastToDisableAltEnding;
				latestAlternateEnding = readAlternateEndingBit(comboBar.bar.alternateEndings, nextAlternateEndingToProcess-1);

				// If this is a different alternate ending skip it
				if (nextAlternateEndingToProcess != latestAlternateEnding) {
					continue;
				}
				// If the right ending, progress to the next one
				else if (nextAlternateEndingToProcess == latestAlternateEnding) {
					storedNextAlternateEnding = nextAlternateEndingToProcess + 1; // Read when repeating
				}
			}
			else {
				// If there are alt endings, skip the ones we have processed
				if (latestAlternateEnding != 0 &&
					latestAlternateEnding == nextAlternateEndingToProcess - 1) {
					barToProgressPastToDisableAltEnding = currentId > barToProgressPastToDisableAltEnding ? currentId : barToProgressPastToDisableAltEnding;
					continue;
				}
			}
			this.barOrder.add(currentId);

			// Check early if this is the fine bar and if so, end unwrapping
			if (fineActivated && fineBar(currentId)) {
				break;
			}
			// If this is a repeat bar, initialize its entry to keep track of potential nested repeats
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
					repeatNTimes = comboBar.bar.repeatCount-1; // Repeat: 2 means do it 1 more time (since we just did the first time)
					i = startOfRepeatBar-1-1; // Example: Bar 1 being index 0, and after continue the for loop increments i + 1
				}
				else if (repeatNTimes > 0) {
					i = startOfRepeatBar-1-1; // Example: Bar 1 being index 0, and after continue the for loop increments i + 1
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
			}
			else if (isInRepeatSection == false &&
					((segnoExists() && (directionBarId = daSegnoBar(currentId)) != 0) ||
				     (segnoSegnoExists() && (directionBarId = daSegnoSegnoBar(currentId)) != 0))) {
				i = directionBarId - 1 - 1; // Go to segno/segno segno bar
				if (repeatTracker.containsKey(barToCheckIfDirectionsAreOk)) {
					repeatTracker.put(barToCheckIfDirectionsAreOk, -1); // Reset repeats when using direction
				}
				for (Entry<Integer, Integer> entry : repeatTracker.entrySet() ) {
						entry.setValue(-1);
					}
				nextAlternateEndingToProcess = 1; // Reset alt ending variables
				
			}
			else if (codaActivated &&
					 isInRepeatSection == false && (directionBarId = daCodaBar(currentId)) != 0) {
				codaActivated = false;
				i = directionBarId - 1 - 1; // Go to coda bar
				for (Entry<Integer, Integer> entry : repeatTracker.entrySet() ) {
					entry.setValue(-1);
				}
			}
			else if (doubleCodaActivated &&
					 isInRepeatSection == false && (directionBarId = daDoubleCodaBar(currentId)) != 0) {
				doubleCodaActivated = false;
				i = directionBarId - 1 - 1; // Go to double coda bar
				for (Entry<Integer, Integer> entry : repeatTracker.entrySet() ) {
					entry.setValue(-1);
				}
			}
		}

		// Now that we know the bar order, start unwrapping
		for (int bar: this.barOrder) {
			this.unwrappedBars.add(new CombinedGPBars(this.bars.get(bar-1)));
		}

		// With all bars in correct order, update the positions of them (reduce truncated positions)
		double sumOfBarWidths = 0;
		for (int i = 0; i < this.unwrappedBars.size(); i++) {
			for (int j = 0; j < this.unwrappedBars.get(i).barBeats.size(); j++) {
				this.unwrappedBars.get(i).barBeats.get(j).position((int)sumOfBarWidths);
				sumOfBarWidths += this.unwrappedBars.get(i).barBeats.get(j).barWidthInTimeMs;
			}
		}
	}

	private boolean daCapoBar(final int barId) {
		if (barId == this.directions.daCapo) {
			this.directions.daCapo = 65535;
			return true;
		}
		if (barId == this.directions.daCapoAlCoda) {
			this.directions.daCapoAlCoda = 65535;
			return true;
		}
		if (barId == this.directions.daCapoAlDoubleCoda) {
			this.directions.daCapoAlDoubleCoda = 65535;
			return true;
		}
		if (barId == this.directions.daCapoAlFine) {
			this.directions.daCapoAlFine = 65535;
			return true;
		}

		return false;
	}
	private int daSegnoBar(final int barId) {
		int directionBarId = 0;
		int symbolBarId = this.directions.segno;

		if (barId == this.directions.daSegno) {
			directionBarId = symbolBarId;
			this.directions.daSegno = 65535;
		}
		else if (barId == this.directions.daSegnoAlCoda) {
			directionBarId = symbolBarId;
			this.directions.daSegnoAlCoda = 65535;
		}
		else if (barId == this.directions.daSegnoAlDoubleCoda) {
			directionBarId = symbolBarId;
			this.directions.daSegnoAlDoubleCoda = 65535;
		}
		else if (barId == this.directions.daSegnoAlFine) {
			directionBarId = symbolBarId;
			this.directions.daSegnoAlFine = 65535;
		}
		return directionBarId;
	}
	private int daSegnoSegnoBar(final int barId) {
		int directionBarId = 0;
		int symbolBarId = this.directions.segnoSegno;
		if (barId == this.directions.daSegnoSegno) {
			directionBarId = symbolBarId;
			this.directions.daSegnoSegno = 65535;
		}
		else if (barId == this.directions.daSegnoSegnoAlCoda) {
			directionBarId = symbolBarId;
			this.directions.daSegnoSegnoAlCoda = 65535;
		}
		else if (barId == this.directions.daSegnoSegnoAlDoubleCoda) {
			directionBarId = symbolBarId;
			this.directions.daSegnoSegnoAlDoubleCoda = 65535;
		}
		else if (barId == this.directions.daSegnoSegnoAlFine) {
			directionBarId = symbolBarId;
			this.directions.daSegnoSegnoAlFine = 65535;
		}
		return directionBarId;
	}
	private int daCodaBar(final int barId) {
		int directionBarId = 0;
		if (barId == this.directions.daCoda) {
			directionBarId = this.directions.coda;
			this.directions.daCoda = 65535;
		}
		return directionBarId;
	}
	private int daDoubleCodaBar(final int barId) {
		int directionBarId = 0;
		if (barId == this.directions.daDoubleCoda) {
			directionBarId = this.directions.doubleCoda;
			this.directions.daDoubleCoda = 65535;
		}
		return directionBarId;
	}
	private boolean fineBar(final int barId) {
		if (barId == this.directions.fine) {
			this.directions.fine = 65535;
			return true;
		}
		return false;
	}
	private boolean codaExists() {
		return (this.directions.coda != 65535);
	}
	private boolean doubleCodaExists() {
		return (this.directions.doubleCoda != 65535);
	}
	private boolean fineExists() {
		return (this.directions.fine != 65535);
	}
	private boolean segnoExists() {
		return (this.directions.segno != 65535);
	}
	private boolean segnoSegnoExists() {
		return (this.directions.segnoSegno != 65535);
	}
	private boolean alCodaBar(final int barId) {
		return (barId == this.directions.daCapoAlCoda ||
				barId == this.directions.daSegnoAlCoda ||
				barId == this.directions.daSegnoSegnoAlCoda ||
				(this.directions.daCapoAlCoda == 65535 && // If there are no "al coda", da coda will trigger by itself
				 this.directions.daSegnoAlCoda == 65535 &&
				 this.directions.daSegnoSegnoAlCoda == 65535));
	}
	private boolean alDoubleCodaBar(final int barId) {
		return (barId == this.directions.daCapoAlDoubleCoda ||
				barId == this.directions.daSegnoAlDoubleCoda ||
				barId == this.directions.daSegnoSegnoAlDoubleCoda ||
				(this.directions.daCapoAlDoubleCoda == 65535 && // If there are no "al double coda", da double coda will trigger by itself
				 this.directions.daSegnoAlDoubleCoda == 65535 &&
				 this.directions.daSegnoSegnoAlDoubleCoda == 65535));
	}
	private boolean alFineBar(final int barId) {
		return (barId == this.directions.daCapoAlFine ||
				barId == this.directions.daSegnoAlFine ||
				barId == this.directions.daSegnoSegnoAlFine ||
				(this.directions.daCapoAlFine == 65535 && // If there are no "al fine", fine will trigger by itself
				 this.directions.daSegnoAlFine == 65535 &&
				 this.directions.daSegnoSegnoAlFine == 65535));
	}

	public final BeatsMap getUnwrappedBeatsMap(final int songLengthMs) {
		if (this.unwrappedBeatsMap.songLengthMs == 0) {
			// Initialize a beat map first time
			BeatsMap beatsMap = new BeatsMap(songLengthMs, false);

			for (CombinedGPBars comboBeat : this.unwrappedBars) {
				for (Beat beat : comboBeat.barBeats) {
					beatsMap.beats.add(beat);
				}
			}
			this.unwrappedBeatsMap = new BeatsMap(beatsMap);
		}
		int numberOfBars = this.unwrappedBars.size();
		int numberOfFirstBeatsInBars = 0;
		int previousPos = 0;
		for (Beat beat : this.unwrappedBeatsMap.beats) {
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

		return this.unwrappedBeatsMap;
	}

	public final BeatsMap getUnwrappedBeatsMap() {
		return this.unwrappedBeatsMap;
	}

	public final int getLastBarStartPosition() {
		for (int i = this.unwrappedBeatsMap.beats.size() - 1; i > 0; i--) {
			Beat beat = this.unwrappedBeatsMap.beats.get(i);
			if (beat.firstInMeasure) {
				return beat.position();
			}
		}
		return 0;
	}
}
