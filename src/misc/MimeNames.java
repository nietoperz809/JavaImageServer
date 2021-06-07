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
        map.put(".avi", "video/x-msvideo");
        map.put(".wmv", "video/x-ms-wmv");
        map.put(".mov", "video/quicktime");
        map.put(".3gp", "video/3gpp");
        map.put(".flv", "video/x-flv");
        map.put(".mkv", "video/x-matroska");
    }

    public static String getMime (String type) {
        return map.get(type);
    }
}
