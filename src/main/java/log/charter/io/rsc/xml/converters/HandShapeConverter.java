package log.charter.io.rsc.xml.converters;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.HandShape;
import log.charter.data.song.position.FractionalPosition;

public class HandShapeConverter implements Converter {
	public static class TemporaryHandShape extends HandShape {
		private final int position;
		private final int endPosition;

		public TemporaryHandShape(final int position, final int endPosition) {
			this.position = position;
			this.endPosition = endPosition;
		}

		public HandShape transform(final ImmutableBeatsMap beats) {
			this.position(FractionalPosition.fromTimeRounded(beats, position));
			this.endPosition(FractionalPosition.fromTimeRounded(beats, endPosition));

			return new HandShape(this);
		}
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public boolean canConvert(final Class type) {
		return HandShape.class.equals(type);
	}

	@Override
	public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
		final HandShape handShape = (HandShape) source;

		writer.addAttribute("p", handShape.position().asString());
		writer.addAttribute("ep", handShape.endPosition().asString());
		writer.addAttribute("templateId", handShape.templateId + "");
	}

	private HandShape generateHandShapeFromPosition(final HierarchicalStreamReader reader) {
		final String positionString = reader.getAttribute("position");
		if (positionString != null && !positionString.isBlank()) {
			final int position = Integer.valueOf(positionString);
			final String lengthString = reader.getAttribute("length");
			final int length = lengthString == null || lengthString.isBlank() ? 0 : Integer.valueOf(lengthString);
			return new TemporaryHandShape(position, position + length);
		}

		return new HandShape(FractionalPosition.fromString(reader.getAttribute("p")),
				FractionalPosition.fromString(reader.getAttribute("ep")));
	}

	private Integer readTemplateId(final String s) {
		return s == null || s.isBlank() ? null : Integer.valueOf(s);
	}

	@Override
	public HandShape unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
		final HandShape handShape = generateHandShapeFromPosition(reader);
		handShape.templateId = readTemplateId(reader.getAttribute("templateId"));

		return handShape;
	}
}
