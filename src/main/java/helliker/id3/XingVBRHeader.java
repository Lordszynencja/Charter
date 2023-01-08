package helliker.id3;

import java.io.RandomAccessFile;
import java.io.IOException;

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
 * Reads information from a Xing info header. Thanks to the Scilla project
 * (http://www.xs4all.nl/~rwvtveer/scilla/) for code ideas.<br />
 *
 * <dl>
 *   <dt> <b>Version History</b> </dt>
 *   <dt> 1.2.1 - <small>2002.1023 by gruni</small> </dt>
 *   <dd> -Made Sourcecode compliant to the Sun CodingConventions</dd>
 *   <dt> 1.2 - <small>2002.0203 by helliker</small> </dt>
 *   <dd> -Uses the file size if no byteSize is present in header.</dd>
 *   <dt> 1.1 - <small>2002.0121 by helliker</small> </dt>
 *   <dd> -Initial version.</dd>
 *
 *@author    Jonathan Hilliker
 *@version   1.2.1
 */


public class XingVBRHeader {

  /**
   * Where the XING head starts
   */
  private final String HEAD_START = "Xing";
  /**
   * the XING header size in int gesaved
   */
  private final int HEAD_SIZE = 4;
  /**
   * the Size of the Flags
   */
  private final int FLAGS_SIZE = 4;
  /**
   * the TOC Size
   */
  private final int TOC_SIZE = 100;
  /**
   * ???
   */
  private final double[] TIMEPERFRAME_TABLE = {-1, 1152, 1152, 384};

  /**
   * number of Frames
   */
  private int numFrames;
  /**
   * number of Bytes
   */
  private int numBytes;
  /**
   * ???
   */
  private int vbrScale;
  /**
   * ???
   */
  private byte[] toc;
  /**
   * average Bitrate
   */
  private int avgBitrate;
  /**
   * averege Playingtime
   */
  private int playingTime;
  /**
   * the legth
   */
  private int length;
  /**
   * Exsistence Mag
   */
  private boolean exists;


  /**
   * Looks for a Xing VBR header in the file. If it is found then that means
   * this is a variable bit rate file and all the data in the header will be
   * parsed.
   *
   *@param raf                         the file to read from
   *@param offset                      the location of the first mpeg frame
   *@param layer                       the layer value read by the
   *      MPEGAudioFrameHeader
   *@param mpegVersion                 the version value read by the
   *      MPEGAudioFrameHeader
   *@param sampleRate                  the sample rate read by the
   *      MPEGAudioFrameHeader
   *@param channelMode                 the channel mode read by the
   *      MPEGAudioFrameHeader
   *@exception IOException             if an error occurs
   *@exception CorruptHeaderException  if an error occurs
   */
  public XingVBRHeader(RandomAccessFile raf, long offset, int layer,
                       int mpegVersion, int sampleRate, int channelMode)
     throws IOException, CorruptHeaderException {

    exists = false;
    numFrames = -1;
    numBytes = -1;
    vbrScale = -1;
    avgBitrate = -1;
    playingTime = -1;
    length = -1;

    exists = checkHeader(raf, offset, channelMode, mpegVersion);

    if (exists) {
      readHeader(raf);
      calcValues(layer, mpegVersion, sampleRate);
    }
  }


  /**
   * Checks to see if a xing header is in this file
   *
   *@param raf              the file to read from
   *@param offset           the location of the first mpeg frame
   *@param channelMode      the channel mode read by the MPEGAudioFrameHeader
   *@param mpegVersion      the version value read by the MPEGAudioFrameHeader
   *@return                 true if a xing header exists
   *@exception IOException  if an error occurs
   */
  private boolean checkHeader(RandomAccessFile raf, long offset,
                              int channelMode, int mpegVersion)
     throws IOException {

    boolean b = false;
    byte head[] = new byte[HEAD_SIZE];

    if (mpegVersion == MPEGAudioFrameHeader.MPEG_V_1) {
      if (channelMode == MPEGAudioFrameHeader.MONO_MODE) {
        raf.seek(offset + 21);
      } else {
        raf.seek(offset + 36);
      }
    } else {
      if (channelMode == MPEGAudioFrameHeader.MONO_MODE) {
        raf.seek(offset + 23);
      } else {
        raf.seek(offset + 21);
      }
    }

    if (raf.read(head) == head.length) {
      b = HEAD_START.equals(new String(head));
    }

    return b;
  }


  /**
   * Parses the header data.
   *
   *@param raf                         the file to read from (pointing after the
   *      "Xing")
   *@exception IOException             if an error occurs
   *@exception CorruptHeaderException  if an error occurs
   */
  private void readHeader(RandomAccessFile raf)
     throws IOException, CorruptHeaderException {

    length = HEAD_SIZE;

    byte flags[] = new byte[FLAGS_SIZE];
    if (raf.read(flags) != flags.length) {
      throw new CorruptHeaderException("XingVBRHeader.readHeader: "
                                      + "Unexpected end-of-file encountered");
    }
    length += flags.length;

    if (BinaryParser.bitSet(flags[3], 0)) {
      numFrames = raf.readInt();
      length += 4;
    }
    if (BinaryParser.bitSet(flags[3], 1)) {
      numBytes = raf.readInt();
      length += 4;
    }
    if (BinaryParser.bitSet(flags[3], 2)) {
      toc = new byte[TOC_SIZE];

      if (raf.read(toc) != toc.length) {
        throw new CorruptHeaderException("XingVBRHeader.readHeader: "
                                        + "Unexpected end-of-file encountered");
      }

      length += TOC_SIZE;
    }
    if (BinaryParser.bitSet(flags[3], 3)) {
      vbrScale = raf.readInt();
      length += 4;
    }

    // If bytes aren't given then we can get a reasonable guess with the
    // file length
    if ((numBytes == -1) || (numBytes == 0)) {
      numBytes = (int) raf.length();
    }
  }


  /**
   * Calculates the playing time and the average bitrate.
   *
   *@param layer        the layer value read by the MPEGAudioFrameHeader
   *@param mpegVersion  the version value read by the MPEGAudioFrameHeader
   *@param sampleRate   the sample rate read by the MPEGAudioFrameHeader
   */
  private void calcValues(int layer, int mpegVersion, int sampleRate) {
    double tpf = TIMEPERFRAME_TABLE[layer] / sampleRate;

    if ((mpegVersion == MPEGAudioFrameHeader.MPEG_V_2)
        || (mpegVersion == MPEGAudioFrameHeader.MPEG_V_25)) {

      tpf /= 2;
    }

    playingTime = (int) (tpf * numFrames);
    avgBitrate = (int) ((numBytes * 8) / (tpf * numFrames * 1000));
  }


  /**
   * Returns true if a Xing VBR header was found in the file passed to the
   * constructor.
   *
   *@return   true if a Xinb VBR header was found
   */
  public boolean headerExists() {
    return exists;
  }


  /**
   * Returns the number of MPEG frames in the file passed to the constructor. If
   * a header is not found, -1 is returned.
   *
   *@return   the number of MPEG frames
   */
  public int getNumFrames() {
    return numFrames;
  }


  /**
   * Returns the number of data bytes in the mpeg frames. If a header is not
   * found, -1 is returned.
   *
   *@return   the number of data bytes in the mpeg frames
   */
  public int getNumBytes() {
    return numBytes;
  }


  /**
   * Returns the VBR scale used to generate this VBR file. If a header is not
   * found, -1 is returned.
   *
   *@return   the VBR scale used to generate this VBR file
   */
  public int getVBRScale() {
    return vbrScale;
  }


  /**
   * Returns the toc used to seek to an area of this VBR file. If a header is
   * not found an empty array is returned.
   *
   *@return   the toc used to seek to an area of this VBR file
   */
  public byte[] getTOC() {
    return toc;
  }


  /**
   * Returns the average bit rate of the mpeg file if a Xing header exists and
   * -1 otherwise.
   *
   *@return   the average bit rate (in kbps)
   */
  public int getAvgBitrate() {
    return avgBitrate;
  }


  /**
   * Returns the calculated playing time of the mpeg file if a Xing header
   * exists and -1 otherwise.
   *
   *@return   the playing time (in seconds) of the mpeg file
   */
  public int getPlayingTime() {
    return playingTime;
  }


  /**
   * Return a string representation of this object. Includes all information
   * read in and computed.
   *
   *@return   a string representation of this object
   */
  public String toString() {
    return "NumFrames:\t\t\t"  + getNumFrames() 
         + "\nNumBytes:\t\t\t" + getNumBytes() 
         + " bytes\nVBRScale:\t\t\t" + getVBRScale() 
         + "\nLength:\t\t\t\t" + getLength() + " bytes";
  }


  /**
   * Returns the length (in bytes) of this Xing VBR header.
   *
   *@return   the length of this Xing VBR header
   */
  public int getLength() {
    return length;
  }

}// XingVBRHeader

