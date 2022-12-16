package log.charter.io.rs.xml.song;

import static java.lang.Math.max;
import static java.lang.Math.min;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.util.CollectionUtils.HashMap2;
import log.charter.util.IntRange;

@XStreamAlias("chordTemplate")
public class ChordTemplate {
	public String chordName;
	public String displayName;
	public HashMap2<Integer, Integer> fingers = new HashMap2<>();
	public HashMap2<Integer, Integer> frets = new HashMap2<>();

	public IntRange getStringRange() {
		int minString = 6;
		int maxString = 0;
		for (final int string : frets.keySet()) {
			minString = min(minString, string);
			maxString = max(maxString, string);
		}

		return new IntRange(minString, maxString);
	}

	public ChordTemplate() {
	}

	public ChordTemplate(final ChordTemplate other) {
		chordName = other.chordName;
		displayName = other.displayName;
		fingers = new HashMap2<>(other.fingers);
		frets = new HashMap2<>(other.frets);
	}
}
