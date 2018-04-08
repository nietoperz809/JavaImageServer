import applications.FtpServerGUI;
import applications.WebServerGUI;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame
{
    private static void start()
    {
        JFrame jf = new JFrame();
        FtpServerGUI ftp = new FtpServerGUI();
        WebServerGUI web = new WebServerGUI();

        jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jf.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing (WindowEvent windowEvent)
            {
                ftp.stop();
                web.stop();
            }
        });

        JTabbedPane tabpane = new JTabbedPane
                (JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabpane.addTab("FTP", ftp);
        tabpane.addTab("HTTP", web);

        jf.add(tabpane);
        jf.pack();
        jf.setVisible(true);
    }

    public static void main (String[] args)
    {
        SwingUtilities.invokeLater (MainFrame::start);
    }
}
