package inetserver.nagaweb;


import misc.Tools;
import naga.NIOSocket;
import transform.Transformation;
import transform.UrlEncodeUTF8;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

//import org.jetbrains.annotations.NotNull;

/**
 * @author Administrator
 */
class NIOWebServerClient {
    private final UrlEncodeUTF8 m_urltransform = new UrlEncodeUTF8();


    /**
     * Is File MP4
     *
     * @param in file name
     * @return TRUE if file is named .mp4
     */
    private boolean isMP4(String in) {
        in = in.toLowerCase();
        return in.endsWith(".mp4") ||
                in.endsWith(".mkv") ||
                in.endsWith(".webm") ||
                in.endsWith(".ogv") ||
                in.endsWith(".3gp");
    }

    private boolean isMP3(String in) {
        in = in.toLowerCase();
        return in.endsWith(".mp3") ||
                in.endsWith(".ogg");
    }

    /**
     * Is File Jpeg?
     *
     * @param in file name
     * @return TRUE if file is jpeg
     */
    private boolean isImage(String in) {
        in = in.toLowerCase();
        return in.endsWith(".jpg") ||
                in.endsWith(".jpeg") ||
                in.endsWith(".png") ||
                in.endsWith(".bmp");
    }

    /**
     * Is File Zip?
     *
     * @param in file name
     * @return TRUE if file is zip
     */
    private boolean isZip(String in) {
        in = in.toLowerCase();
        return in.endsWith(".zip") || in.endsWith(".rar");
    }

    /**
     * Is Text file
     *
     * @param in file name
     * @return TRUE if file is text file
     */
    private boolean isText(String in) {
        in = in.toLowerCase();
        return in.endsWith(".txt") || in.endsWith(".c")
                || in.endsWith(".cpp")
                || in.endsWith(".h") || in.endsWith(".cxx")
                || in.endsWith(".hxx") || in.endsWith(".java");
    }

    private void appendLink(ArrayList<Path> list, StringBuilder sb) {
        for (Path p : list) {
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
    private String buildMainPage(String path) {
        File f = new File(path);
        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        File[] fils = f.listFiles();

        if (fils == null) {
            return null;
        }

        ArrayList<Path> dirs = new ArrayList<>();
        ArrayList<Path> txtfiles = new ArrayList<>();
        ArrayList<Path> otherfiles = new ArrayList<>();

        Path pp = Paths.get(path).getParent();
        if (pp != null) {
            String u8 = m_urltransform.transform(pp.toString());
            sb2.append("<a href=\"").append(u8).append("\">");
            sb2.append("*BACK*").append("</a>").append("<hr>\r\n");
        }
        int imageCtr = 0;
        int vidCtr = 0;

        for (File fil : fils) {
            String name = fil.getName();
            Path p = Paths.get(path, name);
            String u8 = m_urltransform.transform(p.toString());
            if (fil.isDirectory()) {
                dirs.add(p);
            } else if (isImage(name)) {
                imageCtr++;
                sb.append("<a href=\"");
                sb.append("*IMG*");
                sb.append(u8);
                sb.append("\" target=\"_blank\"><img src=\"");
                sb.append(u8);
                sb.append("\" title=\"").append(p.getFileName().toString()).append("\"");
                sb.append("></a>\r\n");
            } else if (isMP4(name)) {
                vidCtr++;
                sb.append("<video width=\"320\" height=\"240\" controls src=\"");
                sb.append(u8).append("\">");
                sb.append("Your user agent does not support the HTML5 Video element.</video>");
                sb.append("\r\n");
            } else if (isMP3(name)) {
                sb.append("<audio controls src=\"");
                sb.append(u8).append("\">");
                sb.append("Your user agent does not support the HTML5 Video element.</audio>");
                sb.append("\r\n");
            }
            else if (isText(name) || isZip(name)) {
                txtfiles.add(p);
            } else
                otherfiles.add(p);
        }
        appendLink(dirs, sb2);
        appendLink(txtfiles, sb2);
        appendLink(otherfiles, sb2);
        sb2.append("<hr>");
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
     *  @param w        Socket writer
     * @param len      File length
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
                "Cache-Control: max-age=31536000, public\n" +
                "Connection: close\n" +
                "\n";
        out.write(b.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Send JPEG to output stream
     *
     * @param out output stream
     */
    private void sendJpegSmall(NIOSocket out, String path) throws Exception {
        File f = new File(path);
        byte[] b = Tools.reduceImg(f);
        imgHead(out, b.length);
        out.write(b);
    }

    private void sendJpegOriginal(NIOSocket out, String fname) throws IOException {
        File f = new File(fname);
        byte[] b = Files.readAllBytes(f.toPath());
        imgHead(out, (int) f.length());
        out.write(b);
    }

    // not tested
    private void sendMedia(NIOSocket out, String fname) throws Exception {
        System.out.println("Sending media: " + fname);
        File f = new File(fname);
        byte[] b = Files.readAllBytes(f.toPath());
        InputStream input = new FileInputStream(f);
        mp4Head(out, f.length(), fname);
        out.write(b);
    }

    // not tested
    private void sendMP3(NIOSocket out, String fname) throws Exception {
        System.out.println("Sending MP4: " + fname);
        File f = new File(fname);
        byte[] b = Files.readAllBytes(f.toPath());
        InputStream input = new FileInputStream(f);
        mp4Head(out, f.length(), fname);
        out.write(b);
    }

    private void sendZip(NIOSocket out, String fname) throws IOException {
        File f = new File(fname);
        byte[] b = Files.readAllBytes(f.toPath());
        mp4Head(out, f.length(), fname);
        out.write(b);
    }

    /**
     * send image page
     *
     * @param out Print Writer
     */
    private void sendImagePage (NIOSocket out, String path) throws Exception {
        String mainPage = buildMainPage(path);
        if (mainPage == null) {
            mainPage = formatTextFile(path);
        }
        String txt = "HTTP/1.1 200 OK\r\n\r\n <!DOCTYPE html><html lang=\"en\"><head><meta charset=\"utf-8\"/></head>\r\n"
                + mainPage
                + "\r\n</html>";
        byte[] bt = txt.getBytes(Transformation.utf8);
        out.write(bt);
    }

    private byte[] loadTextFile(String file) throws IOException {
        Path p = Paths.get(file);
        return Files.readAllBytes(p);
    }

    private String formatTextFile(String path) throws IOException {
        byte[] b = loadTextFile(path);
        String cnt = new String(b, StandardCharsets.UTF_8);
        return "<pre>\r\n" + cnt + "</pre>";
    }

    void perform(String imagePath, String cmd, NIOSocket outputSocket) throws Exception {
        String[] si = cmd.split(" ");
        String path = m_urltransform.retransform(si[0].substring(1));

        if (isImage(path)) {
            if (path.startsWith("*IMG*")) {
                sendJpegOriginal(outputSocket, path.substring(5));
            } else {
                sendJpegSmall(outputSocket, path);
            }
        } else if (isZip(path)) {
            sendZip(outputSocket, path);
        } else if (isMP4(path)) {
            sendMedia(outputSocket, path);
        } else if (isMP3(path)) {
            sendMedia(outputSocket, path);
        }
        else if (isText(path)) {
            String s = "HTTP/1.1 200 OK\r\n\r\n <html>" + formatTextFile(path)+"</html>";
            outputSocket.write (s.getBytes(StandardCharsets.UTF_8));
        }
        else if (path.equals("favicon.ico")) {
            byte[] bt = Tools.gatResourceAsArray(path);
            outputSocket.write(bt);
        }
        else {
            if (path.isEmpty()) {
                path = imagePath;
            }
            sendImagePage(outputSocket, path);
        }
    }
}
