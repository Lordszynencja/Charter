package log.charter.io.rs.xml.song;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.util.IntRange;

@XStreamAlias("chordTemplate")
public class ChordTemplate {
	public String chordName;
	public String displayName;
	public Map<Integer, Integer> fingers = new HashMap<>();
	public Map<Integer, Integer> frets = new HashMap<>();

	public IntRange getStringRange() {
		int minString = 6;
		int maxString = 0;
		for (final int string : frets.keySet()) {
			minString = min(minString, string);
			maxString = max(maxString, string);
		}

		return new IntRange(minString, maxString);
	}
}
