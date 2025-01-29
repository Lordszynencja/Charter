package log.charter.io.gp.gp7.data;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("Automation")
public class GP7Automation {
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
	public String value;

	@Override
	public String toString() {
		return "GP7Automation [type=" + type + ", linear=" + linear + ", bar=" + bar + ", position=" + position
				+ ", visible=" + visible + ", value=" + value + "]";
	}
}
