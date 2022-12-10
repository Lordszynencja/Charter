package log.charter.io.rs.xml.song;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class ArrangementProperties {
	/**
	 * is main arrangement
	 */
	@XStreamAsAttribute
	public int represent;
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
}
