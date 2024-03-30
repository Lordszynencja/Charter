package log.charter.io.rs.xml.song;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.data.song.position.time.IConstantPosition;
import log.charter.io.rs.xml.converters.TimeConverter;

@XStreamAlias("phraseIteration")
public class ArrangementPhraseIteration implements IConstantPosition {
	@XStreamAsAttribute
	@XStreamConverter(TimeConverter.class)
	public int time;
	@XStreamAsAttribute
	public int phraseId;

	public ArrangementPhraseIteration(final int time, final int phraseId) {
		this.time = time;
		this.phraseId = phraseId;
	}

	@Override
	public int position() {
		return time;
	}
}
