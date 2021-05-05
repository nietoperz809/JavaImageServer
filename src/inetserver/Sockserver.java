/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inetserver;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * @author Administrator
 */
public class Sockserver
{
    private final int port;
    private HttpServer server;
    private final String basePath;

    public Sockserver (int p, String bp)
    {
        basePath = bp;
        port = p;
        server = null;
        try
        {
            server = HttpServer.create (new InetSocketAddress (port), 100);
            server.setExecutor (Executors.newFixedThreadPool (100));

        } catch (IOException e)
        {
            return;
        }

        HttpHandler hnd = e ->
        {
            WebServerClient cl = new WebServerClient ();
            e.sendResponseHeaders (200, 0);
            OutputStream os = e.getResponseBody ();
            try
            {
                //System.out.println("+"+Thread.currentThread().getId());
                cl.perform (basePath, e.getRequestURI ().toString (), os);
                //System.out.println("-"+Thread.currentThread().getId());
            }
            catch (Exception e1)
            {
                System.out.println ("oops"+e1);
            }
            os.flush();
            os.close ();
        };

        server.createContext ("/", hnd);
        server.start ();
    }

    public void halt ()
    {
        server.stop (5);
    }
}
