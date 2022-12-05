package helliker.id3;

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
 *  This class contains serveral utility functions for converting, modifying, 
 *  and testing bytes.
 * <br/ >
 * <dl>
 * <dt><b>Version History:</b></dt>
 * <dt>1.3.1 - <small>2002.1015 by gruni</small></dt>
 * <dd>cleaned Sourcecode and Javadoc</dd> 
 * <dt>1.3 - <small>2001.1204 by helliker</small></dt>
 * <dd>Added functions to encode and decode synchsafe integers.</dd>
 *
 * <dt>1.2 - <small>2001.1019 by helliker</small></dt>
 * <dd>All set for release.</dd>
 * </dl>
 * @author  Jonathan Hilliker
 * @version 1.3.1
 */

public final class BinaryParser  {
  /**???
  */
  private static final int NUM_BYTES = 4;
  
  /**???
   */
  private static final int NUM_BITS = 8;
  
  /**???
   */
  private static final int SYNCHSAFE_BITS = 7;

  /**
   * Tests to see if the bit at postion pos is set in byte b
   *
   * @param b the byte to test
   * @param pos a value between 0 (least significant bit) and 7 
   *            (most significant bit) indicating the position to test
   * @return true if the bit at the location is set
   */
  public static boolean bitSet(byte b, int pos) {
    boolean retval = false;
    
    if ((pos >= 0) && (pos < NUM_BITS)) {
      retval = ((b & (byte) (1 << pos)) != 0);
    }
    
    return retval;
  }
  
  /**
   * This function takes an 8 character string representation of a byte 
   * in binary mode.  It will match 0's and 1's at the locations specified
   * and skip other characters (x is the standard wildcard).  If the values
   * of the bits at the locations where a 0 is set is 0 and values of the 
   * bits at the locations where 1 is set is 1, then the function will 
   * return true.
   *
   * @param b the byte to test
   * @param pattern the 8 character long pattern to test the byte
   * @return true if the bits at the locations specified are set
   */
  public static boolean matchPattern(byte b, String pattern) {
    boolean retval = true;
    
    for (int i = 0; (i < NUM_BITS) && (i < pattern.length()) && retval; i++) {
      
      if (pattern.charAt(i) == '1') {
        retval = retval && bitSet (b, NUM_BITS - i - 1);
      } else if (pattern.charAt(i) == '0') {
        retval = retval && !bitSet(b, NUM_BITS - i - 1);
      }
    }
    
    return retval;
  }
  
  /**
   * Convert a portion of a byte to an integer.  This assumes that the length
   * of the byte is end - start bits long with end being the most 
   * significant bit.  The start and end values must be between 0 and 7.  If
   * the start and end values are incorrect, it will set start to 0 and end
   * to 7.
   *
   * @param b the byte to convert from
   * @param start the starting bit
   * @param end the ending bit
   * @return the converted value (unsigned)
   */
  public static int convertToDecimal(byte b, int start, int end) {
    byte ret = 0;
    int bCount = 0;
    int s = start;
    int e = end;
    
    if ((start < 0) || (start >= NUM_BITS)) {
      s = 0;
    }
    
    if ((end < 0) || (end >= NUM_BITS)) {
      e = NUM_BITS - 1;
    }
    
    if (start > end) {
      s = end;
      e = start;
    }
    
    for (int i = s; i <= e; i++) {
      if (bitSet(b, i)) {
        ret = setBit(ret, bCount);
      }
      
      bCount++;
    }
    
    return ret;
  }
  
  /**
   * Convert an array of bytes into an integer.  The array must not contain
   * more than 4 bytes (32 bits).  This assumes that the most significant 
   * byte is in the first index of the array.  If an array is not 4 bytes
   * long, then it will assume that the bytes are preceded by leading 0's.
   *
   * @param b the array of bytes
   * @return the converted integer in decimal
   */
  public static int convertToInt(byte[] b) {
    int retval = 0;
    int pos = 0;
    int start = b.length - 1;
    
    if (start >= NUM_BYTES) {
      start = NUM_BYTES - 1;
    }
    
    
    for (int i = start; i >= 0; i--) {
      for (int j = 0; j < NUM_BITS; j++) {
        if (bitSet(b[i], j)) {
          retval += Math.pow(2, pos);
        }
        
        pos++;
      }
    }
    
    return retval;
  }
  
  /**
   * Converts the byte array to a synchsafe integer as specified in section
   * 6.2 of the id3v2 specification.  Works exactly same as the covnertToInt
   * method except that it only calculates using the first 7 bits of each
   * byte.
   *
   * @param b the array of bytes
   * @return the converted integer in decimal
   */
  public static int convertToSynchsafeInt(byte[] b) {
    int retval = 0;
    int pos = 0;
    int start = b.length - 1;
    
    if (start >= NUM_BYTES) {
      start = NUM_BYTES - 1;
    }
    
    for (int i = start; i >= 0; i--) {
      for (int j = 0; j < SYNCHSAFE_BITS; j++) {
        if (bitSet(b[i], j)) {
          retval += Math.pow(2, pos);
        }
        
        pos++;
      }
    }
    
    return retval;
  }
  
  /**
   * Convert the integer passed to a array of 4 bytes (32-bits).  Does the
   * opposite of the convertToInt method.
   *
   * @param num the integer to convert
   * @return the integer converted to a byte array
   */
  public static byte[] convertToBytes(int num) {
    byte[] b = new byte[NUM_BYTES];
    int count = num;
    boolean done = false;
    
    for (int i = b.length - 1; (i >= 0) && !done; i--) {
      for (int j = 0; (j < NUM_BITS) && !done; j++) {
        if ((count % 2) == 1) {
          b[i] = setBit(b[i], j);
        }
        
        count = count / 2;
        done = (count == 0);
      }
    }
    
    return b;
  }
  
  /**
   * Converts the integer passed to an array of bytes as specified in 
   * section 6.2 of the id3v2 specification.  Works the same as the 
   * convertToBytes method except the most significant bit of each byte is
   * zeroed.
   *
   * @param num the integer to convert
   * @return the integer converted to a byte array
   */
  public static byte[] convertToSynchsafeBytes(int num) {
    byte[] b = new byte[NUM_BYTES];
    int count = num;
    boolean done = false;
    
    for (int i = b.length - 1; (i >= 0) && !done; i--) {
      for (int j = 0; (j < SYNCHSAFE_BITS) && !done; j++) {
        if ((count % 2) == 1) {
          b[i] = setBit(b[i], j);
        }

        count = count / 2;
        done = (count == 0);
      }
    }
    
    return b;
  }
  
  /**
   * Sets the bit at the specified location in the byte given.  Location 
   * should be between 0 and 7.  If an invalid location is specified, 0
   * will be returned.
   *
   * @param b the byte to set the index in
   * @param location the index to set in b
   * @return b with the bit at location set to 1
   */
  public static byte setBit(byte b, int location) {
    byte ret = 0;
    
    if ((location >= 0) && (location < NUM_BITS)) {
      ret = (byte) (b | (byte) (1 << location));
    }
    
    return ret;
  }

}// BinaryParser
