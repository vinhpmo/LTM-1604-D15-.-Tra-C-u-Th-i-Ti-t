package weather;

import org.json.JSONArray;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import weather.WeatherClient;


public class WeatherAppUi extends JFrame {
    private final JTextField txtCity = new JTextField();
    private final JButton btnSearch = new JButton("Tra cứu");
    private final JButton btnMap = new JButton("📍 Bản đồ");
    private final JButton btnAddFav = new JButton("★ Thêm yêu thích");
    private final JButton btnFavWindow = new JButton("Yêu thích");
    private final JComboBox<String> langCombo = new JComboBox<>(new String[]{"vi","en"});

    private final JLabel lblCity = new JLabel("—");
    private final JLabel lblTime = new JLabel("—");
    private final JLabel lblTemp = new JLabel("—");
    private final JLabel lblDesc = new JLabel("—");
    private final JLabel lblHumidity = new JLabel("—");
    private final JLabel lblWind = new JLabel("—");
    private final JLabel mainIcon = new JLabel();

    private final DefaultListModel<String> historyModel = new DefaultListModel<>();
    private final JList<String> historyList = new JList<>(historyModel);

    private final DefaultListModel<String> favModel = new DefaultListModel<>();
    private final JList<String> favList = new JList<>(favModel);

    private final DefaultListModel<String> fiveModel = new DefaultListModel<>();
    private final JList<String> fiveList = new JList<>(fiveModel);

    private final ForecastChartPanel chartPanel = new ForecastChartPanel();

    private WeatherClient client;
    private String apiKey = "29061fdbdaa260c4fce8e14b51f29afe"; // <-- THAY API KEY Ở ĐÂY
    private String lang = "vi";

    private final File configDir;
    private final File favFile;
    private final File histFile;

    public WeatherAppUi() {
        super("Ứng dụng Thời tiết");
        String home = System.getProperty("user.home");
        configDir = new File(home, ".weather_app");
        if (!configDir.exists()) configDir.mkdirs();
        favFile = new File(configDir, "favorites.json");
        histFile = new File(configDir, "history.txt");

        // Nếu WeatherClient có constructor nhận apiKey thì gọi, nếu không fallback
        try {
            client = new WeatherClient(apiKey);
        } catch (Throwable t) {
        	client = new WeatherClient(apiKey);

        }

        initUI();
        loadFavorites();
        loadHistory();
        showWelcome();
    }

    private void initUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 760);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10,10));

        // header gradient
        JPanel header = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                int w = getWidth(), h = getHeight();
                g2.setPaint(new GradientPaint(0,0, new Color(18,120,200), 0,h, new Color(20,180,220)));
                g2.fillRect(0,0,w,h);
            }
        };
        header.setPreferredSize(new Dimension(0,100));
        header.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 30));
        txtCity.setColumns(26);
        header.add(new JLabel("Thành phố:"));
        header.add(txtCity);
        header.add(btnSearch);
        header.add(btnMap);
        header.add(btnAddFav);
        header.add(btnFavWindow);
        header.add(new JLabel("Ngôn ngữ:"));
        header.add(langCombo);
        add(header, BorderLayout.NORTH);

        // left card current
        JPanel leftCard = new RoundedPanel();
        leftCard.setPreferredSize(new Dimension(420,0));
        leftCard.setLayout(new BorderLayout(8,8));
        lblTemp.setFont(new Font("SansSerif", Font.BOLD, 56));
        lblDesc.setFont(new Font("SansSerif", Font.PLAIN, 16));
        lblCity.setFont(new Font("SansSerif", Font.PLAIN, 16));
        lblTime.setFont(new Font("SansSerif", Font.PLAIN, 12));
        mainIcon.setHorizontalAlignment(SwingConstants.CENTER);
        mainIcon.setPreferredSize(new Dimension(160,160));

        JPanel topInfo = new JPanel(new BorderLayout());
        topInfo.setOpaque(false);
        topInfo.add(lblTemp, BorderLayout.WEST);
        topInfo.add(mainIcon, BorderLayout.EAST);

        JPanel meta = new JPanel(new GridLayout(4,1));
        meta.setOpaque(false);
        meta.add(lblCity);
        meta.add(lblTime);
        meta.add(lblDesc);
        meta.add(lblHumidity);
        topInfo.add(meta, BorderLayout.CENTER);
        leftCard.add(topInfo, BorderLayout.NORTH);

        // center: chart + 5-day horizontal panel
        JPanel center = new JPanel(new BorderLayout(8,8));
        center.add(chartPanel, BorderLayout.CENTER);

        fiveList.setCellRenderer(new FiveDayRenderer());
        fiveList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        fiveList.setVisibleRowCount(1);
        JScrollPane fiveScroll = new JScrollPane(fiveList);
        fiveScroll.setPreferredSize(new Dimension(0,150));
        center.add(fiveScroll, BorderLayout.SOUTH);

        // right: history + favorites list
        JPanel right = new RoundedPanel();
        right.setPreferredSize(new Dimension(260,0));
        right.setLayout(new BorderLayout(6,6));
        right.add(new JLabel("Lịch sử", SwingConstants.CENTER), BorderLayout.NORTH);
        historyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        right.add(new JScrollPane(historyList), BorderLayout.CENTER);

        JPanel favPane = new JPanel(new BorderLayout());
        favPane.add(new JLabel("Yêu thích", SwingConstants.CENTER), BorderLayout.NORTH);
        favList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        favPane.add(new JScrollPane(favList), BorderLayout.CENTER);
        right.add(favPane, BorderLayout.SOUTH);

        add(leftCard, BorderLayout.WEST);
        add(center, BorderLayout.CENTER);
        add(right, BorderLayout.EAST);

        // events
        btnSearch.addActionListener(e -> startSearch());
        txtCity.addActionListener(e -> startSearch());
        btnMap.addActionListener(e -> openMap());
        btnAddFav.addActionListener(e -> addCurrentToFav());
        btnFavWindow.addActionListener(e -> openFavoritesWindow());
        langCombo.addActionListener(e -> {
            lang = (String) langCombo.getSelectedItem();
        });

        historyList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String sel = historyList.getSelectedValue();
                    if (sel != null) { txtCity.setText(sel); startSearch(); }
                }
            }
        });
        favList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String sel = favList.getSelectedValue();
                    if (sel != null) { txtCity.setText(sel); startSearch(); }
                }
            }
        });

        setVisible(true);
    }

    private void showWelcome() {
        lblCity.setText("Ứng dụng Thời tiết");
        lblTime.setText("");
        lblTemp.setText("--°C");
        lblDesc.setText("Nhập thành phố rồi nhấn Tra cứu");
        mainIcon.setIcon(chartPanel.loadIcon("weather.gif", 140, 140));
        fiveModel.clear();
        chartPanel.setForecast(null);
    }

    private void startSearch() {
        final String city = txtCity.getText().trim();
        if (city.isEmpty()) { JOptionPane.showMessageDialog(this, "Vui lòng nhập tên thành phố"); return; }
        btnSearch.setEnabled(false);
        SwingWorker<WeatherData, Void> wk = new SwingWorker<>() {
            @Override
            protected WeatherData doInBackground() {
                try {
                    return client.getService().getWeather(city, lang);
                } catch (Exception ex) { ex.printStackTrace(); return null; }
            }
            @Override
            protected void done() {
                try {
                    WeatherData wd = get();
                    if (wd == null) { JOptionPane.showMessageDialog(WeatherAppUi.this, "Không lấy được dữ liệu"); }
                    else {
                        applyWeather(wd);
                        appendHistory(wd.city);
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
                btnSearch.setEnabled(true);
            }
        };
        wk.execute();
    }

    private void applyWeather(WeatherData wd) {
        lblCity.setText("📍 " + wd.city + (wd.country != null && !wd.country.isEmpty() ? ", " + wd.country : ""));
        lblTime.setText("🕒 " + (wd.localTime == null ? "" : wd.localTime));
        lblTemp.setText(String.format("%.0f\u00B0C", wd.temp));
        lblDesc.setText((lang.equals("vi") ? "☁ " : "☁ ") + (wd.description == null ? "" : wd.description));
        lblHumidity.setText((lang.equals("vi") ? "💧 Độ ẩm: " : "💧 Humidity: ") + wd.humidity + "%");
        lblWind.setText((lang.equals("vi") ? "💨 Gió: " : "💨 Wind: ") + wd.windSpeed + " m/s");
        lblDesc.setToolTipText(wd.description);

        ImageIcon mainIc = chartPanel.loadIcon(wd.iconFile, 140, 140);
        mainIcon.setIcon(mainIc);

        // five days list: create textual elements (renderer will show icon)
        fiveModel.clear();
        if (wd.forecast != null) {
            for (WeatherData.ForecastDay fd : wd.forecast) {
                // substring last 5 chars (MM-dd) if length allows
                String datePart = fd.date.length() >= 5 ? fd.date.substring(fd.date.length()-5) : fd.date;
                fiveModel.addElement(String.format("%s   %d°C–%d°C", datePart, Math.round(fd.minTemp), Math.round(fd.maxTemp)));
            }
        }
        fiveList.setModel(fiveModel);

        chartPanel.setForecast(wd.forecast);
    }

    private void openMap() {
        String city = txtCity.getText().trim();
        if (city.isEmpty()) { JOptionPane.showMessageDialog(this, "Nhập thành phố để mở bản đồ"); return; }
        try {
            String q = java.net.URLEncoder.encode(city, "UTF-8");
            String url = "https://www.google.com/maps/search/?api=1&query=" + q;
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Không mở được bản đồ: " + ex.getMessage()); }
    }

    // Favorites persistence
    private void addCurrentToFav() {
        String city = txtCity.getText().trim();
        if (city.isEmpty()) { JOptionPane.showMessageDialog(this, "Nhập thành phố để thêm"); return; }
        if (!favModel.contains(city)) {
            favModel.addElement(city);
            saveFavorites();
            JOptionPane.showMessageDialog(this, city + " đã thêm vào Yêu thích");
        } else JOptionPane.showMessageDialog(this, city + " đã có trong Yêu thích");
    }

    private void openFavoritesWindow() {
        List<String> favs = new ArrayList<>();
        for (int i = 0; i < favModel.size(); i++) favs.add(favModel.get(i));
        // nếu bạn có class FavoritesWindow -> dùng, nếu không, dùng dialog cơ bản
        JDialog d = new JDialog(this, "Yêu thích", true);
        DefaultListModel<String> m = new DefaultListModel<>();
        for (String s : favs) m.addElement(s);
        JList<String> list = new JList<>(m);
        JButton use = new JButton("Sử dụng");
        JButton del = new JButton("Xóa");
        use.addActionListener(e -> {
            String sel = list.getSelectedValue();
            if (sel != null) { txtCity.setText(sel); startSearch(); d.dispose(); }
        });
        del.addActionListener(e -> {
            String sel = list.getSelectedValue();
            if (sel != null) {
                m.removeElement(sel);
                favModel.removeElement(sel);
                saveFavorites();
            }
        });
        JPanel btnp = new JPanel(); btnp.add(use); btnp.add(del);
        d.getContentPane().add(new JScrollPane(list), BorderLayout.CENTER);
        d.getContentPane().add(btnp, BorderLayout.SOUTH);
        d.setSize(300,400);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
        loadFavorites(); // reload after dialog
    }

    private void saveFavorites() {
        try {
            JSONArray arr = new JSONArray();
            for (int i = 0; i < favModel.size(); i++) arr.put(favModel.get(i));
            try (FileWriter fw = new FileWriter(favFile)) {
                fw.write(arr.toString(2));
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void loadFavorites() {
        favModel.clear();
        try {
            if (!favFile.exists()) return;
            String s = readAll(favFile);
            JSONArray arr = new JSONArray(s);
            for (int i = 0; i < arr.length(); i++) favModel.addElement(arr.getString(i));
            favList.setModel(favModel);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ✅ FIXED: try-with-resources must have braces { ... } — sửa ở đây
    private void appendHistory(String city) {
        try {
            if (city == null || city.isBlank()) return;
            if (!historyModel.contains(city)) historyModel.add(0, city);

            // dùng try-with-resources đúng cú pháp
            try (FileWriter fw = new FileWriter(histFile, true)) {
                fw.write(city + System.lineSeparator());
            }

        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void loadHistory() {
        historyModel.clear();
        try {
            if (!histFile.exists()) return;
            try (BufferedReader br = new BufferedReader(new FileReader(histFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !historyModel.contains(line)) historyModel.addElement(line);
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private String readAll(File f) throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            StringBuilder sb = new StringBuilder(); String l;
            while ((l = br.readLine()) != null) { sb.append(l).append("\n"); }
            return sb.toString();
        }
    }

    // custom renderer for five-day list to draw mini icon + text
    private class FiveDayRenderer extends JPanel implements ListCellRenderer<String> {
        private final JLabel icon = new JLabel();
        private final JLabel text = new JLabel();

        FiveDayRenderer() {
            setLayout(new BorderLayout(6,6));
            setOpaque(false);
            text.setFont(new Font("SansSerif", Font.PLAIN, 13));
            add(icon, BorderLayout.WEST);
            add(text, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            text.setText(value);
            icon.setIcon(null);
            WeatherData.ForecastDay[] fd = chartPanel.getForecast();
            if (fd != null && index >= 0 && index < fd.length) {
                ImageIcon ic = chartPanel.loadIcon(fd[index].iconFile, 36, 36);
                icon.setIcon(ic);
            }
            if (isSelected) setBackground(new Color(220,235,255));
            else setBackground(new Color(0,0,0,0));
            return this;
        }
    }

    // main
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new WeatherAppUi();
        });
    }
}
