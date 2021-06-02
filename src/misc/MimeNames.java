package misc;

import java.util.HashMap;

public class MimeNames {
    static HashMap<String, String> map = new HashMap<>();
    static {
        map.put(".css", "text/css");
        map.put(".html", "text/html");
        map.put(".js", "application/javascript");
        map.put(".mp3", "audio/mpeg");
        map.put(".mp4", "video/mp4");
        map.put(".ogg", "application/ogg");
        map.put(".ogv", "video/ogg");
        map.put(".oga", "audio/ogg");
        map.put(".txt", "text/plain");
        map.put(".wav", "audio/x-wav");
        map.put(".webm", "video/webm");
    }

    public static String getMime (String type) {
        return map.get(type);
    }
}
