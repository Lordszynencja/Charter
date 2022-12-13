package log.charter.song;

import static java.util.stream.Collectors.toCollection;

import java.util.List;

import log.charter.io.rs.xml.song.ArrangementSection;
import log.charter.util.CollectionUtils.ArrayList2;

public class Section extends Position {
	public enum SectionType {
		NO_GUITAR("noguitar", "No guitar"), //
		INTRO("intro", "Intro"), //
		PRE_VERSE("preverse", "Pre verse"), //
		VERSE("verse", "Verse"), //
		POST_VERSE("postvs", "Post verse"), //
		MODULATED_VERSE("modverse", "Modulated verse"), //
		PRE_CHORUS("prechorus", "Pre chorus"), //
		CHORUS("chorus", "Chorus"), //
		POST_CHORUS("postchorus", "Post chorus"), //
		MODULATED_CHORUS("modchorus", "Modulated chorus"), //
		HOOK("hook", "Hook"), //
		PRE_BRIDGE("prebrdg", "Pre bridge"), //
		BRIDGE("bridge", "Bridge"), //
		POST_BRIDGE("postbrdg", "Post bridge"), //
		SOLO("solo", "Solo"), //
		OUTRO("outro", "Outro"), //
		AMBIENT("ambient", "Ambient"), //
		BREAKDOWN("breakdown", "Breakdown"), //
		INTERLUDE("interlude", "Interlude"), //
		TRANSITION("transition", "Transition"), //
		RIFF("riff", "Riff"), //
		FADE_IN("fadein", "Fade in"), //
		FADE_OUT("fadeout", "Fade out"), //
		BUILDUP("buildup", "Buildup"), //
		VARIATION("variation", "Variation"), //
		HEAD("head", "Head"), //
		MODULATED_BRIDGE("modbridge", "Modulated bridge"), //
		MELODY("melody", "Melody"), //
		VAMP("vamp", "Vamp"), //
		SILENCE("silence", "Silence"), //
		TAPPING("tapping", "Tapping");

		public final String rsName;
		public final String label;

		private SectionType(final String rsName, final String label) {
			this.rsName = rsName;
			this.label = label;
		}

		public static SectionType findByRSName(final String rsName) {
			for (final SectionType sectionType : values()) {
				if (sectionType.rsName.equals(rsName)) {
					return sectionType;
				}
			}

			return null;
		}
	}

	public static ArrayList2<Section> fromArrangementSections(final List<ArrangementSection> arrangementSections) {
		return arrangementSections.stream()//
				.map(Section::new)//
				.collect(toCollection(ArrayList2::new));
	}

	public SectionType type;

	public Section(final int pos, final SectionType type) {
		super(pos);
		this.type = type;
	}

	private Section(final ArrangementSection arrangementSection) {
		super(arrangementSection.startTime);
		type = SectionType.findByRSName(arrangementSection.name);
	}

	public Section(final Section other) {
		super(other);
		type = other.type;
	}
}
