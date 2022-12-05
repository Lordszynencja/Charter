package helliker.id3;

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
 * An exception to be thrown if the parser is unable to find an mpeg header.
 *
 * <dl>
 * <dt><b>Version History</b></dt>
 * <dt>1.3.1 - <small>2002.1023 by gruni</small></dt>
 * <dd>-Made Sourcecode compliant to the Sun CodingConventions</dd>
 * <dt>1.3 - <small>2002.0318 by helliker</small></dt>
 * <dd>-Inherits from ID3Exception now</dd>
 * <dt>1.2 - <small>2001.1019 by helliker</small></dt>
 * <dd>-All set for release.</dd>
 * </dl>
 *
 *
 * @author Jonathan Hilliker
 * @version 1.3.1 2002/03/18 02:12:17 helliker Exp $
 */

@SuppressWarnings("serial")
public class NoMPEGFramesException extends ID3Exception {

	/**
	 * Create a NoMPEGFramesException with a default message.
	 */
	public NoMPEGFramesException() {
		super("The file specified is not a valid MPEG.");
	}

	/**
	 * Create a NoMPEGFramesException with a specified message.
	 *
	 * @param msg
	 *           the message for this exception
	 */
	public NoMPEGFramesException(final String msg) {
		super(msg);
	}

}
