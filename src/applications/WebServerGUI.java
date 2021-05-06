package applications;/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import inetserver.nagaweb.NIOWebServer;

import javax.swing.*;
import java.awt.*;

/**
 *
 * This is the GUI class for the web server
 */
public class WebServerGUI extends JPanel
{
    private static final long serialVersionUID = 1L;
    private volatile NIOWebServer sockserver = null;

    /**
     * Constructor: creates new form WebServerGUI
     */
    public WebServerGUI ()
    {
        initComponents();
        button.setBackground(Color.RED);
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        pathTxt = new JTextField();
        portTxt = new JTextField();
        JLabel jLabel1 = new JLabel();
        JLabel jLabel2 = new JLabel();
        button = new JButton();

//        setClosable(true);
//        setIconifiable(true);
//        setMaximizable(true);
//        setResizable(true);
//        setTitle("Webserver");
        setVisible(true);

        pathTxt.setText("F:\\\\");

            portTxt.setText("80");

            jLabel1.setText("BasePath");

            jLabel2.setText("Port");

            button.setText("Start");
            button.setDoubleBuffered(true);
            button.addActionListener(evt -> buttonActionPerformed());


            GroupLayout layout = new GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jLabel2)
                            .addGap(18, 18, 18)
                            .addComponent(portTxt))
                        .addComponent(jLabel1)
                        .addGroup(layout.createSequentialGroup()
                            //.addComponent(jLabel3)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)))
                            //.addComponent(buffSizeTxt, GroupLayout.PREFERRED_SIZE, 71, GroupLayout.PREFERRED_SIZE)))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addGap(17, 17, 17)
                            //.addComponent(transmitted, GroupLayout.PREFERRED_SIZE, 111, GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            //.addComponent(jLabel4)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            //.addComponent(fileTime, GroupLayout.PREFERRED_SIZE, 99, GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(button, GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE))
                        .addComponent(pathTxt))
                    .addContainerGap())
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(pathTxt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel2)
                                .addComponent(portTxt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                //.addComponent(transmitted, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
                                //.addComponent(fileTime)
                                //.addComponent(jLabel4))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE))
                                //.addComponent(jLabel3))
                                //.addComponent(buffSizeTxt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addGap(0, 3, Short.MAX_VALUE))
                        .addComponent(button, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addContainerGap())
            );

            //pack();
        }// </editor-fold>//GEN-END:initComponents


    public String getBasePath()
    {
        return pathTxt.getText();
    }

    private void buttonActionPerformed ()
    {
        if (button.getBackground() == Color.GREEN) // selected
        {
            stop();
        }
        else
        {
            start();
        }
    }

    public void setPathTxt (String pathTxt)
    {
        this.pathTxt.setText(pathTxt);
    }

    public void setPortTxt (String portTxt)
    {
        this.portTxt.setText(portTxt);
    }

    public void start()
    {
        if (sockserver == null)
        {
            int port = Integer.parseInt(portTxt.getText());

            sockserver = new NIOWebServer(port, pathTxt.getText());
            new Thread(() -> sockserver.runServer()).start();

            button.setText("stop");
            button.setBackground(Color.GREEN);
        }
    }

    public void stop ()
    {
        if (sockserver != null)
        {
            sockserver.halt();
            sockserver = null;
            button.setText("start");
            button.setBackground(Color.RED);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton button;
    private JTextField pathTxt;
    private JTextField portTxt;
}
