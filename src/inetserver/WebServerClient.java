package inetserver;

import misc.ImageTools;
import misc.Transmitter;
import transform.Transformation;
import transform.UrlEncodeUTF8;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

//import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Administrator
 */
class WebServerClient
{
    //private final WebServerGUI _gui;
    private final UrlEncodeUTF8 m_urltransform;
    //private volatile static int instances;

    //public static final ExecutorService executor = Executors.newFixedThreadPool(20); // .newCachedThreadPool();

    /**
     * Constructor
     *
     */
    WebServerClient ()
    {
        m_urltransform = new UrlEncodeUTF8();
        //_gui = g;
    }

    /**
     * Is File MP4
     *
     * @param in file name
     * @return TRUE if file is named .mp4
     */
    private boolean isMP4(String in)
    {
        return in.toLowerCase().endsWith(".mp4");
    }

    /**
     * Is File Jpeg?
     *
     * @param in file name
     * @return TRUE if file is jpeg
     */
    private boolean isImage (String in)
    {
        in = in.toLowerCase();
        return in.endsWith(".jpg") ||
                in.endsWith(".jpeg") ||
                in.endsWith (".png") ||
                in.endsWith(".bmp");
    }

    /**
     * Is File Zip?
     *
     * @param in file name
     * @return TRUE if file is zip
     */
    private boolean isZip(String in)
    {
        in = in.toLowerCase();
        return in.endsWith(".zip") || in.endsWith(".rar");
    }

    /**
     * Is Text file
     *
     * @param in file name
     * @return TRUE if file is text file
     */
    private boolean isText(String in)
    {
        in = in.toLowerCase();
        return in.endsWith(".txt") || in.endsWith(".c")
                || in.endsWith(".cpp")
                || in.endsWith(".h") || in.endsWith(".cxx")
                || in.endsWith(".hxx") || in.endsWith(".java");
    }

    private void appendLink (ArrayList<Path> list, StringBuilder sb)
    {
        for (Path p : list)
        {
            String u8 = m_urltransform.transform(p.toString());
            sb.append("<a href=\"").append(u8).append("\">");
            sb.append(p.getFileName().toString()).append("</a>").append("<br>\r\n");
        }
    }

    /**
     * Build HTML page for directory
     *
     * @param path dir to be used
     * @return html page
     */
    private String buildMainPage(String path)
    {
        File f = new File(path);
        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        File[] fils = f.listFiles();

        if (fils == null)
        {
            return null;
        }

        ArrayList<Path> dirs = new ArrayList<>();
        ArrayList<Path> txtfiles = new ArrayList<>();
        ArrayList<Path> otherfiles = new ArrayList<>();

        Path pp = Paths.get(path).getParent();
        if (pp != null)
        {
            String u8 = m_urltransform.transform(pp.toString());
            sb2.append("<a href=\"").append(u8).append("\">");
            sb2.append("*BACK*").append("</a>").append("<hr>\r\n");
        }
        for (File fil : fils)
        {
            String name = fil.getName();
            Path p = Paths.get(path, name);
            String u8 = m_urltransform.transform(p.toString());
            if (fil.isDirectory())
            {
                dirs.add(p);
            }
            else if (isImage(name))
            {
                sb.append("<a href=\"");
                sb.append("*IMG*");
                sb.append(u8);
                sb.append("\" target=\"_blank\"><img src=\"");
                sb.append(u8);
                sb.append("\" width=\"100\" height=\"100\"></a>\r\n");
            }
            else if (isMP4(name))
            {
                sb.append("<video controls src=\"");
                sb.append(u8).append("\">");
                sb.append("Your user agent does not support the HTML5 Video element.</video>");
                sb.append("\r\n");
            }
            else if (isText(name) || isZip(name))
            {
                txtfiles.add(p);
            }
            else
                otherfiles.add(p);
        }
        appendLink(dirs, sb2);
        appendLink(txtfiles, sb2);
        appendLink(otherfiles, sb2);
        sb2.append("<hr>");
        sb2.append(sb);
        //System.err.println(sb2.toString());
        return sb2.toString();
    }

//    private void zipHead(PrintWriter w, int len, String filename)
//    {
//        w.println("HTTP/1.1 200 OK");
//        w.println("Pragma: public");
//        w.println("Expires: 0");
//        w.println("Cache-Control: must-revalidate, post-check=0, pre-check=0");
//        w.println("Cache-Control: public");
//        w.println("Content-Description: File Transfer");
//        w.println("Content-type: application/octet-stream");
//        w.println("Content-Disposition: attachment; filename=\"" + filename + "\"");
//        w.println("Content-Transfer-Encoding: binary");
//        w.println("Content-Length: " + len);
//        w.println();
//    }
    /**
     * Send MP4 HTTP header
     *
     * @param w Socket writer
     * @param len File length
     * @param filename name of file
     * @return FALSE if something goes wrong
     */
    private boolean mp4Head(PrintWriter w, long len, String filename)
    {
        if (len <= 0)
        {
            return false;
        }
        w.println("HTTP/1.1 200 OK");
        w.println("Pragma: public");
        w.println("Expires: 0");
        w.println("Cache-Control: must-revalidate, post-check=0, pre-check=0");
        w.println("Cache-Control: public");
        w.println("Content-Description: File Transfer");
        w.println("Content-type: application/octet-stream");
        w.println("Content-Disposition: attachment; filename=\"" + filename + "\"");
        w.println("Content-Transfer-Encoding: binary");
        w.println("Content-Length: " + len);
        w.println();
        return true;
    }

    /**
     * Send image HTTP response header
     *
     * @param out output stream
     * @param len size of image
     */
    private void imgHead (OutputStream out, int len)
    {
        StringBuilder b = new StringBuilder ();
        b.append ("HTTP/1.1 200 OK\n");
        b.append("Content-Length: ").append(len).append ("\n");
        b.append("Content-Type: image/jpeg\n");
        b.append("Cache-Control: max-age=31536000, public\n");
        //w.println("Expires: 06 Apr 2020 19:25:30 GMT");
        b.append("Connection: close\n");
        b.append("\n");
        new PrintWriter (out).print (b.toString ());
    }

    /**
     * Send JPEG to output stream
     *
     * @param out output stream
     */
    private void sendJpegSmall (OutputStream out, String path) throws Exception
    {
        File f = new File(path);
        //PrintWriter w = new PrintWriter(out);
        byte[] b = ImageTools.reduceImg(f, 0.2f);
        imgHead(out, b.length);
        Transmitter t = new Transmitter(b, out);
        t.doTransmission();
        //System.gc ();
        //System.runFinalization ();
    }

    private void sendJpegOriginal(OutputStream out, String fname) throws IOException
    {
        File f = new File(fname);
        //PrintWriter w = new PrintWriter (out);
        InputStream input = new FileInputStream(f);
        imgHead (out, (int) f.length());
        Transmitter t = new Transmitter(input, out);
        t.doTransmission();
    }

    // not tested
    private void sendMP4(OutputStream out, String fname) throws Exception
    {
        System.err.println("Sending MP4: " + fname);
        File f = new File(fname);
        PrintWriter w = new PrintWriter(out);

        InputStream input = new FileInputStream(f);
        if (!mp4Head(w, f.length(), fname))
        {
            w.println("File too small");
            w.close();
        }
        Transmitter t = new Transmitter(input, out);
        t.doTransmission();
    }

    private void sendZip(OutputStream out, String fname) throws IOException
    {
        File f = new File(fname);
        PrintWriter w = new PrintWriter(out);
        InputStream input = new FileInputStream(f);
        mp4Head(w, f.length(), fname);
        Transmitter t = new Transmitter(input, out);
        t.doTransmission();
    }

    /**
     * send image page
     *
     * @param out Print Writer
     */
    private void imagePage(OutputStream out, String path) throws Exception
    {
        String mp = buildMainPage(path);
        if (mp == null)
        {
            //PrintWriter p = new PrintWriter (out);
            textFile(out, path);
            //p.close();
        }
        String txt = "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"utf-8\"/></head>\r\n"
                + mp
                + "\r\n</html>";
        byte[] bt = txt.getBytes(Transformation.utf8);
        out.write(bt);
        out.flush();
    }

    private byte[] getTextFile(String file) throws IOException
    {
        Path p = Paths.get(file);
        return Files.readAllBytes(p);
    }

    private void textFile (OutputStream os, String path) throws IOException
    {
        byte[] b = getTextFile(path);
        String cnt = new String(b, StandardCharsets.UTF_8);
        String txt = "<html><pre>\r\n" + cnt + "</pre></html>";
        new PrintStream(os).print(txt);
    }

    private String[] getInput(String in)
    {
//        while (in.ready() == false)
//        {
//            System.out.println("Tick");
//            Thread.sleep(1000);
//        }
        return in.split(" ");
    }
    
    void perform (String basePath, String cmd, OutputStream os) throws Exception
    {
        os = new BufferedOutputStream (os);
        //PrintWriter out = new PrintWriter(os, true);
        String[] si = getInput(cmd);
        String path = si[0].substring(1);

        path = m_urltransform.retransform(path);

        //String lowpath = path.toLowerCase();
        if (isImage(path))
        {
            if (path.startsWith("*IMG*"))
            {
                sendJpegOriginal(os, path.substring(5));
            }
            else
            {
                sendJpegSmall(os, path);
            }
        }
        else if (isZip(path))
        {
            sendZip(os, path);
        }
        else if (isMP4(path))
        {
            sendMP4(os, path);
        }
        else if (isText(path))
        {
            textFile(os, path);
            //System.out.println("text -- " + path);
        }
        else
        {
            if (path.isEmpty())
            {
                path = basePath;
            }
            imagePage(os, path);
        }
    }
}
