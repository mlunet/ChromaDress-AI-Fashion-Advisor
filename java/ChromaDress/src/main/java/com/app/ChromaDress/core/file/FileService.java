package com.app.ChromaDress.core.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileService {

  @Value("${app.upload.dir}")
  private String uploadDir;

  public String saveFile(MultipartFile file) throws IOException {
    Path uploadPath = Paths.get(uploadDir);
    if (!Files.exists(uploadPath)) {
      Files.createDirectories(uploadPath);
    }

    String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
    Path filePath = uploadPath.resolve(fileName);

    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

    return fileName;
  }

  public void deleteFile(String fileName) {
    if (fileName == null || fileName.isEmpty()) {
      return;
    }

    try {
      Path filePath = Paths.get(uploadDir).resolve(fileName);
      Files.deleteIfExists(filePath);
    } catch (IOException e) {
      System.err.println("Could not delete file: " + fileName + ". Error: " + e.getMessage());
    }
  }
}
