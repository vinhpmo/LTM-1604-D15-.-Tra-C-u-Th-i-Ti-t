package weather;

public class WeatherClient {
    private final WeatherService service;

    public WeatherClient(WeatherService service) {
        this.service = service;
    }

    public WeatherData fetch(String city) throws Exception {
        return service.getWeather(city);
    }
}
