package log.charter.sound;

import static log.charter.io.Logger.error;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sound.sampled.UnsupportedAudioFileException;

import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Info;

import log.charter.io.Logger;
import log.charter.util.RW;

/**
 * Based on ExamplePlayer by Jon Kristensen
 */
public class OggLoader {
	private static final int BUF_SIZE = 2048;

	public static MusicData load(final String path) {
		try {
			return new OggLoader(path).load();
		} catch (final IOException | UnsupportedAudioFileException e) {
			error("Couldnt load ogg file " + path, e);
		}
		return null;
	}

	private byte[] buffer = null;
	private int count = 0;
	private int index = 0;
	private final boolean stopped = false;

	private byte[] convertedBuffer;
	private int convertedBufferSize;

	private final List<byte[]> bytesList = new ArrayList<>();

	private final float[][][] pcmInfo = new float[1][][];
	private int[] pcmIndex;

	private final InputStream inputStream;
	private final Packet joggPacket = new Packet();
	private final Page joggPage = new Page();
	private final StreamState joggStreamState = new StreamState();
	private final SyncState joggSyncState = new SyncState();
	private final DspState jorbisDspState = new DspState();
	private final Block jorbisBlock = new Block(jorbisDspState);
	private final Comment jorbisComment = new Comment();
	private final Info jorbisInfo = new Info();

	private OggLoader(final String path) {
		final byte[] bytes = RW.readB(path);
		Logger.debug("Loaded " + bytes.length + " bytes of ogg audio from " + path);
		inputStream = new ByteArrayInputStream(bytes);
	}

	private void cleanUp() throws IOException {
		joggStreamState.clear();
		jorbisBlock.clear();
		jorbisDspState.clear();
		jorbisInfo.clear();
		joggSyncState.clear();
		inputStream.close();
	}

	private void decodeCurrentPacket() {
		int samples;

		if (jorbisBlock.synthesis(joggPacket) == 0) {
			jorbisDspState.synthesis_blockin(jorbisBlock);
		}

		int range;

		while (((samples = jorbisDspState.synthesis_pcmout(pcmInfo, pcmIndex)) > 0) && !stopped) {
			if (samples < convertedBufferSize) {
				range = samples;
			} else {
				range = convertedBufferSize;
			}

			for (int i = 0; i < jorbisInfo.channels; i++) {
				int sampleIndex = i * 2;

				for (int j = 0; j < range; j++) {
					int value = (int) (pcmInfo[0][i][pcmIndex[i] + j] * 32767);

					if (value > 32767) {
						value = 32767;
					}
					if (value < -32768) {
						value = -32768;
					}

					if (value < 0) {
						value = value | 32768;
					}

					convertedBuffer[sampleIndex] = (byte) value;
					convertedBuffer[sampleIndex + 1] = (byte) (value >>> 8);

					sampleIndex += 2 * jorbisInfo.channels;
				}
			}

			bytesList.add(Arrays.copyOf(convertedBuffer, 2 * jorbisInfo.channels * range));

			jorbisDspState.synthesis_read(range);
		}
	}

	private void initializeJOrbis() {
		joggSyncState.init();
		joggSyncState.buffer(BUF_SIZE);
		buffer = joggSyncState.data;
	}

	private void initializeSound() {
		convertedBufferSize = BUF_SIZE * 2;
		convertedBuffer = new byte[convertedBufferSize];

		jorbisDspState.synthesis_init(jorbisInfo);
		jorbisBlock.init(jorbisDspState);
		pcmIndex = new int[jorbisInfo.channels];
	}

	private MusicData load() throws UnsupportedAudioFileException, IOException {
		initializeJOrbis();

		readHeader();
		initializeSound();
		readBody();
		cleanUp();

		final float rate = jorbisInfo.rate;

		int length = 0;
		for (final byte[] bytes : bytesList) {
			length += bytes.length;
		}

		final byte[] buffer = new byte[length];
		int last = 0;
		for (final byte[] bytes : bytesList) {
			System.arraycopy(bytes, 0, buffer, last, bytes.length);
			last += bytes.length;
		}

		return new MusicData(buffer, rate);
	}

	private void readBody() {
		boolean needMoreData = true;

		while (needMoreData && !stopped) {
			switch (joggSyncState.pageout(joggPage)) {
			case -1:
			case 0:
				break;
			case 1:
				joggStreamState.pagein(joggPage);

				if (joggPage.granulepos() == 0) {
					needMoreData = false;
					break;
				}

				processPackets: while (!stopped) {
					switch (joggStreamState.packetout(joggPacket)) {
					case -1:
					case 0:
						break processPackets;
					case 1:
						decodeCurrentPacket();
					}
				}

				if (joggPage.eos() != 0) {
					needMoreData = false;
				}
				if (stopped) {
					return;
				}
			}

			if (needMoreData) {
				index = joggSyncState.buffer(BUF_SIZE);
				buffer = joggSyncState.data;

				try {
					if (inputStream.available() > 0) {
						count = inputStream.read(buffer, index, BUF_SIZE);
					} else {
						return;
					}
				} catch (final Exception e) {
					error("Exception when reading ogg file", e);
					return;
				}

				joggSyncState.wrote(count);

				if (count == 0) {
					needMoreData = false;
				}
			}
		}
	}

	private void readHeader() {
		boolean needMoreData = true;
		int packet = 1;

		while (needMoreData) {
			try {
				count = inputStream.read(buffer, index, BUF_SIZE);
			} catch (final IOException exception) {
				throw new RuntimeException("Could not read from the input stream.", exception);
			}

			joggSyncState.wrote(count);

			switch (packet) {
			case 1:
				switch (joggSyncState.pageout(joggPage)) {
				case -1:
					throw new RuntimeException("There is a hole in the first packet data.");
				case 0:
					break;
				case 1:
					joggStreamState.init(joggPage.serialno());
					joggStreamState.reset();

					jorbisInfo.init();
					jorbisComment.init();

					if (joggStreamState.pagein(joggPage) == -1) {
						throw new RuntimeException("We got an error while reading the first header page.");
					}

					if (joggStreamState.packetout(joggPacket) != 1) {
						throw new RuntimeException("We got an error while reading the first header packet.");
					}

					if (jorbisInfo.synthesis_headerin(jorbisComment, joggPacket) < 0) {
						throw new RuntimeException(
								"We got an error while interpreting the first packet. Apparently, it's not Vorbis data.");
					}

					packet++;
					break;
				}

				if (packet == 1) {
					break;
				}
			case 2:
			case 3:
				switch (joggSyncState.pageout(joggPage)) {
				case -1:
					throw new RuntimeException("There is a hole in the second or third packet data.");
				case 0:
					break;
				case 1:
					joggStreamState.pagein(joggPage);

					switch (joggStreamState.packetout(joggPacket)) {
					case -1:
						throw new RuntimeException("There is a hole in the first packet data.");
					case 0:
						break;
					case 1:
						jorbisInfo.synthesis_headerin(jorbisComment, joggPacket);
						packet++;

						if (packet == 4) {
							needMoreData = false;
						}
						break;
					default:
						break;
					}
					break;
				}
				break;
			}

			index = joggSyncState.buffer(BUF_SIZE);
			buffer = joggSyncState.data;

			if ((count == 0) && needMoreData) {
				throw new RuntimeException("Not enough header data was supplied.");
			}
		}
	}
}
