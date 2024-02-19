package log.charter.gui.lookAndFeel;

import javax.swing.JSlider;
import javax.swing.plaf.metal.MetalSliderUI;

import log.charter.gui.ChartPanelColors.ColorLabel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class CharterSliderUI extends MetalSliderUI {

    private static Color sliderColor;
    private static Color thumbColor;
    private static Color trackFillColor;

    static {
        sliderColor = ColorLabel.BASE_BORDER.color();
        thumbColor = ColorLabel.BASE_HIGHLIGHT.color();
        trackFillColor = ColorLabel.BASE_HIGHLIGHT.color();
    }

    @Override
    protected void calculateThumbSize() {
        thumbRect.setSize(9, 9);
    }

    @Override
    public void paintThumb(final Graphics g) {
        if (slider.getOrientation() != JSlider.HORIZONTAL) {
            super.paintThumb(g);
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        setupGraphics(g2d);
        
        // thumb color
        g2d.setColor(thumbColor);
        g2d.fillOval(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height);
    }

    @Override
    public void paintTrack(final Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        setupGraphics(g2d);

        int cy = (trackRect.height - 2) / 2;
        int cw = trackRect.width;

        // before thumb
        g2d.setColor(trackFillColor);
        g2d.fillRect(trackRect.x, trackRect.y + cy, thumbRect.x - trackRect.x, 2);

        // after thumb
        g2d.setColor(sliderColor);
        g2d.fillRect(thumbRect.x + thumbRect.width / 2, trackRect.y + cy,
                trackRect.x + cw - thumbRect.x - thumbRect.width / 2, 2);
    }

    private void setupGraphics(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }
}
