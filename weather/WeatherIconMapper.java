package weather;

import java.util.HashMap;
import java.util.Map;

public class WeatherIconMapper {
    private static final Map<String, String> map = new HashMap<>();

    static {
        // Clear sky
        map.put("01d", "clear.gif");
        map.put("01n", "clear.gif");

        // Few clouds
        map.put("02d", "fewclouds.gif");
        map.put("02n", "fewclouds.gif");

        // Clouds
        map.put("03d", "clouds.gif");
        map.put("03n", "clouds.gif");
        map.put("04d", "clouds.gif");
        map.put("04n", "clouds.gif");

        // Rain
        map.put("09d", "rain.gif");
        map.put("09n", "rain.gif");
        map.put("10d", "rain.gif");
        map.put("10n", "rain.gif");

        // Storm
        map.put("11d", "storm.gif");
        map.put("11n", "storm.gif");

        // Snow
        map.put("13d", "snow.gif");
        map.put("13n", "snow.gif");

        // Mist
        map.put("50d", "mist.gif");
        map.put("50n", "mist.gif");
    }

    public static String map(String apiIcon) {
        if (apiIcon == null) return "clouds.gif"; // mặc định
        return map.getOrDefault(apiIcon, "clouds.gif");
    }
}
