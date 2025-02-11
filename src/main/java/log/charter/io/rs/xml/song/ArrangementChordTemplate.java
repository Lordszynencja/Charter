package log.charter.io.rs.xml.song;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.data.song.ChordTemplate;
import log.charter.util.collections.HashMap2;

@XStreamAlias("chordTemplate")
public class ArrangementChordTemplate {
	public String chordName;
	public String displayName;
	public HashMap2<Integer, Integer> fingers = new HashMap2<>();
	public HashMap2<Integer, Integer> frets = new HashMap2<>();

	public ArrangementChordTemplate() {
	}

	public ArrangementChordTemplate(final ChordTemplate chordTemplate) {
		chordName = chordTemplate.chordName;
		displayName = chordName + (chordTemplate.forceArpeggioInRS ? "-arp" : "");
		fingers = new HashMap2<>(chordTemplate.fingers);
		frets = new HashMap2<>(chordTemplate.frets);
	}
}
