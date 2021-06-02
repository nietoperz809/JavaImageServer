package inetserver.videostream;

import misc.Http;
import misc.MimeNames;
import misc.Tools;
import naga.NIOSocket;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RangeResponseTransmitter {
    static final int CHUNKSIZE = 0x100_000;

    static class Range {
        public int start;
        public int end;

        public Range (String in, int filesize)
        {
            in = in.substring(in.indexOf("=") + 1);
            String[] parts = in.split("-");
            int st, en;
            try {
                st = Integer.parseInt(parts[0]);
            } catch (Exception e) {
                st = -1;
            }
            try {
                en = Integer.parseInt(parts[1]);
            } catch (Exception e) {
                en = -1;
            }
            start = st == -1 ? 0 : st;
            end = en == -1 ? filesize-1 : en;
            if (st != -1 && en == -1) {
                start = st;
                end = filesize - 1;
            }
            if (st == -1 && en != -1){
                start = filesize -en;
                end = filesize - 1;
            }

        }

        @Override
        public String toString() {
            return "Range{" +
                    "start=" + start +
                    ", end=" + end +
                    '}';
        }
    }

    public static boolean doIt (NIOSocket socket, Http http) throws IOException {
        FileInputStream raf;
        String filePath = "C:\\input.mp4";
        File f = new File (filePath);
        try {
            raf = new FileInputStream(f);
        } catch (FileNotFoundException e) {
            System.out.println("Cant open:"+e);
            return false;
        }
        int start, end;
        String range = http.getValue("range");
        if (range == null) {
            start = 0;
            end = CHUNKSIZE;
        }
        else {
            Range r = new Range (range, (int)f.length());
            start = r.start;
            end = Math.min (start + CHUNKSIZE, r.end);
        }
        int ctlen = ((start == end) ? 0 : (end - start + 1));
        String ctrange = ""+start + "-" + end + "/" + f.length();
        socket.println("HTTP/1.1 206 OK");
        socket.println("Content-Range: bytes " + start + "-" + end + "/" + f.length());
        socket.println("Content-Length: " + ((start == end) ? 0 : (end - start + 1)));
        socket.println("Content-Type: " + MimeNames.getMime(Tools.getExtension(filePath)));
        socket.println("Accept-Ranges: bytes");
        socket.println("Cache-Control: no-cache");
        socket.println("");
//        StringBuffer sb = new StringBuffer();
//        sb.append("HTTP/1.1 206 Partial Content").append("\r\n");
//        sb.append("Content-Range: ").append(ctrange).append("\r\n");
//        sb.append("Content-Length: ").append(ctlen).append("\r\n");
//        sb.append("Content-Type: ").append(MimeNames.getMime(Tools.getExtension(filePath))).append("\r\n");
//        sb.append("Accept-Ranges: bytes").append("\r\n");
//        sb.append("Cache-Control: no-cache").append("\r\n");
//        sb.append("Connection: keep-alive").append("\r\n");
//        sb.append("\r\n");
        byte[] data = new byte[ctlen];
        try {
            raf.skip(start);
            raf.read(data);
            raf.close();
        } catch (IOException e) {
            System.out.println("file read fail: "+e);
        }
        //socket.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        socket.write(data);
        System.out.println("TX: "+data.length);
        return true;
    }
}

/*

    public static boolean doIt (NIOSocket socket, Http http) throws IOException {
        FileInputStream raf;
        String filePath = "C:\\input.mp4";
        File f = new File (filePath);
        try {
            raf = new FileInputStream(f);
        } catch (FileNotFoundException e) {
            System.out.println("Cant open:"+e);
            return false;
        }
        int start, end;
        String range = http.getValue("range");
        if (range == null) {
            start = 0;
            end = CHUNKSIZE;
        }
        else {
            Range r = new Range (range, (int)f.length());
            start = r.start;
            end = Math.min (start + CHUNKSIZE, r.end);
        }
        int ctlen = ((start == end) ? 0 : (end - start + 1));
        String ctrange = ""+start + "-" + end + "/" + f.length();
        StringBuffer sb = new StringBuffer();
        sb.append("HTTP/1.1 206 Partial Content").append("\r\n");
        sb.append("Content-Range: ").append(ctrange).append("\r\n");
        sb.append("Content-Length: ").append(ctlen).append("\r\n");
        sb.append("Content-Type: ").append(MimeNames.getMime(Tools.getExtension(filePath))).append("\r\n");
        sb.append("Accept-Ranges: bytes").append("\r\n");
        sb.append("Cache-Control: no-cache").append("\r\n");
        sb.append("Connection: keep-alive").append("\r\n");
        sb.append("\r\n");
        byte[] data = new byte[ctlen];
        try {
            raf.skip(start);
            raf.read(data);
            raf.close();
        } catch (IOException e) {
            System.out.println("file read fail: "+e);
        }
        socket.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        socket.write(data);
        return true;
    }

 */
