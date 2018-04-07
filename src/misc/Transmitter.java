/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package misc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Administrator
 */
public class Transmitter
{
    private final InputStream _in;
    private final OutputStream _out;
    private int _blocksize = 0x20000; // 0x10000;
    //private final static AtomicLong counter = new AtomicLong();

//    public static String getCounter()
//    {
//        return "  " + Tools.humanReadableByteCount(counter.longValue());
//    }

    /**
     * Constructor
     *
     * @param i Source
     * @param o Sink
     */
    public Transmitter (InputStream i, OutputStream o)
    {
        _in = i;
        _out = o;
    }

    public Transmitter (byte[] ba, OutputStream o)
    {
        _in = new ByteArrayInputStream(ba);
        _out = o;
    }

    /**
     * Constructor with Block Size
     *
     * @param i Source
     * @param o Sink
     * @param bl Block Size
     */
    public Transmitter (InputStream i, OutputStream o, int bl)
    {
        _blocksize = bl;
        _in = i;
        _out = o;
    }

    /**
     * Constructor using sys.in as source
     *
     * @param o Sink
     */
    public Transmitter (OutputStream o)
    {
        _in = System.in;
        _out = o;
    }

    /**
     * Constructor using sys.out as sink
     *
     * @param i Source
     */
    public Transmitter (InputStream i)
    {
        _in = i;
        _out = System.out;
    }

    /**
     * Does the transmission
     *
     * @throws IOException
     */
    public long doTransmission() throws IOException
    {
        byte b[] = new byte[_blocksize];
        long txTime = System.currentTimeMillis();
        for (;;)
        {
            int r = _in.read(b);
            if (r == -1)
            {
                _out.flush();
                break;
            }
            _out.write(b, 0, r);
            //counter.getAndAdd(r);
        }
        return System.currentTimeMillis() - txTime;
    }
}
