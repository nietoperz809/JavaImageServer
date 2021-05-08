/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inetserver;

import minimalftp.FTPServer;
import minimalftp.impl.NativeFileSystem;
import minimalftp.impl.NoOpAuthenticator;

import java.io.File;
import java.io.IOException;


/**
 *
 * @author Administrator
 */
public class PittiFtpServer
{
    private final String basedir;
    private FTPServer server;
    private int port;

    /**
     * @param path
     * @param port
     */
    public PittiFtpServer (String path, int port)
    {
        this.port = port;
        this.basedir = path;

    }

    public void stop()
    {
        try {
            server.close();
            server = null;
        } catch (IOException e) {
            System.out.println("can't stop ftp: "+e);
        }
    }

    public void start()
    {
        if (server != null)
            return;
        // Start listening synchronously
        try {
            // Uses the current working directory as the root
            File root = new File(basedir);
            // Creates a native file system
            NativeFileSystem fs = new NativeFileSystem(root);
            // Creates a noop authenticator
            NoOpAuthenticator auth = new NoOpAuthenticator(fs);
            // Creates the server with the authenticator
            server = new FTPServer(auth);
            server.listenSync(port);
        } catch (IOException e) {
            System.out.println("can't start ftp: "+e);
        }
    }
}
