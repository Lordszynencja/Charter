package log.charter.services.data.copy.data;

import java.util.List;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.data.song.ChordTemplate;
import log.charter.data.song.Phrase;
import log.charter.services.data.copy.data.positions.CopiedAnchorPosition;
import log.charter.services.data.copy.data.positions.CopiedArrangementEventsPointPosition;
import log.charter.services.data.copy.data.positions.CopiedHandShapePosition;
import log.charter.services.data.copy.data.positions.CopiedSoundPosition;
import log.charter.services.data.copy.data.positions.CopiedToneChangePosition;

@XStreamAlias("fullGuitarCopyData")
public class FullGuitarCopyData implements FullCopyData {
	public final EventPointsCopyData beats;
	public final ToneChangesCopyData toneChanges;
	public final AnchorsCopyData anchors;
	public final SoundsCopyData sounds;
	public final HandShapesCopyData handShapes;

	public FullGuitarCopyData(final Map<String, Phrase> phrases,
			final List<CopiedArrangementEventsPointPosition> arrangementEventsPoints,
			final List<ChordTemplate> chordTemplates, final List<CopiedToneChangePosition> toneChanges,
			final List<CopiedAnchorPosition> anchors, final List<CopiedSoundPosition> sounds,
			final List<CopiedHandShapePosition> handShapes) {
		beats = new EventPointsCopyData(phrases, arrangementEventsPoints);
		this.toneChanges = new ToneChangesCopyData(toneChanges);
		this.anchors = new AnchorsCopyData(anchors);
		this.sounds = new SoundsCopyData(chordTemplates, sounds);
		this.handShapes = new HandShapesCopyData(chordTemplates, handShapes);
	}

	@Override
	public boolean isEmpty() {
		return beats.isEmpty() && toneChanges.isEmpty() && anchors.isEmpty() && sounds.isEmpty()
				&& handShapes.isEmpty();
	}

}
