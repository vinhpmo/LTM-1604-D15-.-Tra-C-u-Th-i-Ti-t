package weather;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Vẽ biểu đồ cột 5 ngày (mỗi ngày 1 cột) — tự vẽ, không dùng JFreeChart.
 * Public methods: setForecast(list), getForecast(), loadIcon(...) để UI dùng.
 */
public class ForecastChartPanel extends JPanel {
    private WeatherData.ForecastDay[] data = new WeatherData.ForecastDay[0];
    private final Map<String, ImageIcon> iconCache = new HashMap<>();

    public ForecastChartPanel() {
        setPreferredSize(new Dimension(800, 260));
        setOpaque(false);

        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleClick(evt.getX(), evt.getY());
            }
        });
    }

    public void setForecast(java.util.List<WeatherData.ForecastDay> list) {
        if (list == null) data = new WeatherData.ForecastDay[0];
        else data = list.toArray(new WeatherData.ForecastDay[0]);
        repaint();
    }

    public WeatherData.ForecastDay[] getForecast() { return data; }

    /**
     * Load icon from classpath "/icons/" or filesystem "src/icons/".
     * maxW/maxH used to scale if necessary.
     */
    public ImageIcon loadIcon(String filename, int maxW, int maxH) {
        if (filename == null) return null;
        if (iconCache.containsKey(filename)) return iconCache.get(filename);
        try {
            URL res = getClass().getResource("/icons/" + filename);
            ImageIcon ic = null;
            if (res != null) ic = new ImageIcon(res);
            else {
                File f = new File("src/icons/" + filename);
                if (f.exists()) ic = new ImageIcon(f.getAbsolutePath());
            }
            if (ic != null) {
                int iw = ic.getIconWidth(), ih = ic.getIconHeight();
                if ( (maxW>0 && iw>maxW) || (maxH>0 && ih>maxH) ) {
                    Image scaled = ic.getImage().getScaledInstance(maxW, maxH, Image.SCALE_SMOOTH);
                    ic = new ImageIcon(scaled);
                }
            }
            iconCache.put(filename, ic);
            return ic;
        } catch (Exception ex) {
            iconCache.put(filename, null);
            return null;
        }
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        g.setColor(new Color(255,255,255,20));
        g.fillRoundRect(8,8,w-16,h-16,14,14);

        if (data == null || data.length == 0) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.PLAIN, 14));
            g.drawString("Không có dữ liệu dự báo", 20, 30);
            return;
        }

        int n = data.length;
        int left = 60, right = 40, top = 20, bottom = 110;
        int chartW = w - left - right;
        int chartH = h - top - bottom;
        if (chartH < 120) chartH = 120;

        double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
        for (WeatherData.ForecastDay fd : data) {
            min = Math.min(min, fd.avgTemp);
            max = Math.max(max, fd.avgTemp);
        }
        if (min == max) { min -= 5; max += 5; }
        double range = max - min;

        int gap = 22;
        int colW = (chartW - (n - 1) * gap) / n;
        if (colW < 28) colW = Math.max(28, chartW / (n * 2));

        // draw horizontal grid + labels
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g.setColor(new Color(200,200,200));
        int grid = 4;
        for (int i = 0; i <= grid; i++) {
            int yy = top + (int)(chartH - (chartH * (i / (double)grid)));
            g.drawLine(left, yy, left + chartW, yy);
            double v = min + (range * (i / (double)grid));
            g.setColor(new Color(150,150,150));
            g.drawString(String.format("%.0f°C", v), 8, yy + 4);
            g.setColor(new Color(200,200,200));
        }

        // draw bars + icons + labels
        for (int i = 0; i < n; i++) {
            WeatherData.ForecastDay fd = data[i];
            int x = left + i * (colW + gap);
            double v = fd.avgTemp;
            int barH = (int)((v - min) / range * chartH);
            int y = top + (chartH - barH);

            GradientPaint gp = new GradientPaint(x, y, new Color(70,160,240), x, y + barH, new Color(120,200,255));
            g.setPaint(gp);
            g.fillRoundRect(x, y, colW, barH, 10, 10);
            g.setColor(new Color(255,255,255,140));
            g.drawRoundRect(x, y, colW, barH, 10, 10);

            // temp label above
            String tlabel = String.format("%.0f°C", v);
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            int tw = g.getFontMetrics().stringWidth(tlabel);
            g.setColor(new Color(30,30,30));
            g.drawString(tlabel, x + (colW - tw)/2, y - 8);

            // icon under bar
            ImageIcon ic = loadIcon(fd.iconFile, 40, 40);
            int iconH = 0;
            if (ic != null) {
                int iw = ic.getIconWidth(), ih = ic.getIconHeight();
                int ix = x + (colW - iw)/2;
                int iy = top + chartH + 6;
                ic.paintIcon(this, g, ix, iy); // preserve animation if not scaled
                iconH = ih;
            }

            // date under icon
            String dl = fd.date.length() >= 10 ? fd.date.substring(5) : fd.date;
            g.setFont(new Font("SansSerif", Font.PLAIN, 12));
            int dw = g.getFontMetrics().stringWidth(dl);
            int dx = x + (colW - dw)/2;
            int dy = top + chartH + 6 + iconH + 16;
            g.setColor(new Color(40,40,40));
            g.drawString(dl, dx, dy);
        }
    }

    private void handleClick(int mx, int my) {
        if (data == null || data.length == 0) return;
        int w = getWidth(), h = getHeight();
        int left = 60, right = 40, top = 20, bottom = 110;
        int chartW = w - left - right;
        int chartH = h - top - bottom;
        int n = data.length;
        int gap = 22;
        int colW = (chartW - (n - 1) * gap) / n;
        for (int i = 0; i < n; i++) {
            int x = left + i * (colW + gap);
            int yTop = top;
            int yBot = top + chartH;
            if (mx >= x && mx <= x + colW && my >= yTop && my <= yBot + bottom) {
                showDetailDialog(data[i]);
                break;
            }
        }
    }

    private void showDetailDialog(WeatherData.ForecastDay fd) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Chi tiết " + fd.date, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(320, 300);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(240,250,255));
        p.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        JLabel ld = new JLabel("📅 " + fd.date, SwingConstants.CENTER);
        ld.setFont(new Font("SansSerif", Font.BOLD, 16));
        ld.setAlignmentX(Component.CENTER_ALIGNMENT);

        ImageIcon ic = loadIcon(fd.iconFile, 120, 120);
        JLabel li = new JLabel();
        if (ic != null) li.setIcon(ic);
        li.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lt = new JLabel(String.format("🌡 %s°C - %s°C", (int)fd.minTemp, (int)fd.maxTemp), SwingConstants.CENTER);
        lt.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel ldsc = new JLabel(fd.description != null ? fd.description : "", SwingConstants.CENTER);
        ldsc.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btn = new JButton("Đóng");
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.addActionListener(e -> dlg.dispose());

        p.add(ld);
        p.add(Box.createVerticalStrut(8));
        p.add(li);
        p.add(Box.createVerticalStrut(8));
        p.add(lt);
        p.add(Box.createVerticalStrut(6));
        p.add(ldsc);
        p.add(Box.createVerticalStrut(12));
        p.add(btn);

        dlg.setContentPane(p);
        dlg.setVisible(true);
    }
}
