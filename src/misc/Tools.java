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
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.*;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Objects;

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

    public static byte[] gatResourceAsArray(String name)
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
     * @throws Exception
     */
    public static byte[] reduceImg (File path, int xy) throws Exception
    {
        BufferedImage image = loadImage(path);
        BufferedImage image2 = resizeImage(image, xy, xy);
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



    /**
     * Creates Image copy of new size
     *
     * @param originalImage Input image
     * @param width         With
     * @param height        Height
     * @return new Image
     */
    private static BufferedImage resizeImage (Image originalImage, int width, int height)
    {
        if (originalImage == null)
        {
            return null;
        }
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
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
}
