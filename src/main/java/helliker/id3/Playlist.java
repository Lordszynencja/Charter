package helliker.id3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

import org.jcodec.common.logging.Logger;

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
 * Description: This class is treated as a winamp styled playlist. It is
 * essentially a collection of MP3Files that can be initalised from a file.
 * Playlists can also be written based on it's data. <br />
 *
 * <dl>
 * <dt><b>Version History</b></dt>
 * <dt>1.6.1 - <small>2002.1023 by gruni</small></dt>
 * <dd>-Made Sourcecode compliant to the Sun CodingConventions</dd>
 * <dt>1.6 - <small>by helliker</small></dt>
 * <dd>-initial release.</dd>
 * </dl>
 *
 *
 * @author Jonathan Hilliker
 * @version 1.6.1
 */

@SuppressWarnings({ "serial", "rawtypes" })
public class Playlist extends LinkedList {

	// Types of playlists
	/**
	 * The Winamp M3u Format
	 */
	public static int WINAMP_FORMAT = 0;
	/**
	 * The MusicMatch format
	 */
	public static int MUSICMATCH_FORMAT = 1;

	/**
	 * the Playlist Extension
	 */
	private final String PLAYLIST_EXT = ".m3u";

	/**
	 * Reads in mp3s from a directory and adds them to this playlist. By default,
	 * subdirectories will be recursed and the files will be sorted.
	 *
	 * @param dir the directory to look for mp3s in
	 * @exception IOException if the files specified is not a directory
	 */
	public void addDirectory(final File dir) throws IOException {
		addDirectory(dir, true, true);
	}

	/**
	 * Reads in mp3s from a directory and adds them to the this playlist. The
	 * recurse parameter should be set if you wish to grab mp3s from subdirectories
	 * as well. If the sort parameter is true the files in each directory will be
	 * sorted before added.
	 *
	 * @param dir     the directory to look for mp3s in
	 * @param recurse whether or not to recurse subdirectories
	 * @param sort    whether or not to sort each directory
	 * @exception IOException if the file specified is not a directory
	 */
	@SuppressWarnings("unchecked")
	public void addDirectory(final File dir, final boolean recurse, final boolean sort) throws IOException {

		if (dir.isDirectory()) {
			final File[] files = dir.listFiles(new MP3FileFilter(true));

			if (sort) {
				Arrays.sort(files);
			}

			MP3File mp3 = null;

			for (int i = 0; i < files.length; i++) {
				if (files[i].isFile()) {
					try {
						mp3 = new MP3File(files[i]);
						this.add(mp3);
					} catch (final Exception e) {
						// Do nothing. Bad mp3, don't add.
					}
				} else if (recurse) {
					addDirectory(files[i], recurse, sort);
				}
			}
		} else {
			throw new IOException(
					"Error loading playlist from a directory: " + dir.getAbsolutePath() + " is not a " + "directory");
		}
	}

	/**
	 * Load a playlist from a file. This works with Winamp and MusicMatch playlists.
	 * The format parameter should be either WINAMP_FORMAT or MUSICMATCH_FORMAT.
	 *
	 * @param m3uFile the playlist file
	 * @param format  the type of playlist to load
	 * @exception PlaylistException     if the file is corrupt
	 * @exception FileNotFoundException if an error occurs
	 * @exception IOException           if an error occurs
	 */
	public void loadFromFile(final File m3uFile, final int format)
			throws PlaylistException, FileNotFoundException, IOException {

		final FileReader in = new FileReader(m3uFile);
		final BufferedReader reader = new BufferedReader(in);

		if (format == WINAMP_FORMAT) {
			loadWinampFile(reader, m3uFile);
		} else if (format == MUSICMATCH_FORMAT) {
			loadMusicMatchFile(reader);
		}

		reader.close();
		in.close();
	}

	/**
	 * Loads the contents of a MusicMatch playlist.
	 *
	 * @param reader the stream to read from
	 * @exception PlaylistException if an error occurs
	 * @exception IOException       if an error occurs
	 */
	@SuppressWarnings("unchecked")
	private void loadMusicMatchFile(final BufferedReader reader) throws PlaylistException, IOException {

		String str;

		while ((str = reader.readLine()) != null) {
			try {
				this.add(new MP3File(str));
			} catch (final Exception e) {
				// Ignore this file
			}
		}
	}

	/**
	 * Loads the contents of a winamp playlist.
	 *
	 * @param reader  the stream to read from
	 * @param m3uFile the Playlist File
	 * @exception PlaylistException if an error occurs
	 * @exception IOException       if an error occurs
	 */
	@SuppressWarnings("unchecked")
	private void loadWinampFile(final BufferedReader reader, final File m3uFile) throws PlaylistException, IOException {

		String str = reader.readLine();
		MP3File mp3 = null;

		if (str.equals("#EXTM3U")) {
			while ((str = reader.readLine()) != null) {
				if (str.indexOf("EXTINF:") != -1) {
					str = reader.readLine();

					if (str.length() != 0) {
						// Check to see if the path in the file is an absolute
						// path. In Unix an absolute path begins with forward
						// slash, in Windows it will have a colon.
						if ((str.charAt(0) != '/') || (str.indexOf(":") != -1)) {
							str = m3uFile.getParent() + File.separator + str;
						}
					}

					try {
						mp3 = new MP3File(str);
						this.add(mp3);
					} catch (final Exception e) {
						// Do nothing, just don't add it
					}
				} else {
					throw new PlaylistException();
				}
			}
		} else {
			throw new PlaylistException();
		}
	}

	/**
	 * Sorts this playlist by using the default path comparisons. Uses
	 * Collections.sort().
	 */
	@SuppressWarnings("unchecked")
	public void sort() {
		Collections.sort(this);
	}

	/**
	 * Sorts this playlist by using the specified comparator. Uses
	 * Collections.sort().
	 *
	 * @param cmp the comparator to use
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void sort(final Comparator cmp) {
		Collections.sort(this, cmp);
	}

	/**
	 * Calls toString on every object contained within (very long).
	 *
	 * @return a string representation of this object
	 */
	@Override
	public String toString() {
		final StringBuffer buf = new StringBuffer();
		final Iterator it = iterator();

		while (it.hasNext()) {
			buf.append(it.next());
		}

		return buf.toString();
	}

	/**
	 * Writes the contents of this playlist in MusicMatch format.
	 *
	 * @param printer the stream to write to
	 * @exception IOException if an error occurs
	 */
	private void writeMusicMatchFile(final PrintWriter printer) throws IOException {
		// Written by David Barron, thanx
		final Iterator it = iterator();
		MP3File mp3;

		while (it.hasNext()) {
			mp3 = (MP3File) it.next();
			printer.println(mp3.getPath());
		}
	}

	/**
	 * Writes this playlist in the format specified. This can be either
	 * WINAMP_FORMAT or MUSICMATCH_FORMAT. If the destination is a directory, then
	 * the file will be created in that directory with the same name as the
	 * directory with a ".m3u" extension added. If the destination is a file then
	 * the playlist will be saved to that file. If the format argument is invalid
	 * the file will not be written.
	 *
	 * @param dest   where to save the playlist
	 * @param format the format to write the file in
	 * @exception IOException if an error occurs
	 */
	public void writeToFile(final File dest, final int format) throws IOException {
		File m3u = dest;

		if (dest.isDirectory()) {
			m3u = new File(dest, dest.getName() + PLAYLIST_EXT);
		}

		final FileOutputStream out = new FileOutputStream(m3u);
		final PrintWriter printer = new PrintWriter(out);

		if (format == WINAMP_FORMAT) {
			writeWinampFile(printer);
		} else if (format == MUSICMATCH_FORMAT) {
			writeMusicMatchFile(printer);
		}

		printer.close();
		out.close();
	}

	/**
	 * Write the contents of this playlist in Winamp format.
	 *
	 * @param printer the stream to write to
	 * @exception IOException if an error occurs
	 */
	private void writeWinampFile(final PrintWriter printer) throws IOException {
		final Iterator it = iterator();
		MP3File mp3;
		String artist = new String();
		String title = new String();
		String display = new String();

		printer.println("#EXTM3U");

		while (it.hasNext()) {
			mp3 = (MP3File) it.next();
			printer.print("#EXTINF:");
			printer.print(mp3.getPlayingTime());
			printer.print(",");

			try {
				artist = mp3.getArtist();
			} catch (final ID3v2FormatException e) {
				Logger.error("Error when reading artist", e);
			}

			try {
				title = mp3.getTitle();
			} catch (final ID3v2FormatException e) {
				Logger.error("Error when reading title", e);
			}

			if ((artist.length() != 0) && (title.length() != 0)) {
				display = artist + " - " + title;
			} else if (title.length() != 0) {
				display = title;
			} else {
				display = mp3.getFileName();
				display = display.substring(0, display.length() - 4);
			}

			printer.println(display);
			printer.println(mp3.getPath());
		}
	}

}
