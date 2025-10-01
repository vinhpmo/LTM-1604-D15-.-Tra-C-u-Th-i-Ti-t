package weather;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FavoritesService {
    private static final String FILE_NAME = "favorites.json";
    private static final List<String> favorites = new ArrayList<>();

    static {
        loadFromFile();
    }

    public static void add(String city) {
        if (!favorites.contains(city)) {
            favorites.add(city);
            saveToFile();
        }
    }

    public static void update(int index, String newCity) {
        if (index >= 0 && index < favorites.size()) {
            favorites.set(index, newCity);
            saveToFile();
        }
    }

    public static void remove(String city) {
        favorites.remove(city);
        saveToFile();
    }

    public static List<String> getAll() {
        return new ArrayList<>(favorites);
    }

    private static void saveToFile() {
        try (FileWriter writer = new FileWriter(FILE_NAME)) {
            new Gson().toJson(favorites, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadFromFile() {
        try (FileReader reader = new FileReader(FILE_NAME)) {
            Type type = new TypeToken<List<String>>() {}.getType();
            List<String> list = new Gson().fromJson(reader, type);
            if (list != null) {
                favorites.clear();
                favorites.addAll(list);
            }
        } catch (Exception e) {
            // file chưa tồn tại thì bỏ qua
        }
    }
}
