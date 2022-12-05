package log.charter.io.midi.writer;

import static log.charter.io.Logger.debug;

import java.util.Map;
import java.util.Map.Entry;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Track;

import log.charter.io.TickMsConverter;

public class SectionsWriter {

	public static void write(final Map<Integer, String> sections, final Track track) throws InvalidMidiDataException {
		debug("Writing sections");

		MetaMessage msg = new MetaMessage();
		msg.setMessage(3, "EVENTS".getBytes(), "EVENTS".getBytes().length);
		track.add(new MidiEvent(msg, 0));

		for (final Entry<Integer, String> entry : sections.entrySet()) {
			msg = new MetaMessage();
			final byte[] bytes = ("[section " + entry.getValue() + "]").getBytes();
			msg.setMessage(1, bytes, bytes.length);
			track.add(new MidiEvent(msg, (long) entry.getKey() * TickMsConverter.ticksPerBeat));
		}

		debug("Writing sections finished");
	}
}
