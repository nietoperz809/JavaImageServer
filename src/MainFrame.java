import applications.ConfigGUI;
import applications.FtpServerGUI;
import applications.WebServerGUI;
import inetserver.nagaweb.videostream.Http206Transmitter;
import misc.ConfigFile;
import misc.Dbg;
import misc.Tools;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MainFrame
{
    private static final ConfigFile configFile = new ConfigFile("serversettings.txt");

    private static boolean dirty = false;

    private static final FtpServerGUI ftp = new FtpServerGUI();
    private static final WebServerGUI web = new WebServerGUI();
    private static final ConfigGUI config = new ConfigGUI();
    private boolean visibility = true;

    private void loadConfiguration()
    {
        configFile.onEntry ("ftp-port", strings -> ftp.setPortTxt(strings[0]));
        configFile.onEntry ("ftp-path", strings -> ftp.setPathTxt(strings[0]));
        configFile.onEntry ("ftp-start", strings -> ftp.button.simulateClick());
        configFile.onEntry ("http-port", strings -> web.setPortTxt(strings[0]));
        configFile.onEntry ("http-path", strings -> web.setPathTxt(strings[0]));
        configFile.onEntry ("imagepage", strings -> web.setImagePageStyle(strings[0]));
        configFile.onEntry ("http-start", strings -> web.button.simulateClick());
        configFile.onEntry ("no-gui", strings -> this.visibility = false);
        configFile.onEntry ("http-browser-start", strings -> web.browser_startflag = true);
        configFile.onEntry ("chunksize", strings
                -> Http206Transmitter.getInstance().setChunkSize(Integer.parseInt(strings[0])));
        configFile.onEntry ("stream-port", strings
                -> Http206Transmitter.getInstance().setPort(Integer.parseInt(strings[0])));
        try
        {
            configFile.execute();
        }
        catch (Exception e)
        {
            Dbg.print("CFG file read error");
        }
    }

    public void start()
    {
        JFrame jf = new JFrame("InetServer");

        jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jf.setIconImage(Tools.loadImageFromResource ("favicon.ico"));
        JTabbedPane tabpane = new JTabbedPane (JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabpane.addTab("FTP", ftp);
        tabpane.addTab("HTTP", web);
        tabpane.addTab("Config", config.getMainPanel());

        tabpane.addChangeListener(e -> {
            if (tabpane.getSelectedIndex() == 2) {
                try {
                    byte[] conf = Files.readAllBytes(Paths.get(configFile.getUsedFilePath()));
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
                        Files.write (Paths.get(configFile.getUsedFilePath()), confTxt.getBytes());
                        loadConfiguration();
                        dirty = false;
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        });

        // Exec config file
        loadConfiguration();

        jf.add(tabpane);
        jf.pack();
        jf.setResizable(false);
        jf.setLocationRelativeTo(null);
        jf.setVisible(visibility);
    }

    public static void main (String[] args)
    {
       SwingUtilities.invokeLater (() -> new MainFrame().start());
    }
}
