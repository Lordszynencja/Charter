package helliker.id3;

import java.io.IOException;

/*
 * Copyright (C) 2001 Jonathan Hilliker
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
 *  A common interface for ID3Tag objects so they can easily communicate with
 *  each other.<br/><dl>
 * <dt><b>Version History:</b></dt>
 * <dt>1.2.1 - <small>2002.1023 by gruni</small></dt>
 * <dd>-Made sourcecode compliant to the Sun Coding Conventions</dd>
 * <dt>1.2 - <small>2002.0127 by  helliker</small></dt>
 * <dd>-Added getBytes method.</dd>
 *
 * <dt>1.1 - <small>2002/01/13 by helliker</small></dt>
 * <dd>-Initial version</dd>
 * </dl>
 * @author  Jonathan Hilliker
 * @version 1.2.1
 */

public interface ID3Tag {
    
    /**
     * Copies information from the ID3Tag parameter and inserts it into
     * this tag.  Previous data will be overwritten.
     *
     * @param tag the tag to copy from
     */
    public void copyFrom(ID3Tag tag);

    /**
     * Saves all data in this tag to the file it is bound to.
     *
     * @exception IOException if an error occurs
     */
    public void writeTag() throws IOException;

    /**
     * Removes this tag from the file it is bound to.
     *
     * @exception IOException if an error occurs
     */
    public void removeTag() throws IOException;
    

    /**
     * Returns a binary representation of the tag as it would appear in
     * a file.
     *
     * @return a binary representation of the tag
     */
    public byte[] getBytes();

} // ID3Tag
