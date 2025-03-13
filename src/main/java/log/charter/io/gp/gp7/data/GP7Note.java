package log.charter.io.gp.gp7.data;

import org.jcodec.common.logging.Logger;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.io.gp.gp7.converters.GP7NoteConverter;

@XStreamAlias("Note")
@XStreamConverter(GP7NoteConverter.class)
public class GP7Note {
	public enum GP7HarmonicType {
		NATURAL, SEMI, PINCH, FEEDBACK;

		public static GP7HarmonicType valueOfWithCheck(final String s) {
			return switch (s) {
				case "Natural" -> NATURAL;
				case "Semi" -> SEMI;
				case "Pinch" -> PINCH;
				case "Feedback" -> FEEDBACK;
				default -> {
					Logger.error("Unknown GP7 harmonic type: " + s);
					yield SEMI;
				}
			};
		}
	}

	public enum SlideInType {
		NONE, //
		FROM_BELOW, // 4
		FROM_ABOVE// 5
		;
	}

	public enum SlideOutType {
		NONE, //
		TO_NEXT_NOTE, // 0
		TO_NEXT_NOTE_LINKED, // 1
		DOWN, // 2
		UP// 3
		;
	}

	public int string;
	public int fret;
	public int finger = -1;
	public int rightFinger = -1;
	public boolean hopoOrigin;
	public boolean hopoDestination;
	public boolean leftHandTapped;
	public boolean tapped;
	public boolean accent;
	public boolean mute;
	public boolean palmMute;
	public boolean popped;
	public boolean slapped;
	public boolean harmonic;
	public double harmonicFret;
	public GP7HarmonicType harmonicType;
	public boolean tieOrigin;
	public boolean tieDestination;
	public boolean vibrato;
	public SlideInType slideIn = SlideInType.NONE;
	public SlideOutType slideOut = SlideOutType.NONE;
	public boolean bend;
	public double bendOriginOffset;
	public double bendOriginValue;
	public double bendMiddleOffset1;
	public double bendMiddleOffset2;
	public double bendMiddleValue;
	public double bendDestinationOffset;
	public double bendDestinationValue;

	@Override
	public String toString() {
		return "GP7Note [string=" + string //
				+ ", fret=" + fret//
				+ ", finger=" + finger//
				+ ", rightFinger=" + rightFinger//
				+ ", hopoOrigin=" + hopoOrigin//
				+ ", hopoDestination=" + hopoDestination//
				+ ", leftHandTapped=" + leftHandTapped//
				+ ", tapped=" + tapped//
				+ ", mute=" + mute//
				+ ", palmMute=" + palmMute//
				+ ", popped=" + popped//
				+ ", slapped=" + slapped//
				+ ", harmonic=" + harmonic//
				+ ", harmonicFret=" + harmonicFret//
				+ ", harmonicType=" + harmonicType//
				+ ", tieOrigin=" + tieOrigin//
				+ ", tieDestination=" + tieDestination//
				+ ", vibrato=" + vibrato//
				+ ", slideIn=" + slideIn//
				+ ", slideOut=" + slideOut//
				+ ", bend=" + bend//
				+ ", bendOriginOffset=" + bendOriginOffset//
				+ ", bendOriginValue=" + bendOriginValue//
				+ ", bendMiddleOffset1=" + bendMiddleOffset1//
				+ ", bendMiddleOffset2=" + bendMiddleOffset2//
				+ ", bendMiddleValue=" + bendMiddleValue//
				+ ", bendDestinationOffset=" + bendDestinationOffset//
				+ ", bendDestinationValue=" + bendDestinationValue + "]";
	}

}
