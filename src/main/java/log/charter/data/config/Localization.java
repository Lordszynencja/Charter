package log.charter.data.config;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import log.charter.gui.menuHandlers.CharterMenuBar;
import log.charter.util.RW;

public class Localization {
	public enum Label {
		ACCENT("Accent"), //
		ADD_BEATS_AMOUNT("Add this many beats:"), //
		ADD_BEATS_AT_THE_START("Add beats at the start, without moving the audio"), //
		ADD_BEATS_PANE("Add beats"), //
		ADD_DEFAULT_SILENCE("Add default silence based on bars"), //
		ADD_SILENCE("Add silence in the beginning"), //
		ANCHOR_PANE("Anchor"), //
		ANCHOR_WIDTH("Anchor width"), //
		ARPEGGIO("Arpeggio"), //
		ARRANGEMENT_MENU("Arrangement"), //
		ARRANGEMENT_MENU_TEMPO_MAP("Tempo map"), //
		ARRANGEMENT_MENU_VOCALS("Vocals"), //
		ARRANGEMENT_NEXT("Next arrangement"), //
		ARRANGEMENT_PREVIOUS("Previous arrangement"), //
		ARRANGEMENT_OPTIONS("Arrangement options"), //
		ARRANGEMENT_SUBTYPE_ALTERNATE("Alternate"), //
		ARRANGEMENT_SUBTYPE_BONUS("Bonus"), //
		ARRANGEMENT_SUBTYPE_MAIN("Main"), //
		ARRANGEMENT_TYPE_BASS("Bass"), //
		ARRANGEMENT_TYPE_COMBO("Combo"), //
		ARRANGEMENT_TYPE_LEAD("Lead"), //
		ARRANGEMENT_TYPE_RHYTHM("Rhythm"), //
		AUDIO_OUTPUT("Audio output"), //
		AUDIO_OUTPUT_L_ID("Left output channel id"), //
		AUDIO_OUTPUT_R_ID("Right output channel id"), //
		BACKUP_DELAY_S("Backup delay (s)"), //
		BASE_AUDIO_FORMAT("Base audio format"), //
		BOOKMARKS_MENU("Bookmarks"), //
		BUFFER_SIZE_MS("Audio buffer size to fill (ms)"), //
		BUTTON_CANCEL("Cancel"), //
		BUTTON_SAVE("Save"), //
		CHANGE_AUDIO("Change audio"), //
		CHOOSE_COLOR_FOR("Choose color for %s"), //
		CONFIG_AUDIO("Audio"), //
		CONFIG_DISPLAY("Display"), //
		CONFIG_GENERAL("General"), //
		CONFIG_PANE_TITLE("Config"), //
		COPY("Copy"), //
		COULDNT_CREATE_FOLDER_CHOOSE_DIFFERENT("Couldn't create folder with this name, please change the name"), //
		COULDNT_IMPORT_GP5("Couldn't properly import Guitar Pro 5 file"), //
		COULDNT_IMPORT_MIDI_TEMPO("Couldn't properly import tempo map from %s"), //
		CREATE_DEFAULT_STRETCHES_IN_BACKGROUND("Create stretched audio in the background when new song is made"), //
		CREATE_PROJECT_FROM_RS_XML("Create project from RS arrangement XML"), //
		DELETE("Delete"), //
		DELETE_ARRANGEMENT("Delete arrangement"), //
		DELETE_ARRANGEMENT_POPUP_MSG("Are you sure you want to delete arrangement %s?"), //
		DELETE_ARRANGEMENT_POPUP_TITLE("Delete arrangement?"), //
		DIRECTORY_DOESNT_EXIST("Directory doesn't exist"), //
		DISTANCE_TYPE_BEATS("1/x beat"), //
		DISTANCE_TYPE_MILISECONDS("ms"), //
		DISTANCE_TYPE_NOTES("1/x note"), //
		DUPLICATED_COUNT_PHRASE("Duplicated COUNT phrase"), //
		DUPLICATED_END_PHRASE("Duplicated END phrase"), //
		EDIT_MENU("Edit"), //
		EDITING("Editing"), //
		ERRORS_TAB_DESCRIPTION("Description"), //
		ERRORS_TAB_POSITION("Position"), //
		ERRORS_TAB_SEVERITY("Severity"), //
		EXIT("Exit"), //
		FILE_MENU("File"), //
		FPS("FPS"), //
		FRET("Fret"), //
		FRET_0("Fret 0"), //
		FRET_1("Fret 1"), //
		FRET_2("Fret 2"), //
		FRET_3("Fret 3"), //
		FRET_4("Fret 4"), //
		FRET_5("Fret 5"), //
		FRET_6("Fret 6"), //
		FRET_7("Fret 7"), //
		FRET_8("Fret 8"), //
		FRET_9("Fret 9"), //
		GUITAR_ARRANGEMENT("Guitar arrangement"), //
		INVERT_STRINGS("Invert strings"), //
		INVERT_STRINGS_IN_PREVIEW("Invert strings in preview"), //
		LEFT_HANDED("Left handed"), //
		MARK_BOOKMARK_0("Mark bookmark 0"), //
		MARK_BOOKMARK_1("Mark bookmark 1"), //
		MARK_BOOKMARK_2("Mark bookmark 2"), //
		MARK_BOOKMARK_3("Mark bookmark 3"), //
		MARK_BOOKMARK_4("Mark bookmark 4"), //
		MARK_BOOKMARK_5("Mark bookmark 5"), //
		MARK_BOOKMARK_6("Mark bookmark 6"), //
		MARK_BOOKMARK_7("Mark bookmark 7"), //
		MARK_BOOKMARK_8("Mark bookmark 8"), //
		MARK_BOOKMARK_9("Mark bookmark 9"), //
		MARKER_POSITION_PX("Marker position (px)"), //
		MAX_STRINGS("Max strings"), //
		MIDI_SOUND_DELAY("Midi sound delay (ms)"), //
		MINIMAL_NOTE_DISTANCE("Minimal note distance"), //
		MINIMAL_TAIL_LENGTH("Minimal note tail length"), //
		MOVE_TO_BOOKMARK_0("Move to bookmark 0"), //
		MOVE_TO_BOOKMARK_1("Move to bookmark 1"), //
		MOVE_TO_BOOKMARK_2("Move to bookmark 2"), //
		MOVE_TO_BOOKMARK_3("Move to bookmark 3"), //
		MOVE_TO_BOOKMARK_4("Move to bookmark 4"), //
		MOVE_TO_BOOKMARK_5("Move to bookmark 5"), //
		MOVE_TO_BOOKMARK_6("Move to bookmark 6"), //
		MOVE_TO_BOOKMARK_7("Move to bookmark 7"), //
		MOVE_TO_BOOKMARK_8("Move to bookmark 8"), //
		MOVE_TO_BOOKMARK_9("Move to bookmark 9"), //
		MUSIC_FOLDER("Music folder"), //
		NEW_PROJECT("New project"), //
		NO_PHRASES_IN_ARRANGEMENT("No phrases in arrangement"), //
		NO_SECTIONS_IN_ARRANGEMENT("No sections in arrangement"), //
		OPEN_PROJECT("Open project"), //
		PASTE("Paste"), //
		REDO("Redo"), //
		SECTION_WITHOUT_PHRASE("Section without phrase"), //
		SELECT_ALL_NOTES("Select all notes"), //
		SELECT_NOTES_BY_TAILS("Select notes by tails"), //
		SHOW_CHORD_IDS("Show chord ids"), //
		SHOW_GRID("Show grid"), //
		SHOW_TEMPO_INSTEAD_OF_BPM("Show tempo instead of BPM"), //
		SONG_OPTIONS("Song options"), //
		SONGS_FOLDER("Songs folder"), //
		SOUND_DELAY("Sound delay (ms)"), //
		SPECIAL_PASTE("Special paste"), //
		SPEED_DECREASE("Decrease speed"), //
		SPEED_DECREASE_FAST("Decrease speed fast"), //
		SPEED_DECREASE_PRECISE("Decrease speed precise"), //
		SPEED_INCREASE("Increase speed"), //
		SPEED_INCREASE_FAST("Increase speed fast"), //
		SPEED_INCREASE_PRECISE("Increase speed precise"), //
		SQUASH_LEVELS("Squash levels"), //
		SUPPORTED_MUSIC_FILE("Mp3, Ogg or Wav file"), //
		TAB_3D_PREVIEW("3D preview"), //
		TAB_CHORD_TEMPLATES_EDITOR("Chord templates"), //
		TAB_ERRORS("Errors"), //
		TAB_HELP("Help"), //
		TAB_QUICK_EDIT("Quick edit"), //
		TAB_TEXT("Text"), //
		THEME_BASIC("Basic"), //
		THEME_MODERN("Modern"), //
		THEME_SQUARE("Square"), //
		TOGGLE_ANCHOR("Toggle anchor"), //
		TOGGLE_CLAPS("Toggle claps"), //
		TOGGLE_METRONOME("Toggle metronome"), //
		TOGGLE_MIDI("Toggle midi notes"), //
		TOGGLE_WAVEFORM_GRAPH("Toggle waveform drawing"), //
		TOOLBAR_CLAPS("Claps"), //
		TOOLBAR_GRID_SIZE("Grid Size: 1/"), //
		TOOLBAR_METRONOME("Metronome"), //
		TOOLBAR_MIDI("MIDI"), //
		TOOLBAR_REPEATER("Repeater"), //
		TOOLBAR_RMS_INDICATOR("RMS"), //
		TOOLBAR_SFX_VOLUME("SFX"), //
		TOOLBAR_SLOWED_PLAYBACK_SPEED("Playback speed"), //
		TOOLBAR_VOLUME("Volume"), //
		TOOLBAR_WAVEFORM_GRAPH("Waveform"), //
		UNDO("Undo"), //
		UNSUPPORTED_MUSIC_FORMAT("Unsupported music format!"), //
		VOCAL_ARRANGEMENT("Vocal arrangement"), //
		WARNING("Warning"), //
		XML_IMPORT_TYPE("XML import type"), //
		XML_IMPORT_AS("Choose what to import the XML as"), //

		FILE_MENU_IMPORT("Import"), //
		FILE_MENU_IMPORT_RS_GUITAR("RS guitar arrangement XML"), //
		FILE_MENU_IMPORT_RS_VOCALS("RS vocals arrangement XML"), //
		FILE_MENU_IMPORT_GP("Guitar Pro file"), //
		FILE_MENU_IMPORT_MIDI_TEMPO("Tempo from Midi file"), //
		SAVE_PROJECT("Save project"), //
		SAVE_PROJECT_AS("Save project as..."), //
		FILE_MENU_OPTIONS("Options"), //
		SHORTCUT_CONFIG("Shortcut config"), //
		FILE_MENU_GRAPHIC_OPTIONS("Graphic options"), //
		FILE_MENU_COLOR_OPTIONS("Color options"), //
		FILE_MENU_TEXTURING_OPTIONS("Texturing options"), //

		GUITAR_BEAT_PANE("Guitar beat options"), //
		GUITAR_BEAT_PANE_SECTION_TYPE("Section"), //
		GUITAR_BEAT_PANE_PHRASE_NAME("Phrase name"), //
		GUITAR_BEAT_PANE_PHRASE_LEVEL("Level"), //
		GUITAR_BEAT_PANE_PHRASE_SOLO("Solo"), //
		GUITAR_BEAT_PANE_EVENT_ADD("Add event"), //
		GUITAR_BEAT_PANE_EVENT_REMOVE("Remove event"), //

		GUITAR_MENU("Guitar"), //
		MOVE_TO_START("Move to start"), //
		MOVE_TO_END("Move to end"), //
		MOVE_TO_FIRST_ITEM("Move to first item"), //
		MOVE_TO_LAST_ITEM("Move to last item"), //
		MOVE_STRING_UP("Move notes string up"), //
		MOVE_STRING_DOWN("Move notes string down"), //
		MOVE_STRING_UP_SIMPLE("Move notes string up keeping the frets"), //
		MOVE_STRING_DOWN_SIMPLE("Move notes string down keeping the frets"), //
		MOVE_FRET_UP("Move notes one fret up"), //
		MOVE_FRET_DOWN("Move notes one fret down"), //
		NOTE_FRET_OPERATIONS("Note fret operations"), //
		NOTE_STATUS_OPERATIONS("Note status operations"), //
		TOGGLE_MUTE("Toggle mutes"), //
		TOGGLE_MUTE_INDEPENDENTLY("Toggle mutes independently"), //
		TOGGLE_HOPO("Toggle HO/PO"), //
		TOGGLE_HOPO_INDEPENDENTLY("Toggle HO/PO independently"), //
		TOGGLE_HARMONIC("Toggle harmonic"), //
		TOGGLE_HARMONIC_INDEPENDENTLY("Toggle harmonic independently"), //
		TOGGLE_ACCENT("Toggle accent"), //
		TOGGLE_ACCENT_INDEPENDENTLY("Toggle accent independently"), //
		TOGGLE_VIBRATO("Toggle vibrato"), //
		TOGGLE_VIBRATO_INDEPENDENTLY("Toggle vibrato independently"), //
		TOGGLE_TREMOLO("Toggle tremolo"), //
		TOGGLE_TREMOLO_INDEPENDENTLY("Toggle tremolo independently"), //
		TOGGLE_LINK_NEXT("Toggle link next"), //
		TOGGLE_LINK_NEXT_INDEPENDENTLY("Toggle link next independently"), //
		MARK_HAND_SHAPE("Mark hand shape"), //
		GUITAR_MENU_EDIT_HAND_SHAPE("Edit hand shape"), //
		GUITAR_MENU_AUTOCREATE_FHP("Autocreate Fret Hand Positions"), //
		TOGGLE_PREVIEW_WINDOW("Windowed preview"), //
		TOGGLE_BORDERLESS_PREVIEW_WINDOW("Borderless windowed preview"), //

		GP5_IMPORT("GP5 Import options"), //
		GP5_IMPORT_BEAT_MAP_CHANGE("Import beat map"), //
		GP5_IMPORT_ARRANGEMENT_NAME("Arrangement %d, %s:"), //
		GP5_IMPORT_SKIP_ARRANGEMENT("Skip arrangement"), //
		GP5_IMPORT_TO_NEW_ARRANGEMENT("To new arrangement"), //
		GP5_IMPORT_TO_EXISTING_ARRANGEMENT("To arrangement %d, %s"), //

		TEMPO_BEAT_PANE("Tempo beat options"), //
		TEMPO_BEAT_PANE_BPM("BPM"), //
		TEMPO_BEAT_PANE_BEATS_IN_MEASURE("Beats in measure"), //
		TEMPO_BEAT_PANE_NOTE_DENOMINATOR("Note denominator"), //

		INFO_MENU("Info"), //
		INFO_MENU_LANGUAGE("Language"), //
		INFO_MENU_VERSION("Version"), //
		LICENSES("Licenses"), //
		LIBRARIES_USED("Libraries used:\n%s"), //
		INFO_MENU_DONATION("Donate for development"), //

		MUSIC_MENU("Music"), //
		TOGGLE_REPEAT_END("Set repeater end"), //
		TOGGLE_REPEAT_START("Set repeater start"), //
		TOGGLE_REPEATER("Toggle repeater"), //

		NOTES_MENU("Notes"), //
		NOTES_MENU_GRID_OPTIONS("Grid options"), //
		SNAP_ALL("Snap all items inbetween to grid"), //
		SNAP_SELECTED("Snap selected items to grid"), //
		DOUBLE_GRID("Double grid resolution"), //
		HALVE_GRID("Halve grid resolution"), //
		PREVIOUS_ITEM("Previous item"), //
		PREVIOUS_ITEM_WITH_SELECT("Previous item with select"), //
		PREVIOUS_GRID_POSITION("Previous grid position"), //
		PREVIOUS_BEAT("Previous beat"), //
		NEXT_ITEM("Next item"), //
		NEXT_ITEM_WITH_SELECT("Next item with select"), //
		NEXT_GRID_POSITION("Next grid position"), //
		NEXT_BEAT("Next beat"), //

		BEAT_GRID_TYPE("Beat"), //
		NOTE_GRID_TYPE("Note"), //

		SLIDE_PANE("Slide options"), //
		SLIDE_PANE_FRET("Slide to"), //
		SLIDE_PANE_UNPITCHED("Unpitched"), //

		SONG_OPTIONS_PANE("Song options"), //
		SONG_OPTIONS_TITLE("Title"), //
		SONG_OPTIONS_ARTIST_NAME("Artist name"), //
		SONG_OPTIONS_ARTIST_NAME_SORTING("Artist name (sorting)"), //
		SONG_OPTIONS_ALBUM("Album"), //
		SONG_OPTIONS_YEAR("Year"), //
		SONG_OPTIONS_CROWD_SPEED("Crowd speed"), //

		ADD_SILENCE_PANE("Add silence"), //
		ADD_SILENCE_SECONDS("Add this many seconds of silence:"), //
		ADD_SILENCE_TYPE_ADD("Add silence"), //
		ADD_SILENCE_TYPE_SET("Set silence"), //

		ADD_DEFAULT_SILENCE_PANE("Add silence"), //
		ADD_DEFAULT_SILENCE_BARS("Add this many bars of silence after 10s:"), //

		ARRANGEMENT_OPTIONS_PANE("Arrangement options"), //
		ARRANGEMENT_OPTIONS_TYPE("Arrangement type"), //
		ARRANGEMENT_OPTIONS_SUBTYPE("Arrangement subtype"), //
		ARRANGEMENT_OPTIONS_BASE_TONE("Base tone"), //
		ARRANGEMENT_OPTIONS_TUNING_TYPE("Tuning"), //
		ARRANGEMENT_OPTIONS_STRINGS("Strings"), //
		ARRANGEMENT_OPTIONS_CAPO("Capo"), //
		ARRANGEMENT_OPTIONS_MOVE_FRETS("Move frets on tuning change"), //

		VOCAL_PANE_CREATION("Vocal creation"), //
		VOCAL_PANE_EDIT("Vocal edit"), //
		VOCAL_PANE_LYRIC("Lyric"), //
		VOCAL_PANE_WORD_PART("Word part"), //
		VOCAL_PANE_PHRASE_END("Phrase end"), //

		VOCALS_MENU("Vocals"), //
		EDIT_VOCALS("Edit selected vocals"), //
		TOGGLE_WORD_PART("Toggle word part"), //
		TOGGLE_PHRASE_END("Toggle phrase end"), //

		HAND_SHAPE_PANE("Hand shape edit"), //

		CHORD_NAME("Chord name"), //
		CHORD_NAME_ADVICE("Chord name advice"), //
		SET_TEMPLATE_ON_CHORDS("Set template on chords"), //

		STRING("String"), //
		CHORD_TEMPLATE_FINGER("Finger"), //

		MUTE("Mute:"), //
		MUTE_FULL("Full"), //
		MUTE_PALM("Palm"), //
		MUTE_NONE("None"), //

		HOPO("HOPO:"), //
		HOPO_HAMMER_ON("HO"), //
		HOPO_PULL_OFF("PO"), //
		HOPO_TAP("Tap"), //
		HOPO_NONE("None"), //

		BASS_PICKING_TECHNIQUE("Bass picking:"), //
		BASS_PICKING_POP("Pop"), //
		BASS_PICKING_SLAP("Slap"), //
		BASS_PICKING_NONE("None"), //

		HARMONIC("Harmonic:"), //
		HARMONIC_NORMAL("Normal"), //
		HARMONIC_PINCH("Pinch"), //
		HARMONIC_NONE("None"), //

		LINK_NEXT("Link next"), //
		SPLIT_INTO_NOTES("Split"), //
		FORCE_NO_NOTES("No notes"), //
		IGNORE("Ignore"), //
		PASS_OTHER_NOTES("Crazy/Arpeggiato"), //
		VIBRATO("Vibrato"), //
		TREMOLO("Tremolo"), //

		SPECIAL_GUITAR_PASTE_PANE("Special paste"), //
		SPECIAL_GUITAR_PASTE_USE_BEATS("Use beats as time"), //
		SPECIAL_GUITAR_PASTE_SECTIONS("Paste sections"), //
		SPECIAL_GUITAR_PASTE_PHRASES("Paste phrases"), //
		SPECIAL_GUITAR_PASTE_EVENTS("Paste events"), //
		SPECIAL_GUITAR_PASTE_TONE_CHANGES("Paste tone changes"), //
		SPECIAL_GUITAR_PASTE_ANCHORS("Paste anchors"), //
		SPECIAL_GUITAR_PASTE_SOUNDS("Paste notes"), //
		SPECIAL_GUITAR_PASTE_HAND_SHAPES("Paste hand shapes"), //

		TONE_CHANGE_PANE("Tone change options"), //
		TONE_CHANGE_TONE_NAME("Tone name"), //

		CHART_PROJECT("Chart Project"), //
		CHOOSE_FOLDER_NAME("Choose folder name"), //
		COULDNT_LOAD_PROJECT("Couldn't load project, reason:\n"), //
		COULDNT_LOAD_ARRANGEMENT("Couldn't load arrangement, reason:\n"), //
		EXIT_POPUP("Exit"), //
		EXIT_MESSAGE("Are you sure you want to exit?"), //
		FAST_BACKWARD("fast backward"), //
		FAST_FORWARD("fast forward"), //
		FOLDER_EXISTS_CHOOSE_DIFFERENT("Given folder already exists, choose different name"), //
		GENERATING_SLOWED_SOUND("Playback speed added to queue"), //
		GP_FILE("GP file (.gp3, .gp4, .gp5)"), //
		GP5_IMPORT_TEMPO_MAP("GP5 import tempo map"), //
		MOVE_BACKWARD("Move backward"), //
		MOVE_FORWARD("Move forward"), //
		LOADING("Please wait, loading..."), //
		LOADING_ARRANGEMENTS("Loading arrangements"), //
		LOADING_DONE("Loading done"), //
		LOADING_MUSIC_FILE("Loading music file"), //
		LOADING_PROJECT_FILE("Loading project file"), //
		MIDI_FILE("Midi file (.mid)"), //
		MISSING_ARRANGEMENT_FILE("Missing arrangement file %s"), //
		MUSIC_FILE_COULDNT_BE_LOADED("Music file couldn't be loaded"), //
		MUSIC_DATA_NOT_FOUND(
				"Music file not found in song folder, something went wrong with copying or the file is invalid"), //
		MUSIC_FILE_NOT_FOUND_PICK_NEW("Music file not found in song folder, please choose new file"), //
		NO_PROJECT("No project"), //
		NOT_A_FOLDER("Given path is not a folder"), //
		OPERATION_CANCELLED("Operation cancelled"), //
		PLAY_AUDIO("Play audio"), //
		PROJECT_IS_NEWER_VERSION("Project is newer version than program handles"), //
		RS_ARRANGEMENT_FILE("RS arrangment file (XML)"), //
		SELECT_FOLDER("Select"), //
		SLOW_BACKWARD("Slow backward"), //
		SLOW_FORWARD("Slow forward"), //
		TONE_NAME_CANT_BE_EMPTY("Tone name can't be empty"), //
		TONE_NAME_PAST_LIMIT("There are already 4 tones, can't add another tone"), //
		UNSAVED_CHANGES_POPUP("Unsaved changes"), //
		UNSAVED_CHANGES_MESSAGE("You have unsaved changes. Do you want to save?"), //
		UNSUPPORTED_FILE_TYPE("This file type is not supported"), //
		USE_TEMPO_MAP_FROM_IMPORT("Do you want to use the tempo map from the imported project?"), //
		VALUE_CANT_BE_EMPTY("Value must not be empty"), //
		VALUE_MUST_BE_GE("Value must be greater or equal to %s"), //
		VALUE_MUST_BE_LE("Value must be lesser or equal to %s"), //
		VALUE_NUMBER_EXPECTED("Number expected"), //
		WRONG_FINGER_VALUE("Wrong finger name, must be one of (T, 1, 2, 3, 4)"), //
		WRONG_MUSIC_FILE("Wrong music file"), //

		SONG_FOLDER_SELECT("Create song folder"), //
		SONG_FOLDER_AUDIO_FILE_FOLDER("Use audio file folder (%s)"), //
		SONG_FOLDER_CREATE_NEW("Create new folder in %s"), //
		SHORTCUT_CONFIG_PANE("Shortcut config"), //
		TIME_MOVEMENT("Time movement"), //
		VOCAL_EDITING("Vocal editing"), //
		GUITAR_EDITING("Guitar editing"), //
		OTHER("Other"), //

		PAGE_TEXTURES("Textures"), //
		PAGE_THEME("Theme"), //
		GRAPHIC_CONFIG_THEME("Theme"), //
		GRAPHIC_CONFIG_EVENTS_CHANGE_HEIGHT("Events height"), //
		GRAPHIC_CONFIG_TONE_CHANGE_HEIGHT("Tone height"), //
		GRAPHIC_CONFIG_ANCHOR_INFO_HEIGHT("Anchor info height"), //
		GRAPHIC_CONFIG_NOTE_HEIGHT("Note height (px)"), //
		GRAPHIC_CONFIG_NOTE_WIDTH("Note width (px)"), //
		GRAPHIC_CONFIG_CHORD_HEIGHT("Chord height"), //
		GRAPHIC_CONFIG_HAND_SHAPES_HEIGHT("Hand shapes height"), //
		GRAPHIC_CONFIG_TIMING_HEIGHT("Timing height"), //
		GRAPHIC_CONFIG_PREVIEW_SCROLL_SPEED("Preview scroll speed"), //

		GRAPHIC_CONFIG_INLAY("Inlay"), //
		GRAPHIC_CONFIG_TEXTURE_PACK("Texture pack"), //

		GRAPHIC_CONFIG_CHART_MAP_PAGE("Chart map"), //
		GRAPHIC_CONFIG_CHART_MAP_HEIGHT_MULTIPLIER("Chart map size"), //

		GRAPHIC_CONFIG_PANE("Graphic config"), //

		// COLORS
		COLOR_BASE_1("Base 1"), //
		COLOR_BASE_2("Base 2"), //
		COLOR_BASE_BG_0("Base background 0"), //
		COLOR_BASE_BG_1("Base background 1"), //
		COLOR_BASE_BG_2("Base background 2"), //
		COLOR_BASE_BG_3("Base background 3"), //
		COLOR_BASE_BG_4("Base background 4"), //
		COLOR_BASE_BG_5("Base background 5"), //

		GRAPHIC_CONFIG_BASE_DARK_TEXT("Dark text"), //
		GRAPHIC_CONFIG_BASE_TEXT("Text"), //

		GRAPHIC_CONFIG_NOTE_BACKGROUND("Note background"), //
		GRAPHIC_CONFIG_NOTE_ADD_LINE("Note add line"), //
		GRAPHIC_CONFIG_LANE("Lane"), //
		GRAPHIC_CONFIG_MAIN_BEAT("Main beat"), //
		GRAPHIC_CONFIG_SECONDARY_BEAT("Secondary beat"), //
		GRAPHIC_CONFIG_GRID("Grid"), //
		GRAPHIC_CONFIG_MARKER("Marker"), //
		GRAPHIC_CONFIG_SECTION_NAME_BG("Section name background"), //
		GRAPHIC_CONFIG_PHRASE_NAME_BG("Phrase name background"), //
		GRAPHIC_CONFIG_EVENT_BG("Event background"), //
		GRAPHIC_CONFIG_HIGHLIGHT("Highlight"), //
		GRAPHIC_CONFIG_SELECT("Select"), //

		GRAPHIC_CONFIG_NOTE_FLAG_MARKER("Note flag marker"), //
		GRAPHIC_CONFIG_SLIDE_NORMAL_FRET_BG("Normal slide fret background"), //
		GRAPHIC_CONFIG_SLIDE_NORMAL_FRET_TEXT("Normal slide fret text"), //
		GRAPHIC_CONFIG_SLIDE_UNPITCHED_FRET_BG("Unpitched slide fret background"), //
		GRAPHIC_CONFIG_SLIDE_UNPITCHED_FRET_TEXT("Unpitched slide fret text"), //
		GRAPHIC_CONFIG_NOTE_FULL_MUTE("Full mute"), //
		GRAPHIC_CONFIG_HAMMER_ON("Hammer on"), //
		GRAPHIC_CONFIG_PULL_OFF("Pull off"), //
		GRAPHIC_CONFIG_TAP("Tap"), //
		GRAPHIC_CONFIG_HARMONIC("Harmonic"), //
		GRAPHIC_CONFIG_PINCH_HARMONIC("Pinch harmonic"), //

		GRAPHIC_CONFIG_NOTE_0("Note 1"), //
		GRAPHIC_CONFIG_NOTE_1("Note 2"), //
		GRAPHIC_CONFIG_NOTE_2("Note 3"), //
		GRAPHIC_CONFIG_NOTE_3("Note 4"), //
		GRAPHIC_CONFIG_NOTE_4("Note 5"), //
		GRAPHIC_CONFIG_NOTE_5("Note 6"), //
		GRAPHIC_CONFIG_NOTE_6("Note 7"), //
		GRAPHIC_CONFIG_NOTE_7("Note 8"), //
		GRAPHIC_CONFIG_NOTE_8("Note 9"), //

		GRAPHIC_CONFIG_ANCHOR("Anchor"), //
		GRAPHIC_CONFIG_HAND_SHAPE("Hand shape"), //
		GRAPHIC_CONFIG_HAND_SHAPE_ARPEGGIO("Arpeggio"), //
		GRAPHIC_CONFIG_TONE_CHANGE("Tone change"), //

		GRAPHIC_CONFIG_VOCAL_LINE_BACKGROUND("Vocal line background"), //
		GRAPHIC_CONFIG_VOCAL_LINE_TEXT("Vocal line text"), //
		GRAPHIC_CONFIG_VOCAL_TEXT("Vocal text"), //
		GRAPHIC_CONFIG_VOCAL_NOTE("Vocal note"), //
		GRAPHIC_CONFIG_VOCAL_NOTE_WORD_PART("Vocal note word part"), //
		GRAPHIC_CONFIG_VOCAL_SELECT("Vocal selection"), //

		GRAPHIC_CONFIG_PREVIEW_3D_FULL_MUTE("3D preview full mute"), //
		GRAPHIC_CONFIG_PREVIEW_3D_PALM_MUTE("3D preview palm mute"), //
		GRAPHIC_CONFIG_PREVIEW_3D_ANCHOR("3D preview anchor"), //
		GRAPHIC_CONFIG_PREVIEW_3D_FRET_LANE("3D preview fret lane"), //
		GRAPHIC_CONFIG_PREVIEW_3D_BEAT("3D preview beat"), //
		GRAPHIC_CONFIG_PREVIEW_3D_CHORD_BOX("Chord box"), //
		GRAPHIC_CONFIG_PREVIEW_3D_CHORD_FULL_MUTE("Full mute chord"), //
		GRAPHIC_CONFIG_PREVIEW_3D_CHORD_PALM_MUTE("Palm mute chord") //
		;

		private final String defaultLabel;

		private Label(final String defaultLabel) {
			this.defaultLabel = defaultLabel;
		}

		public String label() {
			return labels.getOrDefault(name(), defaultLabel);
		}

		public String format(final Object... args) {
			return label().formatted(args);
		}

		public BufferedImage exportAsImage(final Color color, final Font font) {
			final String label = label();

			BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
			Graphics2D graphics = (Graphics2D) img.getGraphics();
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			graphics.setFont(font);
			final Rectangle2D bounds = graphics.getFontMetrics().getStringBounds(label(), graphics);

			img = new BufferedImage((int) bounds.getWidth() + 1, (int) bounds.getHeight() + 1,
					BufferedImage.TYPE_INT_ARGB);
			graphics = (Graphics2D) img.getGraphics();
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			graphics.setFont(font);
			graphics.setColor(color);
			graphics.drawString(label, 0, (int) -bounds.getY());

			return img;
		}

		public void saveAsPng(final String dir, final Color color, final Font font) {
			try {
				ImageIO.write(exportAsImage(color, font), "png", new File(dir, name() + ".png"));
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static final String languagesFolder = new File(RW.getProgramDirectory(), "languages").getAbsolutePath();

	private static Map<String, String> labels;

	public static void init() {
		final StringBuilder localizationFileBuilder = new StringBuilder();
		for (final Label label : Label.values()) {
			localizationFileBuilder.append(label.name()).append('=').append(label.defaultLabel).append('\n');
		}
		RW.write(new File(languagesFolder, "English.txt"), localizationFileBuilder.toString());

		readLocalizationFile();
	}

	private static void readLocalizationFile() {
		try {
			labels = RW.readConfig(new File(languagesFolder, Config.language + ".txt"));
		} catch (final Exception e) {
			Config.language = "English";
			labels = new HashMap<>();
		}
	}

	public static void changeLanguage(final String newLanguage, final CharterMenuBar charterMenuBar) {
		Config.language = newLanguage;
		Config.markChanged();

		readLocalizationFile();
		charterMenuBar.refreshMenus();
	}
}
