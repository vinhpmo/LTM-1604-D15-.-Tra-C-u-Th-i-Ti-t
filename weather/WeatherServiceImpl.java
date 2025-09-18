package weather;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class WeatherServiceImpl implements WeatherService {

    private static final String BASE_URL = "https://api.weatherapi.com/v1/current.json";
    private static final String API_KEY = "0e40c97e7fd84df0b76131907251609";
    @Override
    public String getWeather(String city) throws Exception {
        String encodedCity = URLEncoder.encode(city.trim(), "UTF-8");
        String urlStr = BASE_URL + "?key=" + API_KEY + "&q=" + encodedCity + "&lang=vi";

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
        JsonObject location = json.getAsJsonObject("location");
        JsonObject current = json.getAsJsonObject("current");

        String name = location.get("name").getAsString();
        String country = location.get("country").getAsString();
        String localtime = location.get("localtime").getAsString();  // lấy ngày giờ
        double temp_c = current.get("temp_c").getAsDouble();
        String condition = current.getAsJsonObject("condition").get("text").getAsString();

        int isDay = current.get("is_day").getAsInt(); // 1 = ban ngày, 0 = ban đêm
        String dayNight = (isDay == 1) ? "Ban ngày" : "Ban đêm";

        return String.format("Thành phố: %s, %s\nNgày/Giờ: %s\nNhiệt độ: %.1f °C\nThời tiết: %s\nTrạng thái: %s",
                name, country, localtime, temp_c, condition, dayNight);
    }
}
