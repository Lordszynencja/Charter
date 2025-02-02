package log.charter.sound.audioFormats.wav;
//Wav file IO class

//A.Greensted
//http://www.labbookpages.co.uk

//File format is based on the information from
//http://www.sonicspot.com/guide/wavefiles.html
//http://www.blitter.com/~russtopia/MIDI/~jglatt/tech/wave.htm

//Version 1.0

//Wav file IO class
//A.Greensted
//http://www.labbookpages.co.uk

//File format is based on the information from
//http://www.sonicspot.com/guide/wavefiles.html
//http://www.blitter.com/~russtopia/MIDI/~jglatt/tech/wave.htm

//Version 1.0
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.github.psambit9791.wavfile.WavFileException;

public class WavFile {
	private static class WavFileOpeningVariables {
		public final File file;

		public long chunkId;
		public long chunkSize;
		public long riffTypeId;

		public WavFileOpeningVariables(final File file) {
			this.file = file;
		}
	}

	/** Size of our internal buffer. */
	private final static int BUFFER_SIZE = 4096;

	/** WAV-specific constants. */
	private final static int FORMAT_CHUNK_ID = 0x20746D66;
	private final static int DATA_CHUNK_ID = 0x61746164;
	private final static int RIFF_CHUNK_ID = 0x46464952;
	private final static int RIFF_TYPE_ID = 0x45564157;

	/**
	 * Get little-endian data from the buffer.
	 *
	 * @param buffer   the buffer to read from
	 * @param pos      the starting position
	 * @param numBytes the number of bytes to read
	 * @return a little-endian long
	 */
	private static long getLE(final byte[] buffer, int pos, int numBytes) {
		numBytes--;
		pos += numBytes;

		long val = buffer[pos] & 0xFF;
		for (int b = 0; b < numBytes; b++) {
			val = (val << 8) + (buffer[--pos] & 0xFF);
		}

		return val;
	}

	/**
	 * Put little-endian data into the buffer.
	 *
	 * @param val      the data to write into the buffer
	 * @param buffer   the buffer to write to
	 * @param pos      the position to write to
	 * @param numBytes the number of bytes to write
	 */
	private static void putLE(long val, final byte[] buffer, int pos, final int numBytes) {
		for (int b = 0; b < numBytes; b++) {
			buffer[pos] = (byte) (val & 0xFF);
			val >>= 8;
			pos++;
		}
	}

	/** States that our IO operations can be in. */
	private enum IOState {
		READING, WRITING, CLOSED
	};

	private IOState ioState; // Specifies the IO State of the Wav File (used for snaity checking)
	private int bytesPerSample; // Number of bytes required to store a single sample
	private long numFrames; // Number of frames within the data section
	private FileOutputStream oStream; // Output stream used for writting data
	private FileInputStream iStream; // Input stream used for reading data
	private double floatScale; // Scaling factor used for int <-> float conversion
	private double floatOffset; // Offset factor used for int <-> float conversion
	private boolean wordAlignAdjust; // Specify if an extra byte at the end of the data chunk is required for word
										// alignment

	/** Wav Header. */
	private int numChannels; // 2 bytes unsigned, 0x0001 (1) to 0xFFFF (65,535)
	private long sampleRate; // 4 bytes unsigned, 0x00000001 (1) to 0xFFFFFFFF (4,294,967,295)

	/** Although a java int is 4 bytes, it is signed, so need to use a long */
	private int blockAlign; // 2 bytes unsigned, 0x0001 (1) to 0xFFFF (65,535)
	private int validBits; // 2 bytes unsigned, 0x0002 (2) to 0xFFFF (65,535)

	/** Buffering */
	private final byte[] buffer; // Local buffer used for IO
	private int bufferPointer; // Points to the current position in local buffer
	private int bytesRead; // Bytes read after last read into local buffer
	private long frameCounter; // Current number of frames read or written

	/**
	 * Creates new WAV file interface for writing
	 */
	public WavFile() {
		buffer = new byte[BUFFER_SIZE];
	}

	private void fileReady(final IOState state) {
		bufferPointer = 0;
		bytesRead = 0;
		frameCounter = 0;
		ioState = state;
	}

	private void calculateFloatValuesForReading() {
		if (validBits > 8) {
			// If more than 8 validBits, data is signed
			// Conversion required dividing by magnitude of max negative value
			floatOffset = 0;
			floatScale = 1 << (validBits - 1);
		} else {
			// Else if 8 or less validBits, data is unsigned
			// Conversion required dividing by max positive value
			floatOffset = -1;
			floatScale = 0.5 * ((1 << validBits) - 1);
		}
	}

	public WavFile openForReading(final File file) throws Exception {
		if (ioState != null) {
			throw new Exception("File was already opened");
		}

		final WavFileOpeningVariables openingVariables = new WavFileOpeningVariables(file);

		// Create a new file input stream for reading file data
		iStream = new FileInputStream(openingVariables.file);

		readHeader(openingVariables);
		readFormat(openingVariables);
		readDataChunkHeader(openingVariables);

		fileReady(IOState.READING);

		return this;
	}

	private void readHeader(final WavFileOpeningVariables openingVariables, final int size) throws Exception {
		bytesRead = iStream.read(buffer, 0, size);
		if (bytesRead < size) {
			throw new Exception("Couldn't read header");
		}

		openingVariables.chunkId = getLE(buffer, 0, 4);
		openingVariables.chunkSize = getLE(buffer, 4, 4);
	}

	private void readHeader(final WavFileOpeningVariables openingVariables) throws Exception {
		readHeader(openingVariables, 12);
		if (bytesRead != 12) {
			throw new Exception("Not enough wav file bytes for header");
		}
		openingVariables.riffTypeId = getLE(buffer, 8, 4);

		// Check the header bytes contains the correct signature
		if (openingVariables.chunkId != RIFF_CHUNK_ID) {
			throw new Exception("Invalid Wav Header data, incorrect riff chunk ID");
		}
		if (openingVariables.riffTypeId != RIFF_TYPE_ID) {
			throw new Exception("Invalid Wav Header data, incorrect riff type ID");
		}

		// Check that the file size matches the number of bytes listed in header
		if (openingVariables.file.length() != openingVariables.chunkSize + 8) {
			throw new Exception("Header chunk size (" + openingVariables.chunkSize + ") does not match file size ("
					+ openingVariables.file.length() + ")");
		}
	}

	private void checkCompressionCode() throws Exception {
		// Check this is uncompressed data
		final int compressionCode = (int) getLE(buffer, 0, 2);
		if (compressionCode != 1) {
			throw new Exception("Compression Code " + compressionCode + " not supported");
		}
	}

	private void readNumberOfChannels() throws Exception {
		// Extract the format information
		numChannels = (int) getLE(buffer, 2, 2);
		if (numChannels == 0) {
			throw new Exception("Number of channels specified in header is equal to zero");
		}
	}

	private void readSampleRate() throws Exception {
		sampleRate = getLE(buffer, 4, 4);
		if (sampleRate == 0) {
			throw new Exception("Sample rate is equal to zero");
		}
	}

	private void readBlockAlign() throws Exception {
		blockAlign = (int) getLE(buffer, 12, 2);
		if (blockAlign == 0) {
			throw new Exception("Block Align specified in header is equal to zero");
		}
	}

	private void readValidBits() throws Exception {
		validBits = (int) getLE(buffer, 14, 2);
		if (validBits < 2 || validBits > 64) {
			throw new Exception("Valid Bits specified in header is unsupported (" + validBits + ")");
		}
	}

	private void calculateBytesPerSample() throws Exception {
		bytesPerSample = (validBits + 7) / 8;
		if (bytesPerSample * numChannels != blockAlign) {
			throw new Exception("Block Align does not agree with bytes required for validBits and number of channels");
		}
	}

	private void readFormatChunk(final long numChunkBytes) throws Exception {
		bytesRead = iStream.read(buffer, 0, 16);

		checkCompressionCode();
		readNumberOfChannels();
		readSampleRate();
		readBlockAlign();
		readValidBits();

		calculateBytesPerSample();
		calculateFloatValuesForReading();

		// skip over any extra format bytes
		if (numChunkBytes > 16) {
			iStream.skip(numChunkBytes - 16);
		}
	}

	private void readFormat(final WavFileOpeningVariables openingVariables) throws Exception {
		boolean formatChunkFound = false;
		// Search for the Format and Data Chunks
		while (!formatChunkFound) {
			readHeader(openingVariables, 8);

			// Word align the chunk size
			// chunkSize specifies the number of bytes holding data. However,
			// the data should be word aligned (2 bytes) so we need to calculate
			// the actual number of bytes in the chunk
			final long numChunkBytes = (openingVariables.chunkSize % 2 == 1) ? openingVariables.chunkSize + 1
					: openingVariables.chunkSize;

			if (openingVariables.chunkId == DATA_CHUNK_ID) {
				throw new Exception("Data chunk found before Format chunk");
			}
			if (openingVariables.chunkId != FORMAT_CHUNK_ID) {
				iStream.skip(numChunkBytes);
				continue;
			}

			readFormatChunk(numChunkBytes);
			formatChunkFound = true;
		}
	}

	private void readDataChunkHeader(final WavFileOpeningVariables openingVariables) throws Exception {
		boolean foundData = false;

		// Search for the Format and Data Chunks
		while (!foundData) {
			readHeader(openingVariables, 8);

			// Word align the chunk size
			// chunkSize specifies the number of bytes holding data. However,
			// the data should be word aligned (2 bytes) so we need to calculate
			// the actual number of bytes in the chunk
			final long numChunkBytes = (openingVariables.chunkSize % 2 == 1) ? openingVariables.chunkSize + 1
					: openingVariables.chunkSize;

			if (openingVariables.chunkId != DATA_CHUNK_ID) {
				iStream.skip(numChunkBytes);
				continue;
			}

			// Check that the chunkSize (wav data length) is a multiple of the
			// block align (bytes per frame)
			if (openingVariables.chunkSize % blockAlign != 0) {
				throw new Exception("Data Chunk size is not multiple of Block Align");
			}

			numFrames = openingVariables.chunkSize / blockAlign;

			foundData = true;
		}
	}

	private void calculateFloatValuesForWriting() {
		// Calculate the scaling factor for converting to a normalised double
		if (validBits > 8) {
			// If more than 8 validBits, data is signed
			// Conversion required multiplying by magnitude of max positive value
			floatOffset = 0;
			floatScale = Long.MAX_VALUE >> (64 - validBits);
		} else {
			// Else if 8 or less validBits, data is unsigned
			// Conversion required dividing by max positive value
			floatOffset = 1;
			floatScale = 0.5 * ((1 << validBits) - 1);
		}
	}

	private void writeValues(final int numChannels, final long numFrames, final int validBits, final long sampleRate)
			throws Exception {
		if (numChannels < 1 || numChannels > 65535) {
			throw new Exception("Illegal number of channels, valid range 1 to 65536");
		}
		if (numFrames < 0) {
			throw new Exception("Number of frames must be positive");
		}
		if (validBits < 2 || validBits > 64) {
			throw new Exception("Illegal number of valid bits, valid range 2 to 65536");
		}
		if (sampleRate < 0) {
			throw new Exception("Sample rate must be positive");
		}

		this.numChannels = numChannels;
		this.numFrames = numFrames;
		this.sampleRate = sampleRate;
		bytesPerSample = (validBits + 7) / 8;
		blockAlign = bytesPerSample * numChannels;
		this.validBits = validBits;
		calculateFloatValuesForWriting();
	}

	private void writeHeader(final long chunkId, final long chunkSize) throws IOException {
		putLE(chunkId, buffer, 0, 4);
		putLE(chunkSize, buffer, 4, 4);

		oStream.write(buffer, 0, 8);
	}

	private void writeChunk(final long chunkId, final int chunkSize) throws IOException {
		writeHeader(chunkId, chunkSize);
		oStream.write(buffer, 8, chunkSize);
	}

	private void writeFileHeader(final long dataChunkSize) throws IOException {
		long fileSize = 4 + // Riff Type
				8 + // Format ID and size
				16 + // Format data
				8 + // Data ID and size
				dataChunkSize;

		// Chunks must be word aligned, so if odd number of audio data bytes
		// adjust the main chunk size
		if (dataChunkSize % 2 == 1) {
			fileSize += 1;
			wordAlignAdjust = true;
		} else {
			wordAlignAdjust = false;
		}

		putLE(RIFF_TYPE_ID, buffer, 8, 4);

		writeHeader(RIFF_CHUNK_ID, fileSize);
		oStream.write(buffer, 8, 4);
	}

	private void writeFormatChunk() throws IOException {
		putLE(1, buffer, 8, 2); // Compression Code (Uncompressed)
		putLE(numChannels, buffer, 10, 2); // Number of channels
		putLE(sampleRate, buffer, 12, 4); // Sample Rate
		putLE(sampleRate * blockAlign, buffer, 16, 4); // Average Bytes Per Second
		putLE(blockAlign, buffer, 20, 2); // Block Align
		putLE(validBits, buffer, 22, 2); // Valid Bits

		writeChunk(FORMAT_CHUNK_ID, 16);
	}

	public WavFile openForWriting(final File file, final int numChannels, final long numFrames, final int validBits,
			final long sampleRate) throws Exception {
		if (ioState != null) {
			throw new Exception("File was already opened");
		}

		writeValues(numChannels, numFrames, validBits, sampleRate);

		oStream = new FileOutputStream(file);

		final long dataChunkSize = blockAlign * numFrames;

		writeFileHeader(dataChunkSize);
		writeFormatChunk();
		writeHeader(DATA_CHUNK_ID, dataChunkSize);

		fileReady(IOState.WRITING);

		return this;
	}

	/**
	 * Gets the number of channels.
	 *
	 * @return the number of channels
	 */
	public int getNumChannels() {
		return numChannels;
	}

	/**
	 * Get the number of frames.
	 *
	 * @return the number of frames
	 */
	public long getNumFrames() {
		return numFrames;
	}

	/**
	 * Get the number of frames remaining.
	 *
	 * @return the number of frames remaining
	 */
	public long getFramesRemaining() {
		return numFrames - frameCounter;
	}

	/**
	 * Get the sample rate.
	 *
	 * @return the sample rate
	 */
	public long getSampleRate() {
		return sampleRate;
	}

	/**
	 * Get the number of valid bits.
	 *
	 * @return the number of valid bits
	 */
	public int getValidBits() {
		return validBits;
	}

	public int getBytesPerSample() {
		return bytesPerSample;
	}

	/**
	 * Write a sample to our buffer.
	 *
	 * @param val the sample to write
	 * @throws IOException Signals that an I/O exception has occurred
	 */
	private void writeSample(long val) throws IOException {
		for (int b = 0; b < bytesPerSample; b++) {
			if (bufferPointer == BUFFER_SIZE) {
				oStream.write(buffer, 0, BUFFER_SIZE);
				bufferPointer = 0;
			}

			buffer[bufferPointer] = (byte) (val & 0xFF);
			val >>= 8;
			bufferPointer++;
		}
	}

	/**
	 * Read a sample from the buffer.
	 *
	 * @return the sample read
	 * @throws Exception
	 */
	private long readSample() throws Exception {
		long val = 0;

		for (int b = 0; b < bytesPerSample; b++) {
			if (bufferPointer == bytesRead) {
				final int read = iStream.read(buffer, 0, BUFFER_SIZE);
				if (read == -1) {
					throw new Exception("Not enough data available");
				}
				bytesRead = read;
				bufferPointer = 0;
			}

			int v = buffer[bufferPointer];
			if (b < bytesPerSample - 1 || bytesPerSample == 1) {
				v &= 0xFF;
			}
			val += v << (b * 8);

			bufferPointer++;
		}

		return val;
	}

	/**
	 * Read some number of frames from the buffer into a flat int array.
	 *
	 * @param sampleBuffer    the buffer to read samples into
	 * @param numFramesToRead the number of frames to read
	 * @return the number of frames read
	 * @throws IOException      Signals that an I/O exception has occurred
	 * @throws WavFileException a WavFile-specific exception
	 */
	public int readFrames(final int[] sampleBuffer, final int numFramesToRead) throws Exception {
		return readFrames(sampleBuffer, 0, numFramesToRead);
	}

	/**
	 * Read some number of frames from a specific offset in the buffer into a flat
	 * int array.
	 *
	 * @param sampleBuffer    the buffer to read samples into
	 * @param offset          the buffer offset to read from
	 * @param numFramesToRead the number of frames to read
	 * @return the number of frames read
	 * @throws IOException      Signals that an I/O exception has occurred
	 * @throws WavFileException a WavFile-specific exception
	 */
	public int readFrames(final int[] sampleBuffer, int offset, final int numFramesToRead) throws Exception {
		if (ioState != IOState.READING) {
			throw new IOException("Cannot read from WavFile instance");
		}

		for (int f = 0; f < numFramesToRead; f++) {
			if (frameCounter == numFrames) {
				return f;
			}

			for (int c = 0; c < numChannels; c++) {
				sampleBuffer[offset] = (int) readSample();
				offset++;
			}

			frameCounter++;
		}

		return numFramesToRead;
	}

	/**
	 * Read some number of frames from the buffer into a multi-dimensional int
	 * array.
	 *
	 * @param sampleBuffer    the buffer to read samples into
	 * @param numFramesToRead the number of frames to read
	 * @return the number of frames read
	 * @throws IOException      Signals that an I/O exception has occurred
	 * @throws WavFileException a WavFile-specific exception
	 */
	public int readFrames(final int[][] sampleBuffer, final int numFramesToRead) throws Exception {
		return readFrames(sampleBuffer, 0, numFramesToRead);
	}

	/**
	 * Read some number of frames from a specific offset in the buffer into a
	 * multi-dimensional int array.
	 *
	 * @param sampleBuffer    the buffer to read samples into
	 * @param offset          the buffer offset to read from
	 * @param numFramesToRead the number of frames to read
	 * @return the number of frames read
	 * @throws IOException      Signals that an I/O exception has occurred
	 * @throws WavFileException a WavFile-specific exception
	 */
	public int readFrames(final int[][] sampleBuffer, int offset, final int numFramesToRead) throws Exception {
		if (ioState != IOState.READING) {
			throw new IOException("Cannot read from WavFile instance");
		}

		for (int f = 0; f < numFramesToRead; f++) {
			if (frameCounter == numFrames) {
				return f;
			}

			for (int c = 0; c < numChannels; c++) {
				sampleBuffer[c][offset] = (int) readSample();
			}

			offset++;
			frameCounter++;
		}

		return numFramesToRead;
	}

	/**
	 * Write some number of frames into the buffer from a flat int array.
	 *
	 * @param sampleBuffer     the buffer to read samples from
	 * @param numFramesToWrite the number of frames to write
	 * @return the number of frames written
	 * @throws IOException      Signals that an I/O exception has occurred
	 * @throws WavFileException a WavFile-specific exception
	 */
	public int writeFrames(final short[] sampleBuffer, final int numFramesToWrite) throws Exception {
		return writeFrames(sampleBuffer, 0, numFramesToWrite);
	}

	/**
	 * Write some number of frames into the buffer at a specific offset from a flat
	 * int array.
	 *
	 * @param sampleBuffer     the buffer to read samples from
	 * @param offset           the buffer offset to write into
	 * @param numFramesToWrite the number of frames to write
	 * @return the number of frames written
	 * @throws IOException      Signals that an I/O exception has occurred
	 * @throws WavFileException a WavFile-specific exception
	 */
	public int writeFrames(final short[] sampleBuffer, int offset, final int numFramesToWrite) throws Exception {
		if (ioState != IOState.WRITING) {
			throw new IOException("Cannot write to WavFile instance");
		}

		for (int f = 0; f < numFramesToWrite; f++) {
			if (frameCounter == numFrames) {
				return f;
			}

			for (int c = 0; c < numChannels; c++) {
				writeSample(sampleBuffer[offset]);
				offset++;
			}

			frameCounter++;
		}

		return numFramesToWrite;
	}

	/**
	 * Write some number of frames into the buffer from a multi-dimensional int
	 * array.
	 *
	 * @param sampleBuffer     the buffer to read samples from
	 * @param numFramesToWrite the number of frames to write
	 * @return the number of frames written
	 * @throws IOException      Signals that an I/O exception has occurred
	 * @throws WavFileException a WavFile-specific exception
	 */
	public int writeFrames(final int[][] sampleBuffer, final int numFramesToWrite) throws Exception {
		return writeFrames(sampleBuffer, 0, numFramesToWrite);
	}

	/**
	 * Write some number of frames into the buffer at a specific offset from a
	 * multi-dimensional int array.
	 *
	 * @param sampleBuffer     the buffer to read samples from
	 * @param offset           the buffer offset to write into
	 * @param numFramesToWrite the number of frames to write
	 * @return the number of frames written
	 * @throws IOException      Signals that an I/O exception has occurred
	 * @throws WavFileException a WavFile-specific exception
	 */
	public int writeFrames(final int[][] sampleBuffer, int offset, final int numFramesToWrite) throws Exception {
		if (ioState != IOState.WRITING) {
			throw new IOException("Cannot write to WavFile instance");
		}

		for (int f = 0; f < numFramesToWrite; f++) {
			if (frameCounter == numFrames) {
				return f;
			}

			for (int c = 0; c < numChannels; c++) {
				writeSample(sampleBuffer[c][offset]);
			}

			offset++;
			frameCounter++;
		}

		return numFramesToWrite;
	}

	/**
	 * Read some number of frames from the buffer into a flat long array.
	 *
	 * @param sampleBuffer    the buffer to read samples into
	 * @param numFramesToRead the number of frames to read
	 * @return the number of frames read
	 * @throws IOException      Signals that an I/O exception has occurred
	 * @throws WavFileException a WavFile-specific exception
	 */
	public int readFrames(final long[] sampleBuffer, final int numFramesToRead) throws Exception {
		return readFrames(sampleBuffer, 0, numFramesToRead);
	}

	/**
	 * Read some number of frames from a specific offset in the buffer into a flat
	 * long array.
	 *
	 * @param sampleBuffer    the buffer to read samples into
	 * @param offset          the buffer offset to read from
	 * @param numFramesToRead the number of frames to read
	 * @return the number of frames read
	 * @throws IOException      Signals that an I/O exception has occurred
	 * @throws WavFileException a WavFile-specific exception
	 */
	public int readFrames(final long[] sampleBuffer, int offset, final int numFramesToRead) throws Exception {
		if (ioState != IOState.READING) {
			throw new IOException("Cannot read from WavFile instance");
		}

		for (int f = 0; f < numFramesToRead; f++) {
			if (frameCounter == numFrames) {
				return f;
			}

			for (int c = 0; c < numChannels; c++) {
				sampleBuffer[offset] = readSample();
				offset++;
			}

			frameCounter++;
		}

		return numFramesToRead;
	}

	/**
	 * Read some number of frames from the buffer into a multi-dimensional long
	 * array.
	 *
	 * @param sampleBuffer    the buffer to read samples into
	 * @param numFramesToRead the number of frames to read
	 * @return the number of frames read
	 * @throws IOException      Signals that an I/O exception has occurred
	 * @throws WavFileException a WavFile-specific exception
	 */
	public int readFrames(final long[][] sampleBuffer, final int numFramesToRead) throws Exception {
		return readFrames(sampleBuffer, 0, numFramesToRead);
	}

	/**
	 * Read some number of frames from a specific offset in the buffer into a
	 * multi-dimensional long array.
	 *
	 * @param sampleBuffer    the buffer to read samples into
	 * @param offset          the buffer offset to read from
	 * @param numFramesToRead the number of frames to read
	 * @return the number of frames read
	 * @throws IOException      Signals that an I/O exception has occurred
	 * @throws WavFileException a WavFile-specific exception
	 */
	public int readFrames(final long[][] sampleBuffer, int offset, final int numFramesToRead) throws Exception {
		if (ioState != IOState.READING) {
			throw new IOException("Cannot read from WavFile instance");
		}

		for (int f = 0; f < numFramesToRead; f++) {
			if (frameCounter == numFrames) {
				return f;
			}

			for (int c = 0; c < numChannels; c++) {
				sampleBuffer[c][offset] = readSample();
			}

			offset++;
			frameCounter++;
		}

		return numFramesToRead;
	}

	/**
	 * Write some number of frames into the buffer from a flat long array.
	 *
	 * @param sampleBuffer     the buffer to read samples from
	 * @param numFramesToWrite the number of frames to write
	 * @return the number of frames written
	 * @throws IOException      Signals that an I/O exception has occurred
	 * @throws WavFileException a WavFile-specific exception
	 */
	public int writeFrames(final long[] sampleBuffer, final int numFramesToWrite) throws Exception {
		return writeFrames(sampleBuffer, 0, numFramesToWrite);
	}

	/**
	 * Write some number of frames into the buffer at a specific offset from a flat
	 * long array.
	 *
	 * @param sampleBuffer     the buffer to read samples from
	 * @param offset           the buffer offset to write into
	 * @param numFramesToWrite the number of frames to write
	 * @return the number of frames written
	 * @throws IOException      Signals that an I/O exception has occurred
	 * @throws WavFileException a WavFile-specific exception
	 */
	public int writeFrames(final long[] sampleBuffer, int offset, final int numFramesToWrite) throws Exception {
		if (ioState != IOState.WRITING) {
			throw new IOException("Cannot write to WavFile instance");
		}

		for (int f = 0; f < numFramesToWrite; f++) {
			if (frameCounter == numFrames) {
				return f;
			}

			for (int c = 0; c < numChannels; c++) {
				writeSample(sampleBuffer[offset]);
				offset++;
			}

			frameCounter++;
		}

		return numFramesToWrite;
	}

	/**
	 * Write some number of frames into the buffer from a multi-dimensional long
	 * array.
	 *
	 * @param sampleBuffer     the buffer to read samples from
	 * @param numFramesToWrite the number of frames to write
	 * @return the number of frames written
	 * @throws IOException      Signals that an I/O exception has occurred
	 * @throws WavFileException a WavFile-specific exception
	 */
	public int writeFrames(final long[][] sampleBuffer, final int numFramesToWrite) throws Exception {
		return writeFrames(sampleBuffer, 0, numFramesToWrite);
	}

	/**
	 * Write some number of frames into the buffer at a specific offset from a
	 * multi-dimensional long array.
	 *
	 * @param sampleBuffer     the buffer to read samples from
	 * @param offset           the buffer offset to write into
	 * @param numFramesToWrite the number of frames to write
	 * @return the number of frames written
	 * @throws IOException      Signals that an I/O exception has occurred
	 * @throws WavFileException a WavFile-specific exception
	 */
	public int writeFrames(final long[][] sampleBuffer, int offset, final int numFramesToWrite) throws Exception {
		if (ioState != IOState.WRITING) {
			throw new IOException("Cannot write to WavFile instance");
		}

		for (int f = 0; f < numFramesToWrite; f++) {
			if (frameCounter == numFrames) {
				return f;
			}

			for (int c = 0; c < numChannels; c++) {
				writeSample(sampleBuffer[c][offset]);
			}

			offset++;
			frameCounter++;
		}

		return numFramesToWrite;
	}

	/**
	 * Read some number of frames from the buffer into a flat double array.
	 *
	 * @param sampleBuffer    the buffer to read samples into
	 * @param numFramesToRead the number of frames to read
	 * @return the number of frames read
	 * @throws IOException      Signals that an I/O exception has occurred
	 * @throws WavFileException a WavFile-specific exception
	 */
	public int readFrames(final double[] sampleBuffer, final int numFramesToRead) throws Exception {
		return readFrames(sampleBuffer, 0, numFramesToRead);
	}

	/**
	 * Read some number of frames from a specific offset in the buffer into a flat
	 * double array.
	 *
	 * @param sampleBuffer    the buffer to read samples into
	 * @param offset          the buffer offset to read from
	 * @param numFramesToRead the number of frames to read
	 * @return the number of frames read
	 * @throws IOException Signals that an I/O exception has occurred
	 */
	public int readFrames(final double[] sampleBuffer, int offset, final int numFramesToRead) throws Exception {
		if (ioState != IOState.READING) {
			throw new IOException("Cannot read from WavFile instance");
		}

		for (int f = 0; f < numFramesToRead; f++) {
			if (frameCounter == numFrames) {
				return f;
			}

			for (int c = 0; c < numChannels; c++) {
				sampleBuffer[offset] = floatOffset + (double) readSample() / floatScale;
				offset++;
			}

			frameCounter++;
		}

		return numFramesToRead;
	}

	/**
	 * Read some number of frames from the buffer into a multi-dimensional double
	 * array.
	 *
	 * @param sampleBuffer    the buffer to read samples into
	 * @param numFramesToRead the number of frames to read
	 * @return the number of frames read
	 * @throws IOException Signals that an I/O exception has occurred
	 */
	public int readFrames(final double[][] sampleBuffer, final int numFramesToRead) throws Exception {
		return readFrames(sampleBuffer, 0, numFramesToRead);
	}

	/**
	 * Read some number of frames from a specific offset in the buffer into a
	 * multi-dimensional double array.
	 *
	 * @param sampleBuffer    the buffer to read samples into
	 * @param offset          the buffer offset to read from
	 * @param numFramesToRead the number of frames to read
	 * @return the number of frames read
	 * @throws IOException Signals that an I/O exception has occurred
	 */
	public int readFrames(final double[][] sampleBuffer, int offset, final int numFramesToRead) throws Exception {
		if (ioState != IOState.READING) {
			throw new IOException("Cannot read from WavFile instance");
		}

		for (int f = 0; f < numFramesToRead; f++) {
			if (frameCounter == numFrames) {
				return f;
			}

			for (int c = 0; c < numChannels; c++) {
				sampleBuffer[c][offset] = floatOffset + (double) readSample() / floatScale;
			}

			offset++;
			frameCounter++;
		}

		return numFramesToRead;
	}

	/**
	 * Write some number of frames into the buffer from a flat double array.
	 *
	 * @param sampleBuffer     the buffer to read samples from
	 * @param numFramesToWrite the number of frames to write
	 * @return the number of frames written
	 * @throws IOException Signals that an I/O exception has occurred
	 */
	public int writeFrames(final double[] sampleBuffer, final int numFramesToWrite) throws Exception {
		return writeFrames(sampleBuffer, 0, numFramesToWrite);
	}

	/**
	 * Write some number of frames into the buffer at a specific offset from a flat
	 * double array.
	 *
	 * @param sampleBuffer     the buffer to read samples from
	 * @param offset           the buffer offset to write into
	 * @param numFramesToWrite the number of frames to write
	 * @return the number of frames written
	 * @throws IOException Signals that an I/O exception has occurred
	 */
	public int writeFrames(final double[] sampleBuffer, int offset, final int numFramesToWrite) throws Exception {
		if (ioState != IOState.WRITING) {
			throw new IOException("Cannot write to WavFile instance");
		}

		for (int f = 0; f < numFramesToWrite; f++) {
			if (frameCounter == numFrames) {
				return f;
			}

			for (int c = 0; c < numChannels; c++) {
				writeSample((long) (floatScale * (floatOffset + sampleBuffer[offset])));
				offset++;
			}

			frameCounter++;
		}

		return numFramesToWrite;
	}

	/**
	 * Write some number of frames into the buffer from a multi-dimensional double
	 * array.
	 *
	 * @param sampleBuffer     the buffer to read samples from
	 * @param numFramesToWrite the number of frames to write
	 * @return the number of frames written
	 * @throws IOException Signals that an I/O exception has occurred
	 */
	public int writeFrames(final double[][] sampleBuffer, final int numFramesToWrite) throws Exception {
		return writeFrames(sampleBuffer, 0, numFramesToWrite);
	}

	/**
	 * Write some number of frames into the buffer at a specific offset from a
	 * multi-dimensional double array.
	 *
	 * @param sampleBuffer     the buffer to read samples from
	 * @param offset           the buffer offset to write into
	 * @param numFramesToWrite the number of frames to write
	 * @return the number of frames written
	 * @throws IOException Signals that an I/O exception has occurred
	 */
	public int writeFrames(final double[][] sampleBuffer, int offset, final int numFramesToWrite) throws Exception {
		if (ioState != IOState.WRITING) {
			throw new IOException("Cannot write to WavFile instance");
		}

		for (int f = 0; f < numFramesToWrite; f++) {
			if (frameCounter == numFrames) {
				return f;
			}

			for (int c = 0; c < numChannels; c++) {
				writeSample((long) (floatScale * (floatOffset + sampleBuffer[c][offset])));
			}

			offset++;
			frameCounter++;
		}

		return numFramesToWrite;
	}

	/**
	 * Close the WavFile.
	 *
	 * @throws IOException Signals that an I/O exception has occurred
	 */
	public void close() throws IOException {
		// Close the input stream and set to null
		if (iStream != null) {
			iStream.close();
			iStream = null;
		}

		if (oStream != null) {
			// Write out anything still in the local buffer
			if (bufferPointer > 0) {
				oStream.write(buffer, 0, bufferPointer);
			}

			// If an extra byte is required for word alignment, add it to the end
			if (wordAlignAdjust) {
				oStream.write(0);
			}

			// Close the stream and set to null
			oStream.close();
			oStream = null;
		}

		// Flag that the stream is closed
		ioState = IOState.CLOSED;
	}
}