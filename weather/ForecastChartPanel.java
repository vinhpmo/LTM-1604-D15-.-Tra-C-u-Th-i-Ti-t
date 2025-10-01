package weather;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.TextAnchor;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ForecastChartPanel extends JPanel {
    private DefaultCategoryDataset dataset;

    public ForecastChartPanel() {
        setLayout(new BorderLayout());
        dataset = new DefaultCategoryDataset();

        JFreeChart chart = ChartFactory.createBarChart(
                "Dự báo 5 ngày", "Ngày", "Nhiệt độ (°C)", dataset);

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        BarRenderer renderer = new BarRenderer();

        // màu cột
        renderer.setSeriesPaint(0, new Color(66, 135, 245)); // Min
        renderer.setSeriesPaint(1, new Color(245, 99, 66));  // Max

        // hiện nhãn nhiệt độ bên trong cột (ở giữa)
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelPaint(Color.black);
        renderer.setDefaultPositiveItemLabelPosition(
                new ItemLabelPosition()
        );

        // làm cột nhỏ lại
        renderer.setMaximumBarWidth(0.07);

        // giữ nhãn ngày ở dưới thẳng (không nghiêng)
        plot.getDomainAxis().setCategoryLabelPositions(
                org.jfree.chart.axis.CategoryLabelPositions.STANDARD
        );

        plot.setRenderer(renderer);

        add(new ChartPanel(chart), BorderLayout.CENTER);
        setPreferredSize(new Dimension(0, 250));
    }

    /**
     * Nhận trực tiếp forecast list (giữ nguyên logic gốc của bạn).
     * Nếu thiếu thì hiển thị bao nhiêu ngày có sẵn.
     */
    public void setData(List<WeatherData.ForecastDay> forecast) {
        dataset.clear();
        if (forecast == null || forecast.isEmpty()) return;

        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<WeatherData.ForecastDay> next5 = new ArrayList<>();

        for (WeatherData.ForecastDay fd : forecast) {
            try {
                LocalDate date = LocalDate.parse(fd.date, fmt);
                if (date.isAfter(today)) {
                    next5.add(fd);
                }
            } catch (Exception ignored) {}
            if (next5.size() == 5) break;
        }

        for (WeatherData.ForecastDay fd : next5) {
            dataset.addValue(fd.minTemp, "Min", fd.date);
            dataset.addValue(fd.maxTemp, "Max", fd.date);
        }

        if (next5.size() < 5) {
            System.out.println("⚠️ Dữ liệu dự báo chỉ có " + next5.size() + " ngày sau hôm nay.");
        }
    }
}
