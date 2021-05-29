package misc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ThumbManager {
    private final String thumbsDir;
    public static final String DNAME = "thumbs";
    private boolean folderExists = false;

    public ThumbManager (String basepath)
    {
        thumbsDir = basepath + File.separator + DNAME;
        try {
            Files.createDirectories(Paths.get(thumbsDir));
            folderExists = true;
        } catch (Exception e) {
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
        if (!folderExists)
            return null;
        Path p = Paths.get(thumbsDir+File.separator+name);
        try {
            return Files.readAllBytes(p);
        } catch (IOException e) {
            return null;
        }
    }

    private void saveThumb (byte[] data, String name)
    {
        if (folderExists) {
            Path p = Paths.get(thumbsDir + File.separator + name);
            try {
                Files.write(p, data);
            } catch (IOException e) {
                System.out.println("cannot store thumb: " + e);
            }
        }
    }
}
