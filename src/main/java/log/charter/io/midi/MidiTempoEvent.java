package log.charter.io.midi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

import log.charter.util.data.TimeSignature;

public class MidiTempoEvent {
	public static final int defaultKiloBeatsPerMinute = 120_000;
	public static final TimeSignature defaultTimeSignature = new TimeSignature(4, 4);

	private static final int BPM_CHANGE_ID = 81;
	private static final int TS_CHANGE_ID = 88;

	private static int kiloBeatsPerMinute(final byte[] bytes) {
		if (bytes.length < 6) {
			return defaultKiloBeatsPerMinute;
		}

		final int minutesPerMidiQuarterNote = ((bytes[3] & 0xFF) << 16) | ((bytes[4] & 0xFF) << 8) | (bytes[5] & 0xFF);
		return (int) Math.floor(6.0E10D / minutesPerMidiQuarterNote);
	}

	private static int denominatorFromPower(final int power) {
		int denominator = 1;

		for (int i = 0; i < power; i++) {
			denominator *= 2;
		}

		return denominator;
	}

	private static TimeSignature timeSignature(final byte[] bytes) {
		if (bytes.length < 5) {
			return defaultTimeSignature;
		}

		final int numerator = bytes[3] & 0xFF;
		final int denominator = denominatorFromPower(bytes[4]);
		return new TimeSignature(numerator, denominator);
	}

	private static MidiTempoEvent read(final MidiEvent midiEvent) {
		final byte[] bytes = midiEvent.getMessage().getMessage();
		if (bytes.length < 2) {
			return null;
		}

		final long time = midiEvent.getTick();
		return switch (bytes[1]) {
			case BPM_CHANGE_ID -> new MidiTempoEvent(time, kiloBeatsPerMinute(bytes));
			case TS_CHANGE_ID -> new MidiTempoEvent(time, timeSignature(bytes));
			default -> null;
		};
	}

	public static List<MidiTempoEvent> readTempoTrack(final Sequence seq) {
		if (seq.getTracks().length == 0) {
			return new ArrayList<>();
		}

		final Track track = seq.getTracks()[0];
		final Map<Long, MidiTempoEvent> midiEvents = new HashMap<>();
		for (int i = 0; i < track.size(); i++) {
			final MidiTempoEvent tempoEvent = read(track.get(i));
			if (tempoEvent == null) {
				continue;
			}

			if (midiEvents.containsKey(tempoEvent.time)) {
				midiEvents.get(tempoEvent.time).join(tempoEvent);
			} else {
				midiEvents.put(tempoEvent.time, tempoEvent);
			}
		}

		final List<MidiTempoEvent> events = new ArrayList<>(midiEvents.values());
		events.sort((a, b) -> Long.compare(a.time, b.time));

		for (int i = 0; i < events.size(); i++) {
			events.get(i).fillMissingDataFromPrevious(i == 0 ? null : events.get(i - 1));
		}

		return events;
	}

	public long time;
	public Integer kiloBeatsPerMinute;
	public TimeSignature timeSignature;

	public MidiTempoEvent(final long time, final Integer kiloBeatsPerMinute, final TimeSignature timeSignature) {
		this.time = time;
		this.kiloBeatsPerMinute = kiloBeatsPerMinute;
		this.timeSignature = timeSignature;
	}

	public MidiTempoEvent(final long time, final int kiloBeatsPerMinute) {
		this(time, kiloBeatsPerMinute, null);
	}

	public MidiTempoEvent(final long time, final TimeSignature timeSignature) {
		this(time, null, timeSignature);
	}

	public void join(final MidiTempoEvent other) {
		if (other.kiloBeatsPerMinute != null) {
			kiloBeatsPerMinute = other.kiloBeatsPerMinute;
		}
		if (other.timeSignature != null) {
			timeSignature = other.timeSignature;
		}
	}

	private void fillEmptyData(final int kiloBeatsPerMinute, final TimeSignature timeSignature) {
		if (this.kiloBeatsPerMinute == null) {
			this.kiloBeatsPerMinute = kiloBeatsPerMinute;
		}
		if (this.timeSignature == null) {
			this.timeSignature = timeSignature;
		}
	}

	public void fillMissingDataFromPrevious(final MidiTempoEvent previous) {
		if (previous == null) {
			fillEmptyData(defaultKiloBeatsPerMinute, defaultTimeSignature);
		} else {
			fillEmptyData(previous.kiloBeatsPerMinute, previous.timeSignature);
		}
	}
}