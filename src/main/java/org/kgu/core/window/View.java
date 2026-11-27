package org.kgu.core.window;

import javax.swing.*;
import java.awt.*;

public interface View {
    ViewId getViewId();

    JComponent getComponent();

    Dimension getViewSize();
}
