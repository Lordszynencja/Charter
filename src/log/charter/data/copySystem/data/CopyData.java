package log.charter.data.copySystem.data;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;

@XStreamAlias("copyData")
@XStreamInclude({ AnchorsCopyData.class, BeatsCopyData.class, EmptyCopyData.class, FullGuitarCopyData.class,
		HandShapesCopyData.class, SoundsCopyData.class, VocalsCopyData.class })
public class CopyData {
	public final ICopyData selectedCopy;
	public final ICopyData fullCopy;

	public CopyData(final ICopyData selectedCopy, final ICopyData fullCopy) {
		this.selectedCopy = selectedCopy;
		this.fullCopy = fullCopy;
	}
}
