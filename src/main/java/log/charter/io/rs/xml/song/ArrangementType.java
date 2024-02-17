package log.charter.io.rs.xml.song;

import log.charter.data.config.Localization.Label;

public enum ArrangementType {
	Combo(6, Label.ARRANGEMENT_TYPE_COMBO), //
	Rhythm(6, Label.ARRANGEMENT_TYPE_RHYTHM), //
	Lead(6, Label.ARRANGEMENT_TYPE_LEAD), //
	Bass(4, Label.ARRANGEMENT_TYPE_BASS);

	public final int defaultStrings;
	public final Label label;

	private ArrangementType(final int strings, final Label label) {
		defaultStrings = strings;
		this.label = label;
	}

	@Override
	public String toString() {
		return name();
	}
}
