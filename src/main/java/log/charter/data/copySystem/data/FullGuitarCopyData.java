package log.charter.data.copySystem.data;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.data.copySystem.data.positions.CopiedAnchorPosition;
import log.charter.data.copySystem.data.positions.CopiedArrangementEventsPointPosition;
import log.charter.data.copySystem.data.positions.CopiedHandShapePosition;
import log.charter.data.copySystem.data.positions.CopiedSoundPosition;
import log.charter.data.copySystem.data.positions.CopiedToneChangePosition;
import log.charter.song.ChordTemplate;
import log.charter.song.Phrase;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;

@XStreamAlias("fullGuitarCopyData")
public class FullGuitarCopyData implements FullCopyData {
	public final EventPointsCopyData beats;
	public final ToneChangesCopyData toneChanges;
	public final AnchorsCopyData anchors;
	public final SoundsCopyData sounds;
	public final HandShapesCopyData handShapes;

	public FullGuitarCopyData(final HashMap2<String, Phrase> phrases,
			final ArrayList2<CopiedArrangementEventsPointPosition> arrangementEventsPoints,
			final ArrayList2<ChordTemplate> chordTemplates, final ArrayList2<CopiedToneChangePosition> toneChanges,
			final ArrayList2<CopiedAnchorPosition> anchors, final ArrayList2<CopiedSoundPosition> sounds,
			final ArrayList2<CopiedHandShapePosition> handShapes) {
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
