package log.charter.gui.lookAndFeel;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.CharterFrame;
import log.charter.io.Logger;

import java.awt.*;

public class CharterTheme extends DefaultMetalTheme {
	public static final String name = "Charter";

	public static void install(final CharterFrame frame) {
		MetalLookAndFeel.setCurrentTheme(new CharterTheme());
		CharterThemeInstall();

		try {
			for (final LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Metal".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (final Exception e) {
			Logger.error("Error when setting look and feel", e);
		}
	}

	private static void CharterThemeInstall() {
		CharterRadioButton.install();
		CharterCheckBox.install();
		CharterButtonUI.install(); // add
		CharterToggleButtonUI.install(); // add
		CharterTextFieldUI.install(); // add
		CharterScrollBarUI.install(); // TODO doesn't work but the custom ui is already designed

		//UIManager.put("Label.font", new Font("SansSerif", Font.PLAIN, 12));
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * menu shortcut font color
	 */
	@Override
	public ColorUIResource getAcceleratorForeground() {
		return new ColorUIResource(ColorLabel.BASE_DARK_TEXT.color());
	}

	/**
	 * menu shortcut selected font color
	 */
	@Override
	public ColorUIResource getAcceleratorSelectedForeground() {
		return new ColorUIResource(ColorLabel.BASE_TEXT.color());
	}

	/**
	 * background for popups
	 */
	@Override
	public ColorUIResource getControl() {
		return new ColorUIResource(ColorLabel.BASE_BG_2.color());
	}

	/**
	 * color for dark shadow on buttons
	 */
	@Override
	public ColorUIResource getControlDarkShadow() {
		return new ColorUIResource(ColorLabel.BASE_BG_4.color());
	}

	@Override
	public ColorUIResource getControlDisabled() {
		return new ColorUIResource(ColorLabel.BASE_BG_2.color());
	}

	/**
	 * color for antishadow on buttons
	 */
	@Override
	public ColorUIResource getControlHighlight() {
		return new ColorUIResource(ColorLabel.BASE_BG_1.color());
	}

	@Override
	public ColorUIResource getControlShadow() {
		return new ColorUIResource(ColorLabel.BASE_BG_2.color());
	} // changed

	@Override
	public ColorUIResource getControlTextColor() {
		return new ColorUIResource(ColorLabel.BASE_TEXT.color());
	}

	@Override
	public ColorUIResource getDesktopColor() {
		return new ColorUIResource(ColorLabel.BASE_HIGHLIGHT.color());
	} //changed

	/**
	 * dropdown arrows
	 */
	@Override
	public ColorUIResource getControlInfo() {
		return new ColorUIResource(ColorLabel.BASE_HIGHLIGHT.color());
	} // changed

	/**
	 * focus box on buttons color
	 */
	@Override
	public ColorUIResource getFocusColor() {
		return new ColorUIResource(ColorLabel.BASE_HIGHLIGHT.color());
	} // changed

	/**
	 * highlighted text color
	 */
	@Override
	public ColorUIResource getHighlightedTextColor() {
		return new ColorUIResource(ColorLabel.BASE_TEXT.color());
	}

	/**
	 * menu background color
	 */
	@Override
	public ColorUIResource getMenuBackground() {
		return new ColorUIResource(ColorLabel.BASE_BG_2.color());
	}

	/**
	 * disabled menu option background color
	 */
	@Override
	public ColorUIResource getMenuDisabledForeground() {
		return new ColorUIResource(ColorLabel.BASE_TEXT.color());
	}

	/**
	 * menu font color
	 */
	@Override
	public ColorUIResource getMenuForeground() {
		return new ColorUIResource(ColorLabel.BASE_TEXT.color());
	}

	/**
	 * selected menu option background color
	 */
	@Override
	public ColorUIResource getMenuSelectedBackground() {
		return new ColorUIResource(ColorLabel.BASE_BG_3.color());
	}

	/**
	 * selected menu option font color
	 */
	@Override
	public ColorUIResource getMenuSelectedForeground() {
		return new ColorUIResource(ColorLabel.BASE_TEXT.color());
	}

	/**
	 * icon main color
	 */
	@Override
	public ColorUIResource getPrimaryControl() {
		return new ColorUIResource(ColorLabel.BASE_HIGHLIGHT.color()); // changed
	}

	/**
	 * menu border color
	 */
	@Override
	public ColorUIResource getPrimaryControlDarkShadow() {
		return new ColorUIResource(ColorLabel.BASE_BORDER.color()); // changed
	}

	/**
	 * menu highlight border, icon highlight color
	 */
	@Override
	public ColorUIResource getPrimaryControlHighlight() {
		return new ColorUIResource(ColorLabel.BASE_BG_2.color()); // changed
	}

	/**
	 * menu shadow, icon shadow color
	 */
	@Override
	public ColorUIResource getPrimaryControlShadow() {
		return new ColorUIResource(ColorLabel.BASE_HIGHLIGHT.color()); // changed
	}

	/**
	 * separator background color
	 */
	@Override
	public ColorUIResource getSeparatorBackground() {
		return new ColorUIResource(ColorLabel.BASE_BG_2.color()); // changed
	}

	/**
	 * separator foreground color
	 */
	@Override
	public ColorUIResource getSeparatorForeground() {
		return new ColorUIResource(ColorLabel.BASE_BORDER.color()); // changed
	}

	@Override
	public ColorUIResource getSystemTextColor() { return new ColorUIResource(ColorLabel.BASE_TEXT.color()); } // changed

	@Override
	public ColorUIResource getTextHighlightColor() {
		return new ColorUIResource(ColorLabel.BASE_BG_4.color());
	}

	@Override
	public ColorUIResource getUserTextColor() {
		return new ColorUIResource(ColorLabel.BASE_TEXT.color());
	} // changed

	/**
	 * background of a window color, for example file select
	 */
	@Override
	public ColorUIResource getWindowBackground() {
		return new ColorUIResource(ColorLabel.BASE_BG_2.color());
	}
}
