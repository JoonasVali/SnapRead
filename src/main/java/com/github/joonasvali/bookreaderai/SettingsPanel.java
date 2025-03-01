package com.github.joonasvali.bookreaderai;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.prefs.Preferences;

public class SettingsPanel extends JPanel {

  private JLabel apiKeyStatusLabel;
  private JTextField folderPathField;
  private JButton chooseFolderButton;
  private JLabel folderErrorLabel;
  private JButton continueButton;

  private Preferences preferences;
  private final Consumer<Path> continueAction;

  public SettingsPanel(Consumer<Path> continueAction) {
    this.continueAction = continueAction;
    preferences = Preferences.userNodeForPackage(SettingsPanel.class);

    // Main layout: BorderLayout for easy separation of sections
    setLayout(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    // Top Panel: API Key Status
    JPanel topPanel = createApiKeyPanel();
    add(topPanel, BorderLayout.NORTH);

    // Center Panel: Folder Selection
    JPanel centerPanel = createFolderPanel();
    add(centerPanel, BorderLayout.CENTER);

    // Bottom Panel: Continue Button
    JPanel bottomPanel = createBottomPanel();
    add(bottomPanel, BorderLayout.SOUTH);

    // Check if saved folder is valid; if so, enable "Continue" button
    String savedFolder = preferences.get("inputFolder", "");
    if (!savedFolder.isEmpty() && new File(savedFolder).isDirectory() && containsJpg(new File(savedFolder))) {
      continueButton.setEnabled(true);
    } else {
      continueButton.setEnabled(false);
      if (!savedFolder.isEmpty()) {
        folderErrorLabel.setText("Error: Saved folder is invalid or has no .jpg files.");
        folderErrorLabel.setForeground(Color.RED);
      }
    }
  }

  /**
   * Creates the top panel containing the API key status.
   */
  private JPanel createApiKeyPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    String openaiApiKey = System.getenv("OPENAI_API_KEY");
    if (openaiApiKey == null || openaiApiKey.isEmpty()) {
      apiKeyStatusLabel = new JLabel("Error: OPENAI_API_KEY not found.");
      apiKeyStatusLabel.setForeground(Color.RED);
    } else {
      apiKeyStatusLabel = new JLabel("OPENAI_API_KEY loaded successfully.");
      apiKeyStatusLabel.setForeground(Color.BLACK);
    }
    panel.add(apiKeyStatusLabel);
    return panel;
  }

  /**
   * Creates the center panel for folder selection, using a GridBagLayout
   * for a more controlled layout.
   */
  private JPanel createFolderPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Folder Settings"));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(5, 5, 5, 5);

    // 1) Label: "Input Folder"
    JLabel folderLabel = new JLabel("Input Folder:");
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 0;
    panel.add(folderLabel, gbc);

    // 2) Text field for folder path
    folderPathField = new JTextField(20);
    folderPathField.setEditable(false);
    folderPathField.setText(preferences.get("inputFolder", ""));
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.weightx = 1;
    panel.add(folderPathField, gbc);

    // 3) "Choose Folder" button
    chooseFolderButton = new JButton("Choose Folder");
    chooseFolderButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        onChooseFolder();
      }
    });
    gbc.gridx = 2;
    gbc.gridy = 0;
    gbc.weightx = 0;
    panel.add(chooseFolderButton, gbc);

    // 4) Folder error label (spans the width)
    folderErrorLabel = new JLabel("");
    folderErrorLabel.setForeground(Color.RED);
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.gridwidth = 3;
    gbc.weightx = 1;
    panel.add(folderErrorLabel, gbc);

    return panel;
  }

  /**
   * Creates the bottom panel containing the "Continue" button.
   */
  private JPanel createBottomPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    continueButton = new JButton("Continue");
    continueButton.addActionListener(e -> continueAction.accept(Path.of(folderPathField.getText())));
    // Initially disabled, will be enabled if folder is valid
    continueButton.setEnabled(false);
    panel.add(continueButton);
    return panel;
  }

  /**
   * Opens a directory chooser and validates that the folder contains .jpg files.
   */
  private void onChooseFolder() {
    JFileChooser chooser = new JFileChooser(folderPathField.getText());
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    int result = chooser.showOpenDialog(SettingsPanel.this);
    if (result == JFileChooser.APPROVE_OPTION) {
      File selectedFolder = chooser.getSelectedFile();
      if (selectedFolder.isDirectory() && containsJpg(selectedFolder)) {
        folderPathField.setText(selectedFolder.getAbsolutePath());
        folderErrorLabel.setText("");
        continueButton.setEnabled(true);
        // Save to preferences
        preferences.put("inputFolder", selectedFolder.getAbsolutePath());
      } else {
        folderErrorLabel.setText("Error: Selected folder does not contain any .jpg files.");
        folderErrorLabel.setForeground(Color.RED);
        continueButton.setEnabled(false);
      }
    }
  }

  /**
   * Checks if the provided folder contains at least one file ending in .jpg or .jpeg.
   */
  private boolean containsJpg(File folder) {
    File[] jpgFiles = folder.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        String lowerName = name.toLowerCase();
        return lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg");
      }
    });
    return jpgFiles != null && jpgFiles.length > 0;
  }

}
