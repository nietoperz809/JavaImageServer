package misc;

import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImageTools
{
    /**
     * Load a buffered image from disk
     *
     * @return
     */
    public static BufferedImage loadImage ()
    {
        FileDialog fd = new FileDialog((Frame) null, "Load", FileDialog.LOAD);
        fd.setVisible(true);
        if (fd.getFile() == null)
        {
            return null;
        }
        File f = new File(fd.getDirectory() + fd.getFile());
        try
        {
            return ImageIO.read(f);
        }
        catch (IOException ex)
        {
            System.err.println("LoadImage fail");
        }
        return null;
    }

    public static boolean saveImage (BufferedImage img, boolean jpg)
    {
        if (img == null)
        {
            return false;
        }
        FileDialog fd = new FileDialog((Frame) null, "Save", FileDialog.SAVE);
        fd.setVisible(true);
        return fd.getFile() != null && saveImage(fd.getDirectory() + fd.getFile(), img, jpg);
    }

    private static boolean saveImage (String name, BufferedImage img, boolean jpg)
    {
        if (jpg)
        {
            if (!name.endsWith(".jpg"))
            {
                name = name + ".jpg";
            }
        }
        else
        {
            if (!name.endsWith(".png"))
            {
                name = name + ".png";
            }
        }
        File f = new File(name);
        try
        {
            if (jpg)
            {
                ImageIO.write(img, "jpg", f);
            }
            else
            {
                ImageIO.write(img, "png", f);
            }
        }
        catch (IOException ex)
        {
            Logger.getLogger(Tools.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    static ImageReader reader = ImageIO.getImageReadersByFormatName("jpg").next();

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

    /**
     * Converts a given Image into a BufferedImage
     *
     * @param img The Image to be converted
     * @return The converted BufferedImage
     */
    public static BufferedImage toBufferedImage(Image img)
    {
        if (img instanceof BufferedImage)
        {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }

    public static Image makeColorTransparent (BufferedImage im, final Color color)
    {
        ImageFilter filter = new RGBImageFilter()
        {

            // the color we are looking for... Alpha bits are set to opaque
            final int markerRGB = color.getRGB() | 0xFF000000;

            public final int filterRGB (int x, int y, int rgb)
            {
                if ((rgb | 0xFF000000) == markerRGB)
                {
                    // Mark the alpha bits as zero - transparent
                    return 0x00FFFFFF & rgb;
                }
                else
                {
                    // nothing to do
                    return rgb;
                }
            }
        };

        ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
        return Toolkit.getDefaultToolkit().createImage(ip); //toBufferedImage(im2);
    }

    public static Image loadImageFromRessource (String name)
    {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream is = loader.getResourceAsStream(name);
        try
        {
            return ImageIO.read(is);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
