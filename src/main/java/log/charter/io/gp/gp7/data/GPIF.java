package log.charter.io.gp.gp7.data;

import java.util.List;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import log.charter.io.gp.gp7.converters.GP7BarsConverter;
import log.charter.io.gp.gp7.converters.GP7BeatsConverter;
import log.charter.io.gp.gp7.converters.GP7NotesConverter;
import log.charter.io.gp.gp7.converters.GP7RhythmsConverter;
import log.charter.io.gp.gp7.converters.GP7VoicesConverter;

@XStreamAlias("GPIF")
@XStreamInclude({ GP7Beat.class, GP7MasterBar.class, GP7MasterTrack.class, GP7Rhythm.class, GP7Score.class,
		GP7Track.class })
public class GPIF {
	@XStreamAlias("GPVersion")
	public String gpVersion;
	@XStreamAlias("Score")
	public GP7Score score;
	@XStreamAlias("MasterTrack")
	public GP7MasterTrack masterTrack;
	@XStreamAlias("BackingTrack")
	public GP7BackingTrack backingTrack;
	@XStreamAlias("Tracks")
	public List<GP7Track> tracks;
	@XStreamAlias("MasterBars")
	public List<GP7MasterBar> masterBars;
	@XStreamAlias("Bars")
	@XStreamConverter(GP7BarsConverter.class)
	public Map<Integer, GP7Bar> bars;
	@XStreamAlias("Voices")
	@XStreamConverter(GP7VoicesConverter.class)
	public Map<Integer, GP7Voice> voices;
	@XStreamAlias("Beats")
	@XStreamConverter(GP7BeatsConverter.class)
	public Map<Integer, GP7Beat> beats;
	@XStreamAlias("Notes")
	@XStreamConverter(GP7NotesConverter.class)
	public Map<Integer, GP7Note> notes;
	@XStreamAlias("Rhythms")
	@XStreamConverter(GP7RhythmsConverter.class)
	public Map<Integer, GP7Rhythm> rhythms;
	@XStreamAlias("Assets")
	public List<GP7Asset> assets;

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder("GPIF {")//
				.append("\n gpVersion=").append(gpVersion)//
				.append("\n score=").append(score)//
				.append("\n masterTrack=").append(masterTrack)//
				.append("\n tracks=").append(tracks)//
				.append("\n masterBars=").append(masterBars)//
				.append("\n bars=").append(bars)//
				.append("\n voices=").append(voices)//
				.append("\n beats=").append(beats)//
				.append("\n notes=").append(notes)//
				.append("\n rhythms=").append(rhythms) //
				.append("\n}");

		return b.toString();
	}

	public boolean containsAudioTrackAsset() {
		return backingTrack != null && backingTrack.source != null && backingTrack.source.equalsIgnoreCase("local")
				&& backingTrack.assetId != null;
	}
}
