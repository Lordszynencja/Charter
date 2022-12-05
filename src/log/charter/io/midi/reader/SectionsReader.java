package log.charter.io.midi.reader;

import static log.charter.io.Logger.debug;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import log.charter.io.TickMsConverter;
import log.charter.io.midi.MidTrack;
import log.charter.io.midi.MidTrack.MidEvent;

public class SectionsReader {
	public static Map<Integer, String> read(final MidTrack t) {
		debug("Reading sections");
		final Map<Integer, String> sections = new HashMap<>(t.events.size());

		for (final MidEvent e : t.events) {
			if (e.msg.length >= 3) {
				final String name = new String(Arrays.copyOfRange(e.msg, 3, e.msg.length));
				if (name.startsWith("[section ")) {
					sections.put((int) (e.t / TickMsConverter.ticksPerBeat), name.substring(9, name.length() - 1));
				}
			}
		}

		debug("Reading sections finished");
		return sections;
	}
}
