/*
 * Tools of all kind
 */
package misc;

//import transform.Transformation;

import misc.gifdecoder.AnimatedGIFReader;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Administrator
 */
public class Tools
{
    public static InputStream getResourceAsStream (String name)
    {
        InputStream is = ClassLoader.getSystemResourceAsStream(name);
        return new BufferedInputStream (Objects.requireNonNull (is));
    }

    public static byte[] getResourceAsArray (String name)
    {
        InputStream in = getResourceAsStream(name);
        try {
            byte[] arr = new byte[in.available()];
            in.read(arr);
            return arr;
        } catch (IOException e) {
            return null;
        }
    }

    public static BufferedImage getImageFromResource (String name)
    {
        try {
            return ImageIO.read(getResourceAsStream(name));
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Reduces image quality
     *
     * @param path Path of jpeg file
     * @return byte array of jpeg data
     * @throws Exception if smth. gone wrong
     */
    public static byte[] reduceImg (File path, int xy) throws Exception
    {
        BufferedImage image2 = resizeImage(loadImage(path), xy, xy);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image2, "jpg", os);
        return os.toByteArray();
    }

    public static BufferedImage loadImage (File file) throws Exception
    {
        if (hasExtension(file.getName(), ".gif"))
        {
            FileInputStream fin = new FileInputStream (file);
            AnimatedGIFReader reader = new AnimatedGIFReader();
            BufferedImage img = reader.read(fin);
            fin.close();
            return img;
        }
        else
        {
            return ImageIO.read(file);
        }
    }

    public static boolean hasExtension(String in, String... ext) {
        in = in.toLowerCase();
        for (String s : ext) {
            s = s.toLowerCase();
            if (in.endsWith(s))
                return true;
        }
        return false;
    }

    public static String getExtension (String in)
    {
        int i = in.lastIndexOf('.');
        if (i > 0) {
            return in.substring(i);
        }
        return null;
    }

    public static boolean isVideo(String in) {
        return hasExtension(in, ".mp4", ".mkv", ".webm", ".ogv", ".3gp");
        //return hasExtension(in, ".mp4", ".mkv", ".webm", ".ogv", ".3gp", ".avi", ".wmv");
    }

    public static boolean isAudio(String in) {
        return hasExtension(in, ".mp3", ".ogg", ".wav");
    }

    public static boolean isImage(String in) {
        return hasExtension(in, ".jpg", ".jpeg", ".png", ".bmp", "gif", "jfif");
    }

    public static boolean isZip(String in) {
        return hasExtension(in, ".zip");
    }

    /**
     * Creates Image copy of new size
     *
     * @param originalImage Input image
     * @param width         With
     * @param height        Height
     * @return new Image
     */
    private static BufferedImage resizeImage (BufferedImage originalImage, int width, int height)
    {
        if (originalImage == null)
        {
            return null;
        }
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        //BufferedImage resizedImage = new BufferedImage(width, height, originalImage.getType ());
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);

        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();

        return resizedImage;
    }

    public static void infoBox(String infoMessage, String titleBar)
    {
        JOptionPane.showMessageDialog(null, infoMessage, "InfoBox: " + titleBar, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void infoBox(String infoMessage)
    {
        JOptionPane.showMessageDialog(null, infoMessage,  "Huh???", JOptionPane.INFORMATION_MESSAGE);
    }

    public static String humanReadableByteCount(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }

    /**
     * Get the path of the filesystem where a class is at runtime
     * @param c The class
     * @return The Path as string
     */
    public static String getPathOfClass (Class<?> c) {
        String fp = c.getProtectionDomain().getCodeSource().getLocation().getPath();
        fp = new File(URI.create("file://" + fp)).getAbsolutePath();
        if (fp.endsWith(".jar"))
            return fp.substring(0, fp.lastIndexOf(File.separatorChar) + 1);
        return fp + File.separatorChar;
    }

    public static void runAsync (Runnable r) {
        CompletableFuture.runAsync (r::run
        );
    }

    /**
     * Check if file is text, not binary
     * @param in a file name
     * @return true if it's a text file
     */
    public static boolean isText (String in) {
        return hasExtension (in, ".txt", ".cpp", ".c", ".h", ".java", ".cxx", ".hxx");
    }
}
