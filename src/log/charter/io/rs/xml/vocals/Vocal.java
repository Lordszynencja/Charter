package log.charter.io.rs.xml.vocals;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.data.Config;
import log.charter.io.rs.xml.converters.TimeConverter;

@XStreamAlias("vocal")
public class Vocal {
	public Vocal() {
	}

	public Vocal(final int time, final String text, final boolean wordPart, final boolean phraseEnd) {
		this.time = time;
		lyric = text;

		if (wordPart) {
			lyric += "-";
		}
		length = Config.minTailLength;
	}

	@XStreamAsAttribute
	@XStreamConverter(TimeConverter.class)
	public Integer time;
	@XStreamAsAttribute
	@XStreamConverter(TimeConverter.class)
	public Integer length;
	@XStreamAsAttribute
	public String lyric;

	public boolean isWordPart() {
		return lyric.endsWith("-");
	}

	public void setWordPart(final boolean wordPart) {
		if (wordPart != isWordPart()) {
			if (wordPart) {
				lyric += "-";
			} else {
				lyric = lyric.substring(0, lyric.length() - 1);
			}
		}
	}

	public boolean isPhraseEnd() {
		return lyric.endsWith("+");
	}

	public void setPhraseEnd(final boolean phraseEnd) {
		if (phraseEnd != isPhraseEnd()) {
			if (phraseEnd) {
				lyric += "+";
			} else {
				lyric = lyric.substring(0, lyric.length() - 1);
			}
		}
	}

	public String getText() {
		return lyric.replace("+", "");
	}
}
