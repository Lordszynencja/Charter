package log.charter.services.data.copy.data;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;

@XStreamAlias("copyData")
@XStreamInclude({ AnchorsCopyData.class, EventPointsCopyData.class, EmptyCopyData.class, FullGuitarCopyData.class,
		HandShapesCopyData.class, SoundsCopyData.class, VocalsCopyData.class })
public class CopyData {
	public final ICopyData selectedCopy;
	public final FullCopyData fullCopy;

	public CopyData(final ICopyData selectedCopy, final FullCopyData fullCopy) {
		this.selectedCopy = selectedCopy;
		this.fullCopy = fullCopy;
	}
}
