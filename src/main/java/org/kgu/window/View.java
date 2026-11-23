package org.kgu.window;

import javax.swing.*;
import java.awt.*;

public interface View {
    ViewId getViewId();

    JComponent getComponent();

    Dimension getViewSize();
}
