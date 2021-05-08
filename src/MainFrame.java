import applications.FtpServerGUI;
import applications.WebServerGUI;
import misc.ConfigFile;
import misc.Tools;

import javax.swing.*;
import java.io.IOException;

class MainFrame
{
    private static void start()
    {
        JFrame jf = new JFrame("InetServer");
        FtpServerGUI ftp = new FtpServerGUI();
        WebServerGUI web = new WebServerGUI();

        jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jf.setIconImage(Tools.getImageFromResource("favicon.ico"));
        JTabbedPane tabpane = new JTabbedPane
                (JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabpane.addTab("FTP", ftp);
        tabpane.addTab("HTTP", web);

        jf.add(tabpane);
        jf.pack();
        jf.setResizable(false);
        jf.setLocationRelativeTo(null);
        jf.setVisible(true);

        // Exec config file
        ConfigFile cf = new ConfigFile("serversettings.txt");
        cf.setAction("ftp-port", strings -> ftp.setPortTxt(strings[0]));
        cf.setAction("ftp-path", strings -> ftp.setPathTxt(strings[0]));
        cf.setAction("ftp-start", strings -> ftp.button.fakeClick());
        cf.setAction("http-port", strings -> web.setPortTxt(strings[0]));
        cf.setAction("http-path", strings -> web.setPathTxt(strings[0]));
        cf.setAction("http-start", strings -> web.button.fakeClick());
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
