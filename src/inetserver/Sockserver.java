/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inetserver;

import applications.WebServerGUI;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 *
 * @author Administrator
 */
public class Sockserver implements Runnable
{
    private final int port;
    private HttpServer server;
    private final WebServerGUI _gui;

    public Sockserver (int p, WebServerGUI gui)
    {
        port = p;
        _gui = gui;
       new Thread(this).start();
    }

    public void halt()
    {
        server.stop(5);
    }

    @Override
    public void run()
    {
        server = null;
        try
        {
            server = HttpServer.create(new InetSocketAddress(port),1000);
            server.setExecutor(Executors.newCachedThreadPool()); // multiple Threads

        }
        catch (IOException e)
        {
            return;
        }

        HttpHandler hnd = e ->
        {
            WebServerClient cl = new WebServerClient();
            e.sendResponseHeaders(200, 0);
            OutputStream os = e.getResponseBody();
            try
            {
                cl.perform (e.getRequestURI().toString(), os);
            }
            catch (Exception e1)
            {
                System.out.println("oops");
            }
            os.close();
        };

        for(int s=0; s<1000; s++)
            server.createContext("/", hnd);

        server.start();
    }
}
