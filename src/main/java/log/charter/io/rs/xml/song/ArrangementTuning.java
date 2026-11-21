package log.charter.io.rs.xml.song;

import static java.lang.Math.max;

import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.data.song.configs.Tuning;
import log.charter.io.rs.xml.converters.ArrangementTuningConverter;

@XStreamConverter(ArrangementTuningConverter.class)
public class ArrangementTuning {
	public int[] tuningValues;
	public int strings;

	public ArrangementTuning(final int[] tuningValues, final int strings) {
		this.tuningValues = tuningValues;
		this.strings = strings;
	}

	public ArrangementTuning(final Tuning tuning) {
		strings = tuning.strings();
		tuningValues = tuning.getTuning(max(6, strings));
	}
}
