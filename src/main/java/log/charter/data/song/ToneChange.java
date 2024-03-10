package log.charter.data.song;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.data.song.position.Position;

@XStreamAlias("toneChange")
public class ToneChange extends Position {
	@XStreamAsAttribute
	public String toneName;

	public ToneChange() {
		super(0);
	}

	public ToneChange(final int position, final String toneName) {
		super(position);
		this.toneName = toneName;
	}

	public ToneChange(final ToneChange other) {
		super(other);
		toneName = other.toneName;
	}
}
