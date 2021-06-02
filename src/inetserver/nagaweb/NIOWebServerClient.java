package inetserver.nagaweb;


import misc.Http;
import misc.ThumbManager;
import misc.Tools;
import naga.NIOSocket;
import transform.UrlEncodeUTF8;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingLong;
import static misc.Tools.*;

/**
 * @author Administrator
 */
public class NIOWebServerClient {
    private static final long MAXSIZE = 500_000_000;
    private static final String BIGIMAGE = "*IMG*";
    private static final String NUMSEP = "@";
    private final String m_basePath;
    private final String myscript = "document.onkeydown = checkKey;\n" +
            "function checkKey(e) {\n" +
            "    e = e || window.event;\n" +
            "    if (e.keyCode == '37') prv.click();\n" +
            "    else if (e.keyCode == '39')  nxt.click();\n" +
            "}";
    private final String mystyle = "a:hover {\n" +
            "  color: white;\n" +
            "  background-color: black;\n" +
            "}\n" +
            "a:visited {\n" +
            "  color: green;\n" +
            "}\n";
    private int pathHash;
    private ArrayList<File> fileList;
    private ThumbManager thumbs;

    public NIOWebServerClient(String basePath) {
        m_basePath = basePath;
    }

    public static boolean isText(String in) {
        return hasExtension(in, ".txt", ".cpp", ".c", ".h", ".java", ".cxx", ".hxx");
    }

    private void appendLink(ArrayList<Path> list, StringBuilder sb, boolean isDirlist) {
        if (list.size() == 0)
            return;
        if (isDirlist)
            list.sort((o1, o2) ->
                    o1.getFileName().toString().compareToIgnoreCase(o2.getFileName().toString()));
        for (Path p : list) {
            String u8 = UrlEncodeUTF8.transform(p.toString());
            sb.append("<a href=\"").append(u8).append("\">");
            String filename = isDirlist ? "&lt;" + p.getFileName().toString() + "&gt;" : p.getFileName().toString();
            sb.append(filename).append("</a>").append("<br>\r\n");
        }
        sb.append("<hr>");
    }

    private String createImagePageLink(int idx, Path path) {
        return "<a href=\""
                + "show.html?img=" + idx
                + "\" target=\"_blank\"><img src=\""
                + pathHash + NUMSEP + idx + ".jpg"
                + "\" title=\"" + path.getFileName().toString() + "\""
                + "></a>\r\n";
    }

    private String createVideoPageLink(int idx, Path path) {
        return "<a href=\""
                + "show.html?vid=" + idx
                + "\" target=\"_blank\"><img src=\""
                + pathHash + NUMSEP + idx + ".jpg"
                + "\" title=\"" + path.getFileName().toString() + "\""
                + "></a>\r\n";
    }

    private String createNavigationLink(int idx, boolean back) {
        int newIdx = idx;
        do {
            newIdx = back ? newIdx - 1 : newIdx + 1;
            if (newIdx == -1)
                newIdx = fileList.size() - 1;
            else if (newIdx == fileList.size())
                newIdx = 0;
            if (newIdx == idx)  // detect endless loop
                break;
        } while (!isImage(fileList.get(newIdx).getName()));
        return "<a id=\""
                + (back ? "prv" : "nxt")
                + "\" href=\"show.html?img=" + newIdx
                + "\" target=\"_self\"><img src=\""
                + (back ? "backarrow.ico" : "fwdarrow.ico") + "\"></a>\n";
    }

    /**
     * Build HTML page for directory
     *
     * @param path dir to be used
     * @return html page
     */
    private String buildGalleryPage(String path) {
        thumbs = new ThumbManager(path);
        pathHash = path.hashCode();
        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();

        File[] pa = new File(path).listFiles();
        if (pa == null) {
            return null;
        }
        Arrays.sort(pa, comparingLong(File::lastModified)); // Sort by date

        fileList = new ArrayList<>(Arrays.asList(pa));
        int totalsize = fileList.size();
        fileList = (ArrayList<File>) fileList.stream()
                .filter(p -> p.length() < MAXSIZE)
                .filter(p -> !p.getName().endsWith(ThumbManager.DNAME))
                .collect(Collectors.toList());
        int filtsize = fileList.size();

        ArrayList<Path> dirs = new ArrayList<>();
        ArrayList<Path> txtfiles = new ArrayList<>();
        ArrayList<Path> otherfiles = new ArrayList<>();

        sb2.append("Here: ").append(path).append("  --  \r\n");
        sb2.append("Objects: ").append(totalsize).append(" --- Omitted: ").append(totalsize - filtsize);
        Path pp = Paths.get(path).getParent();
        if (pp != null || !path.equals(m_basePath)) {
            String u8 = UrlEncodeUTF8.transform(pp.toString());
            sb2.append(" --- <a href=\"").append(u8).append("\">");
            sb2.append("*BACK*").append("</a>");
        }
        sb2.append("<hr>\r\n");

        int imageCtr = 0;
        int vidCtr = 0;

        for (int idx = 0; idx < fileList.size(); idx++) {
            File fil = fileList.get(idx);
            String name = fil.getName();
            Path p = Paths.get(path, name);
            String u8 = UrlEncodeUTF8.transform(p.toString());
            if (fil.isDirectory()) {
                dirs.add(p);
            } else if (isImage(name)) {
                imageCtr++;
                sb.append(createImagePageLink(idx, p));
            } else if (isVideo(name)) {
                vidCtr++;
                try {
                    thumbs.getVideoThumbnail(fil);
                } catch (Exception e) {
                    System.out.println("vtn failed" + e);
                }
                sb.append(createVideoPageLink(idx, p));
            } else if (isAudio(name)) {
                sb.append("<figure><figcaption>");
                sb.append(p.getFileName().toString());
                sb.append("</figcaption><audio controls src=\"");
                sb.append(u8).append("\">");
                sb.append("HTML5 Audio not supported</audio></figure>");
                sb.append("\r\n");
            } else if (isText(name) || isZip(name)) {
                txtfiles.add(p);
            } else
                otherfiles.add(p);
        }
        appendLink(dirs, sb2, true);
        appendLink(txtfiles, sb2, false);
        appendLink(otherfiles, sb2, false);
        sb2.append(sb);
        sb2.append("<br>Images: ").append(imageCtr).append("<br>Videos ").append(vidCtr);
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
     * @param w   Socket writer
     * @param len File length
     */
    private void mp4Head(NIOSocket w, long len, String filename) {
        if (len <= 0) {
            return;
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
        w.println("");
    }

    /**
     * Send image HTTP response header
     *
     * @param out output stream
     * @param len size of image
     */
    private void imgHead(NIOSocket out, int len) {
        String b = "HTTP/1.1 200 OK\n" +
                "Content-Length: " + len + "\n" +
                "Content-Type: image/jpeg\n" +
                "Cache-Control: max-age=31536000, public" +
                "Cache-Control: max-age=31536000, public" +
                "\nConnection: close\n" +
                "\n";
        out.write(b.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Send JPEG to output stream
     *
     * @param out output stream
     */
    private void sendJpegSmall(NIOSocket out, File f) throws Exception {
        byte[] bytes;
        if (isVideo(f.getName()))
            bytes = thumbs.getVideoThumbnail(f);
        else
            bytes = thumbs.getImageThumbnail(f);
        imgHead(out, bytes.length);
        out.write(bytes);
    }

    private void sendJpegOriginal(NIOSocket out, File f) throws Exception {
        imgHead(out, (int) f.length());
        transmitFileInChunks(out, f);
    }

    private void transmitFileInChunks (NIOSocket out, File f) throws Exception {
        FileInputStream fi = new FileInputStream(f);
        boolean ret;
        long total = 0;
        while (true)
        {
            int amount = Math.min (fi.available(), 0xA00000);
            if (amount <= 0)
                break;
            byte[] b = new byte[amount];
            fi.read(b);
            ret = out.write(b);
            while (ret == false)
            {
                Thread.sleep(100);
                ret = out.write (b);
            }
            b = null;  // GC should catch it;
            System.gc ();
            System.runFinalization ();
            total += amount;
            System.out.println("Chunked " + Tools.humanReadableByteCount(total));
        }
        fi.close();
    }

    private void sendMedia(NIOSocket out, String fname) throws Exception {
        File f = new File(fname);
        mp4Head(out, f.length(), fname);
        transmitFileInChunks(out, f);
    }

    private void sendZip(NIOSocket out, String fname) throws IOException {
        File f = new File(fname);
        byte[] b = Files.readAllBytes(f.toPath());
        mp4Head(out, f.length(), fname);
        out.write(b);
    }

    private void sendHtmlOverHttp(String content, NIOSocket out) throws Exception {
        String http = "HTTP/1.1 200 OK\r\n\r\n <!DOCTYPE html><html lang=\"en\"><head>"
                + "<meta charset=\"utf-8\"/>"
                + "<style>" + mystyle + "</style>"
                + "</head>\r\n"
                + "<body>" + content + "</body>"
                + "\r\n</html>";
        out.write(http.getBytes(UrlEncodeUTF8.utf8));
    }

    /**
     * Tx main Gallery
     *
     * @param out  Socket
     * @param path points to image stuff
     * @throws Exception if smth. gone wrong
     */
    private void sendGalleryPage(NIOSocket out, String path) throws Exception {
        String mainPage = buildGalleryPage(path);
        if (mainPage == null) {
            mainPage = formatTextFile(path);
        }
        sendHtmlOverHttp(mainPage, out);
    }

    private void sendMediaPage(NIOSocket out, String path) throws Exception {
        int idx = Integer.parseInt(path.substring(path.lastIndexOf('=') + 1));
        String body;
        if (fileList == null)
            body = "<h1>Please reload gallery page</h1>";
        else {
            File current = fileList.get(idx);
            body = "<script>" + myscript + "</script>";
            String headline = ": " + idx + " - " + current.getName() + " - " +
                    humanReadableByteCount(current.length()) + " - " +
                    createNavigationLink(idx, true) +
                    createNavigationLink(idx, false) + "<hr>";
            if (isVideo(current.getName())) {
                String u8 = UrlEncodeUTF8.transform(current.getAbsolutePath());
                body = body + "- Vid" + headline +
                        "<video controls id=\"video\" src=\""+u8+"\" autoplay=\"autoplay\" />";
            } else {
                String img = BIGIMAGE + path.substring(path.indexOf("?img=") + 5) + NUMSEP + pathHash + ".jpg";
                body = body + "- Img" + headline +
                        "<img src=\"" + img + "\" style=\"width: 100%;\" />";
            }
        }
        sendHtmlOverHttp(body, out);
    }

    private byte[] loadTextFile(String file) throws Exception {
        Path p = Paths.get(file);
        return Files.readAllBytes(p);
    }

    private String formatTextFile(String path) {
        String cnt;
        byte[] b = new byte[0];
        try {
            b = loadTextFile(path);
            cnt = new String(b, StandardCharsets.UTF_8);
        } catch (Exception e) {
            cnt = e.toString();
        }
        return "<pre>\r\n" + cnt + "</pre>";
    }

    /**
     * webserver main function
     *
     * @param imagePath    point to image stuff on disk
     * @param http          http object
     * @param outputSocket socket for TX
     * @throws Exception if smth gone wrong
     */
    void handleRequest (String imagePath, Http http, NIOSocket outputSocket) throws Exception {
        String resource = UrlEncodeUTF8.retransform(http.getRequestedResource());
        if (isImage(resource)) {
            resource = resource.substring(0, resource.lastIndexOf('.'));
            if (resource.startsWith(BIGIMAGE)) {
                resource = resource.substring(0, resource.indexOf(NUMSEP));
                int idx = Integer.parseInt(resource.substring(5));
                sendJpegOriginal(outputSocket, fileList.get(idx));
            } else {
                resource = resource.substring(resource.indexOf(NUMSEP) + 1);
                int idx = Integer.parseInt(resource);
                sendJpegSmall(outputSocket, fileList.get(idx));
            }
        } else if (isZip(resource)) {
            sendZip(outputSocket, resource);
        } else if (isVideo(resource)) {
            sendMedia(outputSocket, resource);
        } else if (isAudio(resource)) {
            sendMedia(outputSocket, resource);
        } else if (isText(resource)) {
            String s = "HTTP/1.1 200 OK\r\n\r\n <html>" + formatTextFile(resource) + "</html>";
            outputSocket.write(s.getBytes(StandardCharsets.UTF_8));
        } else if (resource.equals("favicon.ico")) {
            byte[] bt = Tools.gatResourceAsArray(resource);
            outputSocket.write(bt);
        } else if (resource.equals("backarrow.ico")) {
            byte[] bt = Tools.gatResourceAsArray(resource);
            outputSocket.write(bt);
        } else if (resource.equals("fwdarrow.ico")) {
            byte[] bt = Tools.gatResourceAsArray(resource);
            outputSocket.write(bt);
        } else if (resource.startsWith("show.html")) {
            sendMediaPage(outputSocket, resource);
        } else {
            if (resource.isEmpty()) {
                resource = imagePath;
            }
            sendGalleryPage(outputSocket, resource);
        }
    }
}
