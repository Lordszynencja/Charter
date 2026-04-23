package log.charter.util;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public class MultiComposite implements Composite {
	private static int mix(final int a1, final int a2, final int c1, final int c2) {
		return (a1 * c1) / 255 + a2 * (255 - a1) * c2 / 255 / 255;
	}

	private static int clamp(final int v) {
		return max(0, min(255, v));
	}

	class MultiCompositeContext implements CompositeContext {
		private final ColorModel srcCM;
		private final ColorModel dstCM;

		MultiCompositeContext(final ColorModel srcCM, final ColorModel dstCM) {
			this.srcCM = srcCM;
			this.dstCM = dstCM;
		}

		@Override
		public void compose(final Raster src, final Raster dstIn, final WritableRaster dstOut) {
			final int w = Math.min(src.getWidth(), dstIn.getWidth());
			final int h = Math.min(src.getHeight(), dstIn.getHeight());
			for (int x = 0; x < w; ++x) {
				for (int y = 0; y < h; ++y) {
					final int rgb1 = srcCM.getRGB(src.getDataElements(x, y, null));
					final int a1 = ((rgb1 >> 24) & 0xFF) * alpha / 255;
					final int r1 = (rgb1 >> 16) & 0xFF;
					final int g1 = (rgb1 >> 8) & 0xFF;
					final int b1 = rgb1 & 0xFF;

					final int rgb2 = dstCM.getRGB(dstIn.getDataElements(x, y, null));
					final int a2 = (rgb2 >> 24) & 0xFF;
					final int r2 = (rgb2 >> 16) & 0xFF;
					final int g2 = (rgb2 >> 8) & 0xFF;
					final int b2 = rgb2 & 0xFF;

					final int a3 = mix(a1, a2, 255, 255);
					final int r3 = mix(a1, a2, clamp(r1 * colorMultiply / 255 + colorAdd), r2);
					final int g3 = mix(a1, a2, clamp(g1 * colorMultiply / 255 + colorAdd), g2);
					final int b3 = mix(a1, a2, clamp(b1 * colorMultiply / 255 + colorAdd), b2);
					final int rgb3 = (a3 << 24) | (r3 << 16) | (g3 << 8) | b3;

					dstOut.setDataElements(x, y, dstCM.getDataElements(rgb3, null));
				}
			}
		}

		@Override
		public void dispose() {
		}
	}

	private int alpha = 255;
	private int colorAdd = 0;
	private int colorMultiply = 255;

	public int alpha() {
		return alpha;
	}

	public MultiComposite alpha(final int alpha) {
		this.alpha = max(0, min(255, alpha));
		return this;
	}

	public int colorAdd() {
		return colorAdd;
	}

	public MultiComposite colorAdd(final int colorAdd) {
		this.colorAdd = max(0, min(255, colorAdd));
		return this;
	}

	public int colorMultiply() {
		return colorMultiply;
	}

	public MultiComposite colorMultiply(final int colorMultiply) {
		this.colorMultiply = max(0, min(255, colorMultiply));
		return this;
	}

	@Override
	public CompositeContext createContext(final ColorModel srcColorModel, final ColorModel dstColorModel,
			final RenderingHints hints) {
		return new MultiCompositeContext(srcColorModel, dstColorModel);
	}

}
