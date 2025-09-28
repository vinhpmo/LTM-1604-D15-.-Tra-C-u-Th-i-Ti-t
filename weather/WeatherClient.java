package weather;

/**
 * Wrapper khởi tạo service với API key
 */
public class WeatherClient {
    private final WeatherService service;

    public WeatherClient(String apiKey) {
        this.service = new WeatherServiceImpl(apiKey);
    }

    public WeatherService getService() { return service; }
}
