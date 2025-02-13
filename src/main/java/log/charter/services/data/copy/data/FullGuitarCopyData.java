package log.charter.services.data.copy.data;

import java.util.List;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.data.song.ChordTemplate;
import log.charter.data.song.Phrase;
import log.charter.services.data.copy.data.positions.CopiedEventPoint;
import log.charter.services.data.copy.data.positions.CopiedFHP;
import log.charter.services.data.copy.data.positions.CopiedHandShape;
import log.charter.services.data.copy.data.positions.CopiedSound;
import log.charter.services.data.copy.data.positions.CopiedToneChange;

@XStreamAlias("fullGuitarCopyData")
public class FullGuitarCopyData implements FullCopyData {
	public final EventPointsCopyData beats;
	public final ToneChangesCopyData toneChanges;
	public final FHPsCopyData fhps;
	public final SoundsCopyData sounds;
	public final HandShapesCopyData handShapes;

	public FullGuitarCopyData(final Map<String, Phrase> phrases, final List<CopiedEventPoint> arrangementEventsPoints,
			final List<ChordTemplate> chordTemplates, final List<CopiedToneChange> toneChanges,
			final List<CopiedFHP> fhps, final List<CopiedSound> sounds, final List<CopiedHandShape> handShapes) {
		beats = new EventPointsCopyData(phrases, arrangementEventsPoints);
		this.toneChanges = new ToneChangesCopyData(toneChanges);
		this.fhps = new FHPsCopyData(fhps);
		this.sounds = new SoundsCopyData(chordTemplates, sounds);
		this.handShapes = new HandShapesCopyData(chordTemplates, handShapes);
	}

	@Override
	public boolean isEmpty() {
		return beats.isEmpty() && toneChanges.isEmpty() && fhps.isEmpty() && sounds.isEmpty() && handShapes.isEmpty();
	}

}
