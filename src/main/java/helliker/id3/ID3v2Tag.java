package helliker.id3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;

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
 * Description: This class reads and writes id3v2 tags from a file.<br />
 *
 * <dl>
 * <dt><b>Version History</b></dt>
 * <dt>1.11.1 - <small>2002.1023 by gruni</small></dt>
 * <dd>-Made Sourcecode compliant to the Sun CodingConventions</dd>
 * <dt>1.11 - <small>2002.0127 by helliker</small></dt>
 * <dd>-The getBytes method is public to adhere to the ID3Tag interface.</dd>
 *
 * <dt>1.10 - <small>2002.0125 by helliker</small></dt>
 * <dd>-Uses RandomAccessFile.readInt method to read ints.</dd>
 * <dt>1.9 - <small>2002.0125 by helliker</small></dt>
 * <dd>-Writes tag correctly if the 1st mpeg frame is corrupt.</dd>
 * <dd>-Fixed infinite loop in padding calculation.</dd>
 * <dt>1.8 - <small>2002.0124 by helliker</small></dt>
 * <dd>-Throws an exception with an invalid frame size.</dd>
 * <dt>1.7 - <small>2002.0113 by helliker</small></dt>
 * <dd>-Implements new ID3Tag interface.</dd>
 * <dd>-Optimized new padding calculation for writing.</dd>
 * <dt>1.6 - <small>2001.1204 by helliker</small></dd>
 * <dd>-Major revisions to size and padding calculations.</dd>
 * <dd>-Fixes to write and remove methods.</dd>
 * <dt>1.5 - <small>2001.1129 by helliker</small></dt>
 * <dd>-Fixed file handle leaks</dd>
 * <dt>1.4 - <small>2001.1110 by helliker</small></dt>
 * <dd>-Removed the getPaddingBytes method because it was not needed.</dd>
 *
 * <dt>1.3 - <small>2001.1024 by helliker</small></dt>
 * <dd>-The padding size is updated before writing a tag, not during.</dd>
 *
 * <dd>-Created a method to update the padding size.</dd>
 * <dt>1.2 - <small>2001.1019 by helliker</small></dt>
 * <dd>-All set for release.</dd>
 * </dl>
 *
 *
 * @author Jonathan Hilliker
 * @version 1.11.1
 */

public class ID3v2Tag implements ID3Tag {

	/**
	 * encryption type
	 */
	private final String ENC_TYPE = "ISO-8859-1";
	// mp3Ext writes padding (wrongfully) as "MP3ext V...."
	/**
	 * MP3 Ext badid
	 */
	private final String MP3EXT_BADID = "MP3e";
	// Used to calculate padding change
	/**
	 * Padding Change ???
	 */
	private final int NEWTAG_LIMIT = 16000;

	/**
	 * The File ???
	 */
	private File mp3 = null;
	/**
	 * ???
	 */
	private ID3v2Header head = null;
	/**
	 * ???
	 */
	private ID3v2ExtendedHeader ext_head = null;
	/**
	 * ???
	 */
	private ID3v2Frames frames = null;
	/**
	 * ???
	 */
	private ID3v2Footer foot = null;
	/**
	 * ???
	 */
	private int padding;
	/**
	 * ???
	 */
	private int writtenTagSize;
	/**
	 * ???
	 */
	private int writtenPadding;
	/**
	 * ???
	 */
	private boolean exists;
	/**
	 * ???
	 */
	private final long mpegOffset;

	/**
	 * Create an id3v2 tag bound to the file provided as a parameter. If a tag
	 * exists in the file already, then all the information in the tag will be
	 * extracted. If a tag doesn't exist, then this is the file that will be written
	 * to when the writeTag method is called.
	 *
	 * @param mp3        the file to write/read the the tag information to/from
	 * @param mpegOffset the byte offset where the mpeg frames begin
	 * @exception FileNotFoundException if an error occurs
	 * @exception IOException           if an error occurs
	 * @exception ID3v2FormatException  if an exisiting id3v2 tag isn't correct
	 */
	public ID3v2Tag(final File mp3, final long mpegOffset)
			throws FileNotFoundException, IOException, ID3v2FormatException {

		this.mp3 = mp3;
		this.mpegOffset = mpegOffset;

		frames = new ID3v2Frames();
		head = new ID3v2Header(mp3);
		padding = 0;
		exists = head.headerExists();

		if (exists) {
			if (head.getExtendedHeader()) {
				ext_head = new ID3v2ExtendedHeader(mp3);
			}
			if (head.getFooter()) {
				foot = new ID3v2Footer(mp3, head.getTagSize() + head.getHeaderSize());
			}

			RandomAccessFile in = null;

			try {
				in = new RandomAccessFile(mp3, "r");

				// For now only support id3v2.3.0 or greater
				if (head.getMajorVersion() >= 3) {
					parseFrames(in);
				}
			} finally {
				if (in != null) {
					in.close();
				}
			}

			writtenTagSize = head.getTagSize();
			writtenPadding = padding;

			// Check to validate tag size taken out because MusicMatch
			// has some bugs that causes the check to fail
		}
	}

	/**
	 * Copies information from the ID3Tag parameter and inserts it into this tag.
	 * Previous data will be overwritten. [NOT IMPLEMENTED]
	 *
	 * @param tag the tag to copy from
	 */
	@Override
	public void copyFrom(final ID3Tag tag) {
		// Not implemented yet
	}

	/**
	 * Return a binary representation of this object to be written to a file. This
	 * is in the format of the id3v2 specifications. This includes the header,
	 * extended header (if it exists), the frames, padding (if it exists), and a
	 * footer (if it exists).
	 *
	 * @return a binary representation of this id3v2 tag
	 */
	@Override
	public byte[] getBytes() {
		final byte[] b = new byte[getTotalSize()];
		int bytesCopied = 0;
		int length = 0;
		padding = getUpdatedPadding();
		length = head.getHeaderSize();
		System.arraycopy(head.getBytes(), 0, b, bytesCopied, length);
		bytesCopied += length;

		if (head.getExtendedHeader()) {
			length = ext_head.getSize();
			System.arraycopy(ext_head.getBytes(), 0, b, bytesCopied, length);
			bytesCopied += length;
		}

		length = frames.getLength();
		System.arraycopy(frames.getBytes(), 0, b, bytesCopied, length);
		bytesCopied += length;

		// Bytes should all be zero's by default
		if (padding > 0) {
			System.arraycopy(new byte[padding], 0, b, bytesCopied, padding);
			bytesCopied += padding;
		}

		if (head.getFooter()) {
			length = foot.getFooterSize();
			System.arraycopy(foot.getBytes(), 0, b, bytesCopied, length);
			bytesCopied += length;
		}

		return b;
	}

	/**
	 * Returns the data found in the frame specified by the id. If the frame doesn't
	 * exist, then a zero length array is returned.
	 *
	 * @param id the id of the frame to get the data from
	 * @return the data found in the frame
	 */
	public byte[] getFrameData(final String id) {
		byte[] b = new byte[0];

		if (frames.containsKey(id)) {
			b = ((ID3v2Frame) frames.get(id)).getFrameData();
		}

		return b;
	}

	/**
	 * Returns the textual information contained in the frame specified by the id.
	 * Not every type of frame has textual information. If an id is specified that
	 * will not work, the empty string is returned.
	 *
	 * @param id the id of the frame to get text from
	 * @return the text information contained in the frame
	 * @exception ID3v2FormatException if an error is encountered parsing data
	 */
	public String getFrameDataString(final String id) throws ID3v2FormatException {
		String str = new String();

		if (frames.containsKey(id)) {
			str = ((ID3v2Frame) frames.get(id)).getDataString();
		}

		return str;
	}

	/**
	 * Returns the current number of padding bytes in this id3v2 tag.
	 *
	 * @return the current number of padding bytes in this id3v2 tag
	 */
	public int getPadding() {
		return padding;
	}

	/**
	 * Returns the size of this id3v2 tag. This includes only the frames, extended
	 * header, and padding. For the size of the entire tag including the header and
	 * footer, use getTotalSize method.
	 *
	 * @return the size (in bytes) of the id3v2 frames, extended header, footer
	 */
	public int getSize() {
		int size = frames.getLength();
		int sizeDiff = 0;

		if (head.getExtendedHeader()) {
			size += ext_head.getSize();
		}

		size += padding;
		sizeDiff = size - writtenTagSize;

		if ((padding == writtenPadding) && (sizeDiff != 0)) {
			if ((sizeDiff < 0) || (sizeDiff <= padding)) {
				size = head.getTagSize();
			}
		}

		return size;
	}

	/**
	 * Returns the actual size of the tag when written. Includes the header,
	 * extended header, frames, padding, and footer.
	 *
	 * @return the size (in bytes) of the entire id3v2 tag
	 */
	public int getTotalSize() {
		int size = getSize();

		size += head.getHeaderSize();

		if (head.getFooter()) {
			size += foot.getFooterSize();
		}

		return size;
	}

	/**
	 * Determines the new amount of padding to use. If the user has not changed the
	 * amount of padding then existing padding will be overwritten instead of
	 * increasing the size of the file. That is only if there is a sufficient amount
	 * of padding for the updated tag.
	 *
	 * @return the new amount of padding
	 */
	private int getUpdatedPadding() {
		int pad = padding;
		int size = frames.getLength();
		int sizeDiff = 0;

		if (head.getExtendedHeader()) {
			size += ext_head.getSize();
		}

		size += padding;
		sizeDiff = size - writtenTagSize;

		if ((padding == writtenPadding) && (sizeDiff != 0) && exists) {
			if (sizeDiff < 0) {
				pad += Math.abs(sizeDiff);
			} else if (sizeDiff <= padding) {
				pad -= sizeDiff;
			} else {
				// As the id3 team recommends, double the size of the tag
				// if it needs to get bigger
				int newTagSize = 2 * writtenTagSize;
				while (newTagSize < size) {
					newTagSize += writtenTagSize;
				}

				if (newTagSize <= NEWTAG_LIMIT) {
					pad = newTagSize - size;
				} else {
					// Gee if it's over the limit this tag is pretty big,
					// so screw padding altogether
					pad = 0;
				}
			}
		}

		return pad;
	}

	/**
	 * Read the frames from the file and create ID3v2Frame objects from the data
	 * found.
	 *
	 * @param raf the open file to read from
	 * @exception IOException          if an error occurs
	 * @exception ID3v2FormatException if an error occurs
	 */
	@SuppressWarnings("unchecked")
	private void parseFrames(final RandomAccessFile raf) throws IOException, ID3v2FormatException {

		int offset = head.getHeaderSize();
		// Actually length of frames + padding
		int framesLength = head.getTagSize();
		int bytesRead = 0;
		int curLength = 0;
		ID3v2Frame frame = null;
		String id = null;
		byte[] buf;
		byte[] flags;
		boolean done = false;

		if (head.getExtendedHeader()) {
			framesLength -= ext_head.getSize();
			offset += ext_head.getSize();
		}

		raf.seek(offset);

		while ((bytesRead < framesLength) && !done) {
			buf = new byte[4];
			bytesRead += raf.read(buf);
			id = new String(buf);

			if (!(id.equals(MP3EXT_BADID) || (id.indexOf(0) != -1))) {
				bytesRead += 4;
				curLength = raf.readInt();

				// Added by Reed
				if ((curLength < 0) || (curLength > (framesLength - bytesRead))) {
					throw new ID3v2FormatException("ID3v2Tag.parseFrames: " + "Invalid frame size");
				}

				flags = new byte[2];
				bytesRead += raf.read(flags);
				buf = new byte[curLength];
				bytesRead += raf.read(buf);
				frame = new ID3v2Frame(id, flags, buf);
				frames.put(id, frame);
			} else {
				// We've hit padding so stop reading
				done = true;
				bytesRead -= buf.length;
			}
		}

		// Get around the possible precision loss
		final Long tmp = mpegOffset - (long) offset - (long) bytesRead;
		padding = tmp.intValue();
	}

	/**
	 * Remove the frame with the specified id from the file. If there is no frame
	 * with that id nothing will happen.
	 *
	 * @param id the id of the frame to remove
	 */
	public void removeFrame(final String id) {
		frames.remove(id);
	}

	/**
	 * Remove an existing id3v2 tag from the file passed to the constructor.
	 *
	 * @exception FileNotFoundException if an error occurs
	 * @exception IOException           if an error occurs
	 */
	@Override
	public void removeTag() throws FileNotFoundException, IOException {
		if (exists) {
			RandomAccessFile raf = null;
			int fullTagSize = writtenTagSize + head.getHeaderSize();

			if (head.getFooter()) {
				fullTagSize += foot.getFooterSize();
			}

			try {
				final Long bufSize = mp3.length() - (long) fullTagSize;
				final byte[] buf = new byte[bufSize.intValue()];
				raf = new RandomAccessFile(mp3, "rw");

				raf.seek(fullTagSize);

				if (raf.read(buf) != buf.length) {
					throw new IOException("ID3v2Tag.removeTag: unexpected" + " end of file encountered");
				}

				raf.setLength(bufSize.longValue());
				raf.seek(0);
				raf.write(buf);
				raf.close();
			} finally {
				if (raf != null) {
					raf.close();
				}
			}

			exists = false;
		}
	}

	/**
	 * Set the data contained in the comments frame (COMM).
	 *
	 * @param description a description of the comment
	 * @param comment     the comment
	 */
	@SuppressWarnings("deprecation")
	public void setCommentFrame(final String description, final String comment) {
		try {
			final byte[] b = new byte[description.length() + comment.length() + 5];
			int bytesCopied = 0;
			b[bytesCopied++] = 0;
			b[bytesCopied++] = 'e';
			b[bytesCopied++] = 'n';
			b[bytesCopied++] = 'g';
			System.arraycopy(description.getBytes(ENC_TYPE), 0, b, bytesCopied, description.length());
			bytesCopied += description.length();
			b[bytesCopied++] = 0;
			System.arraycopy(comment.getBytes(ENC_TYPE), 0, b, bytesCopied, comment.length());
			bytesCopied += comment.length();

			updateFrameData(ID3v2Frames.COMMENTS, b);
		} catch (final UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set the amount of padding to use when writing this tag. There cannot be any
	 * padding if a footer exists. Nothing will happen if this function is called
	 * and a footer exists or if the number is negative.
	 *
	 * @param pad the amount of padding to use when writing this tag
	 */
	public void setPadding(final int pad) {
		if (!head.getFooter() && (pad >= 0)) {
			padding = pad;
		}
	}

	/**
	 * Set the data contained in a text frame. This includes all frames with an id
	 * that starts with 'T' but excludes "TXXX". If an improper id is passed, then
	 * nothing will happen.
	 *
	 * @param id   the id of the frame to set the data for
	 * @param data the data for the frame
	 */
	@SuppressWarnings("deprecation")
	public void setTextFrame(final String id, final String data) {
		if ((id.charAt(0) == 'T') && !id.equals(ID3v2Frames.USER_DEFINED_TEXT_INFO)) {

			try {
				final byte[] b = new byte[data.length() + 1];
				b[0] = 0;
				System.arraycopy(data.getBytes(ENC_TYPE), 0, b, 1, data.length());

				updateFrameData(id, b);
			} catch (final UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Set the data contained in a URL frame. This includes all frames with an id
	 * that starts with 'W' but excludes "WXXX". If an improper id is passed, then
	 * nothing will happen.
	 *
	 * @param id   the id of the frame to set the data for
	 * @param data the data for the frame
	 */
	@SuppressWarnings("deprecation")
	public void setURLFrame(final String id, final String data) {
		if ((id.charAt(0) == 'W') && !id.equals(ID3v2Frames.USER_DEFINED_URL)) {

			updateFrameData(id, data.getBytes());
		}
	}

	/**
	 * Sets the data contained in the user defined text frame (TXXX).
	 *
	 * @param description a description of the data
	 * @param value       the data for the frame
	 */
	@SuppressWarnings("deprecation")
	public void setUserDefinedTextFrame(final String description, final String value) {
		try {
			final byte[] b = new byte[description.length() + value.length() + 2];
			int bytesCopied = 0;
			b[bytesCopied++] = 0;
			System.arraycopy(description.getBytes(ENC_TYPE), 0, b, bytesCopied, description.length());
			bytesCopied += description.length();
			b[bytesCopied++] = 0;
			System.arraycopy(value.getBytes(ENC_TYPE), 0, b, bytesCopied, value.length());
			bytesCopied += value.length();

			updateFrameData(ID3v2Frames.USER_DEFINED_TEXT_INFO, b);
		} catch (final UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets the data contained in the user defined url frame (WXXX).
	 *
	 * @param description a description of the url
	 * @param value       the url for the frame
	 */
	@SuppressWarnings("deprecation")
	public void setUserDefinedURLFrame(final String description, final String value) {
		try {
			final byte[] b = new byte[description.length() + value.length() + 2];
			int bytesCopied = 0;
			b[bytesCopied++] = 0;
			System.arraycopy(description.getBytes(ENC_TYPE), 0, b, bytesCopied, description.length());
			bytesCopied += description.length();
			b[bytesCopied++] = 0;
			System.arraycopy(value.getBytes(), 0, b, bytesCopied, value.length());
			bytesCopied += value.length();

			updateFrameData(ID3v2Frames.USER_DEFINED_URL, b);
		} catch (final UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns true if an id3v2 tag exists in the file that was passed to the
	 * constructor and false otherwise
	 *
	 * @return true if an id3v2 tag exists in the file passed to the ctor
	 */
	public boolean tagExists() {
		return exists;
	}

	/**
	 * Return a string representation of this object. This includes all data
	 * contained in all parts of this tag.
	 *
	 * @return a string representation of this object
	 */
	@Override
	public String toString() {
		String str = head.toString();

		str += "\nPadding:\t\t\t" + getPadding() + " bytes" + "\nTotalSize:\t\t\t" + getTotalSize() + " bytes";
		;

		if (head.getExtendedHeader()) {
			str += "\n" + ext_head.toString();
		}

		str += "\n" + frames.toString();

		if (head.getFooter()) {
			str += foot.toString();
		}

		return str;
	}

	/**
	 * Updates the data for the frame specified by id. If no frame exists for the id
	 * specified, a new frame with that id is created.
	 *
	 * @param id   the id of the frame to update
	 * @param data the data for the frame
	 */
	@SuppressWarnings("unchecked")
	public void updateFrameData(final String id, final byte[] data) {
		if (frames.containsKey(id)) {
			((ID3v2Frame) frames.get(id)).setFrameData(data);
		} else {
			final ID3v2Frame frame = new ID3v2Frame(id, data);
			frames.put(id, frame);
		}
	}

	/**
	 * Saves all the information in the tag to the file passed to the constructor.
	 * If a tag doesn't exist, a tag is prepended to the file. If the padding has
	 * not changed since the creation of this object and the size is less than the
	 * original size + the original padding, then the previous tag and part of the
	 * previous padding will be overwritten. Otherwise, a new tag will be prepended
	 * to the file.
	 *
	 * @exception FileNotFoundException if an error occurs
	 * @exception IOException           if an error occurs
	 */
	@Override
	public void writeTag() throws FileNotFoundException, IOException {
		RandomAccessFile raf = null;
		final int curSize = getTotalSize();
		head.setTagSize(getSize());

		try {
			raf = new RandomAccessFile(mp3, "rw");

			// This means that the file does not need to change size
			if (mpegOffset >= curSize) {
				final byte[] out = getBytes();
				raf.seek(0);
				raf.write(out);
			} else {
				// START of Guillaume Techene's modification
				final byte[] id3 = getBytes();
				final long size = raf.length() - mpegOffset;
				final byte[] previous_file = new byte[(int) size];
				raf.seek(mpegOffset);

				if (raf.read(previous_file) != previous_file.length) {
					throw new IOException("ID3v2Tag.removeTag: unexpected" + " end of file encountered");
				}

				raf.setLength(size + id3.length);
				raf.seek(0);
				raf.write(id3);
				raf.write(previous_file);
				// END of Guillaume Techene's modification
			}
		} finally {
			if (raf != null) {
				raf.close();
			}
		}

		writtenTagSize = curSize;
		writtenPadding = padding;
		exists = true;
	}

}
