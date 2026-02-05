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
		ADD_AUDIO_STEM("Add audio stem"), //
		ADD_BEATS_AMOUNT("Add this many beats:"), //
		ADD_BEATS_AT_THE_START("Add beats at the start, without moving the audio"), //
		ADD_BEATS_PANE("Add beats"), //
		ADD_DEFAULT_START_SILENCE("Add default starting silence with empty bars"), //
		ADD_LEVEL("Add level"), //
		ADD_SILENCE_AT_THE_END("Add silence at the end"), //
		ADD_SILENCE_IN_THE_BEGINNING("Add silence in the beginning"), //
		APRIL_FOOLS_ENABLED("April fools"), //
		ARPEGGIO("Arpeggio"), //
		ARRANGEMENT_ID_NAME("Arrangement %d, %s:"), //
		ARRANGEMENT_IMPORT_OPTIONS("Arrangement import options"), //
		ARRANGEMENT_MENU("Arrangement"), //
		ARRANGEMENT_MENU_TEMPO_MAP("Tempo map"), //
		ARRANGEMENT_MENU_VOCALS("Vocal path"), //
		ARRANGEMENT_NEXT("Next arrangement"), //
		ARRANGEMENT_PREVIOUS("Previous arrangement"), //
		ARRANGEMENT_OPTIONS("Arrangement options"), //
		ARRANGEMENT_SKIP_ARRANGEMENT("Skip arrangement"), //
		ARRANGEMENT_SUBTYPE_ALTERNATE("Alternate"), //
		ARRANGEMENT_SUBTYPE_BONUS("Bonus"), //
		ARRANGEMENT_SUBTYPE_MAIN("Main"), //
		ARRANGEMENT_TO_EXISTING_ARRANGEMENT("To arrangement %d, %s"), //
		ARRANGEMENT_TO_NEW_ARRANGEMENT("To new arrangement"), //
		ARRANGEMENT_TYPE_BASS("Bass"), //
		ARRANGEMENT_TYPE_COMBO("Combo"), //
		ARRANGEMENT_TYPE_LEAD("Lead"), //
		ARRANGEMENT_TYPE_RHYTHM("Rhythm"), //
		AUDIO_OUTPUT("Audio output"), //
		AUDIO_OUTPUT_L_ID("Left output channel id"), //
		AUDIO_OUTPUT_R_ID("Right output channel id"), //
		AUDIO_STEM_NAME("Audio stem name:"), //
		AUDIO_STEM_NAME_CANT_BE_EMPTY("Audio stem name can't be empty"), //
		AUDIO_STEM_SETTINGS("Audio stem settings"), //
		AUDIO_STEMS("Audio stems"), //
		BACKUP_DELAY_S("Backup delay (s)"), //
		BAND_PASS("Band"), //
		BAND_PASS_SETTINGS("Band pass settings"), //
		BASE_AUDIO_FORMAT("Base audio format"), //
		BEAT_ADD("Add beat"), //
		BEAT_REMOVE("Remove beat"), //
		BOOKMARKS_MENU("Bookmarks"), //
		BPM_DOUBLE("Double BPM"), //
		BPM_HALVE("Halve BPM"), //
		BUFFER_SIZE_MS("Audio buffer size to fill (ms)"), //
		BUTTON_CANCEL("Cancel"), //
		BUTTON_SAVE("Save"), //
		CANT_DROP_WITHOUT_PROJECT("Can't import file without project open"), //
		CENTS("%s cents"), //
		CHANGE_AUDIO("Change audio"), //
		CHANGE_LENGTH("Change length"), //
		CHANGE_LENGTH_BY_SECONDS("Change length by this many seconds:"), //
		CHANGE_SONG_PITCH("Change song pitch"), //
		CHOOSE_COLOR_FOR("Choose color for %s"), //
		CHORD_WITH_NOTE_TAILS("Chord with note tails without any techniques"), //
		CONFIG("Config"), //
		CONFIG_AUDIO("Audio"), //
		CONFIG_DISPLAY("Display"), //
		CONFIG_GENERAL("General"), //
		CONFIG_INSTRUMENT("Instrument"), //
		CONFIG_SECRETS("Secrets"), //
		CONFIGS_AND_LOGS("Open configs and logs folder"), //
		CONFIGS_AND_LOGS_MESSAGE("Configs and logs folder is %s"), //
		COPY("Copy"), //
		COPY_ALL_FILES("Copy all files?"), //
		COPY_ALL_FILES_MESSAGE("Copy all files? If not, only project and main audio will be moved."), //
		COPY_AUDIO("Copy audio?"), //
		COPY_AUDIO_TO_PROJECT_FOLDER("Do you want to copy the audio file to the project folder?"), //
		COULDNT_CREATE_FOLDER_CHOOSE_DIFFERENT("Couldn't create folder with this name, please change the name"), //
		COULDNT_IMPORT_GP5("Couldn't properly import Guitar Pro 3/4/5 file"), //
		COULDNT_IMPORT_GP7("Couldn't properly import Guitar Pro 7/8 file"), //
		COULDNT_IMPORT_MIDI_TEMPO("Couldn't properly import tempo map from %s"), //
		COULDNT_LOAD_AUDIO("Couldn't load audio from %s"), //
		COULDNT_READ_TXT("Couldn't import file %s, unrecognized format"), //
		COULDNT_RUN_UPDATE_SCRIPT(
				"Couldn't run update script, run the program as administrator, install update manually or install it in a different folder"), //
		CREATE_DEFAULT_STRETCHES_IN_BACKGROUND("Create stretched audio in the background when new song is made"), //
		DELETE("Delete"), //
		DELETE_ARRANGEMENT("Delete arrangement"), //
		DELETE_ARRANGEMENT_POPUP_MSG("Are you sure you want to delete arrangement %s?"), //
		DELETE_ARRANGEMENT_POPUP_TITLE("Delete arrangement?"), //
		DELETE_VOCAL_PATH("Delete vocal path"), //
		DELETE_VOCAL_PATH_POPUP_MSG("Are you sure you want to delete vocal path %s?"), //
		DELETE_VOCAL_PATH_POPUP_TITLE("Delete vocal path?"), //
		DIRECTORY_DOESNT_EXIST("Directory doesn't exist"), //
		DISTANCE_TYPE_BEATS("1/x beat"), //
		DISTANCE_TYPE_MILISECONDS("ms"), //
		DISTANCE_TYPE_NOTES("1/x note"), //
		DUPLICATED_COUNT_PHRASE("Duplicated COUNT phrase"), //
		DUPLICATED_END_PHRASE("Duplicated END phrase"), //
		EDIT_MENU("Edit"), //
		EDITING("Editing"), //
		ERROR("Error: %s"), //
		ERRORS_TAB_DESCRIPTION("Description"), //
		ERRORS_TAB_POSITION("Position"), //
		ERRORS_TAB_SEVERITY("Severity"), //
		EXIT("Exit"), //
		EXPLOSIONS("Explosions"), //
		EXPLOSIONS_SHAKY_CAM("Shaky cam"), //
		FHP_PANE("FHP"), //
		FHP_STARTS_ON_WRONG_FRET("Fret Hand Position starts on fret below or equal to capo/0"), //
		FHP_WIDTH("Width"), //
		FILE_MENU("File"), //
		FINGER_NOT_SET_FOR_FRETTED_STRING("Finger not set for fretted string in template [%d] - string %d"), //
		FINGER_SET_FOR_OPEN_STRING("Finger set for open string in template [%d] - string %d"), //
		FIRST_BEAT_BEFORE_10_SECONDS("First beat is set before 10 seconds have passed"), //
		FIRST_FINGER_ON_NOT_LOWEST_FRET(
				"First finger set on fret that's not lowest non-open fret in template [%d] - string %d"), //
		FORCE_ARPEGGIO_IN_RS("in RS"), //
		FPS("FPS"), //
		FRET("Fret"), //
		NAME_CANT_BE_EMPTY("Name can't be empty"), //
		NUMBER_0("Number 0"), //
		NUMBER_1("Number 1"), //
		NUMBER_2("Number 2"), //
		NUMBER_3("Number 3"), //
		NUMBER_4("Number 4"), //
		NUMBER_5("Number 5"), //
		NUMBER_6("Number 6"), //
		NUMBER_7("Number 7"), //
		NUMBER_8("Number 8"), //
		NUMBER_9("Number 9"), //
		FINGER_DIFFERENT_THAN_IN_ARPEGGIO_HANDSHAPE(
				"Finger in sound is different than finger in hand shape under it on the same string"), //
		FRET_DIFFERENT_THAN_IN_ARPEGGIO_HANDSHAPE(
				"Fret in sound is different than fret in hand shape under it on the same string"), //
		FRETS("Frets"), //
		GO_PLAY_ALONG("GoPlayAlong file"), //
		GUITAR_ARRANGEMENT("Guitar arrangement"), //
		GP_FILES_FOLDER("GP files folder"), //
		HALVING_BPM_MUST_START_ON_MEASURE_BEGINNING("You can only halve BPM from the beginning of a measure"), //
		HALVING_BPM_UNEVEN_BEATS_IN_MEASURE("Uneven beats count, can't halve BPM"), //
		HAMMER_ON_ON_FRET_ZERO("Hammer on on fret zero"), //
		HIGH_PASS("High"), //
		HIGH_PASS_SETTINGS("High pass settings"), //
		IMPORTING_AUDIO("Importing audio"), //
		IMPORT_AUDIO_AS_STEM("Import audio as stem?"), //
		IMPORT_LRC_VOCALS("Import LRC vocals file"), //
		IMPORT_USC_VOCALS("Import USC vocals file"), //
		INVERT_STRINGS("Invert strings"), //
		INVERT_STRINGS_IN_PREVIEW("Invert strings in preview"), //
		LEFT_HANDED("Left handed"), //
		LEVEL("Level"), //
		LINKED_NOTE_HAS_NO_LENGTH("Note that's linked to has no length"), //
		LOW_PASS("Low"), //
		LOW_PASS_SETTINGS("Low pass settings"), //
		LRC_FILE("LRC file"), //
		LRC_IMPORTED_SUCCESSFULLY("LRC vocals imported successfully"), //
		MAIN_AUDIO("Main audio"), //
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
		MAX_BEND_VALUE("Max bend value"), //
		MAX_STRINGS("Max strings"), //
		MEASURE_ADD("Add measure"), //
		MEASURE_REMOVE("Remove measure"), //
		MIDI_SOUND_DELAY("Midi sound delay (ms)"), //
		MINIMAL_NOTE_LENGTH("Minimal note length"), //
		MINIMAL_NOTE_SPACE("Minimal space between notes"), //
		MOVE_FRET_DOWN("Move notes one fret down"), //
		MOVE_FRET_DOWN_OCTAVE("Move notes twelve frets down"), //
		MOVE_FRET_UP("Move notes one fret up"), //
		MOVE_FRET_UP_OCTAVE("Move notes twelve frets up"), //
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
		NEW_ARRANGEMENT("New arrangement..."), //
		NEW_PROJECT("New project"), //
		NEW_PROJECT_EMPTY("Empty project"), //
		NEW_PROJECT_RS_XML("Project from RS XML"), //
		NEW_PROJECT_GP7("Project from GP7+ file"), //
		NEW_VERSION("New version"), //
		NEW_VERSION_AVAILABLE_DOWNLOAD("New version %s is available, you are on %s, open the download page?"), //
		NEW_VERSION_AVAILABLE_UPDATE("New version %s is available, you are on %s, download the update?"), //
		NEW_VOCAL_PATH("New vocal path..."), //
		NO_PHRASES_IN_ARRANGEMENT("No phrases in arrangement"), //
		NO_COUNT_PHRASE_IN_ARRANGEMENT("No COUNT phrase in arrangement"), //
		NO_END_PHRASE_IN_ARRANGEMENT("No END phrase in arrangement"), //
		NO_SECTIONS_IN_ARRANGEMENT("No sections in arrangement"), //
		NOTE_FRET_BELOW_CAPO("Note fret is below capo, for open string it should be equal"), //
		NOTE_IN_WRONG_FHP("Note in wrong FHP"), //
		NOTE_SLIDE_FROM_OPEN_STRING("Slide starts on open string"), //
		NOTE_SLIDE_NOT_LINKED("Pitched note slide is not linked to next note"), //
		NOTE_SLIDES_INTO_CHORD("Note slide ends on a chord, should end on note"), //
		NOTE_SLIDES_INTO_WRONG_FRET("Note slide ends on a different fret than next note on string %d"), //
		NOTE_SLIDE_ENDS_ON_DIFFERENT_FINGER("Note slide ends on different finger on string %d"), //
		NOTE_WITHOUT_FHP("Note without FHP"), //
		OFFSET_MS_FIELD("Offset (ms):"), //
		OPEN_PROJECT("Open project"), //
		PASS_FILTER_ALGORITHM("Algorithm"), //
		PASS_FILTER_CENTER_FREQUENCY("Center frequency"), //
		PASS_FILTER_FREQUENCY("Frequency"), //
		PASS_FILTER_FREQUENCY_WIDTH("Frequency width"), //
		PASS_FILTER_ORDER("Order"), //
		PASS_FILTER_RIPPLE_DB("Ripple dB"), //
		PASTE("Paste"), //
		PHRASE_NAME("Phrase name"), //
		PICKED_BASS("Picked"), //
		PITCH_FROM("Change pitch from"), //
		PITCH_SHIFTING_AUDIO("Pitch shifting audio: %s"), //
		PITCH_TO("to"), //
		PITCH_MUST_BE_POSITIVE("Pitch must be positive"), //
		PULL_OFF_ON_HIGHER_EQUAL_FRET("Pull off on a higher or equal fret"), //
		PULL_OFF_WITHOUT_NOTE_BEFORE("Pull off without note before"), //
		REDO("Redo"), //
		SAVE_AS("Save as..."), //
		SECTION_AMBIENT("Ambient"), //
		SECTION_BREAKDOWN("Breakdown"), //
		SECTION_BRIDGE("Bridge"), //
		SECTION_BUILDUP("Buildup"), //
		SECTION_CHORUS("Chorus"), //
		SECTION_FADE_IN("Fade in"), //
		SECTION_FADE_OUT("Fade out"), //
		SECTION_HEAD("Head"), //
		SECTION_HOOK("Hook"), //
		SECTION_INTERLUDE("Interlude"), //
		SECTION_INTRO("Intro"), //
		SECTION_MELODY("Melody"), //
		SECTION_MODULATED_BRIDGE("Modulated bridge"), //
		SECTION_MODULATED_CHORUS("Modulated chorus"), //
		SECTION_MODULATED_VERSE("Modulated verse"), //
		SECTION_NO_GUITAR("No guitar"), //
		SECTION_OUTRO("Outro"), //
		SECTION_POST_BRIDGE("Post bridge"), //
		SECTION_POST_CHORUS("Post chorus"), //
		SECTION_POST_VERSE("Post verse"), //
		SECTION_PRE_BRIDGE("Pre bridge"), //
		SECTION_PRE_CHORUS("Pre chorus"), //
		SECTION_PRE_VERSE("Pre verse"), //
		SECTION_RIFF("Riff"), //
		SECTION_SILENCE("Silence"), //
		SECTION_SOLO("Solo"), //
		SECTION_TAPPING("Tapping"), //
		SECTION_TRANSITION("Transition"), //
		SECTION_TYPE("Section"), //
		SECTION_VAMP("Vamp"), //
		SECTION_VARIATION("Variation"), //
		SECTION_VERSE("Verse"), //
		SECTION_WITHOUT_PHRASE("Section without phrase"), //
		SELECT_ALL("Select all"), //
		SELECT_NOTES_BY_TAILS("Select notes by tails"), //
		SET_LENGTH("Set length"), //
		SET_LENGTH_TO_SECONDS("Set length to be this many seconds:"), //
		SHORTCUTS_SET_CHARTER_DEFAULT("Charter default"), //
		SHORTCUTS_SET_CURRENT("Current"), //
		SHORTCUTS_SET_EOF_DEFAULT("EoF default"), //
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
		STARTING_TONE("Starting tone"), //
		STUDYING_AUDIO("Studying audio: %s"), //
		SUPPORTED_MUSIC_FILE("Flac, Mp3, Ogg or Wav file"), //
		SWITCH_TS_TYPING_PART("Switch TS typing part"), //
		TAB_3D_PREVIEW("3D preview"), //
		TAB_CHORD_TEMPLATES_EDITOR("Chord templates"), //
		TAB_ERRORS("Errors"), //
		TAB_HELP("Help"), //
		TAB_QUICK_EDIT("Quick edit"), //
		TAB_TEXT("Text"), //
		TAP_ON_FRET_ZERO("Tap on fret zero"), //
		THEME_BASIC("Basic"), //
		THEME_MODERN("Modern"), //
		THEME_SQUARE("Square"), //
		TOGGLE_ANCHOR("Toggle anchor"), //
		TOGGLE_CLAPS("Toggle claps"), //
		TOGGLE_BAND_PASS_FILTER("Toggle band pass filter"), //
		TOGGLE_HIGH_PASS_FILTER("Toggle high pass filter"), //
		TOGGLE_LOW_PASS_FILTER("Toggle low pass filter"), //
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
		TRANSFORMING_WAV_TO_OGG("Transforming WAV to OGG<br>Time elapsed: %s"), //
		TUNING_PITCH("Tuning pitch"), //
		TXT_FILE("Text file"), //
		UNDO("Undo"), //
		UNPITCHED_NOTE_SLIDE_LINKED("Unpitched note slide is linked to next note"), //
		UNSUPPORTED_MUSIC_FORMAT("Unsupported music format!"), //
		USC_IMPORTED_SUCCESSFULLY("USC vocals imported successfully"), //
		VOCAL_ARRANGEMENT("Vocal arrangement"), //
		VOCAL_PATH("Vocal path"), //
		VOCAL_PATH_COLOR("Color"), //
		VOCAL_PATH_NAME("Name"), //
		VOCAL_PATH_OPTIONS("Vocal path options"), //
		VOCALS_EXIST("Vocals exist"), //
		VOCALS_EXIST_REPLACE_QUESTION("Vocals exist, replace them with new vocals from import?"), //
		WARNING("Warning"), //
		WRITING_FLAC_FILE("Writing FLAC file<br>Time elapsed: %s"), //
		WRITING_WAV_FILE("Writing WAV file<br>Time elapsed: %s"), //
		FIRST_FINGER_NOT_ON_FIRST_FHP_FRET("First finger is not on first fret in FHP"), //
		XML_IMPORT_TYPE("XML import type"), //
		XML_IMPORT_AS("Choose what to import the XML as"), //

		FILE_MENU_IMPORT("Import"), //
		FILE_MENU_IMPORT_RS_GUITAR("RS guitar arrangement XML"), //
		FILE_MENU_IMPORT_RS_VOCALS("RS vocals arrangement XML"), //
		FILE_MENU_IMPORT_GP("Guitar Pro (3-5, 7-8) file"), //
		FILE_MENU_IMPORT_MIDI_TEMPO("Tempo from Midi file"), //
		SAVE_PROJECT("Save project"), //
		SAVE_PROJECT_AS("Save project as..."), //
		SHORTCUT_CONFIG("Shortcut config"), //
		FILE_MENU_GRAPHIC_OPTIONS("Graphic options"), //
		FILE_MENU_COLOR_OPTIONS("Color options"), //
		FILE_MENU_TEXTURING_OPTIONS("Texturing options"), //

		GUITAR_BEAT_PANE("Guitar beat options"), //
		GUITAR_BEAT_PANE_PHRASE_SOLO("Solo"), //
		EVENT_ADD("Add event"), //
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
		PASS_OTHER_NOTES("Pass other notes"), //
		VIBRATO("Vibrato"), //
		TREMOLO("Tremolo"), //

		SPECIAL_GUITAR_PASTE_PANE("Special paste"), //
		SPECIAL_GUITAR_PASTE_USE_BEATS("Use beats as time"), //
		SPECIAL_GUITAR_PASTE_SECTIONS("Paste sections"), //
		SPECIAL_GUITAR_PASTE_PHRASES("Paste phrases"), //
		SPECIAL_GUITAR_PASTE_EVENTS("Paste events"), //
		SPECIAL_GUITAR_PASTE_TONE_CHANGES("Paste tone changes"), //
		SPECIAL_GUITAR_PASTE_FHPS("Paste FHPs"), //
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
		GP_FILE("GP file (.gp3, .gp4, .gp5, .gp)"), //
		GP7_FILE("GP7+ file (.gp)"), //
		GP_IMPORT_TEMPO_MAP("GP import tempo map"), //
		MOVE_BACKWARD("Move backward"), //
		MOVE_FORWARD("Move forward"), //
		LOADING("Please wait, loading..."), //
		LOADING_ARRANGEMENTS("Loading arrangements"), //
		LOADING_DONE("Loading done"), //
		LOADING_MUSIC_FILE("Loading music file"), //
		LOADING_PROJECT_FILE("Loading project file"), //
		LOADING_STEM("Loading stem"), //
		MIDI_FILE("Midi file (.mid)"), //
		MISSING_ARRANGEMENT_FILE("Missing arrangement file %s"), //
		MUSIC_FILE_COULDNT_BE_LOADED("Music file couldn't be loaded"), //
		MUSIC_DATA_NOT_FOUND(
				"Music file not found in song folder, something went wrong with copying or the file is invalid"), //
		MUSIC_FILE_NOT_FOUND_PICK_NEW("Music file not found in song folder, please choose new file"), //
		NO_PROJECT("No project"), //
		NOT_A_FOLDER("Given path is not a folder"), //
		OPERATION_CANCELLED("Operation cancelled"), //
		PLACE_LYRIC_FROM_TEXT("Place lyric from text"), //
		PLAY_AUDIO("Play audio"), //
		PROJECT_IS_NEWER_VERSION("Project is newer version than program handles"), //
		RS_ARRANGEMENT_FILE("RS arrangment file (XML)"), //
		SAVING_AUDIO("Saving audio"), //
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
		WRONG_MUSIC_FILE("Wrong or missing music file %s, please select new audio file"), //

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
		GRAPHIC_CONFIG_FHP_INFO_HEIGHT("FHP info height"), //
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

		GRAPHIC_CONFIG_FHP("FHP"), //
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
		GRAPHIC_CONFIG_PREVIEW_3D_FHP("3D preview FHP"), //
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
			return args.length == 0 ? label() : label().formatted(args);
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

	public static final String languagesFolder = new File(RW.getJarDirectory(), "languages").getAbsolutePath();

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
			labels = RW.readConfig(new File(languagesFolder, Config.language + ".txt"), true);
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
