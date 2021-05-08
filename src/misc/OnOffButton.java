package misc;

import javax.swing.*;
import java.awt.*;

public class OnOffButton extends JButton {
    private final OnOffButtonHandlers handlers;

    public OnOffButton(OnOffButtonHandlers handlers) {
        this.handlers = handlers;
        setText("START");
        setBackground(Color.RED);
        addActionListener(e -> fakeClick());
    }

    public void fakeClick() {
        if (getBackground() == Color.RED) {
            setText("STOP");
            setBackground(Color.GREEN);
            try {
                handlers.start();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        } else {
            setText("START");
            setBackground(Color.RED);
            try {
                handlers.stop();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }
}
