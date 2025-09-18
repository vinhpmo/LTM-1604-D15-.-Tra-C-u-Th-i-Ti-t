package weather;

import java.util.Scanner;

public class WeatherClient {
    public static void main(String[] args) {
        try {
            WeatherService service = new WeatherServiceImpl();
            Scanner sc = new Scanner(System.in);
            System.out.print("Nhập tên thành phố: ");
            String city = sc.nextLine();
            String result = service.getWeather(city);
            System.out.println(result);
            sc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
