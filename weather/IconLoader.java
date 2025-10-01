package weather;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class IconLoader {
    public static ImageIcon loadIcon(String fileName, int w, int h) {
        try {
            URL url = IconLoader.class.getResource("/icons/" + fileName);
            if (url == null) return null;
            ImageIcon icon = new ImageIcon(url);
            if (w > 0 && h > 0) {
                Image img = icon.getImage().getScaledInstance(w, h, Image.SCALE_DEFAULT);
                return new ImageIcon(img);
            }
            return icon;
        } catch (Exception e) {
            return null;
        }
    }
}
