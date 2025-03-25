package log.charter.services.data.fixers;

import java.util.HashSet;

import log.charter.data.song.Arrangement;

public class MissingFingersOnChordTemplatesFixer {
	public static void fix(final Arrangement arrangement) {
		arrangement.chordTemplates.forEach(chordTemplate -> {
			for (final int string : new HashSet<>(chordTemplate.fingers.keySet())) {
				final Integer finger = chordTemplate.fingers.get(string);
				if (finger == null || finger < 0 || finger > 5) {
					chordTemplate.fingers.remove(string);
				}
			}
		});
	}
}
