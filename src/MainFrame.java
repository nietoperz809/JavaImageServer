import applications.ConfigGUI;
import applications.FtpServerGUI;
import applications.WebServerGUI;
import misc.ConfigFile;
import misc.Tools;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

class MainFrame
{
    private static final String settingFile = "serversettings.txt";
    private static boolean dirty = false;

    private static FtpServerGUI ftp = new FtpServerGUI();
    private static WebServerGUI web = new WebServerGUI();
    private static ConfigGUI config = new ConfigGUI();

    private static boolean loadConfiguration()
    {
        ConfigFile cf = new ConfigFile(settingFile);
        cf.setAction("ftp-port", strings -> ftp.setPortTxt(strings[0]));
        cf.setAction("ftp-path", strings -> ftp.setPathTxt(strings[0]));
        cf.setAction("ftp-start", strings -> ftp.button.simulateClick());
        cf.setAction("http-port", strings -> web.setPortTxt(strings[0]));
        cf.setAction("http-path", strings -> web.setPathTxt(strings[0]));
        cf.setAction("http-start", strings -> web.button.simulateClick());
        cf.setAction("http-browser-start", strings -> web.browser_startflag = true);
        try
        {
            cf.execute();
            return true;
        }
        catch (Exception e)
        {
            System.out.println("CFG file read error");
            return false;
        }
    }

    private static void start()
    {
        JFrame jf = new JFrame("InetServer");

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
                        loadConfiguration();
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
        loadConfiguration();
    }

    public static void main (String[] args)
    {
//        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("JPEG");
//        while (readers.hasNext()) {
//            System.out.println("reader: " + readers.next());
//        }

        SwingUtilities.invokeLater (MainFrame::start);
    }
}
