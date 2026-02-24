package log.charter.data.song;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IFractionalPosition;
import log.charter.io.rsc.xml.converters.ShowlightConverter;

@XStreamAlias("showlight")
@XStreamConverter(ShowlightConverter.class)
public class Showlight implements IFractionalPosition {
	public enum ShowlightType {
		FOG_GREEN(24, new Color(0, 255, 0)), //
		FOG_PINK(25, new Color(255, 64, 128)), //
		FOG_TEAL(26, new Color(0, 255, 255)), //
		FOG_ORANGE(27, new Color(255, 192, 0)), //
		FOG_BLUE(28, new Color(0, 0, 255)), //
		FOG_GREEN_YELLOW(29, new Color(192, 255, 0)), //
		FOG_MAGENTA(30, new Color(255, 0, 255)), //
		FOG_LIGHT_GREEN(31, new Color(0, 255, 128)), //
		FOG_RED(32, new Color(255, 0, 0)), //
		FOG_LIGHT_BLUE(33, new Color(128, 128, 255)), //
		FOG_YELLOW(34, new Color(255, 255, 0)), //
		FOG_PURPLE(35, new Color(255, 0, 128)), //

		BEAMS_OFF(42, new Color(0, 0, 0)), //
		BEAMS_GREEN(48, new Color(0, 255, 0)), //
		BEAMS_PINK(49, new Color(255, 64, 128)), //
		BEAMS_LIGHT_BLUE(50, new Color(128, 128, 255)), //
		BEAMS_ORANGE(51, new Color(255, 192, 0)), //
		BEAMS_BLUE(52, new Color(0, 0, 255)), //
		BEAMS_YELLOW(53, new Color(255, 255, 0)), //
		BEAMS_MAGENTA(54, new Color(255, 0, 255)), //
		BEAMS_TEAL(55, new Color(0, 255, 255)), //
		BEAMS_RED_PLUS(56, new Color(255, 0, 0)), //
		BEAMS_BLUE_PLUS(57, new Color(0, 0, 255)), //
		BEAMS_YELLOW_PLUS(58, new Color(255, 255, 0)), //
		BEAMS_PURPLE_PLUS(59, new Color(255, 0, 128)), //

		LASERS_ON(67, new Color(0, 255, 0)), //
		LASERS_OFF(66, new Color(0, 0, 0));

		public final int note;
		public final Color color;

		public final boolean isFog;
		public final boolean isBeam;

		private ShowlightType(final int note, final Color color) {
			this.note = note;
			this.color = color;

			isFog = note >= 24 && note <= 35;
			isBeam = note >= 42 && note <= 59;
		}

		public static ShowlightType fromNote(final int note) {
			for (final ShowlightType type : values()) {
				if (type.note == note) {
					return type;
				}
			}

			throw new IllegalArgumentException("Wrong value for showlights note: " + note);
		}
	}

	private FractionalPosition position;
	public List<ShowlightType> types = new ArrayList<>();

	public Showlight(final FractionalPosition position, final ShowlightType type) {
		this.position = position;
		types.add(type);
	}

	public Showlight(final Showlight other) {
		position = other.position;
		types = new ArrayList<>(other.types);
	}

	@Override
	public FractionalPosition position() {
		return position;
	}

	@Override
	public void position(final FractionalPosition newPosition) {
		position = newPosition;
	}
}
