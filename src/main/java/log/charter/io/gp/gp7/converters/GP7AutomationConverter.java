package log.charter.io.gp.gp7.converters;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import log.charter.io.gp.gp7.data.GP7Automation;
import log.charter.io.gp.gp7.data.GP7Automation.GP7AutomationValue;
import log.charter.io.gp.gp7.data.GP7Automation.GP7SyncPointValue;
import log.charter.io.gp.gp7.data.GP7Automation.GP7TempoValue;

public class GP7AutomationConverter implements Converter {
	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(final Class type) {
		return GP7Automation.class.isAssignableFrom(type);
	}

	@Override
	public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
	}

	private GP7TempoValue readTempoValue(final HierarchicalStreamReader reader) {
		final String[] tokens = reader.getValue().split(" ");
		return new GP7TempoValue(Double.valueOf(tokens[0]), Integer.valueOf(tokens[1]));
	}

	private GP7SyncPointValue readSyncPointValue(final HierarchicalStreamReader reader) {
		final GP7SyncPointValue value = new GP7SyncPointValue();
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			switch (reader.getNodeName()) {
				case "BarIndex" -> value.barIndex = Integer.valueOf(reader.getValue());
				case "BarOccurrence" -> value.barOccurence = Integer.valueOf(reader.getValue());
				case "ModifiedTempo" -> value.modifiedTempo = Double.valueOf(reader.getValue());
				case "OriginalTempo" -> value.originalTempo = Double.valueOf(reader.getValue());
				case "FrameOffset" -> value.frameOffset = Integer.valueOf(reader.getValue());
				default -> {}
			}
			reader.moveUp();
		}

		return value;
	}

	private GP7AutomationValue readValue(final HierarchicalStreamReader reader, final String type) {
		if (type == null) {
			return new GP7AutomationValue();
		}

		if (type.equals("Tempo")) {
			return readTempoValue(reader);
		} else if (type.equals("SyncPoint")) {
			return readSyncPointValue(reader);
		} else {
			return new GP7AutomationValue();
		}
	}

	@Override
	public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
		final GP7Automation automation = new GP7Automation();
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			switch (reader.getNodeName()) {
				case "Type" -> automation.type = reader.getValue();
				case "Linear" -> automation.linear = Boolean.valueOf(reader.getValue());
				case "Bar" -> automation.bar = Integer.valueOf(reader.getValue());
				case "Position" -> automation.position = Double.valueOf(reader.getValue());
				case "Visible" -> automation.visible = Boolean.valueOf(reader.getValue());
				case "Value" -> automation.value = readValue(reader, automation.type);
			}

			reader.moveUp();
		}

		return automation;
	}
}
