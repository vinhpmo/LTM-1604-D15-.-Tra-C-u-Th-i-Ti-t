package weather;

import java.util.List;

public class WeatherData {
    public String city;
    public String country;
    public String localTime;   // yyyy-MM-dd HH:mm
    public double temp;
    public int humidity;
    public double windSpeed;
    public String description;
    public String iconFile;    // ex "rain.gif"

    public List<ForecastDay> forecast; // next 5 days

    public static class ForecastDay {
        public String date;    // yyyy-MM-dd
        public double minTemp;
        public double maxTemp;
        public double avgTemp;
        public String description;
        public String iconFile;

        public ForecastDay(String date, double minTemp, double maxTemp, double avgTemp, String description, String iconFile) {
            this.date = date;
            this.minTemp = minTemp;
            this.maxTemp = maxTemp;
            this.avgTemp = avgTemp;
            this.description = description;
            this.iconFile = iconFile;
        }
    }
}
