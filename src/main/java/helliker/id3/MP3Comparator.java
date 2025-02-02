package helliker.id3;

import java.util.Comparator;

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
 * Description: This class is a comparator that is ideal for sorting mp3s. The
 * MP3File class uses this object to implement its compareTo method. This works
 * really well for playlists too because it has an order of precedence that
 * attempts to sort the mp3s so that the tracks of the same artists and albums
 * are together and that they are sorted by track number. If an error occurs
 * while doing the comparison, the paths of the mp3s are compared.<br />
 *
 * <dl>
 * <dt><b>Version History</b></dt>
 * <dt>1.3.1 - <small>2002.1023 by gruni</small></dt>
 * <dd>-Made Sourcecode compliant to the Sun CodingConventions</dd>
 * <dt>1.3 - <small>2002.0127 by helliker</small></dt>
 * <dd>-Title not a factor when sorting now.</dd>
 * <dt>1.2 - <small>2001.1019 by helliker</small></dt>
 * <dd>-All set for release.</dd>
 *
 * @author Jonathan Hilliker
 * @version 1.3.1
 */

@SuppressWarnings("rawtypes")
public class MP3Comparator implements Comparator {

	/**
	 * Returns true if the two parameters are acceptable comparison values. In order
	 * to be acceptable, both parameters must not be empty and they must not be
	 * equal.
	 *
	 * @param str1 the first parameter
	 * @param str2 the second parameter
	 * @return true if the two parameters are acceptable comparison values
	 */
	private boolean accept(final String str1, final String str2) {
		return (!str1.equalsIgnoreCase(str2) && (str1.length() != 0) && (str2.length() != 0));
	}

	/**
	 * Compares the objects. Non-MP3File objects will always be less then an MP3File
	 * object and if neither objects are MP3Files then they are considered equal. A
	 * series of tests are conducted to determine the the outcome. First the artist
	 * is tested, then the album, then the track, then the title, then the path.
	 *
	 * @param o1 one object to compare
	 * @param o2 another object to compare
	 * @return a positive number if o1 > o2, zero if o1 = o2, and a negative number
	 *         if o1 < o2
	 */
	@Override
	public int compare(final Object o1, final Object o2) {
		int retval = 0;

		if ((o1 instanceof MP3File) && (o2 instanceof MP3File)) {
			final MP3File m1 = (MP3File) o1;
			final MP3File m2 = (MP3File) o2;

			try {
				if (accept(m1.getArtist(), m2.getArtist())) {
					retval = m1.getArtist().compareToIgnoreCase(m2.getArtist());

				} else if (accept(m1.getAlbum(), m2.getAlbum())) {
					retval = m1.getAlbum().compareToIgnoreCase(m2.getAlbum());
				} else if (accept(m1.getTrackString(), m2.getTrackString())) {
					retval = m1.getTrack() - m2.getTrack();
				} else {
					retval = m1.getPath().compareToIgnoreCase(m2.getPath());
				}
			} catch (final ID3v2FormatException e) {
				retval = m1.getPath().compareToIgnoreCase(m2.getPath());
			}
		} else {
			if (o1 instanceof MP3File) {
				retval = 1;
			} else if (o2 instanceof MP3File) {
				retval = -1;
			} else {
				retval = 0;
			}
		}

		return retval;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	/**
	 * Returns true if the specified object is an MP3Comparator
	 *
	 * @param obj the object to test
	 * @return true if the object is an MP3Comparator
	 */
	@Override
	public boolean equals(final Object obj) {
		return (obj instanceof MP3Comparator);
	}

}// MP3Comparator
