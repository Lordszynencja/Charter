package log.charter.io.rs.xml.song;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("phrase")
public class Phrase {
	@XStreamAsAttribute
	public String name;
	@XStreamAsAttribute
	public int maxDifficulty;
}
