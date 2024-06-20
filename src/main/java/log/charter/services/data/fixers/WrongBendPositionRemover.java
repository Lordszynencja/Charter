package log.charter.services.data.fixers;

import java.util.stream.Collectors;

import log.charter.data.song.notes.CommonNote;

public class WrongBendPositionRemover {
	public static void fixBends(final CommonNote note) {
		note.bendValues(note.bendValues().stream()//
				.filter(b -> b.compareTo(note) >= 0 && b.compareTo(note.endPosition()) <= 0)//
				.collect(Collectors.toList()));
	}
}
