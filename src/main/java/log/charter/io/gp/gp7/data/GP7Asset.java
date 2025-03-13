package log.charter.io.gp.gp7.data;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("Asset")
public class GP7Asset {
	@XStreamAsAttribute
	public int id;
	@XStreamAlias("EmbeddedFilePath")
	public String embeddedFilePath;
}
