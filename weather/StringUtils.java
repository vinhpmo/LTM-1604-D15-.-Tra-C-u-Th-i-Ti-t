package weather;

import java.text.Normalizer;

public class StringUtils {
    public static String normalizeCity(String input) {
        if (input == null) return "";
        // Bỏ khoảng trắng đầu/cuối, viết thường
        String city = input.trim().toLowerCase();

        // Bỏ dấu tiếng Việt
        city = Normalizer.normalize(city, Normalizer.Form.NFD);
        city = city.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // Bỏ khoảng trắng giữa các từ
        city = city.replaceAll("\\s+", "");

        return city;
    }
}
