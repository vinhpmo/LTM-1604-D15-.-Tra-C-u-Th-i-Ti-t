package weather;

import javax.swing.*;
import java.awt.*;

public class RoundedPanel extends JPanel {
    private int cornerRadius = 15;

    public RoundedPanel() {
        super();
        setOpaque(false); // để nền trong suốt
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Dimension arcs = new Dimension(cornerRadius, cornerRadius);
        int width = getWidth();
        int height = getHeight();

        Graphics2D graphics = (Graphics2D) g;
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Gradient giống phần top panel
        GradientPaint gp = new GradientPaint(
                0, 0, new Color(18, 120, 200),
                0, height, new Color(20, 180, 220)
        );
        graphics.setPaint(gp);

        // Vẽ nền bo góc
        graphics.fillRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height);

        // Vẽ border bo góc
        graphics.setColor(getForeground() != null ? getForeground() : Color.WHITE);
        graphics.drawRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height);
    }
}
