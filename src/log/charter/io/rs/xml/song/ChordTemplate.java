package log.charter.io.rs.xml.song;

import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("chordTemplate")
public class ChordTemplate {
	public String chordName;
	public String displayName;
	public Map<Integer, Integer> fingers = new HashMap<>();
	public Map<Integer, Integer> frets = new HashMap<>();
}
