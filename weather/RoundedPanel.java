package weather;

import javax.swing.*;
import java.awt.*;

public class RoundedPanel extends JPanel {
    private final int cornerRadius = 16;

    public RoundedPanel() {
        setOpaque(false);
        setBackground(new Color(255, 255, 255, 200));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension arcs = new Dimension(cornerRadius, cornerRadius);
        int width = getWidth();
        int height = getHeight();
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height);
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height);
    }
}
