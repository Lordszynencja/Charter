package log.charter.data.config.values;

import static log.charter.data.config.values.ValueAccessor.forDouble;
import static log.charter.data.config.values.ValueAccessor.forInteger;
import static log.charter.data.config.values.ValueAccessor.forString;

import java.util.Map;

import log.charter.sound.system.AudioSystemType;

public class AudioConfig {
	public static AudioSystemType outSystem = AudioSystemType.DEFAULT;
	public static String outSystemName = null;
	public static int leftOutChannelId = 0;
	public static int rightOutChannelId = 1;

	public static AudioSystemType in0System = AudioSystemType.DEFAULT;
	public static String in0SystemName = null;
	public static int inChannel0Id = 0;

	public static AudioSystemType in1System = AudioSystemType.DEFAULT;
	public static String in1SystemName = null;
	public static int inChannel1Id = 1;

	public static int bufferSize = 2048;
	public static int bufferedMs = 50;
	public static int delay = 25;
	public static int midiDelay = 200;

	public static double volume = 1;
	public static double sfxVolume = 1;

	public static void init(final Map<String, ValueAccessor> valueAccessors, final String name) {
		valueAccessors.put(name + ".outSystem",
				forString(v -> outSystem = AudioSystemType.valueOf(v), () -> outSystem.name()));
		valueAccessors.put(name + ".outSystemName", forString(v -> outSystemName = v, () -> outSystemName));
		valueAccessors.put(name + ".leftOutChannelId", forInteger(v -> leftOutChannelId = v, () -> leftOutChannelId));
		valueAccessors.put(name + ".rightOutChannelId",
				forInteger(v -> rightOutChannelId = v, () -> rightOutChannelId));

		valueAccessors.put(name + ".in0System",
				forString(v -> in0System = AudioSystemType.valueOf(v), () -> in0System.name()));
		valueAccessors.put(name + ".in0SystemName", forString(v -> in0SystemName = v, () -> in0SystemName));
		valueAccessors.put(name + ".inChannel0Id", forInteger(v -> inChannel0Id = v, () -> inChannel0Id));

		valueAccessors.put(name + ".in1System",
				forString(v -> in1System = AudioSystemType.valueOf(v), () -> in1System.name()));
		valueAccessors.put(name + ".in1SystemName", forString(v -> in1SystemName = v, () -> in1SystemName));
		valueAccessors.put(name + ".inChannel1Id", forInteger(v -> inChannel1Id = v, () -> inChannel1Id));

		valueAccessors.put(name + ".bufferSize", forInteger(v -> bufferSize = v, () -> bufferSize));
		valueAccessors.put(name + ".bufferedMs", forInteger(v -> bufferedMs = v, () -> bufferedMs));
		valueAccessors.put(name + ".delay", forInteger(v -> delay = v, () -> delay));
		valueAccessors.put(name + ".midiDelay", forInteger(v -> midiDelay = v, () -> midiDelay));

		valueAccessors.put(name + ".volume", forDouble(v -> volume = v, () -> volume));
		valueAccessors.put(name + ".sfxVolume", forDouble(v -> sfxVolume = v, () -> sfxVolume));
	}

}
