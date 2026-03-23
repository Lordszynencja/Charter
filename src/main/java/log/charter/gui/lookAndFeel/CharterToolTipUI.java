package log.charter.gui.lookAndFeel;

import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalToolTipUI;

import log.charter.data.config.ChartPanelColors.ColorLabel;

public class CharterToolTipUI extends MetalToolTipUI {

	private static final CharterToolTipUI toolTipUI = new CharterToolTipUI();

	public static ComponentUI createUI(final JComponent c) {
		return toolTipUI;
	}

	@Override
	public void installUI(final JComponent c) {
		super.installUI(c);
		final JToolTip tooltip = (JToolTip) c;
		tooltip.setOpaque(true);
		tooltip.setBackground(ColorLabel.BASE_HIGHLIGHT.color());
		tooltip.setForeground(ColorLabel.BASE_BG_1.color());
		tooltip.setFont(tooltip.getFont().deriveFont(Font.PLAIN));
	}

	static void install() {
		UIManager.put("ToolTipUI", CharterToolTipUI.class.getName());
	}
}
