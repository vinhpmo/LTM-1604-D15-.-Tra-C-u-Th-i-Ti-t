package weather;

import java.util.List;

public class WeatherData {
    public String city;
    public String country;
    public double temperature;
    public String condition;
    public double windSpeed;
    public String icon;
    public double latitude;
    public double longitude;
    public List<ForecastDay> forecast;

    public static class ForecastDay {
        public String date;   // yyyy-MM-dd
        public double minTemp;
        public double maxTemp;
        public String icon;
    }
}
