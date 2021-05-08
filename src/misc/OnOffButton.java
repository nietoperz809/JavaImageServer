package misc;

import javax.swing.*;
import java.awt.*;

public class OnOffButton extends JButton {
    private final OnOffButtonAction onAction;
    private final OnOffButtonAction offAction;

    public OnOffButton (OnOffButtonAction on, OnOffButtonAction off) {
        onAction = on;
        offAction = off;
        setText("START");
        setBackground(Color.RED);
        addActionListener(e -> fakeClick());
    }

    public void fakeClick() {
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
