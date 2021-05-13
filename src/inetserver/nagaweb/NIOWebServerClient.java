package inetserver.nagaweb;


import misc.Tools;
import naga.NIOSocket;
import transform.UrlEncodeUTF8;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * @author Administrator
 */
class NIOWebServerClient {

    File[] fileList;
    static final String BIGIMAGE = "*IMG*";
    static final String NUMSEP = "@";

    boolean hasExtension(String in, String... ext) {
        in = in.toLowerCase();
        for (String s : ext) {
            s = s.toLowerCase();
            if (in.endsWith(s))
                return true;
        }
        return false;
    }

    private boolean isVideo(String in) {
        return hasExtension(in, ".mp4", ".mkv", ".webm", ".ogv", ".3gp");
    }

    private boolean isAudio(String in) {
        return hasExtension(in, ".mp3", ".ogg", ".wav");
    }

    private boolean isImage(String in) {
        return hasExtension(in, ".jpg", ".jpeg", ".png", ".bmp", "gif");
    }

    private boolean isZip(String in) {
        return hasExtension(in, ".zip");
    }

    private boolean isText(String in) {
        return hasExtension(in, ".txt", ".cpp", ".c", ".h", ".java", ".cxx", ".hxx");
    }

    private void appendLink(ArrayList<Path> list, StringBuilder sb) {
        for (Path p : list) {
            String u8 = UrlEncodeUTF8.transform(p.toString());
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
        int hc = path.hashCode();
        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        fileList = new File(path).listFiles();
        if (fileList == null) {
            return null;
        }
        Arrays.sort(fileList, Comparator.comparingLong(File::lastModified)); // Sort by date

        ArrayList<Path> dirs = new ArrayList<>();
        ArrayList<Path> txtfiles = new ArrayList<>();
        ArrayList<Path> otherfiles = new ArrayList<>();

        Path pp = Paths.get(path).getParent();
        if (pp != null) {
            String u8 = UrlEncodeUTF8.transform(pp.toString());
            sb2.append("<a href=\"").append(u8).append("\">");
            sb2.append("*BACK*").append("</a>").append("<hr>\r\n");
        }
        int imageCtr = 0;
        int vidCtr = 0;

        for (int idx = 0; idx < fileList.length; idx++) {
            File fil = fileList[idx];
            String name = fil.getName();
            Path p = Paths.get(path, name);
            String u8 = UrlEncodeUTF8.transform(p.toString());
            if (fil.isDirectory()) {
                dirs.add(p);
            } else if (isImage(name)) {
                imageCtr++;
                sb.append("<a href=\"");
                sb.append(BIGIMAGE).append(idx).append(".jpg");
                sb.append("\" target=\"_blank\"><img src=\"");
                sb.append(hc).append(NUMSEP).append(idx).append(".jpg");
                sb.append("\" title=\"").append(p.getFileName().toString()).append("\"");
                sb.append("></a>\r\n");
            } else if (isVideo(name)) {
                vidCtr++;
                sb.append("<video width=\"320\" height=\"240\" controls src=\"");
                sb.append(u8).append("\">");
                sb.append("HTML5 Video not supported</video>");
                sb.append("\r\n");
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

    private void sendMedia(NIOSocket out, String fname) throws Exception {
        System.out.println("Sending media: " + fname);
        File f = new File(fname);
        byte[] b = Files.readAllBytes(f.toPath());
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
    private void sendImagePage(NIOSocket out, String path) throws Exception {
        String mainPage = buildMainPage(path);
        if (mainPage == null) {
            mainPage = formatTextFile(path);
        }
        String txt = "HTTP/1.1 200 OK\r\n\r\n <!DOCTYPE html><html lang=\"en\"><head><meta charset=\"utf-8\"/></head>\r\n"
                + mainPage
                + "\r\n</html>";
        byte[] bt = txt.getBytes(UrlEncodeUTF8.utf8);
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
        String path = UrlEncodeUTF8.retransform(si[0].substring(1));

        if (isImage(path)) {
            path = path.substring(0, path.lastIndexOf('.'));
            if (path.startsWith(BIGIMAGE)) {
                int idx = Integer.parseInt(path.substring(5));
                sendJpegOriginal(outputSocket, fileList[idx].getAbsolutePath());
            } else {
                path = path.substring(path.indexOf(NUMSEP)+1);
                int idx = Integer.parseInt(path);
                sendJpegSmall(outputSocket, fileList[idx].getAbsolutePath());
            }
        } else if (isZip(path)) {
            sendZip(outputSocket, path);
        } else if (isVideo(path)) {
            sendMedia(outputSocket, path);
        } else if (isAudio(path)) {
            sendMedia(outputSocket, path);
        } else if (isText(path)) {
            String s = "HTTP/1.1 200 OK\r\n\r\n <html>" + formatTextFile(path) + "</html>";
            outputSocket.write(s.getBytes(StandardCharsets.UTF_8));
        } else if (path.equals("favicon.ico")) {
            byte[] bt = Tools.gatResourceAsArray(path);
            outputSocket.write(bt);
        } else {
            if (path.isEmpty()) {
                path = imagePath;
            }
            sendImagePage(outputSocket, path);
        }
    }
}
