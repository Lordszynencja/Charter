package log.charter.io.rs.xml.vocals;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamInclude;

@XStreamAlias("vocals")
@XStreamInclude({ Vocal.class })
public class Vocals {

	@XStreamImplicit
	private List<Vocal> vocals = new ArrayList<>();

	@XStreamAsAttribute
	public int count = 0;

	public List<Vocal> getVocals() {
		return vocals;
	}

	public void setVocals(final List<Vocal> vocals) {
		count = vocals.size();
		this.vocals = vocals;
	}

}