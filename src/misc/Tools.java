/*
 * Tools of all kind
 */
package misc;

//import transform.Transformation;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.*;
import java.util.Iterator;
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
}
