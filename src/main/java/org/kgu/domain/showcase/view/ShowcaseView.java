package org.kgu.domain.showcase.view;

import org.kgu.domain.showcase.feature.ShowcaseScene;
import org.kgu.domain.showcase.view.layout.ShowcaseLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public final class ShowcaseView {
    private ShowcaseView() { }

    public static Optional<ShowcaseScene> choose(Component owner) {
        var list = new JList<>(ShowcaseScene.values());
        var layout = new ShowcaseLayout(list);
        var pane = new JOptionPane(layout, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION,
            null, new String[]{"시연", "취소"}, "시연");
        JDialog dialog = pane.createDialog(owner, "F1 · 장면 시연");
        var launch = new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent event) {
                pane.setValue("시연");
            }
        };
        list.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "launch");
        list.getActionMap().put("launch", launch);
        list.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent event) {
                int index = list.locationToIndex(event.getPoint());
                if (event.getClickCount() == 2 && index >= 0 && list.getCellBounds(index, index).contains(event.getPoint())) {
                    launch.actionPerformed(null);
                }
            }
        });
        SwingUtilities.invokeLater(list::requestFocusInWindow);
        dialog.setVisible(true);
        dialog.dispose();
        return "시연".equals(pane.getValue()) ? Optional.ofNullable(list.getSelectedValue()) : Optional.empty();
    }
}
