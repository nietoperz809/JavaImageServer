package misc.gifdecoder;

import java.io.InputStream;

class LZWTreeDecoder {

    // Variables for code reading
    private int bits_remain = 0;
    private int bytes_available = 0;
    private int temp_byte = 0;
    private int bufIndex = 0;
    private final byte[] bytes_buf = new byte[256];

    private int oldcode = 0;
    private final int[] prefix = new int[4097];
    private final int[] suffix = new int[4097];

    private final int min_code_size;
    private final int clearCode;
    // End of image for GIF or end of information for TIFF
    private final int endOfImage;

    // Variables to clear table
    private int codeLen;
    private int codeIndex;
    private int limit;

    private final int first_code_index;
    private int first_char;

    private final InputStream is;

    private static final int[] MASK = {0x00, 0x001, 0x003, 0x007, 0x00f, 0x01f, 0x03f, 0x07f, 0x0ff, 0x1ff, 0x3ff, 0x7ff, 0xfff};

    private int leftOver = 0;// Used to keep track of the not fully expanded code string.
    private final int[] buf = new int[4097];

    private static final int MAX_CODE = (1 << 12);

    /**
     * There are some subtle differences between the LZW algorithm used by TIFF and GIF images.
     * <p>
     * Variable Length Codes:
     * Both TIFF and GIF use a variation of the LZW algorithm that uses variable length codes.
     * In both cases, the maximum code size is 12 bits. The initial code size, however, is different
     * between the two formats. TIFF's initial code size is always 9 bits. GIF's initial code size
     * is specified on a per-file basis at the beginning of the image descriptor block,
     * with a minimum of 3 bits.
     * <p>
     * TIFF and GIF each switch to the next code size using slightly different algorithms.
     * GIF increments the code size as soon as the LZW string table's length is equal to 2**code-size,
     * while TIFF increments the code size when the table's length is equal to 2**code-size - 1.
     * <p>
     * Packing Bits into Bytes
     * TIFF and GIF LZW algorithms differ in how they pack the code bits into the byte stream.
     * The least significant bit in a TIFF code is stored in the most significant bit of the bytestream,
     * while the least significant bit in a GIF code is stored in the least significant bit of the bytestream.
     * <p>
     * Special Codes
     * TIFF and GIF both add the concept of a 'Clear Code' and a 'End of Information Code' to the LZW algorithm.
     * In both cases, the 'Clear Code' is equal to 2**(code-size - 1) and the 'End of Information Code' is equal
     * to the Clear Code + 1. These 2 codes are reserved in the string table. So in both cases, the LZW string
     * table is initialized to have a length equal to the End of Information Code + 1.
     */
    public LZWTreeDecoder(InputStream is, int min_code_size) {
        if (min_code_size < 2 || min_code_size > 12)
            throw new IllegalArgumentException("invalid min_code_size: " + min_code_size);
        this.is = is;
        this.min_code_size = min_code_size;
        clearCode = (1 << min_code_size);
        endOfImage = clearCode + 1;
        first_code_index = endOfImage + 1;
        // Reset string table
        clearStringTable();
    }

    private void clearStringTable() {
        // Reset string table
        codeLen = min_code_size + 1;
        limit = (1 << codeLen) - 1;
        codeIndex = endOfImage;
    }

    public void decode(byte[] pix, int offset, int len) throws Exception {
        int counter = 0;// Keep track of how many bytes have been decoded.
        ///////////////
        int tempcode;
        int i;
        //////////////////////////////////////////////////////////
        if (leftOver > 0) {//flush out left over first.
            for (int j = leftOver - 1; j >= 0; j--, leftOver--) {
                if ((offset >= pix.length) || (counter >= len))// Will this ever happen?!
                    return;
                pix[offset++] = (byte) buf[j];
                counter++;
            }
        }
        //////////////////////////////////////////////////////////
        label:
        do {
            i = 0;
            int code = readLZWCode();
            tempcode = code;

            if (code == clearCode) {
                clearStringTable();
            } else if (code == endOfImage) {
                break;
            } else {
                if (code >= codeIndex) {
                    tempcode = oldcode;
                    buf[i++] = first_char;
                }
                while (tempcode >= first_code_index) {
                    buf[i++] = suffix[tempcode];
                    tempcode = prefix[tempcode];
                }
                buf[i++] = tempcode;

                suffix[codeIndex] = first_char = tempcode;
                prefix[codeIndex] = oldcode;
                // Check boundary to deal with deferred clear code in LZW compression
                if (codeIndex < MAX_CODE) codeIndex++;

                oldcode = code;

                if ((codeIndex > limit) && (codeLen < 12)) {
                    codeLen++;
                    limit = (1 << codeLen) - 1;
                }
                // Output strings for the current code
                leftOver = i;
                for (int j = i - 1; j >= 0; j--, leftOver--, counter++) {
                    if ((offset >= pix.length) || (counter >= len))
                        break label;
                    pix[offset++] = (byte) buf[j];
                }
            }
        } while (true);

    }

    private int readLZWCode() throws Exception {
        int temp;
        temp = (temp_byte >> (8 - bits_remain));

        while (codeLen > bits_remain) {
            if (bytes_available == 0) {
                // find another data block available
                // Start a new image data sub-block if possible!
                // The block size bytes_available is no bigger than 0xff
                bytes_available = is.read();

                if (bytes_available > 0) {
                    IOUtils.readFully(is, bytes_buf, 0, bytes_available);
                    bufIndex = 0;
                } else if (bytes_available == 0)
                    return endOfImage;
                else {
                    return endOfImage;
                }
            }
            temp_byte = bytes_buf[bufIndex++] & 0xff;
            bytes_available--;
            temp |= (temp_byte << bits_remain);
            bits_remain += 8;
        }

        bits_remain -= codeLen;

        return (temp & MASK[codeLen]);
    }
}
