/*
 * FLAC library (Java)
 *
 * Copyright (c) Project Nayuki
 * https://www.nayuki.io/page/flac-library-java
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program (see COPYING.txt and COPYING.LESSER.txt).
 * If not, see <http://www.gnu.org/licenses/>.
 */

package io.nayuki.flac.encode;

import java.io.IOException;

import io.nayuki.flac.common.StreamInfo;
import io.nayuki.flac.encode.SubframeEncoder.SearchOptions;

public final class FlacEncoder {
	public static final int DEFAULT_BLOCK_SIZE = 4096;

	private final StreamInfo info;
	private final SearchOptions opt;
	private final int blockSize;

	private int pos = 0;

	public FlacEncoder(final StreamInfo info, final SearchOptions opt) {
		this(info, opt, DEFAULT_BLOCK_SIZE);
	}

	public FlacEncoder(final StreamInfo info, final SearchOptions opt, final int blockSize) {
		this.info = info;
		this.opt = opt;
		this.blockSize = blockSize;
	}

	private void setStartingInfoValues() {
		info.minBlockSize = blockSize;
		info.maxBlockSize = blockSize;
		info.minFrameSize = 0;
		info.maxFrameSize = 0;
	}

	public void encode(final int[][] samples, final BitOutputStream out) throws IOException {
		setStartingInfoValues();

		while (pos < samples[0].length) {
			final int blockLength = Math.min(samples[0].length - pos, blockSize);
			final long[][] subsamples = getRange(samples, pos, blockLength);

			final FrameEncoder enc = FrameEncoder.computeBest(pos, subsamples, info.sampleDepth, info.sampleRate,
					opt).encoder;
			final long startByte = out.getByteCount();
			enc.encode(subsamples, out);
			final long frameSize = out.getByteCount() - startByte;
			if (frameSize < 0 || (int) frameSize != frameSize) {
				throw new AssertionError();
			}

			if (info.minFrameSize == 0 || frameSize < info.minFrameSize) {
				info.minFrameSize = (int) frameSize;
			}
			if (frameSize > info.maxFrameSize) {
				info.maxFrameSize = (int) frameSize;
			}

			pos += blockLength;
		}
	}

	// Returns the subrange array[ : ][off : off + len] upcasted to long.
	private static long[][] getRange(final int[][] array, final int off, final int len) {
		final long[][] result = new long[array.length][len];
		for (int i = 0; i < array.length; i++) {
			final int[] src = array[i];
			final long[] dest = result[i];

			for (int j = 0; j < len; j++) {
				dest[j] = src[off + j];
			}
		}
		return result;
	}

	public int getCurrentPosition() {
		return pos;
	}
}
