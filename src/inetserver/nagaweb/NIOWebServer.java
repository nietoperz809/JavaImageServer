/*
Copyright (c) 2008-2011 Christoffer Lernö

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package inetserver.nagaweb;

import misc.Dbg;
import misc.Http;
import naga.*;

import java.io.IOException;

/**
 * Creates a very simple echo server.
 * <p>
 * Run using {@code java naga.examples.EchoServer [port]}
 *
 * @author Christoffer Lerno
 */
public class NIOWebServer {
    private final int port;
    private final String basePath;
    private NIOService service;

    public NIOWebServer(int port, String basePath) {
        this.port = port;
        this.basePath = basePath;
    }

    public void halt() {
        service.close();
    }

    private void doListen (NIOServerSocket socket)
    {
        NIOWebServerClient client = new NIOWebServerClient(this.basePath);

        socket.listen(new ServerSocketObserverAdapter() {
            public void newConnection (NIOSocket nioSocket) {
                Dbg.print("Client " + nioSocket.getIp() + " connected.");
                nioSocket.listen(new SocketObserverAdapter() {
                    public void packetReceived (NIOSocket socket, byte[] packet) {
                        Http http = new Http(packet);
                        try {
                            client.handleRequest(basePath, http, socket);
                        } catch (Exception e) {
                            Dbg.print("WS client fail: " + e);
                        }
                        socket.closeAfterWrite();
                    }

                    public void connectionBroken (NIOSocket nioSocket, Exception exception) {
                        Dbg.print("Client " + nioSocket.getIp() + " disconnected.");
                    }
                });
            }
        });
    }

    public void runServer() {
        try {
            service = new NIOService();
            NIOServerSocket socket = service.openServerSocket (port, 100);
            NIOWebServerClient client = new NIOWebServerClient(this.basePath);
            doListen(socket);
            socket.setConnectionAcceptor(ConnectionAcceptor.ALLOW);
            while (true) {
                if (!service.isOpen()) {
                    return;
                }
                service.selectBlocking();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
