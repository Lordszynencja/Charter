package log.charter.io.rs.xml.converters;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import log.charter.io.rs.xml.vocals.ArrangementVocal;
import log.charter.io.rs.xml.vocals.ArrangementVocals;
import log.charter.util.CollectionUtils.ArrayList2;

public class VocalsConverter implements Converter {
	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(final Class type) {
		return type.equals(ArrangementVocals.class);
	}

	@Override
	public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
		final ArrangementVocals vocals = (ArrangementVocals) source;
		writer.addAttribute("count", "" + vocals.vocals.size());
		context.convertAnother(vocals.vocals);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
		final ArrayList2<ArrangementVocal> list = (ArrayList2<ArrangementVocal>) context.convertAnother(null, ArrayList2.class);
		return new ArrangementVocals(list);
	}

}