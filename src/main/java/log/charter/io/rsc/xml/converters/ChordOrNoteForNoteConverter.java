package log.charter.io.rsc.xml.converters;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.notes.ChordOrNote.ChordOrNoteForNote;

public class ChordOrNoteForNoteConverter implements Converter {
	private static final NoteConverter noteConverter = new NoteConverter();

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(final Class type) {
		return type.equals(ChordOrNoteForNote.class);
	}

	@Override
	public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
		noteConverter.marshal(((ChordOrNote) source).note(), writer, context);
	}

	@Override
	public ChordOrNoteForNote unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
		return ChordOrNote.from(noteConverter.unmarshal(reader, context));
	}

}
