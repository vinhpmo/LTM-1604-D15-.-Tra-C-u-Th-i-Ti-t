package weather;

public interface WeatherService {
    WeatherData getWeather(String city) throws Exception;
}
