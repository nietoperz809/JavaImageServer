package misc;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import java.awt.*;
import java.awt.image.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

    public static boolean saveImage (String name, BufferedImage img, boolean jpg)
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

    /**
     * Reduces image quality
     *
     * @param path Path of jpeg file
     * @param qual Quality 0.0 ... 1.0
     * @return byte array of jpeg data
     * @throws Exception
     */
    public static byte[] reduceImg (File path, float qual) throws Exception
    {
        BufferedImage image = ImageIO.read(path);
        image = resizeImage(image, 100, 100);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();

        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(qual); // Change this, float between 0.0 and 1.0

        writer.setOutput(ImageIO.createImageOutputStream(os));
        writer.write(null, new IIOImage(image, null, null), param);
        writer.dispose();
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
    public static BufferedImage resizeImage (BufferedImage originalImage, int width, int height)
    {
        if (originalImage == null)
        {
            return null;
        }
        int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
        BufferedImage resizedImage = new BufferedImage(width, height, type);
        Graphics2D g = resizedImage.createGraphics();
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
        Image im2 = Toolkit.getDefaultToolkit().createImage(ip);
        return im2; //toBufferedImage(im2);
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
