package log.charter.song.vocals;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import log.charter.io.rs.xml.vocals.ArrangementVocals;
import log.charter.util.CollectionUtils.ArrayList2;

@XStreamAlias("vocals")
@XStreamInclude(Vocal.class)
public class Vocals {

	public ArrayList2<Vocal> vocals = new ArrayList2<>();

	public Vocals() {
	}

	public Vocals(final ArrayList2<Vocal> vocals) {
		this.vocals = vocals;
	}

	public Vocals(final ArrangementVocals arrangementVocals) {
		vocals = arrangementVocals.vocals.map(Vocal::new);
	}

	public Vocals(final Vocals other) {
		vocals = other.vocals.map(Vocal::new);
	}

	public int insertNote(final int position, final String text, final boolean wordPart, final boolean phraseEnd) {
		final Vocal vocal = new Vocal(position, text, wordPart, phraseEnd);

		if (vocals.isEmpty() || vocals.getLast().position() < position) {
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