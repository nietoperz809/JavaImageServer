package inetserver.nagaweb.videostream;

import misc.Dbg;
import misc.Http;
import misc.MimeNames;
import misc.Tools;
import naga.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Http206Transmitter {
    private int m_chunksize = 0x100_000;
    private int m_port = 8899; // default
    private File m_video;
    private static Http206Transmitter instance;

    public void setChunkSize (int n)
    {
        m_chunksize = 0x100_000 * n;
    }

    public void setPort (int port)
    {
        m_port = port;
    }

    public int getPort()
    {
        return m_port;
    }

    public void setVideo (String name){
        m_video = new File (name);
        Dbg.print("Vid to play: "+name);
    }

    public static Http206Transmitter getInstance() {
        if (instance == null)
            instance = new Http206Transmitter();
        return instance;
    }

    private Http206Transmitter() {
    }

    private void handleRequest(NIOSocket socket, Http http) {
        FileInputStream fileStream;
        try {
            fileStream = new FileInputStream(m_video);
        } catch (FileNotFoundException e) {
            Dbg.print("Cant open:"+e);
            return;
        }
        int start, end;
        String range = http.getHeaderValue("range");
        if (range == null) {
            start = 0;
            end = (int) Math.min (m_chunksize, m_video.length());
        }
        else {
            RangeCalculator r = new RangeCalculator(range, (int)m_video.length());
            start = r.start;
            end = Math.min (start + m_chunksize, r.end);
        }
        int contentLen = ((start == end) ? 0 : (end - start + 1));
        String contentRange = ""+start + "-" + end + "/" + m_video.length();
        socket.println("HTTP/1.1 206 Partial Content");
        socket.println("Content-Range: bytes " + contentRange);
        socket.println("Content-Length: " + contentLen);
        socket.println("Content-Type: " + MimeNames.getMime(Tools.getExtension(m_video.getName())));
        socket.println("Accept-Ranges: bytes");
        socket.println("Cache-Control: no-cache");
        socket.println("");
        byte[] data = new byte[contentLen];
        try {
            fileStream.skip(start);
            fileStream.read(data);
            fileStream.close();
        } catch (IOException e) {
            Dbg.print("file read fail: "+e);
        }
        socket.write(data); // Send it out!
        Dbg.print("TX: "+data.length);
    }

    public void startServer() {
        try {
            NIOService serv2 = new NIOService();
            NIOServerSocket socket = serv2.openServerSocket (m_port, 100);

            socket.listen(new ServerSocketObserverAdapter() {
                public void newConnection (NIOSocket nioSocket) {
                    nioSocket.listen(new SocketObserverAdapter() {
                        public void packetReceived (NIOSocket socket, byte[] packet) {
                            Http http = new Http(packet);
                            try {
                                handleRequest(socket, http);
                            } catch (Exception e) {
                                Dbg.print("RRT failed: "+e);
                            }
                            socket.closeAfterWrite();
                        }

                        public void connectionBroken (NIOSocket nioSocket, Exception exception) {
                        }
                    });
                }
            });

            socket.setConnectionAcceptor(ConnectionAcceptor.ALLOW);
            while (true) {
                if (!serv2.isOpen()) {
                    return;
                }
                serv2.selectBlocking();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
