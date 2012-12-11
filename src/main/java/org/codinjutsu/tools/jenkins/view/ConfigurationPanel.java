/*
 * Copyright (c) 2012 David Boissier
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

package org.codinjutsu.tools.jenkins.view;

import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.JenkinsConfiguration;
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;
import org.codinjutsu.tools.jenkins.logic.RequestManager;
import org.codinjutsu.tools.jenkins.security.AuthenticationException;
import org.codinjutsu.tools.jenkins.security.SecurityMode;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.view.annotation.FormValidator;
import org.codinjutsu.tools.jenkins.view.annotation.GuiField;
import org.codinjutsu.tools.jenkins.view.validator.NotNullValidator;
import org.codinjutsu.tools.jenkins.view.validator.UIValidator;
import org.codinjutsu.tools.jenkins.view.validator.UrlValidator;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import static org.codinjutsu.tools.jenkins.JenkinsConfiguration.RESET_STR_VALUE;
import static org.codinjutsu.tools.jenkins.view.validator.ValidatorTypeEnum.*;

@SuppressWarnings({"unchecked"})
public class ConfigurationPanel {

    private static final Color CONNECTION_TEST_SUCCESSFUL_COLOR = new Color(0, 165, 0);
    private static final Color CONNECTION_TEST_FAILED_COLOR = new Color(165, 0, 0);


    @GuiField(validators = URL)
    private JTextField serverUrl;

    @GuiField(validators = {NOTNULL, POSITIVE_INTEGER})
    private JTextField buildDelay;

    private JCheckBox enableJobAutoRefresh;

    @GuiField(validators = {NOTNULL, STRICT_POSITIVE_INTEGER})
    private JTextField jobRefreshPeriod;

    private JCheckBox enableRssAutoRefresh;

    @GuiField(validators = {NOTNULL, STRICT_POSITIVE_INTEGER})
    private JTextField rssRefreshPeriod;

    private JCheckBox enableAuthentication;

    private JTextField username;

    private JPasswordField passwordField;
    private JTextField crumbDataField;

    private JPanel rootPanel;

    private JButton testConnexionButton;
    private JLabel connectionStatusLabel;
    private JPanel debugPanel;
    private JTextPane debugTextPane;

    private final FormValidator formValidator;

    private SecurityMode securityMode = SecurityMode.NONE;

    private final RequestManager requestManager;

    public ConfigurationPanel(final RequestManager requestManager) {
        this.requestManager = requestManager;

        serverUrl.setName("serverUrl");
        buildDelay.setName("buildDelay");
        enableJobAutoRefresh.setName("enableJobAutoRefresh");
        jobRefreshPeriod.setName("jobRefreshPeriod");
        enableRssAutoRefresh.setName("enableRssAutoRefresh");
        rssRefreshPeriod.setName("rssRefreshPeriod");
        enableAuthentication.setName("enableAuthentication");
        username.setName("username");

        passwordField.setName("passwordFile");
        crumbDataField.setName("crumbDataFile");

        testConnexionButton.setName("testConnexionButton");
        connectionStatusLabel.setName("connectionStatusLabel");

        debugPanel.setVisible(false);

        initDebugTextPane();

        initListeners();

        formValidator = FormValidator.init(this)
                .addValidator(enableAuthentication, new UIValidator<JCheckBox>() {
                    public void validate(JCheckBox component) throws ConfigurationException {
                        if (enableAuthentication.isSelected()) {
                            new NotNullValidator().validate(username);
                            if (passwordField.isEnabled()) {    //TODO a revoir
                                String password = String.valueOf(passwordField.getPassword());
                                if (StringUtils.isBlank(password)) {
                                    throw new ConfigurationException(String.format("'%s' must be set", passwordField.getName()));
                                }
                            }
                        }
                    }
                });


    }

    private void initDebugTextPane() {
        HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
        HTMLDocument htmlDocument = new HTMLDocument();

        debugTextPane.setEditable(false);
        debugTextPane.setBackground(Color.WHITE);
        debugTextPane.setEditorKit(htmlEditorKit);
        htmlEditorKit.install(debugTextPane);
        debugTextPane.setDocument(htmlDocument);
    }

    //TODO use annotation to create a guiwrapper so isModified could be simplified
    public boolean isModified(JenkinsConfiguration configuration) {
        return !configuration.getServerUrl().equals(serverUrl.getText())
                || !(configuration.getBuildDelay() == Integer.parseInt(buildDelay.getText()))
                || !(configuration.isEnableJobAutoRefresh() == enableJobAutoRefresh.isSelected())
                || !(configuration.getJobRefreshPeriod() == Integer.parseInt(jobRefreshPeriod.getText()))
                || !(configuration.isEnableRssAutoRefresh() == enableRssAutoRefresh.isSelected())
                || !(configuration.getRssRefreshPeriod() == Integer.parseInt(rssRefreshPeriod.getText()))
                || !(configuration.getSecurityMode() == securityMode)
                || !(configuration.getUsername().equals(username.getText()))
                || !(configuration.getPassword().equals(String.valueOf(passwordField.getPassword())))
                || !(configuration.getCrumbFile().equals(crumbDataField.getText()))
                ;
    }


    //TODO use annotation to create a guiwrapper so isModified could be simplified
    public void applyConfigurationData(JenkinsConfiguration configuration) throws ConfigurationException {
        formValidator.validate();

        if (!StringUtils.equals(configuration.getServerUrl(), serverUrl.getText())) {
            configuration.getFavoriteJobs().clear();
            configuration.setLastSelectedView(null);
        }

        configuration.setServerUrl(serverUrl.getText());
        configuration.setDelay(Integer.valueOf(buildDelay.getText()));
        configuration.setEnableJobAutoRefresh(enableJobAutoRefresh.isSelected());
        configuration.setJobRefreshPeriod(Integer.valueOf(jobRefreshPeriod.getText()));
        configuration.setEnableRssAutoRefresh(enableRssAutoRefresh.isSelected());
        configuration.setRssRefreshPeriod(Integer.valueOf(rssRefreshPeriod.getText()));
        configuration.setRssRefreshPeriod(Integer.valueOf(rssRefreshPeriod.getText()));
        configuration.setSecurityMode(securityMode);

        configuration.setUsername(username.getText());
        configuration.setPassword(String.valueOf(passwordField.getPassword()));
        configuration.setCrumbFile(crumbDataField.getText());
    }

    public void loadConfigurationData(JenkinsConfiguration configuration) {
        serverUrl.setText(configuration.getServerUrl());
        buildDelay.setText(String.valueOf(configuration.getBuildDelay()));

        boolean jobAutoRefresh = configuration.isEnableJobAutoRefresh();
        enableJobAutoRefresh.setSelected(jobAutoRefresh);
        jobRefreshPeriod.setText(String.valueOf(configuration.getJobRefreshPeriod()));
        jobRefreshPeriod.setEnabled(jobAutoRefresh);

        boolean rssAutoRefresh = configuration.isEnableRssAutoRefresh();
        enableRssAutoRefresh.setSelected(rssAutoRefresh);
        rssRefreshPeriod.setText(String.valueOf(configuration.getRssRefreshPeriod()));
        rssRefreshPeriod.setEnabled(rssAutoRefresh);

        setSecurityMode(configuration.getSecurityMode(), configuration.getUsername(), configuration.getPassword(), configuration.getCrumbFile());
    }

    private void setSecurityMode(SecurityMode securityMode, @Nullable String usernameValue, @Nullable String passwordFileValue, @Nullable String crumbFile) {
        boolean isEnableAuthentication = SecurityMode.BASIC.equals(securityMode);

        enableAuthentication.setSelected(isEnableAuthentication);

        username.setText(usernameValue);
        username.setEnabled(isEnableAuthentication);

        passwordField.setText(passwordFileValue);
        passwordField.setEnabled(isEnableAuthentication);

        crumbDataField.setText(crumbFile);

        this.securityMode = securityMode;
    }


    public JPanel getRootPanel() {
        return rootPanel;
    }


    private void initListeners() {
        String resetPeriodValue = Integer.toString(JenkinsConfiguration.RESET_PERIOD_VALUE);

        testConnexionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    debugPanel.setVisible(false);

                    new NotNullValidator().validate(serverUrl);
                    new UrlValidator().validate(serverUrl);
                    requestManager.authenticate(
                            serverUrl.getText(), securityMode, username.getText(), String.valueOf(passwordField.getPassword()), crumbDataField.getText());
                    setConnectionFeedbackLabel(CONNECTION_TEST_SUCCESSFUL_COLOR, "Successful");
                } catch (Exception ex) {
                    setConnectionFeedbackLabel(CONNECTION_TEST_FAILED_COLOR, "[Fail] " + ex.getMessage());
                    if (ex instanceof AuthenticationException) {
                        AuthenticationException authenticationException = (AuthenticationException) ex;
                        String responseBody = authenticationException.getResponseBody();
                        if (StringUtils.isNotBlank(responseBody)) {
                            debugPanel.setVisible(true);
                            debugTextPane.setText(responseBody);
                        }
                    }
                }
            }
        });

        enableJobAutoRefresh.addItemListener(new EnablerFieldListener(enableJobAutoRefresh,
                jobRefreshPeriod, resetPeriodValue));
        enableRssAutoRefresh.addItemListener(new EnablerFieldListener(enableRssAutoRefresh,
                rssRefreshPeriod, resetPeriodValue));

        enableAuthentication.addItemListener(new EnablerFieldListener(enableAuthentication, username, RESET_STR_VALUE));
        enableAuthentication.addItemListener(new EnablerPasswordListener(enableAuthentication, passwordField));

        enableAuthentication.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (enableAuthentication.isSelected()) {
                    securityMode = SecurityMode.BASIC;
                } else {
                    securityMode = SecurityMode.NONE;
                }
            }
        });
    }

    private void setConnectionFeedbackLabel(final Color labelColor, final String labelText) {
        GuiUtil.runInSwingThread(new Runnable() {
            public void run() {
                connectionStatusLabel.setForeground(labelColor);
                connectionStatusLabel.setText(labelText);
            }
        });
    }

    private class EnablerFieldListener implements ItemListener {
        private final JCheckBox enablerCheckBox;
        private final JTextField fieldToEnable;
        private final String resetValue;


        private EnablerFieldListener(JCheckBox enablerCheckBox,
                                     JTextField fieldToEnable, String resetValue) {
            this.enablerCheckBox = enablerCheckBox;
            this.fieldToEnable = fieldToEnable;
            this.resetValue = resetValue;
        }


        public void itemStateChanged(ItemEvent event) {
            final boolean isSelected = enablerCheckBox.isSelected();
            if (!isSelected) {
                fieldToEnable.setText(resetValue);
            }
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    fieldToEnable.setEnabled(isSelected);
                }
            });
        }
    }

    private class EnablerPasswordListener implements ItemListener {
        private final JCheckBox enablerCheckBox;
        private final JPasswordField fieldToEnable;


        private EnablerPasswordListener(JCheckBox enablerCheckBox,
                                        JPasswordField fieldToEnable) {
            this.enablerCheckBox = enablerCheckBox;
            this.fieldToEnable = fieldToEnable;
        }


        public void itemStateChanged(ItemEvent event) {

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    final boolean isSelected = enablerCheckBox.isSelected();
                    if (!isSelected) {
                        fieldToEnable.setText(RESET_STR_VALUE);
                    }
                    fieldToEnable.setEnabled(isSelected);
                }
            });
        }
    }
}