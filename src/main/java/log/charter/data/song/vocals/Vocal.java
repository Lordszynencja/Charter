package log.charter.data.song.vocals;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.IFractionalPositionWithEnd;
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

	public Vocal(final FractionalPosition position, final FractionalPosition endPosition, final String text,
			final VocalFlag flag) {
		this.position = position;
		this.endPosition = endPosition;
		flag(flag);
		text(text);
	}

	public Vocal(final Vocal other) {
		this(other.position, other.endPosition, other.text, other.flag);
	}

	public Vocal(final FractionalPosition position, final FractionalPosition endPosition) {
		this(position, endPosition, "", VocalFlag.NONE);
	}

	public Vocal(final String lyric) {
		this(new FractionalPosition(0), new FractionalPosition(0), lyric, VocalFlag.NONE);
	}

	public Vocal(final FractionalPosition position, final FractionalPosition endPosition, final String lyric) {
		this(position, endPosition, lyric, VocalFlag.NONE);
	}

	public Vocal() {
		this(new FractionalPosition(0), new FractionalPosition(0), "", VocalFlag.NONE);
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
	public FractionalPosition fractionalPosition() {
		return position;
	}

	@Override
	public void fractionalPosition(final FractionalPosition newPosition) {
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
}
