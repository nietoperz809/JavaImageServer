package misc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ThumbManager {
    public static final String DNAME = "thumbs";
    private final String thumbsDir;
    private boolean folderExists = false;
    private static MyExecutor3 pool = new MyExecutor3 (10);

    public ThumbManager (String basepath) {
        thumbsDir = basepath + File.separator + DNAME;
        try {
            Files.createDirectories (Paths.get (thumbsDir));
            folderExists = true;
            System.out.println ("Thumb builder start: " + basepath);
            File[] files = new File (basepath).listFiles ();
            for (File f : files) {
                if (f.isDirectory ())
                    continue;
                if (!Tools.isImage (f.getName ()))
                    continue;
                pool.execute (() -> {
                    createIfNotExists (f);
                    System.out.println (Thread.currentThread ().getName ()+ " end!");
                });
            }
            System.out.println ("Thumb builder done: " + basepath);

        } catch (Exception e) {
            Dbg.print ("failed to create thumbs dir: " + e);
        }
    }

    public byte[] getImageThumbnail (File f) throws Exception {
        byte[] bytes = loadThumb (f.getName ());
        if (bytes == null) {
            bytes = Tools.reduceImg (f, 100);
            saveThumb (bytes, f.getName ());
        }
        return bytes;
    }

    public byte[] getVideoThumbnail (File f) throws Exception {
        String name = f.getName () + ".jpg";
        byte[] bytes = loadThumb (name);
        if (bytes == null) {
            bytes = VideoThumbCreator.makeSnapshot (f.getAbsolutePath (), 1000, 100, 100);
            saveThumb (bytes, name);
        }
        return bytes;
    }

    public void createIfNotExists (File f) {
        String name = thumbsDir + File.separator + f.getName ();
        if (!new File (name).exists ()) {
            System.out.println (Thread.currentThread ().getName ()+" loading " + name);
            try {
                byte[] bytes = Tools.reduceImg (f, 100);
                saveThumb (bytes, f.getName ());
                bytes = null;
            } catch (Exception e) {
                e.printStackTrace ();
            }
        }
    }

    private byte[] loadThumb (String name) {
        if (!folderExists)
            return null;
        Path p = Paths.get (thumbsDir + File.separator + name);
        try {
            return Files.readAllBytes (p);
        } catch (IOException e) {
            return null;
        }
    }

    private void saveThumb (byte[] data, String name) {
        if (folderExists) {
            Path p = Paths.get (thumbsDir + File.separator + name);
            try {
                Files.write (p, data);
            } catch (IOException e) {
                Dbg.print ("cannot store thumb: " + e);
            }
        }
    }
}
