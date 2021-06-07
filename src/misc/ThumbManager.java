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
            Dbg.print("failed to create thumbs dir: "+e);
        }
    }

    public byte[] getImageThumbnail(File f) throws Exception {
        byte[] bytes = loadThumb(f.getName());
        if (bytes == null) {
            bytes = Tools.reduceImg(f, 100);
            saveThumb(bytes, f.getName());
        }
        return bytes;
    }

    public byte[] getVideoThumbnail(File f) throws Exception {
        String name = f.getName()+".jpg";
        byte[] bytes = loadThumb(name);
        if (bytes == null) {
            bytes = VideoThumbCreator.makeSnapshot(f.getAbsolutePath(), 1000, 100, 100);
            saveThumb(bytes, name);
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
                Dbg.print("cannot store thumb: " + e);
            }
        }
    }
}
