package log.charter.data.song.vocals;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPositionWithEnd;
import log.charter.data.song.position.fractional.IFractionalPositionWithEnd;
import log.charter.io.rsc.xml.converters.VocalConverter;

@XStreamAlias("vocal")
@XStreamConverter(VocalConverter.class)
public class Vocal implements IFractionalPositionWithEnd {
	public enum VocalFlag {
		NONE, WORD_PART, PHRASE_END;
	}

	private FractionalPosition position;
	private FractionalPosition endPosition;
	private String text;
	private VocalFlag flag;
	public int note = 255;

	public Vocal(final FractionalPosition position, final FractionalPosition endPosition, final String text,
			final VocalFlag flag, final int note) {
		this.position = position;
		this.endPosition = endPosition;
		flag(flag);
		text(text);
		this.note = note;
	}

	public Vocal(final FractionalPosition position, final FractionalPosition endPosition, final String text,
			final VocalFlag flag) {
		this.position = position;
		this.endPosition = endPosition;
		flag(flag);
		text(text);
	}

	public Vocal(final Vocal other) {
		this(other.position, other.endPosition, other.text, other.flag, other.note);
	}

	public Vocal(final FractionalPosition position, final FractionalPosition endPosition) {
		this(position, endPosition, "", VocalFlag.NONE);
	}

	public Vocal(final String lyric, final VocalFlag flag) {
		this(new FractionalPosition(), new FractionalPosition(), lyric, flag);
	}

	public Vocal(final FractionalPosition position, final FractionalPosition endPosition, final String text,
			final int note) {
		this(position, endPosition, text, VocalFlag.NONE, note);
	}

	public Vocal() {
		this(new FractionalPosition(), new FractionalPosition(), "", VocalFlag.NONE);
	}

	public String text() {
		return text;
	}

	public void text(final String text) {
		if (flag != VocalFlag.NONE) {
			this.text = text;
			return;
		}

		if (text.endsWith("+")) {
			flag = VocalFlag.PHRASE_END;
			this.text = text.substring(0, text.length() - 1);
			return;
		}
		if (text.endsWith("-")) {
			flag = VocalFlag.WORD_PART;
			this.text = text.substring(0, text.length() - 1);
			return;
		}

		this.text = text;
	}

	public VocalFlag flag() {
		return flag;
	}

	public void flag(VocalFlag flag) {
		if (flag == null) {
			flag = VocalFlag.NONE;
		}

		this.flag = flag;
	}

	public String lyrics() {
		return text + switch (flag) {
			case PHRASE_END -> "+";
			case WORD_PART -> "-";
			default -> "";
		};
	}

	@Override
	public FractionalPosition position() {
		return position;
	}

	@Override
	public void position(final FractionalPosition newPosition) {
		if (newPosition == null) {
			throw new IllegalArgumentException("Can't set position to null");
		}

		position = newPosition;
	}

	@Override
	public FractionalPosition endPosition() {
		return endPosition;
	}

	@Override
	public void endPosition(final FractionalPosition newEndPosition) {
		if (newEndPosition == null) {
			throw new IllegalArgumentException("Can't set position to null");
		}

		endPosition = newEndPosition;
	}

	@Override
	public IConstantFractionalPositionWithEnd toFraction(final ImmutableBeatsMap beats) {
		return this;
	}
}
