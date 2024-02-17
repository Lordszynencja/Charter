package log.charter.io.rsc.xml.converters;

import java.util.Map;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.AbstractCollectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import log.charter.song.Phrase;
import log.charter.util.CollectionUtils.HashMap2;

public class PhraseDataConverter extends AbstractCollectionConverter {

	public PhraseDataConverter(final Mapper mapper) {
		super(mapper);
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public boolean canConvert(final Class type) {
		return Map.class.isAssignableFrom(type);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
		final Map<String, Phrase> phrases = (Map<String, Phrase>) source;

		phrases.forEach((name, phrase) -> {
			writer.startNode("phrase");
			writer.addAttribute("name", name);
			if (phrase.maxDifficulty > 0) {
				writer.addAttribute("maxDifficulty", phrase.maxDifficulty + "");
			}
			if (phrase.solo) {
				writer.addAttribute("solo", "T");
			}
			writer.endNode();
		});
	}

	private int readMaxDifficulty(final HierarchicalStreamReader reader) {
		final String s = reader.getAttribute("maxDifficulty");
		return s == null ? 0 : Integer.valueOf(s);
	}

	private boolean readSolo(final HierarchicalStreamReader reader) {
		final String s = reader.getAttribute("solo");
		return s == null ? false : "T".equals(s);
	}

	@Override
	public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
		final Map<String, Phrase> phrases = new HashMap2<>();

		while (reader.hasMoreChildren()) {
			reader.moveDown();
			try {
				final String name = reader.getAttribute("name");
				final int maxDifficulty = readMaxDifficulty(reader);
				final boolean solo = readSolo(reader);
				phrases.put(name, new Phrase(maxDifficulty, solo));
			} catch (final Exception e) {
			}
			reader.moveUp();
		}

		return phrases;
	}
}
