package log.charter.io.gp.gp7.data;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.io.gp.gp5.data.GPDuration;
import log.charter.io.gp.gp7.converters.GP7RhythmConverter;

@XStreamAlias("Rhythm")
@XStreamConverter(GP7RhythmConverter.class)
public class GP7Rhythm {
	public GPDuration duration;
	public int dots = 0;
	public GP7Tuplet primaryTuplet = new GP7Tuplet();
	public GP7Tuplet secondaryTuplet = new GP7Tuplet();

	@Override
	public String toString() {
		return "GP7Rhythm {duration=" + duration //
				+ (dots > 0 ? ", dots=" + dots : "")//
				+ (primaryTuplet.numerator != 1 || primaryTuplet.denominator != 1 ? ", primaryTuplet=" + primaryTuplet
						: "")//
				+ (secondaryTuplet.numerator != 1 || secondaryTuplet.denominator != 1
						? ", secondaryTuplet=" + secondaryTuplet
						: "")//
				+ "}";
	}

}
