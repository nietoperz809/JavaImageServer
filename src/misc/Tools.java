/*
 * Tools of all kind
 */
package misc;

//import transform.Transformation;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
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
    private static final String m_path = "../ser/";
    private static final ExecutorService globalExecutor = Executors.newFixedThreadPool(20);

    public static void execute (Runnable r)
    {
        if (getExecutorFreeSlots() <= 0)
        {
            System.out.println("Thread pool exhausted");
        }
        globalExecutor.execute(r);
    }

    private static int getExecutorFreeSlots ()
    {
        int tc = ((ThreadPoolExecutor) globalExecutor).getActiveCount();
        int tm = ((ThreadPoolExecutor) globalExecutor).getCorePoolSize();
        System.out.println(tc + "/" + tm);
        return tm - tc;
    }

    public static Future submit (Runnable r)
    {
        if (getExecutorFreeSlots() <= 0)
        {
            System.out.println("Thread pool exhausted");
        }
        return globalExecutor.submit(r);
    }

    public static String humanReadableByteCount (long bytes, boolean si)
    {
        int unit = si ? 1000 : 1024;
        if (bytes < unit)
        {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static String humanReadableByteCount (long bytes)
    {
        DecimalFormat myFormatter = new DecimalFormat("000,000,000");
        return "  " + myFormatter.format(bytes);
    }

    public static double readDouble (JTextField jf, double defaultvalue)
    {
        double res;
        try
        {
            res = Double.parseDouble(jf.getText());
        }
        catch (Exception ex)
        {
            res = defaultvalue;
        }
        System.out.println(res);
        return res;
    }

    public static int readInt (JTextField jf, int defaultvalue)
    {
        int res;
        try
        {
            res = Integer.parseInt(jf.getText());
        }
        catch (Exception ex)
        {
            res = defaultvalue;
        }
        System.out.println(res);
        return res;
    }

    static void printStringArray (String[] sa)
    {
        for (String s : sa)
        {
            System.out.println(s);
            System.out.println(s.length());
            System.out.println("----------------");
        }
    }

    static void printStringList (List<String> sa)
    {
        for (String s : sa)
        {
            System.out.println(s);
            System.out.println("----------------");
        }
    }

    /**
     * Create a save
     *
     * @param filename
     * @param o
     * @throws Exception
     */
    public static void serialize (String filename, Object o) throws Exception
    {
        new File(m_path).mkdirs();
        FileOutputStream f_out = new FileOutputStream(m_path + filename);
        ObjectOutputStream obj_out = new ObjectOutputStream(f_out);
        obj_out.writeObject(o);
        obj_out.close();
        f_out.close();
    }

    /**
     * Delete a save
     *
     * @param filename
     */
    public static void deleteSave (String filename)
    {
        File f = new File(m_path + filename);
        String message = f.exists() ? "is in use by another app" : "does not exist";
        if (!f.delete())
        {
            System.out.println("Could not delete " + m_path + filename + " -- " + message);
        }
    }

    /**
     * Open a save
     *
     * @param filename
     * @return
     * @throws Exception
     */
    public static Object deSerialize (String filename) throws Exception
    {
        Object ret = null;
        FileInputStream f_in = new FileInputStream(m_path + filename);
        ObjectInputStream obj_in = new ObjectInputStream(f_in);
        try
        {
            ret = obj_in.readObject();
        }
        finally
        {
            obj_in.close();
            f_in.close();
        }
        return ret;
    }

    /**
     * @return
     */
    public static String getClipBoardString ()
    {
        Clipboard clipboard = getDefaultToolkit().getSystemClipboard();
        Transferable clipData = clipboard.getContents(clipboard);
        if (clipData != null)
        {
            try
            {
                if (clipData.isDataFlavorSupported(stringFlavor))
                {
                    return (String) (clipData.getTransferData(stringFlavor));
                }
            }
            catch (UnsupportedFlavorException | IOException ufe)
            {
                System.err.println("getClipoardString fail");
            }
        }
        return null;
    }

    public static void toClipBoard (final String s)
    {
        StringSelection selection = new StringSelection(s);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }

    public static String removeHTML (String s)
    {
        s = s.replace("<br>", "\n");
        s = s.replaceAll("<.*?>", "");
        return s;
    }

    /**
     * Get a list of all saves
     *
     * @return
     */
    public static List<String> listSaves ()
    {
        List<String> result = new ArrayList<>();
        File[] files = new File(m_path).listFiles();
        if (files == null)
        {
            return result;
        }
        for (File file : files)
        {
            if (file.isFile())
            {
                result.add(file.getName());
            }
        }
        return result;
    }

//    /**
//     * Get all file names of a package
//     *
//     * @param path package (as "hello/world/uh" instead of hello.world.uh)
//     * @return String array with all names
//     */
//    public static String[] listPackage (String path)
//    {
//        try
//        {
//            int pathLen = path.length();
//            URL url1 = ClassLoader.getSystemResource(path);
//
//            String jarFileName;
//            JarFile jf;
//            Enumeration<JarEntry> jarEntries;
//            String entryName;
//            ArrayList<String> list = new ArrayList<>();
//
//            jarFileName = URLDecoder.decode(url1.getFile(), Transformation.utf8);
//            jarFileName = jarFileName.substring(5, jarFileName.indexOf("!"));
//            jf = new JarFile(jarFileName);
//            jarEntries = jf.entries();
//            while (jarEntries.hasMoreElements())
//            {
//                entryName = jarEntries.nextElement().getName();
//                if (entryName.startsWith(path))
//                {
//                    entryName = entryName.substring(pathLen);
//                    if (!entryName.isEmpty())
//                    {
//                        list.add(entryName);
//                    }
//                }
//            }
//            String[] arr = new String[list.size()];
//            list.toArray(arr);
//            return arr;
//        }
//        catch (Exception ex)
//        {
//            Logger.getLogger(Tools.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return null;
//    }

    /**
     * Centers Component
     *
     * @param a Component to center
     * @param b Parent component
     */
    public static void centerComponent (Component a, Component b)
    {
        Dimension db = b.getSize();
        Dimension da = a.getSize();
        Point pt = new Point((db.width - da.width) / 2, (db.height - da.height) / 2);
        if (pt.x < 0)
        {
            pt.x = 0;
        }
        if (pt.y < 0)
        {
            pt.y = 0;
        }
        a.setLocation(pt);
    }

    /**
     * Calls str.replace repeatedly
     *
     * @param s input string
     * @param a substring to search
     * @param b replacement
     * @return changed string
     */
    public static String realReplaceAll (String s, String a, String b)
    {
        for (; ; )
        {
            String n = s.replace(a, b);
            if (n.equals(s))
            {
                return n;
            }
            s = n;
        }
    }

    public static String getJavaInfo()
    {
        return System.getProperty("java.version");
    }

}
