package inetserver.videostream;

import misc.Http;
import misc.MimeNames;
import misc.Tools;
import naga.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ChunkWiseTransmitter {
    private int m_chunksize = 0x100_000;
    private int m_port = 8899; // default
    private File m_video;
    private static ChunkWiseTransmitter instance;

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
    }

    public static ChunkWiseTransmitter getInstance()
    {
        if (instance == null)
            instance = new ChunkWiseTransmitter();
        return instance;
    }

    private ChunkWiseTransmitter()
    {
    }

    private void handleRequest(NIOSocket socket, Http http) {
        FileInputStream fileStream;
        try {
            fileStream = new FileInputStream(m_video);
        } catch (FileNotFoundException e) {
            System.out.println("Cant open:"+e);
            return;
        }
        int start, end;
        String range = http.getHeaderValue("range");
        if (range == null) {
            start = 0;
            end = m_chunksize;
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
            System.out.println("file read fail: "+e);
        }
        socket.write(data); // Send it out!
        System.out.println("TX: "+data.length);
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
                                System.out.println("RRT failed: "+e);
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
