package weather;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class WeatherServiceImpl implements WeatherService {
    private static final String API_KEY = "29061fdbdaa260c4fce8e14b51f29afe"; // thay bằng API key của bạn
    private static final String API_URL =
            "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric&lang=vi";
    private static final String FORECAST_URL =
            "https://api.openweathermap.org/data/2.5/forecast?q=%s&appid=%s&units=metric&lang=vi";

    @Override
    public WeatherData getWeather(String city) throws Exception {
        // Thời tiết hiện tại
        String urlStr = String.format(API_URL, city, API_KEY);
        JSONObject obj = readJson(urlStr);

        WeatherData d = new WeatherData();
        d.city = obj.optString("name", city);
        JSONObject sys = obj.optJSONObject("sys");
        if (sys != null) {
            d.country = sys.optString("country", "");
        }
        JSONObject main = obj.getJSONObject("main");
        d.temperature = main.getDouble("temp");
        JSONArray weatherArr = obj.getJSONArray("weather");
        JSONObject w = weatherArr.getJSONObject(0);
        d.condition = w.getString("description");
        d.icon = w.getString("icon");
        JSONObject wind = obj.getJSONObject("wind");
        d.windSpeed = wind.getDouble("speed");

        // Lấy toạ độ
        if (obj.has("coord")) {
            JSONObject coord = obj.getJSONObject("coord");
            d.latitude = coord.optDouble("lat", 0);
            d.longitude = coord.optDouble("lon", 0);
        }

        // Forecast 5 ngày
        urlStr = String.format(FORECAST_URL, city, API_KEY);
        JSONObject objF = readJson(urlStr);
        JSONArray list = objF.getJSONArray("list");
        List<WeatherData.ForecastDay> forecasts = new ArrayList<>();
        for (int i = 0; i < list.length(); i += 8) { // mỗi 8 record ~ 1 ngày
            JSONObject item = list.getJSONObject(i);
            WeatherData.ForecastDay fd = new WeatherData.ForecastDay();
            fd.date = item.getString("dt_txt").split(" ")[0];
            JSONObject mainF = item.getJSONObject("main");
            fd.minTemp = mainF.getDouble("temp_min");
            fd.maxTemp = mainF.getDouble("temp_max");
            JSONArray weatherF = item.getJSONArray("weather");
            fd.icon = weatherF.getJSONObject(0).getString("icon");
            forecasts.add(fd);
        }
        d.forecast = forecasts;

        return d;
    }

    private JSONObject readJson(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();
        return new JSONObject(sb.toString());
    }
}
