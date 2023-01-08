package log.charter.gui.lookAndFeel;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;

import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.CharterFrame;
import log.charter.io.Logger;

public class CharterTheme extends DefaultMetalTheme {
	public static final String name = "RS charter";

	public static void install(final CharterFrame frame) {
		MetalLookAndFeel.setCurrentTheme(new CharterTheme());
		CharterRadioButton.install();
		CharterCheckBox.install();

		for (final LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			if ("Metal".equals(info.getName())) {
				try {
					UIManager.setLookAndFeel(info.getClassName());
				} catch (final Exception e) {
					Logger.error("Error when setting look and feel", e);
				}
				break;
			}
		}
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
		return new ColorUIResource(ColorLabel.BASE_BG_3.color());
	}

	@Override
	public ColorUIResource getControlTextColor() {
		return new ColorUIResource(ColorLabel.BASE_TEXT.color());
	}

	@Override
	public ColorUIResource getDesktopColor() {
		return new ColorUIResource(0, 255, 255);
	}

	/**
	 * dropdown arrows
	 */
	@Override
	public ColorUIResource getControlInfo() {
		return new ColorUIResource(ColorLabel.BASE_1.color());
	}

	/**
	 * focus box on buttons color
	 */
	@Override
	public ColorUIResource getFocusColor() {
		return new ColorUIResource(ColorLabel.BASE_1.color());
	}

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
		return new ColorUIResource(ColorLabel.BASE_2.color());
	}

	/**
	 * menu border color
	 */
	@Override
	public ColorUIResource getPrimaryControlDarkShadow() {
		return new ColorUIResource(ColorLabel.BASE_BG_4.color());
	}

	/**
	 * menu highlight border, icon highlight color
	 */
	@Override
	public ColorUIResource getPrimaryControlHighlight() {
		return new ColorUIResource(ColorLabel.BASE_BG_3.color());
	}

	/**
	 * menu shadow, icon shadow color
	 */
	@Override
	public ColorUIResource getPrimaryControlShadow() {
		return new ColorUIResource(ColorLabel.BASE_BG_4.color());
	}

	/**
	 * separator background color
	 */
	@Override
	public ColorUIResource getSeparatorBackground() {
		return new ColorUIResource(ColorLabel.BASE_BG_1.color());
	}

	/**
	 * separator foreground color
	 */
	@Override
	public ColorUIResource getSeparatorForeground() {
		return new ColorUIResource(ColorLabel.BASE_BG_4.color());
	}

	@Override
	public ColorUIResource getSystemTextColor() {
		return new ColorUIResource(ColorLabel.BASE_DARK_TEXT.color());
	}

	@Override
	public ColorUIResource getTextHighlightColor() {
		return new ColorUIResource(ColorLabel.BASE_BG_4.color());
	}

	@Override
	public ColorUIResource getUserTextColor() {
		return new ColorUIResource(ColorLabel.BASE_DARK_TEXT.color());
	}

	/**
	 * background of a window color, for example file select
	 */
	@Override
	public ColorUIResource getWindowBackground() {
		return new ColorUIResource(ColorLabel.BASE_BG_3.color());
	}
}
