package log.charter.services.editModes;

public enum EditMode {
	EMPTY, //
	GUITAR, //
	SHOWLIGHTS, //
	TEMPO_MAP, //
	VOCALS;

	public static final EditMode[] nonEmpty = { GUITAR, SHOWLIGHTS, TEMPO_MAP, VOCALS };
	public static final EditMode[] withItems = { GUITAR, SHOWLIGHTS, VOCALS };
}
