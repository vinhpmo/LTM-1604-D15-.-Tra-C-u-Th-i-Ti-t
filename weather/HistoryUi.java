package weather;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class HistoryUi extends JFrame {
    private DefaultListModel<String> model;
    private JTextField searchField;
    private List<WeatherData> list;

    public HistoryUi(WeatherAppUi parent) {
        setTitle("🕒 Lịch sử tra cứu");
        setSize(500, 400);
        setLocationRelativeTo(parent);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(255, 250, 240));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Thanh tìm kiếm ---
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchField = new JTextField();
        JButton searchBtn = styledButton("🔍 Tìm", new Color(255, 140, 0));
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchBtn, BorderLayout.EAST);
        mainPanel.add(searchPanel, BorderLayout.NORTH);

        // --- Danh sách ---
        model = new DefaultListModel<>();
        list = HistoryService.getAll();
        loadList("");

        JList<String> jlist = new JList<>(model);
        jlist.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        jlist.setBackground(new Color(255, 255, 240));
        jlist.setBorder(BorderFactory.createLineBorder(new Color(220, 180, 120), 1, true));

        mainPanel.add(new JScrollPane(jlist), BorderLayout.CENTER);

        // --- Nút chức năng ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        buttonPanel.setBackground(new Color(255, 250, 240));

        JButton delBtn = styledButton("❌ Xoá mục", new Color(231, 76, 60));
        JButton clearBtn = styledButton("🗑 Xoá tất cả", new Color(128, 0, 0));

        buttonPanel.add(delBtn);
        buttonPanel.add(clearBtn);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // --- Logic ---
        jlist.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int idx = jlist.getSelectedIndex();
                if (idx >= 0) {
                    parent.searchForCity(list.get(idx).city);
                    dispose();
                }
            }
        });

        searchBtn.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            loadList(keyword);
        });

        delBtn.addActionListener(e -> {
            int idx = jlist.getSelectedIndex();
            if (idx >= 0) {
                HistoryService.remove(idx);
                list = HistoryService.getAll();
                loadList(searchField.getText().trim());
            }
        });

        clearBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn xoá toàn bộ lịch sử?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                HistoryService.clear();
                list = HistoryService.getAll();
                loadList("");
            }
        });

        setVisible(true);
    }

    private void loadList(String keyword) {
        model.clear();
        List<WeatherData> filtered = list;
        if (!keyword.isEmpty()) {
            filtered = list.stream()
                    .filter(d -> d.city.toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toList());
        }
        for (WeatherData d : filtered) {
            model.addElement("📍 " + d.city + " - " + String.format("%.1f°C", d.temperature));
        }
    }

    private JButton styledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
