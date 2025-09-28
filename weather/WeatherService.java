package weather;

/**
 * Interface service trả về WeatherData
 */
public interface WeatherService {
    /**
     * Lấy dữ liệu thời tiết (current + forecast 5 ngày)
     * @param city tên thành phố
     * @param lang "vi" hoặc "en"
     */
    WeatherData getWeather(String city, String lang) throws Exception;
}
