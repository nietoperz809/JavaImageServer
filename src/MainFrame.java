import applications.FtpServerGUI;
import applications.WebServerGUI;
import misc.ConfigFile;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

class MainFrame
{
    private static void start()
    {
        JFrame jf = new JFrame("InetServer");
        FtpServerGUI ftp = new FtpServerGUI();
        WebServerGUI web = new WebServerGUI();

        jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//        jf.addWindowListener(new WindowAdapter()
//        {
//            @Override
//            public void windowClosing (WindowEvent windowEvent)
//            {
//                ftp.stop();
//                web.stop();
//            }
//        });

        JTabbedPane tabpane = new JTabbedPane
                (JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabpane.addTab("FTP", ftp);
        tabpane.addTab("HTTP", web);

        jf.add(tabpane);
        jf.pack();
        jf.setVisible(true);

        // Exec config file
        ConfigFile cf = new ConfigFile("serversettings.txt");
        cf.setAction("ftp-port", strings -> ftp.setPortTxt(strings[0]));
        cf.setAction("ftp-path", strings -> ftp.setPathTxt(strings[0]));
        cf.setAction("ftp-start", strings -> ftp.start());
        cf.setAction("http-port", strings -> web.setPortTxt(strings[0]));
        cf.setAction("http-path", strings -> web.setPathTxt(strings[0]));
        cf.setAction("http-start", strings -> web.start());
        try
        {
            cf.execute();
        }
        catch (IOException e)
        {
            System.out.println("CFG file read error");;
        }
    }

    public static void main (String[] args)
    {
        SwingUtilities.invokeLater (MainFrame::start);
    }
}
