package log.charter.data.copySystem.data.positions;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.song.Beat;
import log.charter.song.Section;
import log.charter.song.SectionType;
import log.charter.util.CollectionUtils.ArrayList2;

@XStreamAlias("copiedSection")
public class CopiedSectionPosition extends CopiedOnBeatPosition<Section> {
	@XStreamAsAttribute
	public final SectionType sectionType;

	public CopiedSectionPosition(final ArrayList2<Beat> beats, final int baseBeat, final Section onBeat) {
		super(beats, baseBeat, onBeat);
		sectionType = onBeat.type;
	}

	@Override
	protected Section createValue() {
		return new Section(null, sectionType);
	}
}
