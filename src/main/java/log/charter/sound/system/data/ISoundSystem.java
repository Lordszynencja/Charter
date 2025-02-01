package log.charter.sound.system.data;

import javax.sound.sampled.AudioFormat;

public interface ISoundSystem {
	ISoundLine getNewLine(AudioFormat format);
}