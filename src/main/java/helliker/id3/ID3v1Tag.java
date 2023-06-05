package helliker.id3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;

/*
 * Copyright (C) 2001,2002 Jonathan Hilliker
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
/**
 * This class reads and writes id3v1.1 tags from/to files. <br/>
 * <dl>
 * <dt><b>Version History:</b></dt>
 * <dt>1.9.1 - <small>2002.1023 by gruni</small></dt>
 * <dd>-Made sourcecode compliant to the Sun Coding Conventions</dd>
 * <dt>1.9 - <small>by helliker</small></dt>
 * </dl>
 *
 * @author Jonathan Hilliker
 * @version 1.9
 */

public class ID3v1Tag implements ID3Tag {

	/**
	 * The size of an ID3v1 Tag
	 */
	private final int TAG_SIZE = 128;
	/**
	 * The size of the Title
	 */
	private final int TITLE_SIZE = 30;
	/**
	 * The size of the Artist
	 */
	private final int ARTIST_SIZE = 30;
	/**
	 * The size of the Album
	 */
	private final int ALBUM_SIZE = 30;
	/**
	 * The size of the Year
	 */
	private final int YEAR_SIZE = 4;
	/**
	 * The size of the Comment
	 */
	private final int COMMENT_SIZE = 28;
	/**
	 * The Track location
	 */
	private final int TRACK_LOCATION = 126;
	/**
	 * The Genre location
	 */
	private final int GENRE_LOCATION = 127;
	/**
	 * Maximum Number of genres ?
	 */
	private final int MAX_GENRE = 255;
	/**
	 * Maximum Tracknumber ?
	 */
	private final int MAX_TRACK = 255;
	/**
	 * Tag Start of ID3v1
	 */
	private final String TAG_START = "TAG";

	/**
	 * The MP3 File
	 */
	private File mp3 = null;
	/**
	 * Gives Information if header Exists ?
	 */
	private boolean headerExists = false;
	/**
	 * The title
	 */
	private String title = null;
	/**
	 * The artist
	 */
	private String artist = null;
	/**
	 * The album
	 */
	private String album = null;
	/**
	 * The year
	 */
	private String year = null;
	/**
	 * The Comment
	 */
	private String comment = null;
	/**
	 * The Genre
	 */
	private int genre;
	/**
	 * The Track Number
	 */
	private int track;

	/**
	 * Create an id3v1tag from the file specified. If the file contains a tag, the
	 * information is automatically extracted. Supports only id3v1.1 directly but
	 * id3v1.0 tags will most likely work correctly too.
	 *
	 * @exception FileNotFoundException if an error occurs
	 * @exception IOException           if an error occurs
	 * @param mp3 the file to read/write the tag to
	 */
	public ID3v1Tag(final File mp3) throws FileNotFoundException, IOException {
		this.mp3 = mp3;

		title = new String();
		artist = new String();
		album = new String();
		year = new String();
		comment = new String();
		genre = -1;
		track = -1;

		RandomAccessFile in = null;

		try {
			in = new RandomAccessFile(mp3, "r");
			headerExists = checkHeader(in);

			if (headerExists) {
				readTag(in);
			}
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	/**
	 * Checks whether a header for the id3 tag exists yet
	 *
	 * @exception FileNotFoundException if an error occurs
	 * @exception IOException           if an error occurs
	 * @param raf the open file to read from
	 * @return true if a tag is found
	 */
	private boolean checkHeader(final RandomAccessFile raf) throws FileNotFoundException, IOException {

		boolean retval = false;

		if (raf.length() > TAG_SIZE) {
			raf.seek(raf.length() - TAG_SIZE);
			final byte[] buf = new byte[3];

			if (raf.read(buf) != 3) {
				throw new IOException("Error encountered reading ID3 header");
			} else {
				final String result = new String(buf, 0, 3);
				retval = result.equals(TAG_START);
			}
		}

		return retval;
	}

	/**
	 * Finds the substring of the String parameter but ends the string with the
	 * first null byte encountered.
	 *
	 * @param s     the string chop
	 * @param start where to start the string
	 * @param end   where to end the string if a null byte isn't found
	 * @return the chopped string
	 */
	private String chopSubstring(final String s, final int start, final int end) {
		String str = s.substring(start, end);
		final int loc = str.indexOf('\0');

		if (loc != -1) {
			str = str.substring(0, loc);
		}

		return str;
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
	 * Return the album field of the tag
	 *
	 * @return the album field of the tag
	 */
	public String getAlbum() {
		return album.trim();
	}

	/**
	 * Return the artist field of the tag
	 *
	 * @return the artist field of the tag
	 */
	public String getArtist() {
		return artist.trim();
	}

	/**
	 * Returns a binary representation of this id3v1 tag. It is in the correct
	 * format according to the id3v1.1 specifications.
	 *
	 * @return a binary representation of this id3v1 tag
	 */
	@Override
	public byte[] getBytes() {
		final byte[] tag = new byte[TAG_SIZE];
		int bytesCopied = 0;

		System.arraycopy(TAG_START.getBytes(), 0, tag, bytesCopied, TAG_START.length());
		bytesCopied += TAG_START.length();
		System.arraycopy(title.getBytes(), 0, tag, bytesCopied, title.length());
		bytesCopied += TITLE_SIZE;
		System.arraycopy(artist.getBytes(), 0, tag, bytesCopied, artist.length());
		bytesCopied += ARTIST_SIZE;
		System.arraycopy(album.getBytes(), 0, tag, bytesCopied, album.length());
		bytesCopied += ALBUM_SIZE;
		System.arraycopy(year.getBytes(), 0, tag, bytesCopied, year.length());
		bytesCopied += YEAR_SIZE;
		System.arraycopy(comment.getBytes(), 0, tag, bytesCopied, comment.length());
		tag[TRACK_LOCATION] = (byte) track;
		tag[GENRE_LOCATION] = (byte) genre;

		return tag;
	}

	/**
	 * Return the comment field of the tag
	 *
	 * @return the comment field of the tag
	 */
	public String getComment() {
		return comment.trim();
	}

	/**
	 * Converts a string to an array of bytes with the length specified
	 *
	 * @exception UnsupportedEncodingException When Encoding not Supported ?
	 * @param str    the string to convert
	 * @param length the size of the byte array
	 * @return the array of bytes converted from the string
	 */
	@SuppressWarnings("unused")
	private byte[] getFieldBytes(final String str, final int length) throws UnsupportedEncodingException {

		final StringBuffer buf = new StringBuffer(str);

		for (int i = str.length(); i < length; i++) {
			buf.append('\0');
		}

		return buf.toString().getBytes();
	}

	/**
	 * Return the genre field of the tag, -1 if tag doesn't exist
	 *
	 * @return the genre field of the tag
	 */
	public int getGenre() {
		return genre;
	}

	/**
	 * Return the genre name based on the ID3/Nullsoft standards. If the genre value
	 * is not valid, an empty String is returned.
	 *
	 * @return return the genre name or null if the genre value is not valid
	 */
	public String getGenreString() {
		return NullsoftID3GenreTable.getGenre(genre);
	}

	/**
	 * Return the size in bytes of the tag. This returns 128 if the tag exists and 0
	 * otherwise.
	 *
	 * @return the size of the tag in bytes
	 */
	public int getSize() {
		int retval = 0;

		if (headerExists) {
			retval = TAG_SIZE;
		}

		return retval;
	}

	/**
	 * Return the title field of the tag
	 *
	 * @return the title field of the tag
	 */
	public String getTitle() {
		return title.trim();
	}

	/**
	 * Return the track field of the tag, or -1 if the tag does not exist.
	 *
	 * @return the track field of the tag
	 */
	public int getTrack() {
		return track;
	}

	/**
	 * Return the year field of the tag
	 *
	 * @return the year field of the tag
	 */
	public String getYear() {
		return year.trim();
	}

	/**
	 * Reads the data from the id3v1 tag
	 *
	 * @exception FileNotFoundException if an error occurs
	 * @exception IOException           if an error occurs
	 * @param raf the open file to read from
	 */
	private void readTag(final RandomAccessFile raf) throws FileNotFoundException, IOException {

		raf.seek(raf.length() - TAG_SIZE);
		final byte[] buf = new byte[TAG_SIZE];
		raf.read(buf, 0, TAG_SIZE);
		final String tag = new String(buf, 0, TAG_SIZE);
		int start = TAG_START.length();
		title = chopSubstring(tag, start, start += TITLE_SIZE);
		artist = chopSubstring(tag, start, start += ARTIST_SIZE);
		album = chopSubstring(tag, start, start += ALBUM_SIZE);
		year = chopSubstring(tag, start, start += YEAR_SIZE);
		comment = chopSubstring(tag, start, start += COMMENT_SIZE);
		track = buf[TRACK_LOCATION] & 0xff; // reed - access genre ids > 127
		genre = buf[GENRE_LOCATION] & 0xff;
	}

	/**
	 * Removes the id3v1 tag from the file specified in the constructor
	 *
	 * @exception FileNotFoundException if an error occurs
	 * @exception IOException           if an error occurs
	 */
	@Override
	public void removeTag() throws FileNotFoundException, IOException {
		if (headerExists) {
			RandomAccessFile raf = null;

			try {
				raf = new RandomAccessFile(mp3, "rw");
				final Long bufSize = raf.length() - (long) TAG_SIZE;
				// Possible loss of precision here, but I doubt there will be
				// a 2 billion byte mp3 out there
				final byte[] buf = new byte[bufSize.intValue()];

				if (raf.read(buf) != bufSize.intValue()) {
					throw new IOException("Error encountered while removing tag");
				}

				raf.setLength(bufSize.longValue());
				raf.seek(0);
				raf.write(buf);
			} finally {
				if (raf != null) {
					raf.close();
				}
			}

			headerExists = false;
		}
	}

	/**
	 * Set the album field of the tag. The maximum size of the String is 30. If the
	 * size exceeds the maximum size, the String will be truncated.
	 *
	 * @param newAlbum the album for the tag
	 */
	public void setAlbum(final String newAlbum) {
		if (newAlbum.length() > ALBUM_SIZE) {
			album = newAlbum.substring(0, ALBUM_SIZE);
		} else {
			album = newAlbum;
		}
	}

	/**
	 * Set the artist field of the tag. The maximum size of the String is 30. If the
	 * size exceeds the maximum size, the String will be truncated.
	 *
	 * @param newArtist the artist for the tag
	 */
	public void setArtist(final String newArtist) {
		if (newArtist.length() > ARTIST_SIZE) {
			artist = newArtist.substring(0, ARTIST_SIZE);
		} else {
			artist = newArtist;
		}
	}

	/**
	 * Set the comment field of the tag. The maximum size of the String is 30. If
	 * the size exceeds the maximum size, the String will be truncated.
	 *
	 * @param newComment the comment of the tag
	 */
	public void setComment(final String newComment) {
		if (newComment.length() > COMMENT_SIZE) {
			comment = newComment.substring(0, COMMENT_SIZE);
		} else {
			comment = newComment;
		}
	}

	/**
	 * Set the genre field of the tag. This probably should not be greater than 115,
	 * but supports values from 0-255.
	 *
	 * @exception ID3FieldDataException if the value supplie is invalid
	 * @param newGenre the genre of the tag
	 */
	public void setGenre(final int newGenre) throws ID3FieldDataException {
		if ((newGenre <= MAX_GENRE) && (newGenre >= 0)) {
			genre = newGenre;
		} else {
			throw new ID3FieldDataException("Invalid genre value.  Must be between 0 and 255.");
		}
	}

	/**
	 * Attempt to set the genre value of this tag from the string specified. The
	 * value returned is based on the ID3/Nullsoft standards. Returns true if a
	 * match is found in the table and false otherwise.
	 *
	 * @param str the string value of the genre to attempt to set
	 * @return true if a match is found, false otherwise
	 */
	public boolean setGenreString(final String str) {
		final int result = NullsoftID3GenreTable.getGenre(str);
		boolean retval = false;

		if (result != -1) {
			genre = result;
			retval = true;
		}

		return retval;
	}

	/**
	 * Set the title field of the tag. The maximum size of the String is 30. If the
	 * size exceeds the maximum size, the String will be truncated.
	 *
	 * @param newTitle the title for the tag
	 */
	public void setTitle(final String newTitle) {
		if (newTitle.length() > TITLE_SIZE) {
			title = newTitle.substring(0, TITLE_SIZE);
		} else {
			title = newTitle;
		}
	}

	/**
	 * Set the track field of the tag. The track number has to be between 0 and 255.
	 * If it is not, nothing will happen.
	 *
	 * @param newTrack the track of the tag
	 */
	public void setTrack(final int newTrack) {
		if ((newTrack <= MAX_TRACK) && (newTrack >= 0)) {
			track = newTrack;
		}
	}

	/**
	 * Set the year field of the tag. The maximum size of the String is 4. If the
	 * size exceeds the maximum size, the String will be truncated.
	 *
	 * @param newYear the year for the tag
	 */
	public void setYear(final String newYear) {
		if (newYear.length() > YEAR_SIZE) {
			year = newYear.substring(0, YEAR_SIZE);
		} else {
			year = newYear;
		}
	}

	/**
	 * Checks if a tag exists
	 *
	 * @return true if a tag exists
	 */
	public boolean tagExists() {
		return headerExists;
	}

	/**
	 * Returns a String representation of this object. Contains all information
	 * within the tag.
	 *
	 * @return a String representation of this object
	 */
	@Override
	public String toString() {
		return "ID3v1.1\nTagSize:\t\t\t" + getSize() + " bytes\nTitle:\t\t\t\t" + getTitle() + "\nArtist:\t\t\t\t"
				+ getArtist() + "\nAlbum:\t\t\t\t" + getAlbum() + "\nYear:\t\t\t\t" + getYear() + "\nComment:\t\t\t"
				+ getComment() + "\nTrack:\t\t\t\t" + getTrack() + "\nGenre:\t\t\t\t" + getGenreString();
	}

	/**
	 * Writes the information in this tag to the file specified in the constructor.
	 * If a tag does not exist, one will be created. If a tag already exists, it
	 * will be overwritten.
	 *
	 * @exception FileNotFoundException if an error occurs
	 * @exception IOException           if an error occurs
	 */
	@Override
	public void writeTag() throws FileNotFoundException, IOException {

		RandomAccessFile raf = null;

		try {
			raf = new RandomAccessFile(mp3, "rw");

			if (headerExists) {
				raf.seek(raf.length() - TAG_SIZE);
			} else {
				raf.seek(raf.length());
			}

			raf.write(getBytes());
		} finally {
			if (raf != null) {
				raf.close();
			}
		}

		headerExists = true;
	}

} // ID3v1Tag
