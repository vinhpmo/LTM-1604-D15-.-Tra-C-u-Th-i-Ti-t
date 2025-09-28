package weather;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * Thực hiện call OpenWeatherMap current + forecast
 * Gom forecast 3h theo ngày => tính min/max/avg cho 5 ngày tiếp theo.
 */
public class WeatherServiceImpl implements WeatherService {
    private final String apiKey;

    public WeatherServiceImpl(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public WeatherData getWeather(String cityRaw, String lang) throws Exception {
        if (cityRaw == null || cityRaw.isBlank()) throw new IllegalArgumentException("city required");
        String city = cityRaw.trim();

        String curUrl = String.format("https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric&lang=%s",
                java.net.URLEncoder.encode(city, "UTF-8"), apiKey, (lang == null ? "vi" : lang));
        JSONObject cur = fetchJson(curUrl);

        WeatherData wd = new WeatherData();
        wd.city = cur.optString("name", city);
        wd.country = cur.has("sys") ? cur.getJSONObject("sys").optString("country", "") : "";
        if (cur.has("main")) {
            wd.temp = cur.getJSONObject("main").optDouble("temp", 0.0);
            wd.humidity = cur.getJSONObject("main").optInt("humidity", 0);
        }
        if (cur.has("wind")) wd.windSpeed = cur.getJSONObject("wind").optDouble("speed", 0.0);

        String desc = "";
        String iconCode = null;
        if (cur.has("weather")) {
            JSONArray wa = cur.getJSONArray("weather");
            if (wa.length() > 0) {
                JSONObject w0 = wa.getJSONObject(0);
                desc = w0.optString("description", "");
                iconCode = w0.optString("icon", null);
            }
        }
        wd.description = desc;
        wd.iconFile = mapToIcon(iconCode, desc);

        long dt = cur.optLong("dt", System.currentTimeMillis()/1000L);
        int tz = cur.optInt("timezone", 0);
        long localMillis = (dt + tz) * 1000L;
        wd.localTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(localMillis));

        // forecast
        String fUrl = String.format("https://api.openweathermap.org/data/2.5/forecast?q=%s&appid=%s&units=metric&lang=%s",
                java.net.URLEncoder.encode(city, "UTF-8"), apiKey, (lang == null ? "vi" : lang));
        JSONObject fj = fetchJson(fUrl);
        JSONArray list = fj.has("list") ? fj.getJSONArray("list") : new JSONArray();

        // determine current local date to skip today's remaining blocks
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTimeInMillis(localMillis);
        String today = String.format("%1$tY-%1$tm-%1$td", cal);

        LinkedHashMap<String, List<Double>> temps = new LinkedHashMap<>();
        LinkedHashMap<String, List<String>> descs = new LinkedHashMap<>();
        LinkedHashMap<String, List<String>> icons = new LinkedHashMap<>();

        for (int i = 0; i < list.length(); i++) {
            JSONObject item = list.getJSONObject(i);
            String dt_txt = item.optString("dt_txt", "");
            if (dt_txt.length() < 10) continue;
            String date = dt_txt.substring(0,10);
            if (date.equals(today)) continue; // skip today to get next days

            double t = item.getJSONObject("main").optDouble("temp", 0.0);
            JSONObject w = item.getJSONArray("weather").getJSONObject(0);
            String d = w.optString("description", "");
            String code = w.optString("icon", null);

            temps.computeIfAbsent(date, k -> new ArrayList<>()).add(t);
            descs.computeIfAbsent(date, k -> new ArrayList<>()).add(d);
            icons.computeIfAbsent(date, k -> new ArrayList<>()).add(code);
        }

        List<WeatherData.ForecastDay> days = new ArrayList<>();
        int cnt = 0;
        for (Map.Entry<String, List<Double>> e : temps.entrySet()) {
            if (cnt >= 5) break;
            String date = e.getKey();
            List<Double> arr = e.getValue();
            double min = arr.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
            double max = arr.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
            double avg = arr.stream().mapToDouble(Double::doubleValue).average().orElse((min+max)/2.0);
            String d = chooseMostFrequent(descs.getOrDefault(date, Collections.emptyList()));
            String code = chooseMostFrequent(icons.getOrDefault(date, Collections.emptyList()));
            String iconFile = mapToIcon(code, d);
            days.add(new WeatherData.ForecastDay(date, round1(min), round1(max), round1(avg), d, iconFile));
            cnt++;
        }
        wd.forecast = days;
        return wd;
    }

    private static double round1(double v) { return Math.round(v*10.0)/10.0; }

    private String chooseMostFrequent(List<String> arr) {
        if (arr == null || arr.isEmpty()) return "";
        Map<String,Integer> cnt = new HashMap<>();
        for (String s : arr) if (s != null) cnt.put(s, cnt.getOrDefault(s,0)+1);
        return cnt.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
    }

    private JSONObject fetchJson(String urlstr) throws Exception {
        URL url = new URL(urlstr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        int code = conn.getResponseCode();
        BufferedReader in = new BufferedReader(new InputStreamReader((code >= 200 && code < 400) ? conn.getInputStream() : conn.getErrorStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) sb.append(line);
        in.close();
        return new JSONObject(sb.toString());
    }

    private String mapToIcon(String iconCode, String description) {
        if (description != null) {
            String low = description.toLowerCase(Locale.ROOT);
            if (low.contains("snow") || low.contains("tuyết")) return "snow.gif";
            if (low.contains("rain") || low.contains("mưa") || low.contains("shower")) return "rain.gif";
            if (low.contains("storm") || low.contains("dông")) return "storm.gif";
            if (low.contains("fog") || low.contains("sương") || low.contains("mist")) return "foggy.gif";
            if (low.contains("wind") || low.contains("gió")) return "whirlwind.gif";
            if (low.contains("cloud") || low.contains("mây")) return "cloudy.gif";
            if (low.contains("clear") || low.contains("nắng") || low.contains("hot")) return "hot.gif";
        }
        if (iconCode == null) return "weather.gif";
        switch (iconCode) {
            case "01d": return "hot.gif";
            case "01n": return "night.gif";
            case "02d": case "02n":
            case "03d": case "03n":
            case "04d": case "04n": return "cloudy.gif";
            case "09d": case "09n":
            case "10d": case "10n": return "rain.gif";
            case "11d": case "11n": return "storm.gif";
            case "13d": case "13n": return "snow.gif";
            case "50d": case "50n": return "foggy.gif";
            default: return "weather.gif";
        }
    }
}
