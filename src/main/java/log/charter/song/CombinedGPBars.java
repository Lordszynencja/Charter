package log.charter.song;

import java.util.List;

import log.charter.io.gp.gp5.GPMasterBar;
import log.charter.io.gp.gp5.GPNote;
import log.charter.io.Logger;
import log.charter.io.gp.gp5.GPBeat;
import log.charter.io.gp.gp5.GPBeatEffects;
import log.charter.io.gp.gp5.GPChord;
import log.charter.io.gp.gp5.GPDuration;
import log.charter.util.CollectionUtils.ArrayList2;

public class CombinedGPBars {
	public class GPBeatUnwrapper extends GPBeat {
		double note_duration;
		double note_time_ms;

		public GPBeatUnwrapper(final int tempo, final int dots, final boolean isEmpty, final GPDuration duration,
				final int tupletNumerator, final int tupletDenominator, final GPBeatEffects beatEffects,
				final GPChord chord, final List<GPNote> notes, final String text) {
			super(tempo,dots,isEmpty,duration,tupletNumerator,tupletDenominator,beatEffects,chord,notes,text);
			this.note_duration = note_duration(gp_duration_to_note_denominator(duration), tupletNumerator, tupletDenominator, dots != 0);
			this.note_time_ms = note_time_from_duration(tempo);
		}
		public GPBeatUnwrapper(final GPBeat beat) {
			super(beat.tempo,beat.dots,beat.isEmpty,beat.duration,beat.tupletNumerator,beat.tupletDenominator,beat.beatEffects,beat.chord,beat.notes,beat.text);
			this.note_duration = note_duration(gp_duration_to_note_denominator(duration), tupletNumerator, tupletDenominator, dots != 0);
			this.note_time_ms = note_time_from_duration(tempo);
		}
		
		final int gp_duration_to_note_denominator(GPDuration duration) {
			// Example length 16 -> 64/16 = 4 -> 1/4 (quarter)
			return (64/duration.length);
		}
		final double gp_duration_to_time(GPDuration duration) {
			// Example length 16 -> 64/16 = 4 -> 1/4 (quarter)
			double note_duration = note_duration(gp_duration_to_note_denominator(duration), this.tupletNumerator, this.tupletDenominator, this.dots != 0);
			double note_time_ms = note_time_from_custom_duration(this.tempo, note_duration);
			return note_time_ms;
		}
		static final double four_over_four_beat_length(int bpm) {
			return 60000/(double)bpm;
		}

		double note_time_from_duration(int bpm) {
			final double four_over_four_beat_length = four_over_four_beat_length(bpm); // Same as whole note note time
			final double note_time_ms = four_over_four_beat_length * this.note_duration;
			return note_time_ms;
		}
		double note_time_from_custom_duration(int bpm, double duration) {
			final double four_over_four_beat_length = four_over_four_beat_length(bpm); // Same as whole note note time
			final double note_time_ms = four_over_four_beat_length * duration;
			return note_time_ms;
		}
		double note_time(int bpm, int note_length_den, int note_tuple_num, int note_tuple_den, boolean is_dotted_note) {
			final double four_over_four_beat_length = four_over_four_beat_length(bpm); // Same as whole note note time
			final double note_duration = note_duration(note_length_den, note_tuple_num, note_tuple_den, is_dotted_note);
			final double note_time_ms = (int)(four_over_four_beat_length * note_duration);
			return note_time_ms;
		}
		double note_duration(int note_length_den, int note_tuple_num, int note_tuple_den, boolean is_dotted_note) {
			// Example triplet 16th note: 4/16 -> 1/4 (0.25) -> 0.1667 
			final double whole_note_length = 4;
			final double note_duration = whole_note_length / (double)note_length_den;
			double note_tupled_length = note_duration * ((double)note_tuple_den / (double)note_tuple_num);
			if (is_dotted_note) {
				note_tupled_length *= 1.5;
			}
			return note_tupled_length;
		}
		static double bar_duration(int num, int den) {
			// Example 6/8 -> 3/4, 2/2 -> 4/4, 12/8 -> 6/4
			final double relation_to_four_den = (double)den / 4;
			final double relation_to_four_num = (double)num / relation_to_four_den;
			return relation_to_four_num;
		}
		static double bar_time(int bpm, int num, int den) {
			double base_bar_length = four_over_four_beat_length(bpm);
			double bar_duration = bar_duration(num, den);
			return (base_bar_length * bar_duration) / 4;
		}
		static double bar_duration_to_time(double duration, int tempo) {
			return (duration * bar_time(tempo,4,4));
		}
	}

	public class BeatUnwrapper extends Beat {
		double bar_width_in_time_ms;
		double float_pos;
		public BeatUnwrapper(final Beat beat) {
			super(beat);
		}
		public BeatUnwrapper(final BeatUnwrapper beat) {
			super(beat);
			this.bar_width_in_time_ms = beat.bar_width_in_time_ms;
			this.float_pos = beat.float_pos;
		}
	}

	public GPMasterBar bar;
	public ArrayList2<BeatUnwrapper> bar_beats;
	public ArrayList2<GPBeatUnwrapper> note_beats;
	public int gp_bar_id;
	public int available_space_in_64ths;
	public int notes_in_bar;

	public CombinedGPBars(final GPMasterBar bar, final int id) {
		this.bar = bar;
		this.gp_bar_id = id;
		this.bar_beats = new ArrayList2<>();
		this.note_beats = new ArrayList2<>();
	}

	public CombinedGPBars(final CombinedGPBars combo_bar) {
		this.bar = combo_bar.bar;
		this.gp_bar_id = combo_bar.gp_bar_id;
		this.available_space_in_64ths = combo_bar.available_space_in_64ths;
		this.notes_in_bar = combo_bar.notes_in_bar;
		this.bar_beats = new ArrayList2<>();
		for (BeatUnwrapper beat : combo_bar.bar_beats) {
			bar_beats.add(new BeatUnwrapper(beat));
		}
		this.note_beats = new ArrayList2<>();
		for (GPBeatUnwrapper beat : combo_bar.note_beats) {
			note_beats.add(new GPBeatUnwrapper(beat));
		}
	}

	public void update_bars_from_note_tempo() {
		double sum_of_note_lengths = 0;
		double sum_of_note_durations = 0;

		int beat_index = 0;
		double beat_duration = 4.0 / this.bar_beats.get(beat_index).noteDenominator;
		double duration_to_extract_position = beat_duration;

		this.bar_beats.get(beat_index).position(0);
		this.bar_beats.get(beat_index).anchor = true;
		this.bar_beats.get(beat_index).firstInMeasure = true;

		beat_index++;

		for (final GPBeatUnwrapper note_beat : this.note_beats) {
			sum_of_note_lengths += note_beat.note_time_ms;
			sum_of_note_durations += note_beat.note_duration;

			if ((int)sum_of_note_durations * 16 > this.available_space_in_64ths) {
				Logger.error("Bar exceeds allowed duration. Bar: " + this.gp_bar_id);
				return;
			}
			// While loop to handle long notes over multiple beats
			while (sum_of_note_durations >= duration_to_extract_position) {
				double overshoot_duration = sum_of_note_durations - duration_to_extract_position;
				double overshoot_time = 0;

				if (overshoot_duration > 0) {
					overshoot_time = (overshoot_duration * note_beat.note_time_ms) / note_beat.note_duration;
				}

				if (beat_index < this.bar_beats.size()) {
					this.bar_beats.get(beat_index).position((int)(sum_of_note_lengths-overshoot_time));
					this.bar_beats.get(beat_index).float_pos = sum_of_note_lengths-overshoot_time;
					this.bar_beats.get(beat_index).firstInMeasure = false;
				}
				if (beat_index > 0) {
					this.bar_beats.get(beat_index-1).bar_width_in_time_ms = sum_of_note_lengths - overshoot_time - this.bar_beats.get(beat_index-1).float_pos;
				}
				beat_index++;
				duration_to_extract_position += beat_duration;
			}
		}
	}
}
