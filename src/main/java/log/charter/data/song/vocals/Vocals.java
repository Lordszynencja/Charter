package log.charter.data.song.vocals;

import static log.charter.util.CollectionUtils.map;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import log.charter.io.rs.xml.vocals.ArrangementVocals;

@XStreamAlias("vocals")
@XStreamInclude(Vocal.class)
public class Vocals {

	public List<Vocal> vocals = new ArrayList<>();

	public Vocals() {
	}

	public Vocals(final List<Vocal> vocals) {
		this.vocals = vocals;
	}

	public Vocals(final ArrangementVocals arrangementVocals) {
		vocals = map(arrangementVocals.vocals, Vocal::new);
	}

	public Vocals(final Vocals other) {
		vocals = map(other.vocals, Vocal::new);
	}

	public int insertNote(final int position, final String text, final boolean wordPart, final boolean phraseEnd) {
		final Vocal vocal = new Vocal(position, text, wordPart, phraseEnd);

		if (vocals.isEmpty() || vocals.get(vocals.size() - 1).position() < position) {
			vocals.add(vocal);
			return vocals.size() - 1;
		}

		for (int i = vocals.size() - 1; i >= 0; i--) {
			if (vocals.get(i).position() < position) {
				vocals.add(i + 1, vocal);
				return i + 1;
			}
		}

		vocals.add(0, vocal);
		return 0;
	}

	public void removeNote(final int id) {
		vocals.remove(id);
	}
}