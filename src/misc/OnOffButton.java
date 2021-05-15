package misc;

import javax.swing.*;
import java.awt.*;

public class OnOffButton extends JButton {
    private final ButtonAction onAction;
    private final ButtonAction offAction;

    @FunctionalInterface
    public interface ButtonAction {
        void doIt();
    }

    public OnOffButton (ButtonAction on, ButtonAction off) {
        onAction = on;
        offAction = off;
        setText("START");
        setBackground(Color.RED);
        addActionListener(e -> simulateClick());
    }

    public void simulateClick() {
        if (getBackground() == Color.RED) {
            setText("STOP");
            setBackground(Color.GREEN);
            onAction.doIt();
        } else {
            setText("START");
            setBackground(Color.RED);
            offAction.doIt();
        }
    }
}
