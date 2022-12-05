package log.charter.io.midi.writer;

import static log.charter.io.Logger.debug;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Track;

import log.charter.song.Tempo;
import log.charter.song.TempoMap;

public class TempoWriter {
	public static void write(final TempoMap tempoMap, final Track track) throws InvalidMidiDataException {
		debug("Writing tempo");

		int lastTempoKbpm = -1;
		int lastBeats = -1;

		for (final Tempo t : tempoMap.tempos) {
			if (t.kbpm != lastTempoKbpm) {
				lastTempoKbpm = t.kbpm;
				final MetaMessage msg = new MetaMessage();
				final int mpq = (int) Math.floor(6.0E10D / t.kbpm);
				msg.setMessage(81, new byte[] { (byte) ((mpq >> 16) & 0xFF), (byte) ((mpq >> 8)
						& 0xFF), (byte) (mpq & 0xFF) }, 3);
				track.add(new MidiEvent(msg, Math.round(t.pos)));
			}
			if (t.beats != lastBeats) {
				lastBeats = t.beats;
				final MetaMessage msg = new MetaMessage();
				msg.setMessage(88, new byte[] { (byte) t.beats, 2, 24, 8 }, 4);
				track.add(new MidiEvent(msg, Math.round(t.pos)));
			}
		}

		debug("Writing tempo finished");
	}
}
