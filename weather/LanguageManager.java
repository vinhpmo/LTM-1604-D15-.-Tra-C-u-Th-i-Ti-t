package weather;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Quản lý ngôn ngữ (ResourceBundle).
 * Tạo file messages_vi.properties và messages_en.properties trong classpath/resources.
 */
public class LanguageManager {
    private ResourceBundle bundle;
    private Locale locale;

    public LanguageManager(Locale defaultLocale) {
        setLocale(defaultLocale);
    }

    public LanguageManager() {
        setLocale(new Locale("vi"));
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
        try {
            bundle = ResourceBundle.getBundle("messages", this.locale);
        } catch (Exception e) {
            bundle = ResourceBundle.getBundle("messages", Locale.ENGLISH);
        }
    }

    public Locale getLocale() {
        return locale;
    }

    public String get(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return key;
        }
    }
}
