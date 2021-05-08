import applications.ConfigGUI;
import applications.FtpServerGUI;
import applications.WebServerGUI;
import misc.ConfigFile;
import misc.Tools;

import javax.swing.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;

class MainFrame
{
    private static final String settingFile = "serversettings.txt";
    private static boolean dirty = false;

    private static void start()
    {
        JFrame jf = new JFrame("InetServer");
        FtpServerGUI ftp = new FtpServerGUI();
        WebServerGUI web = new WebServerGUI();
        ConfigGUI config = new ConfigGUI();

        jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jf.setIconImage(Tools.getImageFromResource("favicon.ico"));
        JTabbedPane tabpane = new JTabbedPane (JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabpane.addTab("FTP", ftp);
        tabpane.addTab("HTTP", web);
        tabpane.addTab("Config", config.getMainPanel());

        tabpane.addChangeListener(e -> {
            if (tabpane.getSelectedIndex() == 2) {
                try {
                    byte[] conf = Files.readAllBytes(Paths.get(settingFile));
                    String confTxt = new String(conf);
                    config.getTextPanel().setText(confTxt);
                    dirty = true;
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            } else {
                if (dirty) {
                    try {
                        String confTxt = config.getTextPanel().getText();
                        Files.write (Paths.get(settingFile), confTxt.getBytes());
                        dirty = false;
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        });

        jf.add(tabpane);
        jf.pack();
        jf.setResizable(false);
        jf.setLocationRelativeTo(null);
        jf.setVisible(true);

        // Exec config file
        ConfigFile cf = new ConfigFile(settingFile);
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
