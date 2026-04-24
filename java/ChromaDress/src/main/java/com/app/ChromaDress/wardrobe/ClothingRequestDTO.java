package com.app.ChromaDress.wardrobe;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record ClothingRequestDTO(@NotNull String name, @NotNull String category, @NotNull MultipartFile image) {
}
