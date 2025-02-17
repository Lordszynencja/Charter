package log.charter.data.song.vocals;

import static log.charter.util.CollectionUtils.firstAfter;
import static log.charter.util.CollectionUtils.map;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import log.charter.data.config.Localization.Label;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPositionWithEnd;
import log.charter.data.song.vocals.Vocal.VocalFlag;
import log.charter.io.rs.xml.vocals.ArrangementVocals;

@XStreamAlias("vocals")
@XStreamInclude(Vocal.class)
public class VocalPath {
	public String name = "";
	public Color color = new Color(160, 160, 160);
	public List<Vocal> vocals = new ArrayList<>();

	public VocalPath() {
	}

	public VocalPath(final List<Vocal> vocals) {
		this.vocals = vocals;
	}

	public VocalPath(final ImmutableBeatsMap beats, final ArrangementVocals arrangementVocals) {
		vocals = map(arrangementVocals.vocals, v -> {
			final FractionalPosition position = FractionalPosition.fromTime(beats, v.time);
			final FractionalPosition endPosition = v.length == null ? position
					: FractionalPosition.fromTime(beats, v.time + v.length);
			return new Vocal(position, endPosition, v.lyric, v.tone);
		});
	}

	public VocalPath(final VocalPath other) {
		vocals = map(other.vocals, Vocal::new);
	}

	public int insertVocal(final IConstantFractionalPositionWithEnd position, final String text, final VocalFlag flag) {
		final Vocal vocal = new Vocal(position.position(), position.endPosition(), text, flag);
		final Integer idAfter = firstAfter(vocals, vocal).findId();
		if (idAfter != null) {
			vocals.add(idAfter, vocal);
			return idAfter;
		}

		vocals.add(vocal);
		return vocals.size() - 1;
	}

	public void removeNote(final int id) {
		vocals.remove(id);
	}

	public String getName(final int id) {
		String label = Label.VOCAL_PATH.label();
		if (name != null && !name.isBlank()) {
			label += ": " + name;
		}
		return "[%d] %s".formatted(id + 1, label);
	}
}