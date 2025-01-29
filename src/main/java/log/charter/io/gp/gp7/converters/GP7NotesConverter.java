package log.charter.io.gp.gp7.converters;

import com.thoughtworks.xstream.converters.Converter;

public class GP7NotesConverter extends GP7ItemWithIdConverter {
	private final GP7NoteConverter gp7NoteConverter = new GP7NoteConverter();

	@Override
	protected Converter getItemConverter() {
		return gp7NoteConverter;
	}
}
