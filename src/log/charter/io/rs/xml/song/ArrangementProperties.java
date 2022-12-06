package log.charter.io.rs.xml.song;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class ArrangementProperties {
	/**
	 * is main arrangement
	 */
	@XStreamAsAttribute
	int represent;
	@XStreamAsAttribute
	int bonusArr;
	@XStreamAsAttribute
	int standardTuning;
	@XStreamAsAttribute
	int nonStandardChords;
	@XStreamAsAttribute
	int barreChords;
	@XStreamAsAttribute
	int powerChords;
	@XStreamAsAttribute
	int dropDPower;
	@XStreamAsAttribute
	int openChords;
	@XStreamAsAttribute
	int fingerPicking;
	@XStreamAsAttribute
	int pickDirection;
	@XStreamAsAttribute
	int doubleStops;
	@XStreamAsAttribute
	int palmMutes;
	@XStreamAsAttribute
	int harmonics;
	@XStreamAsAttribute
	int pinchHarmonics;
	@XStreamAsAttribute
	int hopo;
	@XStreamAsAttribute
	int tremolo;
	@XStreamAsAttribute
	int slides;
	@XStreamAsAttribute
	int unpitchedSlides;
	@XStreamAsAttribute
	int bends;
	@XStreamAsAttribute
	int tapping;
	@XStreamAsAttribute
	int vibrato;
	@XStreamAsAttribute
	int fretHandMutes;
	@XStreamAsAttribute
	int slapPop;
	@XStreamAsAttribute
	int twoFingerPicking;
	@XStreamAsAttribute
	int fifthsAndOctaves;
	@XStreamAsAttribute
	int syncopation;
	@XStreamAsAttribute
	int bassPick;
	@XStreamAsAttribute
	int sustain;
	@XStreamAsAttribute
	int pathLead;
	@XStreamAsAttribute
	int pathRhythm;
	@XStreamAsAttribute
	int pathBass;
}
