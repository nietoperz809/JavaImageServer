package misc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ThumbManager {
    private final String thumbsDir;

    public ThumbManager (String basepath)
    {
        thumbsDir = basepath+ File.separator+"thumbs";
        try {
            Files.createDirectories(Paths.get(thumbsDir));
        } catch (IOException e) {
            System.out.println("failed to create thumbs dir: "+e);
        }
    }

    public byte[] getThumbnail (File f) throws Exception {
        byte[] bytes = loadThumb(f.getName());
        if (bytes == null) {
            bytes = Tools.reduceImg(f, 100);
            saveThumb(bytes, f.getName());
        }
        return bytes;
    }

    private byte[] loadThumb (String name)
    {
        Path p = Paths.get(thumbsDir+File.separator+name);
        try {
            return Files.readAllBytes(p);
        } catch (IOException e) {
            return null;
        }
    }

    private void saveThumb (byte[] data, String name)
    {
        Path p = Paths.get(thumbsDir+File.separator+name);
        try {
            Files.write (p, data);
        } catch (IOException e) {
            System.out.println("cannot store thumb: "+e);
        }
    }
}
