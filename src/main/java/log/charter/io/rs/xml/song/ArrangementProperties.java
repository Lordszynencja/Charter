package log.charter.io.rs.xml.song;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.song.Arrangement.ArrangementSubtype;

@XStreamAlias("arrangementProperties")
public class ArrangementProperties {
	/**
	 * is main arrangement
	 */
	@XStreamAsAttribute
	public int represent = 1;
	@XStreamAsAttribute
	public int bonusArr;
	@XStreamAsAttribute
	public int standardTuning;
	@XStreamAsAttribute
	public int nonStandardChords;
	@XStreamAsAttribute
	public int barreChords;
	@XStreamAsAttribute
	public int powerChords;
	@XStreamAsAttribute
	public int dropDPower;
	@XStreamAsAttribute
	public int openChords;
	@XStreamAsAttribute
	public int fingerPicking;
	@XStreamAsAttribute
	public int pickDirection;
	@XStreamAsAttribute
	public int doubleStops;
	@XStreamAsAttribute
	public int palmMutes;
	@XStreamAsAttribute
	public int harmonics;
	@XStreamAsAttribute
	public int pinchHarmonics;
	@XStreamAsAttribute
	public int hopo;
	@XStreamAsAttribute
	public int tremolo;
	@XStreamAsAttribute
	public int slides;
	@XStreamAsAttribute
	public int unpitchedSlides;
	@XStreamAsAttribute
	public int bends;
	@XStreamAsAttribute
	public int tapping;
	@XStreamAsAttribute
	public int vibrato;
	@XStreamAsAttribute
	public int fretHandMutes;
	@XStreamAsAttribute
	public int slapPop;
	@XStreamAsAttribute
	public int twoFingerPicking;
	@XStreamAsAttribute
	public int fifthsAndOctaves;
	@XStreamAsAttribute
	public int syncopation;
	@XStreamAsAttribute
	public int bassPick;
	@XStreamAsAttribute
	public int sustain;
	@XStreamAsAttribute
	public int pathLead;
	@XStreamAsAttribute
	public int pathRhythm;
	@XStreamAsAttribute
	public int pathBass;

	public ArrangementType getType() {
		if (pathLead == 1) {
			return ArrangementType.Lead;
		}
		if (pathBass == 1) {
			return ArrangementType.Bass;
		}
		if (pathRhythm == 1) {
			return ArrangementType.Rhythm;
		}

		return ArrangementType.Combo;
	}

	public void setType(final ArrangementType type) {
		switch (type) {
			case Bass:
				pathBass = 1;
				break;
			case Lead:
				pathLead = 1;
				break;
			case Rhythm:
				pathRhythm = 1;
				break;
			case Combo:
			default:
				break;
		}
	}

	public ArrangementSubtype getSubtype() {
		if (represent == 1) {
			return ArrangementSubtype.MAIN;
		} else if (bonusArr == 1) {
			return ArrangementSubtype.BONUS;
		}

		return ArrangementSubtype.ALTERNATE;
	}

	public void setSubtype(final ArrangementSubtype subType) {
		represent = 0;
		bonusArr = 0;

		if (subType == ArrangementSubtype.MAIN) {
			represent = 1;
		} else if (subType == ArrangementSubtype.BONUS) {
			bonusArr = 1;
		}
	}
}
