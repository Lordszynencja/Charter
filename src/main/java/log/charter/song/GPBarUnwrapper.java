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
	ArrayList2<CombinedGPBars> unwrapped_bars;
	BeatsMap unwrapped_beats_map;
	List<Integer> bar_order;

	public GPBarUnwrapper(Directions directions) {
		this.directions = directions;
		this.bars = new ArrayList2<CombinedGPBars>();
		this.unwrapped_bars = new ArrayList2<CombinedGPBars>();
		this.unwrapped_beats_map = new BeatsMap(0);
		this.bar_order = new ArrayList<>();
	}

	public boolean add_bar(final CombinedGPBars bar) {
		return this.bars.add(bar);
	}

	public final CombinedGPBars get(final int index) {
		return this.bars.get(index);
	}

	public final CombinedGPBars getLast() {
		return this.bars.getLast();
	}

	private final int read_alternate_ending_bit(final int bitmask, final int start_bit)
	{
		int alternate_ending = start_bit;

		for (int i = start_bit; i < 8; i++) {
			if (((bitmask >> i) & 1) != 0) {
				alternate_ending = i+1;
				break;
			}
		}
		return alternate_ending;
	}

	public void unwrap() {
		// Repeat handling
		int start_of_repeat_bar = 1; // If no repeat starts are set, the first bar is used
        HashMap<Integer, Integer> repeat_tracker = new HashMap<>();
		
		// Alternate ending handling
		int next_alternate_ending_to_process = 1;
		int stored_next_alternate_ending = 0;
		int latest_alternate_ending = 1;
		int bar_to_progress_past_to_disable_alt_ending = 0;
		
		// Direction handling
		boolean fine_activated = false;
		boolean coda_activated = false;
		boolean double_coda_activated = false;

		boolean is_in_repeat_section = false;
		int bar_to_check_if_directions_are_ok = 0;

		for (int i = 0; i < this.bars.size(); i++) {
			final CombinedGPBars combo_bar = this.bars.get(i);
			final int current_id = combo_bar.gp_bar_id;
			for (int j = i; j < this.bars.size(); j++) {
				if (this.bars.get(j).bar.isRepeatStart ||
					this.directions.coda == j ||
					this.directions.double_coda == j) {
					is_in_repeat_section = false;
					break;
				}
				else if (this.bars.get(j).bar.repeatCount != 0) {
					is_in_repeat_section = true;
					bar_to_check_if_directions_are_ok = j+1;
					break;
				}
				else {
					is_in_repeat_section = false;
				}
			}

			if (is_in_repeat_section) {
				if (repeat_tracker.containsKey(bar_to_check_if_directions_are_ok)) {
					if (repeat_tracker.get(bar_to_check_if_directions_are_ok) == 0) {
						is_in_repeat_section = false;
					}
				}
			}

			if (combo_bar.bar.alternateEndings != 0) {
				bar_to_progress_past_to_disable_alt_ending = current_id > bar_to_progress_past_to_disable_alt_ending ? current_id : bar_to_progress_past_to_disable_alt_ending;
				latest_alternate_ending = read_alternate_ending_bit(combo_bar.bar.alternateEndings, next_alternate_ending_to_process-1);

				// If this is a different alternate ending skip it
				if (next_alternate_ending_to_process != latest_alternate_ending) {
					continue;
				}
				// If the right ending, progress to the next one
				else if (next_alternate_ending_to_process == latest_alternate_ending) {
					stored_next_alternate_ending = next_alternate_ending_to_process + 1; // Read when repeating
				}
			}
			else {
				// If there are alt endings, skip the ones we have processed
				if (latest_alternate_ending != 0 &&
					latest_alternate_ending == next_alternate_ending_to_process - 1) {
					bar_to_progress_past_to_disable_alt_ending = current_id > bar_to_progress_past_to_disable_alt_ending ? current_id : bar_to_progress_past_to_disable_alt_ending;
					continue;
				}
			}
			this.bar_order.add(current_id);

			// Check early if this is the fine bar and if so, end unwrapping
			if (fine_activated && fine_bar(current_id)) {
				break;
			}
			// If this is a repeat bar, initialize its entry to keep track of potential nested repeats
			if (combo_bar.bar.repeatCount != 0) {
				repeat_tracker.putIfAbsent(current_id, -1);
			}

			// Store repeat start
			if (combo_bar.bar.isRepeatStart) {
				// When passing a new repeat bar we can reset alternate ending variables
				if (start_of_repeat_bar != current_id) {
					next_alternate_ending_to_process = 1;
					stored_next_alternate_ending = 0;
					latest_alternate_ending = 1;
				}
				start_of_repeat_bar = current_id;
			}

			// Handle repeat bars
			if (repeat_tracker.containsKey(current_id)) {
				int repeat_n_times = repeat_tracker.get(current_id);

				if (repeat_n_times == -1 && combo_bar.bar.repeatCount != 0) {
					repeat_n_times = combo_bar.bar.repeatCount-1; // Repeat: 2 means do it 1 more time (since we just did the first time)
					i = start_of_repeat_bar-1-1; // Example: Bar 1 being index 0, and after continue the for loop increments i + 1
				}
				else if (repeat_n_times > 0) {
					i = start_of_repeat_bar-1-1; // Example: Bar 1 being index 0, and after continue the for loop increments i + 1
				}

				repeat_n_times--; // We are sending it to repeat
				repeat_tracker.put(current_id, repeat_n_times);
				next_alternate_ending_to_process = stored_next_alternate_ending;
				stored_next_alternate_ending = 0;
				latest_alternate_ending = 0;
				if (repeat_n_times >= 0) {
					continue; // Don't continue to check directions if we are repeating
				}
			}

			if (fine_exists() && al_fine_bar(current_id)) {
				fine_activated = true;
			}
			if (coda_exists() && al_coda_bar(current_id)) {
				coda_activated = true;
			}
			if (double_coda_exists() && al_double_coda_bar(current_id)) {
				double_coda_activated = true;
			}

			int direction_bar_id = 0;

			if (da_capo_bar(current_id)) {
				i = -1; // Return to first bar
			}
			else if (is_in_repeat_section == false &&
					((segno_exists() && (direction_bar_id = da_segno_bar(current_id)) != 0) ||
				     (segno_segno_exists() && (direction_bar_id = da_segno_segno_bar(current_id)) != 0))) {
				i = direction_bar_id - 1 - 1; // Go to segno/segno segno bar
				if (repeat_tracker.containsKey(bar_to_check_if_directions_are_ok)) {
					repeat_tracker.put(bar_to_check_if_directions_are_ok, -1); // Reset repeats when using direction
				}
				for (Entry<Integer, Integer> entry : repeat_tracker.entrySet() ) {
						entry.setValue(-1);
					}
				next_alternate_ending_to_process = 1; // Reset alt ending variables
				
			}
			else if (coda_activated &&
					 is_in_repeat_section == false && (direction_bar_id = da_coda_bar(current_id)) != 0) {
				coda_activated = false;
				i = direction_bar_id - 1 - 1; // Go to coda bar
				for (Entry<Integer, Integer> entry : repeat_tracker.entrySet() ) {
					entry.setValue(-1);
				}
			}
			else if (double_coda_activated &&
					 is_in_repeat_section == false && (direction_bar_id = da_double_coda_bar(current_id)) != 0) {
				double_coda_activated = false;
				i = direction_bar_id - 1 - 1; // Go to double coda bar
				for (Entry<Integer, Integer> entry : repeat_tracker.entrySet() ) {
					entry.setValue(-1);
				}
			}
		}

		// Now that we know the bar order, start unwrapping
		for (int bar: this.bar_order) {
			this.unwrapped_bars.add(new CombinedGPBars(this.bars.get(bar-1)));
		}

		// With all bars in correct order, update the positions of them (reduce truncated positions)
		double sum_of_bar_widths = 0;
		for (int i = 0; i < this.unwrapped_bars.size(); i++) {
			for (int j = 0; j < this.unwrapped_bars.get(i).bar_beats.size(); j++) {
				this.unwrapped_bars.get(i).bar_beats.get(j).position((int)sum_of_bar_widths);
				sum_of_bar_widths += this.unwrapped_bars.get(i).bar_beats.get(j).bar_width_in_time_ms;
			}
		}
	}

	private boolean da_capo_bar(final int bar_id) {
		if (bar_id == this.directions.da_capo) {
			this.directions.da_capo = 65535;
			return true;
		}
		if (bar_id == this.directions.da_capo_al_coda) {
			this.directions.da_capo_al_coda = 65535;
			return true;
		}
		if (bar_id == this.directions.da_capo_al_double_coda) {
			this.directions.da_capo_al_double_coda = 65535;
			return true;
		}
		if (bar_id == this.directions.da_capo_al_fine) {
			this.directions.da_capo_al_fine = 65535;
			return true;
		}

		return false;
	}
	private int da_segno_bar(final int bar_id) {
		int direction_bar_id = 0;
		int symbol_bar_id = this.directions.segno;

		if (bar_id == this.directions.da_segno) {
			direction_bar_id = symbol_bar_id;
			this.directions.da_segno = 65535;
		}
		else if (bar_id == this.directions.da_segno_al_coda) {
			direction_bar_id = symbol_bar_id;
			this.directions.da_segno_al_coda = 65535;
		}
		else if (bar_id == this.directions.da_segno_al_double_coda) {
			direction_bar_id = symbol_bar_id;
			this.directions.da_segno_al_double_coda = 65535;
		}
		else if (bar_id == this.directions.da_segno_al_fine) {
			direction_bar_id = symbol_bar_id;
			this.directions.da_segno_al_fine = 65535;
		}
		return direction_bar_id;
	}
	private int da_segno_segno_bar(final int bar_id) {
		int direction_bar_id = 0;
		int symbol_bar_id = this.directions.segno_segno;
		if (bar_id == this.directions.da_segno_segno) {
			direction_bar_id = symbol_bar_id;
			this.directions.da_segno_segno = 65535;
		}
		else if (bar_id == this.directions.da_segno_segno_al_coda) {
			direction_bar_id = symbol_bar_id;
			this.directions.da_segno_segno_al_coda = 65535;
		}
		else if (bar_id == this.directions.da_segno_segno_al_double_coda) {
			direction_bar_id = symbol_bar_id;
			this.directions.da_segno_segno_al_double_coda = 65535;
		}
		else if (bar_id == this.directions.da_segno_segno_al_fine) {
			direction_bar_id = symbol_bar_id;
			this.directions.da_segno_segno_al_fine = 65535;
		}
		return direction_bar_id;
	}
	private int da_coda_bar(final int bar_id) {
		int direction_bar_id = 0;
		if (bar_id == this.directions.da_coda) {
			direction_bar_id = this.directions.coda;
			this.directions.da_coda = 65535;
		}
		return direction_bar_id;
	}
	private int da_double_coda_bar(final int bar_id) {
		int direction_bar_id = 0;
		if (bar_id == this.directions.da_double_coda) {
			direction_bar_id = this.directions.double_coda;
			this.directions.da_double_coda = 65535;
		}
		return direction_bar_id;
	}
	private boolean fine_bar(final int bar_id) {
		if (bar_id == this.directions.fine) {
			this.directions.fine = 65535;
			return true;
		}
		return false;
	}
	private boolean coda_exists() {
		return (this.directions.coda != 65535);
	}
	private boolean double_coda_exists() {
		return (this.directions.double_coda != 65535);
	}
	private boolean fine_exists() {
		return (this.directions.fine != 65535);
	}
	private boolean segno_exists() {
		return (this.directions.segno != 65535);
	}
	private boolean segno_segno_exists() {
		return (this.directions.segno_segno != 65535);
	}
	private boolean al_coda_bar(final int bar_id) {
		return (bar_id == this.directions.da_capo_al_coda ||
				bar_id == this.directions.da_segno_al_coda ||
				bar_id == this.directions.da_segno_segno_al_coda ||
				(this.directions.da_capo_al_coda == 65535 && // If there are no "al coda", da coda will trigger by itself
				 this.directions.da_segno_al_coda == 65535 &&
				 this.directions.da_segno_segno_al_coda == 65535));
	}
	private boolean al_double_coda_bar(final int bar_id) {
		return (bar_id == this.directions.da_capo_al_double_coda ||
				bar_id == this.directions.da_segno_al_double_coda ||
				bar_id == this.directions.da_segno_segno_al_double_coda ||
				(this.directions.da_capo_al_double_coda == 65535 && // If there are no "al double coda", da double coda will trigger by itself
				 this.directions.da_segno_al_double_coda == 65535 &&
				 this.directions.da_segno_segno_al_double_coda == 65535));
	}
	private boolean al_fine_bar(final int bar_id) {
		return (bar_id == this.directions.da_capo_al_fine ||
				bar_id == this.directions.da_segno_al_fine ||
				bar_id == this.directions.da_segno_segno_al_fine ||
				(this.directions.da_capo_al_fine == 65535 && // If there are no "al fine", fine will trigger by itself
				 this.directions.da_segno_al_fine == 65535 &&
				 this.directions.da_segno_segno_al_fine == 65535));
	}

	public final BeatsMap get_unwrapped_beats_map(final int song_length_ms) {
		if (this.unwrapped_beats_map.songLengthMs == 0) {
			// Initialize a beat map first time
			BeatsMap beats_map = new BeatsMap(song_length_ms, false);

			for (CombinedGPBars combo_beat : this.unwrapped_bars) {
				for (Beat beat : combo_beat.bar_beats) {
					beats_map.beats.add(beat);
				}
			}
			this.unwrapped_beats_map = new BeatsMap(beats_map);
		}
		int number_of_bars = this.unwrapped_bars.size();
		int number_of_first_beats_in_bars = 0;
		int previous_pos = 0;
		for (Beat beat : this.unwrapped_beats_map.beats) {
			if (beat.firstInMeasure) {
				number_of_first_beats_in_bars++;
			}
			if (previous_pos > beat.position()) {
				Logger.error("Unwrapping issue detected: earlier beats were placed ahead.");
			}
			previous_pos = beat.position();
		}

		if (number_of_bars != number_of_first_beats_in_bars) {
			Logger.error("Unwrapping issue detected: incorrect number of first beats in bars.");
		}

		return this.unwrapped_beats_map;
	}

	public final BeatsMap get_unwrapped_beats_map() {
		return this.unwrapped_beats_map;
	}

	public final int get_last_bar_start_position() {
		for (int i = this.unwrapped_beats_map.beats.size() - 1; i > 0; i--) {
			Beat beat = this.unwrapped_beats_map.beats.get(i);
			if (beat.firstInMeasure) {
				return beat.position();
			}
		}
		return 0;
	}
}
