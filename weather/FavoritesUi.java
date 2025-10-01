package weather;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class FavoritesUi extends JFrame {
    private DefaultListModel<String> model;
    private JTextField searchField;

    public FavoritesUi(WeatherAppUi parent) {
        setTitle("🌟 Danh sách Yêu thích");
        setSize(500, 400);
        setLocationRelativeTo(parent);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(245, 250, 255));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Thanh tìm kiếm ---
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchField = new JTextField();
        JButton searchBtn = styledButton("🔍 Tìm", new Color(100, 149, 237));
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchBtn, BorderLayout.EAST);
        mainPanel.add(searchPanel, BorderLayout.NORTH);

        // --- Danh sách ---
        model = new DefaultListModel<>();
        loadList("");

        JList<String> jlist = new JList<>(model);
        jlist.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        jlist.setBackground(new Color(250, 250, 255));
        jlist.setBorder(BorderFactory.createLineBorder(new Color(180, 200, 230), 1, true));

        mainPanel.add(new JScrollPane(jlist), BorderLayout.CENTER);

        // --- Nút chức năng ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        buttonPanel.setBackground(new Color(245, 250, 255));

        JButton addBtn = styledButton("➕ Thêm", new Color(46, 204, 113));
        JButton editBtn = styledButton("✏️ Sửa", new Color(241, 196, 15));
        JButton delBtn = styledButton("❌ Xoá", new Color(231, 76, 60));

        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(delBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // --- Logic ---
        jlist.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int idx = jlist.getSelectedIndex();
                if (idx >= 0) {
                    parent.searchForCity(model.get(idx));
                    dispose();
                }
            }
        });

        searchBtn.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            loadList(keyword);
        });

        addBtn.addActionListener(e -> {
            String city = JOptionPane.showInputDialog(this, "Nhập tên thành phố:");
            if (city != null && !city.trim().isEmpty()) {
                FavoritesService.add(city.trim());
                loadList(searchField.getText().trim());
            }
        });

        editBtn.addActionListener(e -> {
            int idx = jlist.getSelectedIndex();
            if (idx >= 0) {
                String current = model.get(idx);
                String city = JOptionPane.showInputDialog(this, "Sửa tên thành phố:", current);
                if (city != null && !city.trim().isEmpty()) {
                    FavoritesService.update(idx, city.trim());
                    loadList(searchField.getText().trim());
                }
            }
        });

        delBtn.addActionListener(e -> {
            int idx = jlist.getSelectedIndex();
            if (idx >= 0) {
                FavoritesService.remove(model.get(idx));
                loadList(searchField.getText().trim());
            }
        });

        setVisible(true);
    }

    private void loadList(String keyword) {
        model.clear();
        List<String> list = FavoritesService.getAll();
        if (!keyword.isEmpty()) {
            list = list.stream()
                    .filter(c -> c.toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toList());
        }
        for (String city : list) model.addElement(city);
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
