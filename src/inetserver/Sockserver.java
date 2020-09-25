/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inetserver;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * @author Administrator
 */
public class Sockserver implements Runnable
{
    private final int port;
    private HttpServer server;
    private final String basePath;

    public Sockserver (int p, String bp)
    {
        basePath = bp;
        port = p;
        //new Thread(this).start();
        run ();
    }

    public void halt ()
    {
        server.stop (5);
    }

    @Override
    public void run ()
    {
        server = null;
        try
        {
            server = HttpServer.create (new InetSocketAddress (port), 10);
            server.setExecutor (Executors.newFixedThreadPool (20)); // multiple Threads

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
                cl.perform (basePath, e.getRequestURI ().toString (), os);
            }
            catch (Exception e1)
            {
                System.out.println ("oops");
                System.out.println (e1);
            }
            os.close ();
        };

        //for(int s=0; s<100; s++)
        server.createContext ("/", hnd);

        server.start ();
    }
}
