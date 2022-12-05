package helliker.id3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/*
   Copyright (C) 2001,2002 Jonathan Hilliker
   This library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.
   This library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
   Lesser General Public License for more details.
   You should have received a copy of the GNU Lesser General Public
   License along with this library; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
/**
 * This class reads through the file specified and tries to find an mpeg frame.
 * It then reads data from the header of the first frame encountered. <br/>
 *
 * <dl>
 * <dt><b>Version History</b></dt>
 * <dt>1.8 - <small>2002.10/20 by Tech</small></dt>
 * <dd>Fixed some bugs when detecting the first MPEG frame</dd>
 * <dt>1.7.1 - <small>2002.10/17 by gruni</small></dt>
 * <dd>Made Sourcecode compliant to the Sun CodingConventions</dd>
 * <dt>1.7 - <small>2002/02/25 by helliker</small></dt>
 * <dd>-Implemented a much better mpeg identification algorithm.</dd>
 * <dd>-Added method for computing number of frames.</dd>
 * <dt>1.6 - <small>2002/02/03 by helliker</small></dt>
 * <dd>-New algorithm for determining presence of 1st frame.</dd>
 * <dd>-More checks to ensure stability.</dd>
 * <dt>1.5 - <small>2002/01/22 by helliker</small></dt>
 * <dd>-Made stupid mistake that causes an infininte recursive loop when reading
 * non-VBR files.</dd>
 * <dt>1.4 - <small>2002/01/21 by helliker</small></dt>
 * <dd>-Added support for VBR files.</dd>
 * <dd>-Added fields for padding and private bits.</dd>
 * <dd>-Added getFrameLength method.</dd>
 * <dt>1.3 - <small>2001/11/10 by helliker</small></dt>
 * <dd>-Fixed file handle leaks.</dd>
 * <dd>-Added accessor to find the offset of the MPEG data.</dd>
 * <dt>1.2 - <small>2001/10/19 by helliker</small></dt>
 * <dd>All set for release.</dd>
 * </dl>
 *
 *
 * @author Jonathan Hilliker
 * @version 1.8
 */

public class MPEGAudioFrameHeader {

	/**
	 * MPEG Version 2.5
	 */
	public final static int MPEG_V_25 = 0;
	/**
	 * MPEG Version 2
	 */
	public final static int MPEG_V_2 = 2;
	/**
	 * MPEG Version 1
	 */
	public final static int MPEG_V_1 = 3;
	/**
	 * Layer III
	 */
	public final static int MPEG_L_3 = 1;
	/**
	 * Layer II
	 */
	public final static int MPEG_L_2 = 2;
	/**
	 * Layer I
	 */
	public final static int MPEG_L_1 = 3;
	/**
	 * Mono
	 */
	public final static int MONO_MODE = 3;

	/**
	 * The string with which a ID3 Tag starts
	 */
	@SuppressWarnings("unused")
	private final String TAG_START = "ID3";

	/**
	 * Header Size
	 */
	private final int HEADER_SIZE = 4;
	/**
	 * Maximum Tries for trying to find an MP3 frame
	 */
	private final int MAX_TRIES = 9;
	/**
	 * The MPEG Bitrate Table. -2 means "free bitrate" and -1 means "not
	 * allowed".
	 */
	private final int[][] bitrateTable = {
			{ -2, -2, -2, -2, -2 },
			{ 32, 32, 32, 32, 8 },
			{ 64, 48, 40, 48, 16 },
			{ 96, 56, 48, 56, 24 },
			{ 128, 64, 56, 64, 32 },
			{ 160, 80, 64, 80, 40 },
			{ 192, 96, 80, 96, 48 },
			{ 224, 112, 96, 112, 56 },
			{ 256, 128, 112, 128, 64 },
			{ 288, 160, 128, 144, 80 },
			{ 320, 192, 160, 160, 96 },
			{ 352, 224, 192, 176, 112 },
			{ 384, 256, 224, 192, 128 },
			{ 416, 320, 256, 224, 144 },
			{ 448, 384, 320, 256, 160 },
			{ -1, -1, -1, -1, -1 } };
	/**
	 * The MPEG Sample rate Table
	 */
	private final int[][] sampleTable = {
			{ 44100, 22050, 11025 },
			{ 48000, 24000, 12000 },
			{ 32000, 16000, 8000 },
			{ -1, -1, -1 } };
	/**
	 * MPEG Version Lables
	 */
	private final String[] versionLabels = { "MPEG Version 2.5", null,
			"MPEG Version 2.0",
			"MPEG Version 1.0" };
	/**
	 * MPEG Layer Lables
	 */
	private final String[] layerLabels = { null, "Layer III", "Layer II",
			"Layer I" };
	/**
	 * MPEG ChannelLables
	 */
	private final String[] channelLabels = { "Stereo", "Joint Stereo (STEREO)",
			"Dual Channel (STEREO)",
			"Single Channel (MONO)" };
	/**
	 * Emphasis Lables
	 */
	private final String[] emphasisLabels = { "none", "50/15 ms", null,
			"CCIT J.17" };
	/**
	 * MPEG Slotlegths
	 */
	private final int[] slotLength = { -1, 1, 1, 4 };// in bytes

	/**
	 * XING MP3 Header ?
	 */
	private XingVBRHeader xingHead = null;
	/**
	 * The MP3 File
	 */
	private File mp3 = null;
	/**
	 * The Version
	 */
	private int version;
	/**
	 * The Layer
	 */
	private int layer;
	/**
	 * The Bitrate
	 */
	private int bitRate;
	/**
	 * The Samplerate
	 */
	private int sampleRate;
	/**
	 * The Channelode
	 */
	private int channelMode;
	/**
	 * Copyright Flag
	 */
	private boolean copyrighted;
	/**
	 * CRC Flag
	 */
	private boolean crced;
	/**
	 * Original Flag
	 */
	private boolean original;
	/**
	 * Private Bit
	 */
	private boolean privateBit;
	/**
	 * Emphais
	 */
	private int emphasis;
	/**
	 * Header Location in File?
	 */
	private long location;
	/**
	 * Framelength
	 */
	@SuppressWarnings("unused")
	private int frameLength;
	/**
	 * Padding Flag
	 */
	private boolean padding;

	/**
	 * Create an MPEGAudioFrameHeader from the file specified. Upon creation
	 * information will be read in from the first frame header the object
	 * encounters in the file.
	 *
	 * @param mp3
	 *           the file to read from
	 * @exception NoMPEGFramesException
	 *               if the file is not a valid mpeg
	 * @exception FileNotFoundException
	 *               if an error occurs
	 * @exception IOException
	 *               if an error occurs
	 * @exception CorruptHeaderException
	 *               if an error occurs
	 */
	public MPEGAudioFrameHeader(final File mp3)
			throws NoMPEGFramesException, FileNotFoundException, IOException,
			CorruptHeaderException {
		this(mp3, 0);
	}

	/**
	 * Create an MPEGAudioFrameHeader from the file specified. Upon creation
	 * information will be read in from the first frame header the object
	 * encounters in the file. The offset tells the object where to start
	 * searching for an MPEG frame. If you know the size of an id3v2 tag attached
	 * to the file and pass it to this ctor, it will take less time to find the
	 * frame.
	 *
	 * @param mp3
	 *           the file to read from
	 * @param offset
	 *           the offset to start searching from
	 * @exception NoMPEGFramesException
	 *               if the file is not a valid mpeg
	 * @exception FileNotFoundException
	 *               if an error occurs
	 * @exception IOException
	 *               if an error occurs
	 * @exception CorruptHeaderException
	 *               if an error occurs
	 */
	public MPEGAudioFrameHeader(final File mp3, final int offset)
			throws NoMPEGFramesException, FileNotFoundException, IOException,
			CorruptHeaderException {

		this.mp3 = mp3;

		version = -1;
		layer = -1;
		bitRate = -1;
		sampleRate = -1;
		channelMode = -1;
		copyrighted = false;
		crced = false;
		original = false;
		emphasis = -1;
		location = -1;
		padding = false;

		RandomAccessFile in = null;

		try {
			in = new RandomAccessFile(mp3, "r");
			location = findOffset(in, offset, MAX_TRIES);

			if (location != -1) {
				readHeader(in, location);
				xingHead = new XingVBRHeader(in, location, layer, version,
						sampleRate, channelMode);
			} else {
				throw new NoMPEGFramesException();
			}
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	/**
	 * Based on the bitrate index found in the header, try to find and set the
	 * bitrate from the table.
	 *
	 * @param bitrateIndex
	 *           the bitrate index read from the header
	 */
	private void findBitRate(final int bitrateIndex) {
		int ind = -1;

		if (version == MPEG_V_1) {
			if (layer == MPEG_L_1) {
				ind = 0;
			} else if (layer == MPEG_L_2) {
				ind = 1;
			} else if (layer == MPEG_L_3) {
				ind = 2;
			}
		} else if ((version == MPEG_V_2) || (version == MPEG_V_25)) {
			if (layer == MPEG_L_1) {
				ind = 3;
			} else if ((layer == MPEG_L_2) || (layer == MPEG_L_3)) {
				ind = 4;
			}
		}

		if ((ind != -1) && (bitrateIndex >= 0) && (bitrateIndex <= 15)) {
			bitRate = bitrateTable[bitrateIndex][ind];
		}
	}

	/**
	 * Attempts to find the location of the first valid mpeg frame in the file
	 * Skips id3 tag if present then loops through until it finds a valid frame,
	 * and then it computes the length of the frame and tests to see if there is
	 * another frame where it should be. To speed things up when scanning big
	 * non-mp3 files, there is a limit on the number of attempts and on how much
	 * data to scan.
	 *
	 * @param in
	 *           the file to scan
	 * @param offset
	 *           where to start looking
	 * @param maxTries
	 *           the maximum number of attempts to find a valid frame sequence
	 * @return the location of the first valid mpeg frame or -1 if it fails
	 * @exception CorruptHeaderException
	 *               if an error occurs
	 * @exception IOException
	 *               if an error occurs
	 */
	private long findOffset(final RandomAccessFile in, final long offset, final int maxTries)
			throws CorruptHeaderException, IOException {

		byte test;
		long loc = -1;
		in.seek(offset);

		long new_offset = offset;
		if (offset == 0) {
			// new_offset = skipV2Tag();

			final byte[] b = { in.readByte(), in.readByte(), in.readByte() };
			final String s = new String(b);
			if (s.equals("ID3")) {
				in.seek(in.getFilePointer() + 3);
				final byte[] b2 = { in.readByte(), in.readByte(), in.readByte(), in.readByte() };
				new_offset = BinaryParser.convertToSynchsafeInt(b2);
			}
		}

		in.seek(new_offset);

		while (loc == -1) {
			test = in.readByte();

			if (BinaryParser.matchPattern(test, "11111111")) {
				test = in.readByte();

				// Frame sync and layer version tests
				if (BinaryParser.matchPattern(test, "111xxxxx")
						&& !BinaryParser.matchPattern(test, "xxxxx00x")) {
					test = in.readByte();
					// Bitrate and frequency tests
					// if(!BinaryParser.matchPattern(test, "1111xxxx")
					// && !BinaryParser.matchPattern( test, "xxxx11xx" )
					// && !BinaryParser.matchPattern( test, "0000xxxx" )) {
					if (!BinaryParser.matchPattern(test, "1111xxxx")
							&& !BinaryParser.matchPattern(test, "xxxx11xx")) {
						test = in.readByte();
						// Emphasis test
						if (!BinaryParser.matchPattern(test, "xxxxxx10")) {
							loc = in.getFilePointer() - 4;
						} else {
							in.seek(in.getFilePointer() - 3);
						}
					} else {
						in.seek(in.getFilePointer() - 2);
					}
				} else {
					in.seek(in.getFilePointer() - 1);
				}
			}
		}

		// For using the maxTries variable ; but is it really useful ?
		/*
		 * while( loc == -1 && curTry < maxTries) { test = in.readByte(); if(
		 * BinaryParser.matchPattern( test, "11111111" ) ) { test = in.readByte();
		 * / Frame sync and layer version tests if( BinaryParser.matchPattern(
		 * test, "111xxxxx" ) && !BinaryParser.matchPattern( test, "xxxxx00x" ) )
		 * { test = in.readByte(); / Bitrate and frequency tests
		 * if(!BinaryParser.matchPattern(test, "1111xxxx") &&
		 * !BinaryParser.matchPattern( test, "xxxx11xx" )) { test = in.readByte();
		 * / Emphasis test if(!BinaryParser.matchPattern( test, "xxxxxx10" )) {
		 * loc = in.getFilePointer() - 4; } else { curTry++;
		 * in.seek(in.getFilePointer() - 3); } } else { curTry++;
		 * in.seek(in.getFilePointer() - 2); } } else { curTry++;
		 * in.seek(in.getFilePointer() - 1); } } }
		 */
		return loc;
	}

	/**
	 * Based on the sample rate index found in the header, attempt to lookup and
	 * set the sample rate from the table.
	 *
	 * @param sampleIndex
	 *           the sample rate index read from the header
	 */
	private void findSampleRate(final int sampleIndex) {
		int ind = -1;

		switch (version) {
		case MPEG_V_1:
			ind = 0;
			break;
		case MPEG_V_2:
			ind = 1;
			break;
		case MPEG_V_25:
			ind = 2;
		}

		if ((ind != -1) && (sampleIndex >= 0) && (sampleIndex <= 3)) {
			sampleRate = sampleTable[sampleIndex][ind];
		}
	}

	/**
	 * Returns the bitrate of this mpeg. If it is a VBR file the average bitrate
	 * is returned.
	 *
	 * @return the bitrate of this mpeg (in kbps)
	 */
	public int getBitRate() {
		int br = 0;

		if (xingHead.headerExists()) {
			br = xingHead.getAvgBitrate();
		} else {
			br = bitRate;
		}

		return br;
	}

	/**
	 * Return the channel mode of the mpeg in string form. Ex: Joint Stereo
	 * (STEREO)
	 *
	 * @return the channel mode of the mpeg
	 */
	public String getChannelMode() {
		String str = null;

		if ((channelMode >= 0) && (channelMode < channelLabels.length)) {
			str = channelLabels[channelMode];
		}

		return str;
	}

	/**
	 * Returns the emphasis. I don't know what this means, it just does it...
	 *
	 * @return the emphasis
	 */
	public String getEmphasis() {
		String str = null;

		if ((emphasis >= 0) && (emphasis < emphasisLabels.length)) {
			str = emphasisLabels[emphasis];
		}

		return str;
	}

	/**
	 * Computes the length of the frame found. This is not necessarily constant
	 * for all frames.
	 *
	 * @return the length of the frame found
	 */
	public int getFrameLength() {
		int length = -1;
		int padAmount = 0;

		if (padding) {
			padAmount = slotLength[layer];
		}

		if (layer == MPEG_L_1) {
			length = (((12 * (bitRate * 1000)) / sampleRate) + padAmount) * 4;
		} else {
			length = ((144 * (bitRate * 1000)) / sampleRate) + padAmount;
		}

		return length;
	}

	/**
	 * Return the layer description of the mpeg in string form. Ex: Layer III
	 *
	 * @return the layer description of the mpeg
	 */
	public String getLayer() {
		String str = null;

		if ((layer >= 0) && (layer < layerLabels.length)) {
			str = layerLabels[layer];
		}

		return str;
	}

	/**
	 * Returns the offset at which the first mpeg frame was found in the file.
	 *
	 * @return the offset of the mpeg data
	 */
	public long getLocation() {
		return location;
	}

	/**
	 * Returns the number of frames in this mpeg file. This does not subtract the
	 * size of and id3v1 tag if present so it is not deadly accurate.
	 *
	 * @return the number of frames in this mpeg file
	 */
	public int getNumFrames() {
		int num = 0;

		if (xingHead.headerExists()) {
			num = xingHead.getNumFrames();
		} else {
			num = ((int) (mp3.length() - location)) / getFrameLength();
		}

		return num;
	}

	/**
	 * Returns the sample rate of the mpeg in Hz
	 *
	 * @return the sample rate of the mpeg in Hz
	 */
	public int getSampleRate() {
		return sampleRate;
	}

	/**
	 * If this is a VBR file, return an accurate playing time of this mpeg. If
	 * this is not a VBR file -1 is returned.
	 *
	 * @return an accurate playing time of this mpeg
	 */
	public int getVBRPlayingTime() {
		return xingHead.getPlayingTime();
	}

	/**
	 * Return the version of the mpeg in string form. Ex: MPEG Version 1.0
	 *
	 * @return the version of the mpeg
	 */
	public String getVersion() {
		String str = null;

		if ((version >= 0) && (version < versionLabels.length)) {
			str = versionLabels[version];
		}

		return str;
	}

	/**
	 * Returns true if the mpeg frames are padded in this file.
	 *
	 * @return true if the mpeg frames are padded in this file
	 */
	public boolean hasPadding() {
		return padding;
	}

	/**
	 * Returns true if the audio is copyrighted
	 *
	 * @return true if the audio is copyrighted
	 */
	public boolean isCopyrighted() {
		return copyrighted;
	}

	/**
	 * Returns true if the file passed to the constructor is an mp3 (MPEG layer
	 * III).
	 *
	 * @return true if the file is an mp3
	 */
	public boolean isMP3() {
		return (layer == MPEG_L_3);
	}

	/**
	 * Returns true if this is the original media
	 *
	 * @return true if this is the original media
	 */
	public boolean isOriginal() {
		return original;
	}

	/**
	 * Returns true if this mpeg is protected by CRC
	 *
	 * @return true if this mpeg is protected by CRC
	 */
	public boolean isProtected() {
		return crced;
	}

	/**
	 * Returns true if this mpeg is encoded in VBR
	 *
	 * @return if VBR is present
	 */
	public boolean isVBR() {
		return xingHead.headerExists();
	}

	/**
	 * Returns true if the private bit is set.
	 *
	 * @return true if the private bit is set
	 */
	public boolean privateBitSet() {
		return privateBit;
	}

	/**
	 * Finds the ID3v2 tag (if any) of the MP3 file and returns the offset that
	 * represents its end. Used by findOffset() in order to skip the ID3v2 tag
	 * and not getting messed up when trying to find the frame header.
	 *
	 * @param raf
	 *           Description of the Parameter
	 * @param location
	 *           Description of the Parameter
	 * @exception IOException
	 *               if an error occurs while reading the file.
	 * @exception CorruptHeaderException
	 *               Description of the Exception
	 */
	/*
	 * private long skipV2Tag() throws IOException { RandomAccessFile in = new
	 * RandomAccessFile(mp3, "r"); long endOfTag = 0; byte[] b = {in.readByte(),
	 * in.readByte(), in.readByte()}; String s = new String(b);
	 * if(s.equals("ID3")) { in.seek(in.getFilePointer()+3); byte[] b2 =
	 * {in.readByte(),in.readByte(),in.readByte(),in.readByte()}; endOfTag =
	 * BinaryParser.convertToSynchsafeInt(b2); } return endOfTag; }
	 */
	/**
	 * Read in all the information found in the mpeg header.
	 *
	 * @param raf
	 *           the open file to find the frame in
	 * @param location
	 *           the location of the header (found by findFrame)
	 * @exception CorruptHeaderException
	 *               if an error occurs
	 * @exception IOException
	 *               if an error occurs
	 */
	private void readHeader(final RandomAccessFile raf, final long location)
			throws IOException, CorruptHeaderException {

		final byte[] head = new byte[HEADER_SIZE];
		raf.seek(location);

		if (raf.read(head) != HEADER_SIZE) {
			throw new CorruptHeaderException("Error reading MPEG frame header.");
		}

		version = BinaryParser.convertToDecimal(head[1], 3, 4);
		layer = BinaryParser.convertToDecimal(head[1], 1, 2);
		findBitRate(BinaryParser.convertToDecimal(head[2], 4, 7));
		findSampleRate(BinaryParser.convertToDecimal(head[2], 2, 3));
		padding = BinaryParser.bitSet(head[2], 1);
		privateBit = BinaryParser.bitSet(head[2], 0);
		channelMode = BinaryParser.convertToDecimal(head[3], 6, 7);
		copyrighted = BinaryParser.bitSet(head[3], 3);
		crced = !BinaryParser.bitSet(head[1], 0);
		original = BinaryParser.bitSet(head[3], 2);
		emphasis = BinaryParser.convertToDecimal(head[3], 0, 1);
	}

	/**
	 * Return a string representation of this object. Includes all information
	 * read in.
	 *
	 * @return a string representation of this object
	 */
	@Override
	public String toString() {
		String str = new String();

		str = getVersion() + " " + getLayer()
				+ "\nBitRate:\t\t\t" + getBitRate()
				+ "kbps\nSampleRate:\t\t\t" + getSampleRate()
				+ "Hz\nChannelMode:\t\t\t" + getChannelMode()
				+ "\nCopyrighted:\t\t\t" + isCopyrighted()
				+ "\nOriginal:\t\t\t" + isOriginal()
				+ "\nCRC:\t\t\t\t" + isProtected()
				+ "\nEmphasis:\t\t\t" + getEmphasis()
				+ "\nOffset:\t\t\t\t" + getLocation()
				+ "\nPrivateBit:\t\t\t" + privateBitSet()
				+ "\nPadding:\t\t\t" + hasPadding()
				+ "\nFrameLength:\t\t\t" + getFrameLength()
				+ "\nVBR:\t\t\t\t" + isVBR()
				+ "\nNumFrames:\t\t\t\t" + getNumFrames();

		if (isVBR()) {
			str += "\n" + xingHead.toString();
		}

		return str;
	}

}
