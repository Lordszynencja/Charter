package log.charter.gui.lookAndFeel;

import static log.charter.util.ColorUtils.mix;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.ButtonModel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;

import log.charter.gui.ChartPanelColors.ColorLabel;

class CharterRadioButton {
	private static class RadioIcon extends SimpleIcon {
		/**
		 * 0-empty<br/>
		 * 1 - interior<br/>
		 * 2 - edge<br/>
		 * 3 - edgeHighlight<br/>
		 * 4 - edgeShadow
		 */
		private static final int[][] colorMap = { //
				{ 0, 0, 0, 0, 0, 2, 2, 2, 2, 0, 0, 0, 0, 0 }, //
				{ 0, 0, 0, 2, 2, 4, 4, 4, 4, 2, 2, 0, 0, 0 }, //
				{ 0, 0, 2, 4, 4, 4, 1, 1, 4, 4, 4, 2, 0, 0 }, //
				{ 0, 2, 4, 4, 1, 1, 1, 1, 1, 1, 3, 3, 2, 0 }, //
				{ 0, 2, 4, 1, 1, 1, 1, 1, 1, 1, 1, 3, 2, 0 }, //
				{ 2, 4, 4, 1, 1, 1, 1, 1, 1, 1, 1, 3, 3, 2 }, //
				{ 2, 4, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 2 }, //
				{ 2, 4, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 2 }, //
				{ 2, 4, 4, 1, 1, 1, 1, 1, 1, 1, 1, 3, 3, 2 }, //
				{ 0, 2, 4, 1, 1, 1, 1, 1, 1, 1, 1, 3, 2, 0 }, //
				{ 0, 2, 4, 3, 1, 1, 1, 1, 1, 1, 3, 3, 2, 0 }, //
				{ 0, 0, 2, 3, 3, 3, 1, 1, 3, 3, 3, 2, 0, 0 }, //
				{ 0, 0, 0, 2, 2, 3, 3, 3, 3, 2, 2, 0, 0, 0 }, //
				{ 0, 0, 0, 0, 0, 2, 2, 2, 2, 0, 0, 0, 0, 0 },//
		};

		private final Color interiorColor;
		private final Color disabledInteriorColor;
		private final Color armedInteriorColor;
		private final Color edgeColor;
		private final Color edgeHighlightColor;
		private final Color edgeShadowColor;
		private final Color selectColor;

		private final BufferedImage[] images = new BufferedImage[2];

		private void initImages() {
			for (int i = 0; i < 2; i++) {
				final Color fillColor = i == 1 ? interiorColor : disabledInteriorColor;

				final Color[] colors = new Color[] { //
						new Color(0, 0, 0, 0), //
						fillColor, //
						edgeColor, //
						edgeHighlightColor, //
						edgeShadowColor };

				images[i] = IconMaker.createIcon(width, height, colorMap, colors);
			}
		}

		public RadioIcon(final Color interiorColor, final Color disabledInteriorColor, final Color edgeColor,
				final Color selectColor) {
			super(14, 14);
			this.interiorColor = interiorColor;
			this.disabledInteriorColor = disabledInteriorColor;
			armedInteriorColor = mix(interiorColor, disabledInteriorColor, 0.5);
			this.edgeColor = edgeColor;
			edgeHighlightColor = mix(interiorColor, Color.WHITE, 0.5);
			edgeShadowColor = mix(edgeColor, Color.BLACK, 0.5);
			this.selectColor = selectColor;

			initImages();
		}

		private Color getFillColor(final ButtonModel model) {
			if (!model.isEnabled()) {
				return disabledInteriorColor;
			}

			if (model.isArmed()) {
				return armedInteriorColor;
			}

			return interiorColor;
		}

		@Override
		public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
			final JRadioButton radioButton = (JRadioButton) c;

			final Color fillColor = getFillColor(radioButton.getModel());

			final Color[] colors = new Color[] { //
					new Color(0, 0, 0, 0), //
					fillColor, //
					edgeColor, //
					edgeHighlightColor, //
					edgeShadowColor };

			g.translate(x, y);
			g.drawImage(IconMaker.createIcon(width, height, colorMap, colors), 0, 0, null);

			if (radioButton.isSelected()) {
				g.setColor(selectColor);
				g.fillRect(5, 6, 1, 2);
				g.fillRect(6, 5, 2, 4);
				g.fillRect(8, 6, 1, 2);
			}

			g.translate(-x, -y);
		}
	}

	static void install() {
		final Color interiorColor = ColorLabel.BASE_BG_3.color();
		final Color disabledInteriorColor = ColorLabel.BASE_BG_2.color();
		final Color edgeColor = ColorLabel.BASE_BG_2.color();
		final Color selectColor = ColorLabel.BASE_BG_1.color();

		UIManager.put("RadioButton.icon", new RadioIcon(interiorColor, disabledInteriorColor, edgeColor, selectColor));
	}
}
