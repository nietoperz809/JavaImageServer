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
    private byte buffer[] = new byte[0x20000];

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
     * Does the transmission
     *
     * @throws IOException
     */
    public void doTransmission() throws IOException
    {
        for (;;)
        {
            int r = _in.read(buffer);
            if (r == -1)
            {
                //_out.flush();
                break;
            }
            _out.write(buffer, 0, r);
            Thread.yield();
        }
    }
}
