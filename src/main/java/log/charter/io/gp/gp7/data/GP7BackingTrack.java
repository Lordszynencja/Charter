package log.charter.io.gp.gp7.data;

import com.thoughtworks.xstream.annotations.XStreamAlias;

public class GP7BackingTrack {
	@XStreamAlias("Source")
	public String source;
	@XStreamAlias("AssetId")
	public Integer assetId;
	@XStreamAlias("FramePadding")
	public Integer framePadding;
}
