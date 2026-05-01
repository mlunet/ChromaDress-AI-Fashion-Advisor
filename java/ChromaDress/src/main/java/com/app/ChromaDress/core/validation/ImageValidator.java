package com.app.ChromaDress.core.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.util.List;

public class ImageValidator implements ConstraintValidator<ValidImage, MultipartFile> {

  private static final List<String> ALLOWED_TYPES = List.of(MediaType.IMAGE_JPEG_VALUE,
      MediaType.IMAGE_PNG_VALUE);

  @Override
  public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {

    if (file == null || file.isEmpty()) {
      return false;
    }
    return ALLOWED_TYPES.contains(file.getContentType());
  }
}
