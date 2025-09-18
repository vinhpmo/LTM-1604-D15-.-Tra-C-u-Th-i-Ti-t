package weather;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;

public class WeatherAppUI extends JFrame {
	private JTextField cityField;
	private JButton searchButton;
	private JLabel cityLabel, tempLabel, descLabel, timeLabel, statusLabel, iconLabel;

	private WeatherService weatherService;

	public WeatherAppUI() {
		weatherService = new WeatherServiceImpl();

		setTitle("Ứng dụng Tra cứu Thời tiết");
		setSize(650, 500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());

		// === Header ===
		JLabel header = new JLabel("🌤️  Ứng dụng Tra cứu Thời tiết Online", SwingConstants.CENTER);
		header.setFont(new Font("Segoe UI", Font.BOLD, 22));
		header.setOpaque(true);
		header.setBackground(new Color(0x4A90E2));
		header.setForeground(Color.WHITE);
		header.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		add(header, BorderLayout.NORTH);

		// === Input panel ===
		JPanel inputPanel = new JPanel(new FlowLayout());
		inputPanel.setBackground(new Color(0xF2F2F2));
		cityField = new JTextField(20);
		searchButton = new JButton("🔍 Tìm kiếm");
		searchButton.setBackground(new Color(0x4A90E2));
		searchButton.setForeground(Color.WHITE);
		searchButton.setFocusPainted(false);
		inputPanel.add(new JLabel("Thành phố:"));
		inputPanel.add(cityField);
		inputPanel.add(searchButton);

		// === Info cards panel ===
		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new GridLayout(5, 1, 10, 10));
		infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		infoPanel.setBackground(new Color(0xFAFAFA));

		// Các card với màu riêng
		cityLabel = createCard("🌍 Thành phố", "-", new Color(0xC8E6C9));
		timeLabel = createCard("📅 Ngày/Giờ", "-", new Color(0xBBDEFB));
		tempLabel = createCard("🌡️ Nhiệt độ", "-", new Color(0xFFCDD2));
		descLabel = createCard("☁️ Thời tiết", "-", new Color(0xCFD8DC));
		statusLabel = createCard("🌞/🌙 Trạng thái", "-", new Color(0xFFE0B2));

		infoPanel.add(cityLabel.getParent());
		infoPanel.add(timeLabel.getParent());
		infoPanel.add(tempLabel.getParent());
		infoPanel.add(descLabel.getParent());
		infoPanel.add(statusLabel.getParent());

		// === Icon Panel ===
		iconLabel = new JLabel("", SwingConstants.CENTER);
		iconLabel.setPreferredSize(new Dimension(150, 150));

		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.add(infoPanel, BorderLayout.CENTER);
		centerPanel.add(iconLabel, BorderLayout.EAST);

		add(centerPanel, BorderLayout.CENTER);
		add(inputPanel, BorderLayout.SOUTH);

		// === Search Action ===
		searchButton.addActionListener((ActionEvent e) -> {
			String city = cityField.getText().trim();
			if (city.isEmpty()) {
				JOptionPane.showMessageDialog(this, "⚠️ Vui lòng nhập tên thành phố!");
				return;
			}

			try {
				String weather = weatherService.getWeather(city);

				updateCard(cityLabel, "🌍 Thành phố", city);
				updateCard(timeLabel, "📅 Ngày/Giờ", getValue(weather, "Ngày/Giờ"));
				updateCard(tempLabel, "🌡️ Nhiệt độ", getValue(weather, "Nhiệt độ"));
				updateCard(descLabel, "☁️ Thời tiết", getValue(weather, "Thời tiết"));
				updateCard(statusLabel, "🌞/🌙 Trạng thái", getValue(weather, "Trạng thái"));

				// Icon
				String iconName = getIconNameFromText(weather);
				ImageIcon icon = loadIconFromResource(iconName);
				if (icon != null) {
					iconLabel.setIcon(icon);
				} else {
					iconLabel.setIcon(null);
					iconLabel.setText("❓");
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(this, "❌ Lỗi khi lấy dữ liệu thời tiết!");
			}
		});
	}

	// Tạo 1 card có màu riêng (KHÔNG viền ô vuông)
	private JLabel createCard(String title, String value, Color bgColor) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(bgColor);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // chỉ khoảng cách, không viền

		JLabel label = new JLabel(title + ": " + value);
		label.setFont(new Font("Segoe UI", Font.BOLD, 15));
		panel.add(label, BorderLayout.CENTER);

		return label;
	}

	// Cập nhật giá trị trong card
	private void updateCard(JLabel label, String title, String value) {
		label.setText(title + ": " + value);
	}

	private String getValue(String text, String key) {
		for (String line : text.split("\n")) {
			if (line.startsWith(key)) {
				return line.replace(key + ":", "").trim();
			}
		}
		return "-";
	}

	private String getIconNameFromText(String text) {
		if (text == null)
			return "default.png";
		String lower = text.toLowerCase();

		if (lower.contains("đêm") || lower.contains("night"))
			return "night.png";
		if (lower.contains("mưa") || lower.contains("rain"))
			return "rain.png";
		if (lower.contains("bão") || lower.contains("storm") || lower.contains("thunder"))
			return "storm.png";
		if (lower.contains("tuyết") || lower.contains("snow"))
			return "snow.png";
		if (lower.contains("mây") || lower.contains("cloud"))
			return "cloud.png";
		if (lower.contains("nắng") || lower.contains("sunny") || lower.contains("clear"))
			return "sunny.png";
		return "default.png";
	}

	private ImageIcon loadIconFromResource(String iconName) {
		String path = "/icons/" + iconName;
		try (InputStream is = getClass().getResourceAsStream(path)) {
			if (is == null) {
				System.err.println("Không tìm thấy icon: " + path);
				return null;
			}
			BufferedImage img = ImageIO.read(is);
			Image scaled = img.getScaledInstance(120, 120, Image.SCALE_SMOOTH);
			return new ImageIcon(scaled);
		} catch (Exception ex) {
			return null;
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new WeatherAppUI().setVisible(true));
	}
}
