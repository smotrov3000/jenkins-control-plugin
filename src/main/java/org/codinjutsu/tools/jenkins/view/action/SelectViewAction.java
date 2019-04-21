/*
 * Copyright (c) 2013 David Boissier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codinjutsu.tools.jenkins.view.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBList;
import org.codinjutsu.tools.jenkins.model.FavoriteView;
import org.codinjutsu.tools.jenkins.model.View;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.codinjutsu.tools.jenkins.view.JenkinsNestedViewComboRenderer;
import org.codinjutsu.tools.jenkins.view.JenkinsViewComboRenderer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

/**
 * Note: Inspiration from git4idea.history.wholeTree.BasePopupAction (git4idea plugin)
 */
public class SelectViewAction extends DumbAwareAction implements CustomComponentAction {

    private static final Icon ARROWS_ICON = GuiUtil.loadIcon("/ide/", "statusbar_arrows.png");

    protected final JLabel selectViewLabel;
    protected final JPanel selectViewPanel;
    private final BrowserPanel browserPanel;

    public SelectViewAction(final BrowserPanel browserPanel) {
        this.browserPanel = browserPanel;
        selectViewPanel = new JPanel();
        final BoxLayout layout = new BoxLayout(selectViewPanel, BoxLayout.X_AXIS);
        selectViewPanel.setLayout(layout);
        selectViewLabel = new JLabel();
        final JLabel textLabel = new JLabel("View:");
        textLabel.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 5));
        selectViewPanel.add(textLabel);
        selectViewPanel.add(selectViewLabel);
        selectViewPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 3));
        final JLabel iconLabel = new JLabel(ARROWS_ICON);
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
        selectViewPanel.add(iconLabel, selectViewLabel);

        selectViewPanel.addMouseListener(new MyMouseAdapter());
    }

    private JBList buildViewList(List<View> views, BrowserPanel browserPanel) {
        List<View> nonFlatViews = flatViewList(views);

        if (browserPanel.hasFavoriteJobs()) {
            nonFlatViews.add(FavoriteView.create());
        }

        final JBList viewList = new JBList(nonFlatViews);

        if (hasNestedViews(nonFlatViews)) {
            viewList.setCellRenderer(new JenkinsNestedViewComboRenderer());
        } else {
            viewList.setCellRenderer(new JenkinsViewComboRenderer());
        }
        return viewList;
    }


    @Override
    public void update(AnActionEvent e) {
        View currentSelectedView = browserPanel.getCurrentSelectedView();
        if (currentSelectedView != null) {
            selectViewLabel.setText(currentSelectedView.getName());
        } else {
            selectViewLabel.setText("");
        }
    }

    @Override
    public JComponent createCustomComponent(@NotNull Presentation presentation) {
        return selectViewPanel;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
    }


    private static List<View> flatViewList(List<View> views) {
        List<View> flattenViewList = new LinkedList<>();
        for (View view : views) {
            flattenViewList.add(view);
            if (view.hasNestedView()) {
                flattenViewList.addAll(view.getSubViews());
            }
        }

        return flattenViewList;
    }


    private static boolean hasNestedViews(List<View> views) {
        for (View view : views) {
            if (view.hasNestedView()) return true;
        }
        return false;
    }

    private class MyMouseAdapter extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            List<View> views = browserPanel.getJenkins().getViews();
            if (views.isEmpty()) {
                return;
            }

            final JBList viewList = buildViewList(views, browserPanel);

            JBPopup popup = new PopupChooserBuilder(viewList)
                    .setMovable(false)
                    .setCancelKeyEnabled(true)
                    .setItemChoosenCallback(() -> {
                        final View view = (View) viewList.getSelectedValue();
                        if (view == null || view.hasNestedView()) {
                            browserPanel.dispose();
                            return;
                        }
                        browserPanel.loadView(view);
                    })
                    .createPopup();
            popup.show(RelativePoint.getSouthOf(selectViewPanel));
        }
    }
}

