package log.charter.data.song;

import log.charter.data.config.Localization.Label;

public enum SectionType {
	NO_GUITAR("noguitar"), //
	INTRO("intro"), //
	PRE_VERSE("preverse"), //
	VERSE("verse"), //
	POST_VERSE("postvs"), //
	MODULATED_VERSE("modverse"), //
	PRE_CHORUS("prechorus"), //
	CHORUS("chorus"), //
	POST_CHORUS("postchorus"), //
	MODULATED_CHORUS("modchorus"), //
	HOOK("hook"), //
	PRE_BRIDGE("prebrdg"), //
	BRIDGE("bridge"), //
	POST_BRIDGE("postbrdg"), //
	SOLO("solo"), //
	OUTRO("outro"), //
	AMBIENT("ambient"), //
	BREAKDOWN("breakdown"), //
	INTERLUDE("interlude"), //
	TRANSITION("transition"), //
	RIFF("riff"), //
	FADE_IN("fadein"), //
	FADE_OUT("fadeout"), //
	BUILDUP("buildup"), //
	VARIATION("variation"), //
	HEAD("head"), //
	MODULATED_BRIDGE("modbridge"), //
	MELODY("melody"), //
	VAMP("vamp"), //
	SILENCE("silence"), //
	TAPPING("tapping");

	public final String rsName;
	public final Label label;

	private SectionType(final String rsName) {
		this.rsName = rsName;
		label = Label.valueOf("SECTION_" + name());
	}
}