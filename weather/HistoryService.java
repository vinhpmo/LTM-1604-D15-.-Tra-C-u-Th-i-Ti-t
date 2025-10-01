package weather;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class HistoryService {
    private static final String FILE_NAME = "history.json";
    private static final List<WeatherData> history = new ArrayList<>();

    static {
        loadFromFile();
    }

    public static void add(WeatherData d) {
        history.add(d);
        saveToFile();
    }

    public static void remove(int index) {
        if (index >= 0 && index < history.size()) {
            history.remove(index);
            saveToFile();
        }
    }

    public static void clear() {
        history.clear();
        saveToFile();
    }

    public static List<WeatherData> getAll() {
        return new ArrayList<>(history);
    }

    private static void saveToFile() {
        try (FileWriter writer = new FileWriter(FILE_NAME)) {
            new Gson().toJson(history, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadFromFile() {
        try (FileReader reader = new FileReader(FILE_NAME)) {
            Type type = new TypeToken<List<WeatherData>>() {}.getType();
            List<WeatherData> list = new Gson().fromJson(reader, type);
            if (list != null) {
                history.clear();
                history.addAll(list);
            }
        } catch (Exception e) {
            // file chưa tồn tại thì bỏ qua
        }
    }
}
