package log.charter.io.rsc.xml.converters;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import log.charter.data.song.notes.ChordOrNote;

public class ChordOrNoteConverter implements Converter {
	private static final ChordConverter chordConverter = new ChordConverter();
	private static final NoteConverter noteConverter = new NoteConverter();

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(final Class type) {
		return type.equals(ChordOrNote.class);
	}

	@Override
	public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
	}

	private boolean readBoolean(final String s) {
		return s == null ? false : "T".equals(s);
	}

	@Override
	public ChordOrNote unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
		final boolean isChord = readBoolean(reader.getAttribute("chord"));
		if (isChord) {
			return ChordOrNote.from(chordConverter.unmarshal(reader, context));
		}

		return ChordOrNote.from(noteConverter.unmarshal(reader, context));
	}

}
