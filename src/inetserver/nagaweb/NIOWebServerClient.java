package inetserver.nagaweb;

import inetserver.nagaweb.videostream.Http206Transmitter;
import misc.*;
import naga.NIOSocket;
import transform.UrlEncodeUTF8;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingLong;
import static misc.ThumbManager.DNAME;
import static misc.Tools.getResource;
import static org.apache.commons.io.FileUtils.deleteDirectory;

/**
 * Serves HTTP for this imageserver
 *
 * @author Administrator
 */
public class NIOWebServerClient {
    private static final String BIGIMAGE = "*IMG*";
    private static final String NUMSEP = "@";
    private final String m_basePath;
    private final char m_pageStyle;
    private int saltValue;
    private ArrayList<File> fileList;
    private ThumbManager thumbs;

    /**
     * Constructor
     *
     * @param basePath  Path tha is served
     * @param pageStyle
     */
    public NIOWebServerClient (String basePath, char pageStyle) {
        m_basePath = basePath;
        m_pageStyle = pageStyle;
    }

    private void appendLink (ArrayList<Path> list, StringBuilder sb, boolean isDirlist) {
        if (list.size () == 0)
            return;
        if (isDirlist) {
            list.sort ((o1, o2) ->
                    o1.getFileName ().toString ().compareToIgnoreCase (o2.getFileName ().toString ()));
        }
        sb.append ("<table><tr>");
        int cnt = 0;
        for (Path p : list) {
            cnt++;
            if (cnt % 10 == 0)
                sb.append ("</tr><tr>");
            String u8 = UrlEncodeUTF8.transform (p.toString ());
            sb.append ("<td><a href=\"").append (u8).append ("\">");
            String filename = p.getFileName ().toString ();
            sb.append (filename).append ("</a>").append ("</td>\r\n");
        }
        sb.append ("</tr></table><hr>");
    }

    /**
     * Create image link for gallery page
     *
     * @param idx  index of file
     * @param path Path to file
     * @return the link as String
     */
    private String createImagePageLink (int idx, Path path) {
        String arg = "'trash=" + idx + "'";
        return "<span class=\"container\">\n" +
                "<a href=\"show.html?img=" + idx + "\" target=\"_blank\">" +
                "<img src=\""
                + saltValue + NUMSEP + idx + ".jpg"
                + "\" title=\"" + path.getFileName ().toString () + "\""
                + "></a><button class=\"btn\" onclick=\"proceed("
                + arg
                + ");\">Move to trash</button>\n</span>\r\n";
    }

    /**
     * Create video link for gallery page
     *
     * @param idx  index of file
     * @param path Path to file
     * @return the link as String
     */
    private String createVideoPageLink (int idx, Path path) {
        return "<a href=\""
                + "show.html?vid=" + idx
                + "\" target=\"_blank\"><img src=\""
                + saltValue + NUMSEP + idx + ".jpg"
                + "\" title=\"" + path.getFileName ().toString () + "\""
                + "></a>\r\n";
    }

    String createNavIndex (int idx, boolean back) {
        int newIdx = idx;
        do {
            newIdx = back ? newIdx - 1 : newIdx + 1;
            if (newIdx == -1)
                newIdx = fileList.size () - 1;
            else if (newIdx == fileList.size ())
                newIdx = 0;
            if (newIdx == idx)  // detect endless loop
                break;
        } while (!Tools.isImage (fileList.get (newIdx).getName ()));
        return "" + newIdx;
    }

// --Commented out by Inspection START (2/14/2022 8:00 PM):
//    /**
//     * create fwd or bckwd navigation links
//     *
//     * @param idx
//     * @param back
//     * @return the link as string
//     */
//    private String createNavigationLink (int idx, boolean back) {
//        String newIdx = createNavIndex (idx, back);
//        return "<a id=\""
//                + (back ? "prv" : "nxt")
//                + "\" href=\"show.html?img=" + newIdx
//                + "\" target=\"_self\"><img src=\""
//                + (back ? "backarrow.ico" : "fwdarrow.ico") + "\"></a>\n";
//    }
// --Commented out by Inspection STOP (2/14/2022 8:00 PM)

    /**
     * Build HTML page for directory
     *
     * @param path dir to be used
     * @return html page
     */
    private String buildGalleryPage (String path) {
        thumbs = new ThumbManager (path);
        saltValue = (int) System.currentTimeMillis (); // path.hashCode ();
        StringBuilder sb = new StringBuilder ();
        StringBuilder sb2 = new StringBuilder ();

        File[] pa = new File (path).listFiles ();
        if (pa == null) {
            return null;
        }
        Arrays.sort (pa, comparingLong (File::lastModified)); // Sort by date

        fileList = new ArrayList<> (Arrays.asList (pa));
        int totalsize = fileList.size ();
        fileList = (ArrayList<File>) fileList.stream ()
                //.filter(p -> p.length() < MAXSIZE)
                .filter (p -> !p.getName ().endsWith (DNAME))
                .collect (Collectors.toList ());
        int filtsize = fileList.size ();

        ArrayList<Path> dirs = new ArrayList<> ();
        ArrayList<Path> txtfiles = new ArrayList<> ();
        ArrayList<Path> otherfiles = new ArrayList<> ();

        sb2.append ("Here: ").append (path).append ("  --  \r\n");
        sb2.append ("Objects: ").append (totalsize).append (" --- Omitted: ").append (totalsize - filtsize);
        Path pp = Paths.get (path).getParent ();
        if (pp != null && !path.equals (m_basePath)) {
            String u8 = UrlEncodeUTF8.transform (pp.toString ());
            sb2.append (" --- <a href=\"").append (u8).append ("\">");
            sb2.append ("*BACK*").append ("</a>");
        }
        sb2.append ("<hr>\r\n");

        int imageCtr = 0;
        int vidCtr = 0;

        for (int idx = 0; idx < fileList.size (); idx++) {
            File fil = fileList.get (idx);
            String name = fil.getName ();
            //System.out.println (name);
            Path p = Paths.get (path, name);
            String u8 = UrlEncodeUTF8.transform (p.toString ());
            if (fil.isDirectory ()) {
                dirs.add (p);
            } else if (Tools.isImage (name)) {
                imageCtr++;
                sb.append (createImagePageLink (idx, p));
            } else if (Tools.isVideo (name)) {
                vidCtr++;
                try {
                    thumbs.getVideoThumbnail (fil);
                } catch (Exception e) {
                    Dbg.print ("vtn failed" + e);
                }
                sb.append (createVideoPageLink (idx, p));
            } else if (Tools.isAudio (name)) {
                sb.append ("<figure><figcaption>");
                sb.append (p.getFileName ().toString ());
                sb.append ("</figcaption><audio controls src=\"");
                sb.append (u8).append ("\">");
                sb.append ("HTML5 Audio not supported</audio></figure>");
                sb.append ("\r\n");
            } else if (Tools.isText (name) || Tools.isZip (name)) {
                txtfiles.add (p);
            } else
                otherfiles.add (p);
        }
        appendLink (dirs, sb2, true);
        appendLink (txtfiles, sb2, false);
        appendLink (otherfiles, sb2, false);
        sb2.append (sb);
        sb2.append ("<br>Images: ").append (imageCtr).append ("<br>Videos ").append (vidCtr);
        sb2.append ("<form action=\"delthumb\">\n" + "<input type=\"hidden\" name=\"").append (path).append ("\">").append ("    <input type=\"submit\" value=\"Regenerate Thumbs\" />\n").append ("</form>");
        return sb2.toString ();
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
     * @param w   Socket writer
     * @param len File length
     */
    private void dataFileHead (NIOSocket w, long len, String filename) {
        if (len <= 0) {
            return;
        }
        w.println ("HTTP/1.1 200 OK");
        w.println ("Pragma: public");
        w.println ("Expires: 0");
        w.println ("Cache-Control: must-revalidate, post-check=0, pre-check=0");
        w.println ("Cache-Control: public");
        w.println ("Content-Description: File Transfer");
        w.println ("Content-Type: " + MimeNames.getMime (Tools.getExtension (filename)));
        w.println ("Content-Disposition: attachment; filename=\"" + filename + "\"");
        w.println ("Content-Transfer-Encoding: binary");
        w.println ("Content-Length: " + len);
        w.println ("");
    }

    /**
     * Send image HTTP response header
     *
     * @param out output stream
     * @param len size of image
     */
    private void imgHead (NIOSocket out, int len) {
        String b = "HTTP/1.1 200 OK\n" +
                "Content-Length: " + len + "\n" +
                "Content-Type: image/jpeg\n" +
                "Cache-Control: max-age=31536000, public" +
                "Cache-Control: max-age=31536000, public" +
                "\nConnection: close\n" +
                "\n";
        out.write (b.getBytes (StandardCharsets.UTF_8));
    }

    /**
     * Send JPEG to output stream
     *
     * @param out output stream
     */
    private void sendJpegSmall (NIOSocket out, File f) throws Exception {
        byte[] bytes;
        if (Tools.isVideo (f.getName ()))
            bytes = thumbs.getVideoThumbnail (f);
        else
            bytes = thumbs.getImageThumbnail (f);
        imgHead (out, bytes.length);
        out.write (bytes);
    }

    private void sendJpegOriginal (NIOSocket out, File f) throws Exception {
        imgHead (out, (int) f.length ());
        transmitFileInChunks (out, f);
    }

    private void transmitFileInChunks (NIOSocket out, File f) throws Exception {
        FileInputStream fi = new FileInputStream (f);
        boolean ret;
        long total = 0;
        while (true) {
            int amount = Math.min (fi.available (), 0xA00000);
            if (amount <= 0)
                break;
            byte[] b = new byte[amount];
            fi.read (b);
            ret = out.write (b);
            while (!ret) {
                Thread.yield ();
                ret = out.write (b);
            }
            b = null;  // GC should catch it;
            System.gc ();
            System.runFinalization ();
            total += amount;
            Dbg.print ("Chunked " + Tools.humanReadableByteCount (total));
        }
        fi.close ();
    }

    private void sendDataFile (NIOSocket out, String fname) throws Exception {
        File f = new File (fname);
        dataFileHead (out, f.length (), fname);
        transmitFileInChunks (out, f);
    }

    private void sendHtmlOverHttp2 (String content, NIOSocket out) throws Exception {
        String http = "HTTP/1.1 200 OK\r\n\r\n " + content;
        out.write (http.getBytes (UrlEncodeUTF8.utf8));
    }

    private void sendHtmlOverHttp (String content, NIOSocket out) throws Exception {
        String mystyle = getResource ("style1.css");
        String rightclick = getResource ("buttonclick.html");
        String containercss = getResource ("container.css");
        String http = "HTTP/1.1 200 OK\r\n\r\n <!DOCTYPE html><html lang=\"en\"><head>\n"
                + "<meta charset=\"utf-8\"/>\n"
                + "<style>" + mystyle + "</style>"
                + "<style>" + containercss + "</style>"
                + rightclick
                + "</head>\r\n"
                + "<body>" + content
                + "</body>"
                + "\r\n</html>";
        out.write (http.getBytes (UrlEncodeUTF8.utf8));
    }

    /**
     * Tx main Gallery
     *
     * @param out  Socket
     * @param path points to image stuff
     * @throws Exception if smth. gone wrong
     */
    private void sendGalleryPage (NIOSocket out, String path) throws Exception {
        String mainPage = buildGalleryPage (path);
        if (mainPage == null) {
            mainPage = formatTextFile (path);
        }
        sendHtmlOverHttp (mainPage, out);
    }

    private void toTrash (NIOSocket out, String path) throws Exception {
        String num = path.substring (path.lastIndexOf ('=') + 1);
        num = num.substring(0, num.length() - 1);
        int idx = Integer.parseInt (num);
        String msg;
        if (fileList == null)
            msg = "Please reload gallery page";
        else {
            File current = fileList.get (idx);
            boolean b = Tools.moveToTrash (current.getAbsolutePath ());
            if (b)
                msg = "Success!";
            else
                msg = "Fail!";
        }
        sendHtmlOverHttp2 ("<h1>"+msg+"</h1>", out);
    }

    private void sendMediaPage2 (NIOSocket out, String path) throws Exception {
        int idx = Integer.parseInt (path.substring (path.lastIndexOf ('=') + 1));
        String body;
        if (fileList == null)
            body = "<h1>Please reload gallery page</h1>";
        else {
            File current = fileList.get (idx);
            String headline = ": " + idx + " - " + current.getName () + " - " +
                    Tools.humanReadableByteCount (current.length ()) + " - ";
            String backidx = createNavIndex (idx, true);
            String fwdidx = createNavIndex (idx, false);
            if (Tools.isVideo (current.getName ())) {
                body = getResource ("videopage.html");
                InetAddress inetAddress = InetAddress.getLocalHost ();
                String vidserv = "http://" + inetAddress.getHostAddress () + ":" +
                        Http206Transmitter.getInstance ().getPort (); // server
                String vid = current.getAbsolutePath (); // video
                Http206Transmitter.getInstance ().setVideo (vid);
                body = body.replace ("@@VIDSRC", vidserv);
                body = body.replace ("@@VIDINFO", "- Vid" + headline);
            } else {
                if (m_pageStyle == '0')
                    body = getResource ("imgpage0.html");
                else
                    body = getResource ("imagepage.html");
                String img = BIGIMAGE + path.substring (path.indexOf ("?img=") + 5) + NUMSEP + saltValue + ".jpg";
                body = body.replace ("@@THEIMG", img);
                body = body.replace ("@@IMGINFO", "- Img" + headline);
            }
            body = body.replace ("@@PRV", backidx);
            body = body.replace ("@@NXT", fwdidx);
        }
        sendHtmlOverHttp2 (body, out);
    }

//    private void sendMediaPage (NIOSocket out, String path) throws Exception {
//        int idx = Integer.parseInt (path.substring (path.lastIndexOf ('=') + 1));
//        String body;
//        if (fileList == null)
//            body = "<h1>Please reload gallery page</h1>";
//        else {
//            File current = fileList.get (idx);
//            String myscript = "document.onkeydown = checkKey;\n" +
//                    "function checkKey(e) {\n" +
//                    "    e = e || window.event;\n" +
//                    "    if (e.keyCode == '37') prv.click();\n" +
//                    "    else if (e.keyCode == '39')  nxt.click();\n" +
//                    "}";
//            body = "<script>" + myscript + "</script>";
//            String headline = ": " + idx + " - " + current.getName () + " - " +
//                    Tools.humanReadableByteCount (current.length ()) + " - " +
//                    createNavigationLink (idx, true) +
//                    createNavigationLink (idx, false) + "<hr>";
//            if (Tools.isVideo (current.getName ())) {
//                InetAddress inetAddress = InetAddress.getLocalHost ();
//                String vidserv = "http://" + inetAddress.getHostAddress () + ":" +
//                        Http206Transmitter.getInstance ().getPort (); // server
//                String vid = current.getAbsolutePath (); // video
//                Http206Transmitter.getInstance ().setVideo (vid);
//                body = body + "- Vid" + headline +
//                        "<video width=\"100%\" controls id=\"video\" src=\"" + vidserv + "\" autoplay=\"autoplay\" />";
//                // "<iframe src=\""+vidserv+"\"></iframe>";
//            } else {
//                String img = BIGIMAGE + path.substring (path.indexOf ("?img=") + 5) + NUMSEP + saltValue + ".jpg";
//                body = body + "- Img" + headline +
//                        "<img src=\"" + img + "\" style=\"width: 100%;\" />";
//            }
//        }
//        sendHtmlOverHttp (body, out);
//    }

    private byte[] loadTextFile (String file) throws Exception {
        Path p = Paths.get (file);
        return Files.readAllBytes (p);
    }

    private String formatTextFile (String path) {
        String cnt;
        byte[] b; // = new byte[0];
        try {
            b = loadTextFile (path);
            cnt = new String (b, StandardCharsets.UTF_8);
        } catch (Exception e) {
            cnt = e.toString ();
        }
        return "<pre>\r\n" + cnt + "</pre>";
    }

    /**
     * webserver main function
     *
     * @param imagePath    point to image stuff on disk
     * @param http         http object
     * @param outputSocket socket for TX
     * @throws Exception if smth gone wrong
     */
    void handleRequest (String imagePath, Http http, NIOSocket outputSocket) throws Exception {
        String resource = UrlEncodeUTF8.retransform (http.getRequestedResource ());
        if (Objects.requireNonNull (resource).startsWith ("delthumb?")) {
            String target = resource.substring (9, resource.length () - 1);
            String thumbsdir = target + File.separator + DNAME;
            deleteDirectory (new File (thumbsdir));
            sendGalleryPage (outputSocket, target);
        } else if (resource.startsWith ("trash")) {
            //System.out.println (resource);
            toTrash (outputSocket, resource);
        } else if (Tools.isImage (resource)) {
            resource = resource.substring (0, resource.lastIndexOf ('.'));
            if (resource.startsWith (BIGIMAGE)) {
                resource = resource.substring (0, resource.indexOf (NUMSEP));
                int idx = Integer.parseInt (resource.substring (5));
                sendJpegOriginal (outputSocket, fileList.get (idx));
            } else {
                resource = resource.substring (resource.indexOf (NUMSEP) + 1);
                int idx = Integer.parseInt (resource);
                sendJpegSmall (outputSocket, fileList.get (idx));
            }
        } else if (Tools.isZip (resource) ||
                Tools.isVideo (resource) ||
                Tools.isAudio (resource)) {
            sendDataFile (outputSocket, resource);
        } else if (Tools.isText (resource)) {
            String s = "HTTP/1.1 200 OK\r\n\r\n <html>" + formatTextFile (resource) + "</html>";
            outputSocket.write (s.getBytes (StandardCharsets.UTF_8));
        } else if (resource.equals ("favicon.ico") ||
                resource.equals ("backarrow.ico") ||
                resource.equals ("fwdarrow.ico") ||
                resource.equals ("jquery.min.js") ||
                resource.equals ("jquery.zoom.js")) {
            //System.out.println ("load "+resource);
            byte[] bt = Tools.getResourceAsArray (resource);
            outputSocket.write (bt);
        } else if (resource.startsWith ("show.html")) {
            //sendMediaPage (outputSocket, resource);
            sendMediaPage2 (outputSocket, resource);
        } else {
            if (resource.isEmpty ()) {
                resource = imagePath;
            }
            if (new File (resource).isDirectory ())
                sendGalleryPage (outputSocket, resource);
            else
                sendDataFile (outputSocket, resource);
        }
    }
}
