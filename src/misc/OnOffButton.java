package misc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Callable;

public class OnOffButton extends JButton {
    private final Callable<Void> on;
    private final Callable<Void> off;

    public OnOffButton(Callable<Void> on, Callable<Void> off)
    {
        this.on = on;
        this.off = off;
        setText("START");
        setBackground(Color.RED);
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (getBackground() == Color.RED)
                {
                    setText("STOP");
                    setBackground(Color.GREEN);
                    try {
                        on.call();
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
                else
                {
                    setText("START");
                    setBackground(Color.RED);
                    try {
                        off.call();
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            }
        });
    }
}
