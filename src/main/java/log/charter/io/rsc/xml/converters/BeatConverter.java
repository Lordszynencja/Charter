package log.charter.io.rsc.xml.converters;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import log.charter.song.Beat;

public class BeatConverter implements Converter {

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(final Class type) {
		return type.equals(Beat.class);
	}

	@Override
	public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
		final Beat beat = (Beat) source;
		writer.addAttribute("t", beat.position() + "");
		writer.addAttribute("sig", beat.beatsInMeasure + "/" + beat.noteDenominator);
		if (beat.firstInMeasure) {
			writer.addAttribute("bar", "T");
		}
		if (beat.anchor) {
			writer.addAttribute("anchor", "T");
		}
	}

	private Beat readOldBeat(final HierarchicalStreamReader reader) {
		final int position = Integer.valueOf(reader.getAttribute("position"));
		final int beatsInMeasure = Integer.valueOf(reader.getAttribute("beatsInMeasure"));
		final int noteDenominator = Integer.valueOf(reader.getAttribute("noteDenominator"));
		final boolean firstInMeasure = Boolean.valueOf(reader.getAttribute("noteDenominator"));
		final boolean anchor = Boolean.valueOf(reader.getAttribute("anchor"));

		return new Beat(position, beatsInMeasure, noteDenominator, firstInMeasure, anchor);
	}

	@Override
	public Beat unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
		if (reader.getNodeName().equals("beat")) {
			return readOldBeat(reader);
		}

		final int t = Integer.valueOf(reader.getAttribute("t"));
		final String signature = reader.getAttribute("sig");
		int beatsInMeasure;
		int noteDenominator;
		final String[] signatureParts = signature.split("/");
		beatsInMeasure = Integer.valueOf(signatureParts[0]);
		noteDenominator = Integer.valueOf(signatureParts[1]);

		final boolean bar = "T".equals(reader.getAttribute("bar"));
		final boolean anchor = "T".equals(reader.getAttribute("anchor"));

		return new Beat(t, beatsInMeasure, noteDenominator, bar, anchor);
	}

}
