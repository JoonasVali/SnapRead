package com.github.joonasvali.bookreaderai;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileHandler {
  private final Path outputFolder;

  public FileHandler(Path outputFolder) {
    this.outputFolder = outputFolder;
  }

  public Path getFilePath(String fileNameBody) {
    return outputFolder.resolve(fileNameBody + ".txt");
  }

  public void saveToFile(String fileNameBody, String content) throws IOException {
    System.out.println("Saving to file: " + fileNameBody);
    Path filePath = getFilePath(fileNameBody);
    Files.createDirectories(filePath.getParent());
    Files.writeString(filePath, content);
  }

  public String loadFromFile(String fileNameBody) throws IOException {
    System.out.println("Loading from file: " + fileNameBody);
    Path filePath = getFilePath(fileNameBody);
    if (Files.exists(filePath)) {
      return Files.readString(filePath);
    }
    return "";
  }
}