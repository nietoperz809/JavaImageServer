package misc.gifdecoder;

import java.io.InputStream;

class GifHeader {
    final byte[] signature = new byte[3];
    final byte[] version = new byte[3];

    int screen_width;
    int screen_height;
    byte flags;
    byte bgcolor;

    void readHeader(InputStream is) throws Exception {
        int nindex = 0;
        byte[] bhdr = new byte[13];

        IOUtils.readFully(is, bhdr, 0, 13);

        for (int i = 0; i < 3; i++)
            signature[i] = bhdr[nindex++];

        for (int i = 0; i < 3; i++)
            version[i] = bhdr[nindex++];

        screen_width = ((bhdr[nindex++] & 0xff) | ((bhdr[nindex++] & 0xff) << 8));
        screen_height = ((bhdr[nindex++] & 0xff) | ((bhdr[nindex++] & 0xff) << 8));
        flags = bhdr[nindex++];
        bgcolor = bhdr[nindex++];
        byte aspectRatio = bhdr[nindex++];
        // The end
    }
}
