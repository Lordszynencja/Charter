package helliker.id3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/*
   Copyright (C) 2001 Jonathan Hilliker
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
 * If the id3v2 tag has an extended header, this class will read/write the
 * information contained within it. NOTE: this class is untested and has no
 * mutators. In other words, this class will only be used if an mp3 already has
 * an extended header (at this point at least). <br/>
 *
 * <dl>
 * <dt><b>Version History:</b></dt>
 * <dt>1.4.1 - <small>2002.1023 by gruni</small></dt>
 * <dd>-Made sourcecode compliant to the Sun Coding Conventions</dd>
 * <dt>1.4 - <small>2002.0125 by helliker</small></dt>
 * <dd>-Decodes/encodes length field as synchsafe int.</dd>
 * <dt>1.3 - <small>2001.1129 by helliker</small></dt>
 * <dd>-Fixed file handle leaks</dd>
 * <dt>1.2 - <small>2001.1019 by helliker<small></dt>
 * <dd>-All set for release.</dd>
 * </dl>
 *
 *
 * @author Jonathan Hilliker
 * @version 1.4.1
 */

public class ID3v2ExtendedHeader {

	/**
	 * The position in the File
	 */
	private final int EXT_HEAD_LOCATION = 10;
	/**
	 * Minimum Size of Ext Header
	 */
	private final int MIN_SIZE = 6;
	/**
	 * Size of CRC
	 */
	private final int CRC_SIZE = 5;
	/**
	 * ???
	 */
	private final int[] MAX_TAG_FRAMES_TABLE = { 128, 64, 32, 32 };
	/**
	 * ???
	 */
	private final int[] MAX_TAG_SIZE_TABLE = { 8000000, 1024000, 320000, 32000 };
	/**
	 * ???
	 */
	private final int[] MAX_TEXT_SIZE_TABLE = { -1, 1024, 128, 30 };

	/**
	 * the File
	 */
	@SuppressWarnings("unused")
	private File mp3 = null;
	/**
	 * the Size (of what)?
	 */
	private int size;
	/**
	 * ???
	 */
	private int numFlagBytes;
	/**
	 * ???
	 */
	private boolean update;
	/**
	 * ???
	 */
	private boolean crced;
	/**
	 * ???
	 */
	private final byte[] crc;
	/**
	 * ???
	 */
	@SuppressWarnings("unused")
	private final int maxFrames;
	/**
	 * ???
	 */
	private int maxTagSize;
	/**
	 * ???
	 */
	private boolean textEncode;
	/**
	 * ???
	 */
	private int maxTextSize;
	/**
	 * ???
	 */
	private boolean imageEncode;
	/**
	 * ???
	 */
	private int imageRestrict;

	/**
	 * Create an extended header object from the file passed. Information in the
	 * file's extended header will be read and stored.
	 *
	 * @param mp3
	 *           the file to read/write to
	 * @exception FileNotFoundException
	 *               if an error occurs
	 * @exception IOException
	 *               if an error occurs
	 * @exception ID3v2FormatException
	 *               if an error occurs
	 */
	public ID3v2ExtendedHeader(final File mp3)
			throws FileNotFoundException, IOException, ID3v2FormatException {

		this.mp3 = mp3;

		size = 0;
		numFlagBytes = 0;
		update = false;
		crced = false;
		crc = new byte[CRC_SIZE];
		maxFrames = -1;
		maxTagSize = -1;
		textEncode = false;
		maxTextSize = -1;
		imageEncode = false;
		imageRestrict = -1;

		RandomAccessFile in = null;

		try {
			in = new RandomAccessFile(mp3, "r");
			readExtendedHeader(in);
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	/**
	 * Return an array of bytes representing this extended header in the standard
	 * format to be written to a file.
	 *
	 * @return a binary represenation of this extended header
	 */
	public byte[] getBytes() {
		final byte[] b = new byte[size];
		int bytesCopied = 0;

		System.arraycopy(BinaryParser.convertToSynchsafeBytes(size), 0, b,
				bytesCopied, 4);
		bytesCopied += 4;
		b[bytesCopied++] = (byte) numFlagBytes;
		System.arraycopy(getFlagBytes(), 0, b, bytesCopied, numFlagBytes);
		bytesCopied += numFlagBytes;

		return b;
	}

	/**
	 * If there is crc data in the extended header, then the attached 5 byte crc
	 * will be returned. An empty array will be returned if this has not been
	 * set.
	 *
	 * @return the attached crc data if there is any
	 */
	public byte[] getCRC() {
		return crc;
	}

	/**
	 * Returns true if CRC information is provided for this tag
	 *
	 * @return true if CRC information is provided for this tag
	 */
	public boolean getCRCed() {
		return crced;
	}

	/**
	 * A helper function for the getBytes method that returns a byte array
	 * representing the extended flags field of the extended header.
	 *
	 * @return the extended flags field of the extended header
	 */
	private byte[] getFlagBytes() {
		final byte[] b = new byte[numFlagBytes];
		int bytesCopied = 1;
		b[0] = 0;

		if (update) {
			b[0] = BinaryParser.setBit(b[0], 7);
			b[bytesCopied++] = 0;
		}
		if (crced) {
			b[0] = BinaryParser.setBit(b[0], 6);
			b[bytesCopied++] = (byte) crc.length;
			System.arraycopy(crc, 0, b, bytesCopied, crc.length);
			bytesCopied += crc.length;
		}
		if ((maxTagSize != -1) || textEncode || (maxTextSize != -1)
				|| imageEncode || (imageRestrict != -1)) {

			b[0] = BinaryParser.setBit(b[0], 5);
			b[bytesCopied++] = 0x01;
			byte restrict = 0;
			if (maxTagSize != -1) {
				if (BinaryParser.bitSet((byte) maxTagSize, 0)) {
					restrict = BinaryParser.setBit(restrict, 6);
				}
				if (BinaryParser.bitSet((byte) maxTagSize, 1)) {
					restrict = BinaryParser.setBit(restrict, 7);
				}
			}
			if (textEncode) {
				restrict = BinaryParser.setBit(restrict, 5);
			}
			if (maxTextSize != -1) {
				if (BinaryParser.bitSet((byte) maxTextSize, 0)) {
					restrict = BinaryParser.setBit(restrict, 3);
				}
				if (BinaryParser.bitSet((byte) maxTextSize, 1)) {
					restrict = BinaryParser.setBit(restrict, 4);
				}
			}
			if (imageEncode) {
				restrict = BinaryParser.setBit(restrict, 2);
			}
			if (imageRestrict != -1) {
				if (BinaryParser.bitSet((byte) imageRestrict, 0)) {
					restrict = BinaryParser.setBit(restrict, 0);
				}
				if (BinaryParser.bitSet((byte) imageRestrict, 1)) {
					restrict = BinaryParser.setBit(restrict, 1);
				}
			}

			b[bytesCopied++] = restrict;
		}

		return b;
	}

	/**
	 * Returns true if the image encode flag is set
	 *
	 * @return true if the image encode flag is set
	 */
	public boolean getImageEncode() {
		return imageEncode;
	}

	/**
	 * Returns the value of the image restriction field or -1 if not set
	 *
	 * @return the value of the image restriction field or -1 if not set
	 */
	public int getImageRestriction() {
		return imageRestrict;
	}

	/**
	 * Returns the maximum number of frames if set. If unset, returns -1
	 *
	 * @return the maximum number of frames or -1 if unset
	 */
	public int getMaxFrames() {
		int retval = -1;

		if ((maxTagSize >= 0) && (maxTagSize < MAX_TAG_FRAMES_TABLE.length)) {
			retval = MAX_TAG_FRAMES_TABLE[maxTagSize];
		}

		return retval;
	}

	/**
	 * Returns the maximum tag size or -1 if unset
	 *
	 * @return the maximum tag size or -1 if unset
	 */
	public int getMaxTagSize() {
		int retval = -1;

		if ((maxTagSize >= 0) && (maxTagSize < MAX_TAG_SIZE_TABLE.length)) {
			retval = MAX_TAG_SIZE_TABLE[maxTagSize];
		}

		return retval;
	}

	/**
	 * Returns the maximum length of a string if set or -1
	 *
	 * @return the maximum length of a string if set or -1
	 */
	public int getMaxTextSize() {
		int retval = -1;

		if ((maxTextSize >= 0) && (maxTextSize < MAX_TEXT_SIZE_TABLE.length)) {
			retval = MAX_TEXT_SIZE_TABLE[maxTextSize];
		}

		return retval;
	}

	/**
	 * Returns the number of extended flag bytes
	 *
	 * @return the number of extended flag bytes
	 */
	public int getNumFlagBytes() {
		return numFlagBytes;
	}

	/**
	 * Returns the size of the extended header
	 *
	 * @return the size of the extended header
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Returns true if the text encode flag is set
	 *
	 * @return true if the text encode flag is set
	 */
	public boolean getTextEncode() {
		return textEncode;
	}

	/**
	 * Returns true if this tag is an update of a previous tag
	 *
	 * @return true if this tag is an update of a previous tag
	 */
	public boolean getUpdate() {
		return update;
	}

	/**
	 * Parse the extended header flag bytes
	 *
	 * @param flags
	 *           the array of extended flags
	 * @exception ID3v2FormatException
	 *               if an error occurs
	 */
	private void parseFlags(final byte[] flags) throws ID3v2FormatException {
		int bytesRead = 1;

		if (BinaryParser.bitSet(flags[0], 6)) {
			update = true;
			bytesRead += 1;
		}
		if (BinaryParser.bitSet(flags[0], 5)) {
			crced = true;
			bytesRead += 1;
			for (int i = 0; i < crc.length; i++) {
				crc[i] = flags[bytesRead++];
			}
		}
		if (BinaryParser.bitSet(flags[0], 4)) {
			bytesRead += 1;
			maxTagSize = BinaryParser.convertToDecimal(
					flags[bytesRead], 6, 7);
			textEncode = BinaryParser.bitSet(flags[bytesRead], 5);
			maxTextSize = BinaryParser.convertToDecimal(
					flags[bytesRead], 3, 4);
			imageEncode = BinaryParser.bitSet(flags[bytesRead], 2);
			imageRestrict = BinaryParser.convertToDecimal(
					flags[bytesRead], 0, 1);
			bytesRead += 1;
		}

		if (bytesRead != numFlagBytes) {
			throw new ID3v2FormatException("The number of found flag bytes "
					+ "in the extended header is not "
					+ "equal to the number specified " + "in the extended header.");
		}
	}

	/**
	 * Read the information in the file's extended header
	 *
	 * @param raf
	 *           the open file to read from
	 * @exception FileNotFoundException
	 *               if an error occurs
	 * @exception IOException
	 *               if an error occurs
	 * @exception ID3v2FormatException
	 *               if an error occurs
	 */
	private void readExtendedHeader(final RandomAccessFile raf)
			throws FileNotFoundException, IOException, ID3v2FormatException {

		raf.seek(EXT_HEAD_LOCATION);

		byte[] buf = new byte[4];
		if (raf.read(buf) != buf.length) {
			throw new IOException("Error reading extended header:size");
		}

		size = BinaryParser.convertToSynchsafeInt(buf);
		if (size < MIN_SIZE) {
			throw new ID3v2FormatException("The extended header size data"
					+ " is less than the minimum required size.");
		}

		buf = new byte[1];
		if (raf.read(buf) != buf.length) {
			throw new IOException("Error reading extended header:numflags");
		}

		numFlagBytes = buf[0];
		buf = new byte[numFlagBytes + 1];

		if (raf.read(buf) != buf.length) {
			throw new IOException("Error reading extended header:flags");
		}

		parseFlags(buf);
	}

	/**
	 * Returns a string representation of this object that contains all
	 * information within.
	 *
	 * @return a string representation of this object
	 */
	@Override
	public String toString() {
		return "ExtendedSize:\t\t\t" + getSize() + " bytes"
				+ "\nNumFlagBytes:\t\t\t" + getNumFlagBytes()
				+ "\nUpdated:\t\t\t" + getUpdate()
				+ "\nCRC:\t\t\t\t" + getCRCed()
				+ "\nMaxFrames:\t\t\t" + getMaxFrames()
				+ "\nMaxTagSize:\t\t\t" + getMaxTagSize()
				+ "\nTextEncoded:\t\t\t" + getTextEncode()
				+ "\nMaxTextSize:\t\t\t" + getMaxTextSize()
				+ "\nImageEncoded:\t\t\t" + getImageEncode()
				+ "\nImageRestriction:\t\t" + getImageRestriction();
	}

}
