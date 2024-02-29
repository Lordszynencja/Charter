package log.charter.song;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.data.config.Config.maxStrings;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.data.config.Config;
import log.charter.io.rs.xml.song.ArrangementChordTemplate;
import log.charter.io.rsc.xml.converters.ChordTemplateConverter;
import log.charter.util.CollectionUtils.HashMap2;
import log.charter.util.IntRange;

@XStreamAlias("chordTemplate")
@XStreamConverter(ChordTemplateConverter.class)
public class ChordTemplate {
	public static final HashMap2<String, Integer> fingerIds = new HashMap2<>();
	public static final HashMap2<Integer, String> fingerNames = new HashMap2<>();

	static {
		fingerIds.put("T", 0);
		fingerIds.put("1", 1);
		fingerIds.put("2", 2);
		fingerIds.put("3", 3);
		fingerIds.put("4", 4);

		fingerIds.forEach((name, id) -> fingerNames.put(id, name));
		fingerNames.put(null, "");
	}

	public String chordName = "";
	public boolean arpeggio;
	public HashMap2<Integer, Integer> fingers = new HashMap2<>();
	public HashMap2<Integer, Integer> frets = new HashMap2<>();

	public ChordTemplate() {
	}

	public ChordTemplate(final ArrangementChordTemplate arrangementChordTemplate) {
		chordName = arrangementChordTemplate.chordName;
		arpeggio = arrangementChordTemplate.displayName.contains("-arp");
		fingers = new HashMap2<>(arrangementChordTemplate.fingers);
		frets = new HashMap2<>(arrangementChordTemplate.frets);
	}

	public ChordTemplate(final ChordTemplate other) {
		chordName = other.chordName;
		arpeggio = other.arpeggio;
		fingers = new HashMap2<>(other.fingers);
		frets = new HashMap2<>(other.frets);
	}

	public ChordTemplate(final String chordName, final boolean arpeggio, final HashMap2<Integer, Integer> fingers,
			final HashMap2<Integer, Integer> frets) {
		this.chordName = chordName;
		this.arpeggio = arpeggio;
		this.fingers = fingers;
		this.frets = frets;
	}

	public IntRange getStringRange() {
		int minString = maxStrings;
		int maxString = 0;
		for (final int string : frets.keySet()) {
			minString = min(minString, string);
			maxString = max(maxString, string);
		}

		return new IntRange(minString, maxString);
	}

	public int getLowestFret() {
		int lowestFret = Config.frets;

		for (final int fret : frets.values()) {
			lowestFret = min(lowestFret, fret);
		}

		return lowestFret;
	}

	public int getLowestNonzeroFret() {
		int lowestFret = Config.frets;

		for (final int fret : frets.values()) {
			if (fret == 0) {
				continue;
			}

			lowestFret = min(lowestFret, fret);
		}

		return lowestFret;
	}

	public int getHighestFret() {
		int highestFret = 0;

		for (final int fret : frets.values()) {
			highestFret = max(highestFret, fret);
		}

		return highestFret;
	}

	public String getTemplateFrets(final int strings) {
		boolean allFretsBelow10 = true;
		final String[] fretNames = new String[strings];
		for (int string = 0; string < strings; string++) {
			final Integer fret = frets.get(string);
			fretNames[string] = fret == null ? "-" : fret.toString();
			if (fret != null && fret >= 10) {
				allFretsBelow10 = false;
			}
		}

		return String.join(allFretsBelow10 ? "" : " ", fretNames);
	}

	public String getTemplateFrets(final int strings, final int width) {
		final String numberFormat = "%" + width + "d";
		final String empty = " ".repeat(width - 1) + "-";

		final String[] fretNames = new String[strings];
		for (int string = 0; string < strings; string++) {
			final Integer fret = frets.get(string);
			fretNames[string] = fret == null ? empty : numberFormat.formatted(fret);
		}

		return String.join(" ", fretNames);
	}

	public String getTemplateFingers(final int strings) {
		final String[] fingers = new String[strings];
		for (int string = 0; string < strings; string++) {
			final Integer finger = this.fingers.get(string);
			fingers[string] = finger == null ? " " : fingerNames.get(finger);
		}

		return String.join("", fingers);
	}

	public String name() {
		return chordName + (arpeggio ? "(arp)" : "");
	}

	public String getNameWithFrets(final int strings) {
		return chordName //
				+ (arpeggio ? "(arpeggio)" : "")//
				+ " (" + getTemplateFrets(strings) + ")";
	}

	public boolean equals(final ChordTemplate other) {
		return chordName.equals(other.chordName)//
				&& arpeggio == other.arpeggio//
				&& frets.equals(other.frets)//
				&& fingers.equals(other.fingers);
	}
}
