package log.charter.io.rs.xml.song;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.song.configs.Tuning;

public class ArrangementTuning {
	@XStreamAsAttribute
	public int string0;
	@XStreamAsAttribute
	public int string1;
	@XStreamAsAttribute
	public int string2;
	@XStreamAsAttribute
	public int string3;
	@XStreamAsAttribute
	public int string4;
	@XStreamAsAttribute
	public int string5;

	public ArrangementTuning(final Tuning tuning) {
		final int[] tuningValues = tuning.getTuning(6);
		string0 = tuningValues[0];
		string1 = tuningValues[1];
		string2 = tuningValues[2];
		string3 = tuningValues[3];
		string4 = tuningValues[4];
		string5 = tuningValues[5];
	}
}
