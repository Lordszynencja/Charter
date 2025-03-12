package log.charter.io.gp.gp7.data;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.io.gp.gp7.converters.GP7AutomationConverter;

@XStreamConverter(GP7AutomationConverter.class)
@XStreamAlias("Automation")
public class GP7Automation {
	public static class GP7AutomationValue {
		public GP7TempoValue asTempoValue() {
			return null;
		}

		public GP7SyncPointValue asSyncPointValue() {
			return null;
		}
	}

	public static class GP7TempoValue extends GP7AutomationValue {
		public double tempo;
		public int noteType;

		public GP7TempoValue(final double tempo, final int noteType) {
			this.tempo = tempo;
			this.noteType = noteType;
		}

		@Override
		public GP7TempoValue asTempoValue() {
			return this;
		}
	}

	public static class GP7SyncPointValue extends GP7AutomationValue {
		public int barIndex;
		public int barOccurence;
		public double modifiedTempo;
		public double originalTempo;
		public int frameOffset;

		@Override
		public GP7SyncPointValue asSyncPointValue() {
			return this;
		}
	}

	@XStreamAlias("Type")
	public String type;
	@XStreamAlias("Linear")
	public boolean linear;
	@XStreamAlias("Bar")
	public int bar;
	@XStreamAlias("Position")
	public double position;
	@XStreamAlias("Visible")
	public boolean visible;
	@XStreamAlias("Value")
	public GP7AutomationValue value;

	@Override
	public String toString() {
		return "GP7Automation [type=" + type + ", linear=" + linear + ", bar=" + bar + ", position=" + position
				+ ", visible=" + visible + ", value=" + value + "]";
	}
}
