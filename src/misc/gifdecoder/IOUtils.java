package misc.gifdecoder;

import java.io.IOException;
import java.io.InputStream;

class IOUtils {

    public static void readFully(InputStream is, byte[] b) throws IOException {
        readFully(is, b, 0, b.length);
    }

    public static void readFully(InputStream is, byte[] b, int off, int len) throws IOException {
        if (len < 0)
            throw new IndexOutOfBoundsException();
        int n = 0;
        while (n < len) {
            int count = is.read(b, off + n, len - n);
            if (count < 0)
                return; //throw new EOFException();
            n += count;
        }
    }

    public static int readUnsignedShort(InputStream is) throws IOException {
        byte[] buf = new byte[2];
        readFully(is, buf);

        return ((buf[1] & 0xff) << 8) | (buf[0] & 0xff);
    }

    public static void skipFully(InputStream is, int n) throws IOException {
        readFully(is, new byte[n]);
    }

    private IOUtils() {
    }
}
