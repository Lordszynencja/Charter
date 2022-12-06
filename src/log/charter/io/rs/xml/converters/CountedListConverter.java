package log.charter.io.rs.xml.converters;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class CountedListConverter implements Converter {
	public static class CountedList<T> {
		public final List<T> list;

		public CountedList() {
			list = new ArrayList<>();
		}

		public CountedList(final List<T> list) {
			this.list = list;
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(final Class type) {
		return type.equals(CountedList.class);
	}

	@Override
	public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
		final CountedList<?> list = (CountedList<?>) source;
		writer.addAttribute("count", "" + list.list.size());
		context.convertAnother(list.list);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
		final List<Object> list = (List<Object>) context.convertAnother(null, List.class);
		return new CountedList<Object>(list);
	}

}
