package weather;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class WeatherAppUi extends JFrame {
    private final JTextField txtCity;
    private final JButton btnSearch, btnFav, btnHist, btnLoc;
    private final JLabel lblCity, lblTemp, lblCond, lblWind, lblBigIcon;
    private final JPanel forecastPanel;
    private final ForecastChartPanel chartPanel;

    private WeatherData lastData;

    public WeatherAppUi() {
        setTitle("Tra cứu Thời tiết");
        setSize(1100, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Top bar
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        txtCity = new JTextField(30);
        btnSearch = new JButton("Tra cứu");
        btnFav = new JButton("Yêu thích");
        btnHist = new JButton("Lịch sử");
        btnLoc = new JButton("Vị trí");

        styleButton(btnSearch);
        styleButton(btnFav);
        styleButton(btnHist);
        styleButton(btnLoc);

        top.setBackground(new Color(230, 240, 255));
        top.add(new JLabel("Thành phố:"));
        top.add(txtCity);
        top.add(btnSearch);
        top.add(btnFav);
        top.add(btnHist);
        top.add(btnLoc);

        add(top, BorderLayout.NORTH);

        // Center
        JPanel center = new JPanel(new BorderLayout(12, 12));
        center.setBackground(new Color(245, 250, 255));

        lblBigIcon = new JLabel("", SwingConstants.CENTER);
        lblBigIcon.setPreferredSize(new Dimension(240, 240));
        center.add(lblBigIcon, BorderLayout.WEST);

        JPanel info = new JPanel(new GridLayout(4, 1, 6, 6));
        info.setBackground(new Color(245, 250, 255));

        lblCity = new JLabel("Chưa tra cứu", SwingConstants.LEFT);
        lblCity.setFont(lblCity.getFont().deriveFont(Font.BOLD, 22f));
        lblCity.setForeground(new Color(0, 102, 204));

        lblTemp = new JLabel("", SwingConstants.LEFT);
        lblTemp.setFont(lblTemp.getFont().deriveFont(18f));
        lblTemp.setForeground(new Color(220, 50, 50));

        lblCond = new JLabel("", SwingConstants.LEFT);
        lblCond.setForeground(new Color(60, 60, 60));

        lblWind = new JLabel("", SwingConstants.LEFT);
        lblWind.setForeground(new Color(60, 60, 60));

        info.add(lblCity);
        info.add(lblTemp);
        info.add(lblCond);
        info.add(lblWind);

        center.add(info, BorderLayout.CENTER);

        forecastPanel = new JPanel(new GridLayout(1, 5, 12, 12));
        forecastPanel.setPreferredSize(new Dimension(0, 240));
        forecastPanel.setBackground(new Color(250, 252, 255));
        center.add(forecastPanel, BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);

        chartPanel = new ForecastChartPanel();
        add(chartPanel, BorderLayout.SOUTH);

        // Events
        btnSearch.addActionListener(this::onSearch);
        txtCity.addActionListener(e -> btnSearch.doClick());
        btnFav.addActionListener(e -> new FavoritesUi(this));
        btnHist.addActionListener(e -> new HistoryUi(this));
        btnLoc.addActionListener(e -> openLocation());

        setVisible(true);
    }

    private void styleButton(JButton btn) {
        btn.setBackground(new Color(100, 149, 237));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(btn.getFont().deriveFont(Font.BOLD, 13f));
    }

    private void onSearch(ActionEvent e) {
        String rawCity = txtCity.getText();
        if (rawCity == null || rawCity.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên thành phố.");
            return;
        }
        try {
            String city = StringUtils.normalizeCity(rawCity); // chuẩn hóa tên city

            WeatherService svc = new WeatherServiceImpl();
            WeatherData data = svc.getWeather(city);
            if (data == null) {
                JOptionPane.showMessageDialog(this, "Không lấy được dữ liệu.");
                return;
            }
            // Giữ lại city gốc để hiển thị
            data.city = rawCity.trim();
            lastData = data;

            HistoryService.add(data);
            updateUI(data);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi lấy dữ liệu: " + ex.getMessage());
        }
    }

    private void updateUI(WeatherData d) {
        lblCity.setText(d.city + (d.country != null ? ", " + d.country : ""));
        lblTemp.setText(String.format("Nhiệt độ: %.1f °C", d.temperature));
        lblCond.setText("Thời tiết: " + d.condition);
        lblWind.setText(String.format("Gió: %.1f m/s", d.windSpeed));

        // Big icon
        String mapped = WeatherIconMapper.map(d.icon);
        ImageIcon big = IconLoader.loadIcon(mapped, 220, 220);
        lblBigIcon.setIcon(big);

        // Forecast 5 ngày
        forecastPanel.removeAll();
        if (d.forecast != null && !d.forecast.isEmpty()) {
            for (WeatherData.ForecastDay fd : d.forecast) {
                RoundedPanel card = new RoundedPanel();
                card.setLayout(new BorderLayout());
                card.setBackground(new Color(240, 248, 255));

                JLabel date = new JLabel(fd.date, SwingConstants.CENTER);
                date.setForeground(new Color(0, 102, 204));

                String mappedF = WeatherIconMapper.map(fd.icon);
                ImageIcon ic = IconLoader.loadIcon(mappedF, 64, 64);
                JLabel il = new JLabel(ic);
                il.setHorizontalAlignment(SwingConstants.CENTER);

                JLabel temps = new JLabel(String.format("Min %.0f° / Max %.0f°", fd.minTemp, fd.maxTemp), SwingConstants.CENTER);
                temps.setForeground(new Color(200, 50, 50));

                card.add(date, BorderLayout.NORTH);
                card.add(il, BorderLayout.CENTER);
                card.add(temps, BorderLayout.SOUTH);
                forecastPanel.add(card);
            }
        }
        forecastPanel.revalidate();
        forecastPanel.repaint();

        // chart
        chartPanel.setData(d.forecast);
    }

    private void openLocation() {
        if (lastData == null) {
            JOptionPane.showMessageDialog(this, "Chưa có dữ liệu để tra cứu.");
            return;
        }
        try {
            double lat = lastData.latitude;
            double lon = lastData.longitude;

            if (lat == 0 && lon == 0) {
                JOptionPane.showMessageDialog(this, "Không có thông tin tọa độ để mở bản đồ.");
                return;
            }

            String url = String.format("https://www.google.com/maps/search/?api=1&query=%f,%f", lat, lon);
            Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Không mở được bản đồ: " + ex.getMessage());
        }
    }

    public void searchForCity(String city) {
        txtCity.setText(city);
        btnSearch.doClick();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WeatherAppUi::new);
    }
}
