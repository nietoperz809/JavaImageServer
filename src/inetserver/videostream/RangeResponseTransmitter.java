package inetserver.videostream;

import misc.Http;
import misc.MimeNames;
import misc.Tools;
import naga.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class RangeResponseTransmitter {
    final int MAX_CHUNKSIZE = 0x100_000;
    private File m_video;
    public static RangeResponseTransmitter instance;

    public RangeResponseTransmitter()
    {
        instance = this;
    }

    public void setVideo (String name){
        m_video = new File (name);
    }

    private void handeRequest (NIOSocket socket, Http http) throws IOException {
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
            end = MAX_CHUNKSIZE;
        }
        else {
            RangeCalculator r = new RangeCalculator(range, (int)m_video.length());
            start = r.start;
            end = Math.min (start + MAX_CHUNKSIZE, r.end);
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

    public void startServer (int port) {
        try {
            NIOService serv2 = new NIOService();
            NIOServerSocket socket = serv2.openServerSocket (port, 100);

            socket.listen(new ServerSocketObserverAdapter() {
                public void newConnection (NIOSocket nioSocket) {
                    nioSocket.listen(new SocketObserverAdapter() {
                        public void packetReceived (NIOSocket socket, byte[] packet) {
                            Http http = new Http(packet);
                            try {
                                handeRequest(socket, http);
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
