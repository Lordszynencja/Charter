package log.charter.io.rsc.xml.converters;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.notes.ChordOrNote.ChordOrNoteForChord;

public class ChordOrNoteForChordConverter implements Converter {
	private static final ChordConverter chordConverter = new ChordConverter();

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(final Class type) {
		return type.equals(ChordOrNoteForChord.class);
	}

	@Override
	public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
		chordConverter.marshal(((ChordOrNote) source).chord(), writer, context);
	}

	@Override
	public ChordOrNoteForChord unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
		return ChordOrNote.from(chordConverter.unmarshal(reader, context));
	}
}
