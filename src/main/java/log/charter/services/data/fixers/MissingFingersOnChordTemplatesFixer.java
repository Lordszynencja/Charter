package log.charter.services.data.fixers;

import java.util.HashSet;

import log.charter.data.song.Arrangement;

public class MissingFingersOnChordTemplatesFixer {
	public static void fix(final Arrangement arrangement) {
		arrangement.chordTemplates.forEach(chordTemplate -> {
			for (final int string : new HashSet<>(chordTemplate.fingers.keySet())) {
				if (chordTemplate.fingers.get(string) == null) {
					chordTemplate.fingers.remove(string);
				}
			}
		});
	}
}
