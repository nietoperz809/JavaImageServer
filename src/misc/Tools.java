/*
 * Tools of all kind
 */
package misc;

//import transform.Transformation;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import static java.awt.Toolkit.getDefaultToolkit;
import static java.awt.datatransfer.DataFlavor.stringFlavor;

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
    public static byte[] reduceImg (File path) throws Exception
    {
        BufferedImage image = ImageIO.read(path);
        BufferedImage image2 = resizeImage(image, 100, 100);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image2, "jpg", os);
        return os.toByteArray();
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
