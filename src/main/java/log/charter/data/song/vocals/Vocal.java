package log.charter.data.song.vocals;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.data.config.Config;
import log.charter.data.song.notes.PositionWithLength;
import log.charter.io.rs.xml.vocals.ArrangementVocal;

@XStreamAlias("vocal")
public class Vocal extends PositionWithLength {
	@XStreamAsAttribute
	public String lyric;

	public Vocal(final int position) {
		super(position);
	}

	public Vocal(final String lyric) {
		super(0);
		this.lyric = lyric;
	}

	public Vocal(final ArrangementVocal arrangementVocal) {
		super(arrangementVocal.time, arrangementVocal.length == null ? 0 : arrangementVocal.length);
		lyric = arrangementVocal.lyric;
	}

	public Vocal(final int time, final String text, final boolean wordPart, final boolean phraseEnd) {
		super(time, Config.minTailLength);
		lyric = text;

		if (wordPart) {
			lyric += "-";
		} else if (phraseEnd) {
			lyric += "+";
		}
	}

	public Vocal(final Vocal other) {
		super(other);
		lyric = other.lyric;
	}

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
		String text = lyric.replace("+", "");
		if (text.endsWith("-")) {
			text = text.substring(0, text.length() - 1);
		}

		return text;
	}
}
