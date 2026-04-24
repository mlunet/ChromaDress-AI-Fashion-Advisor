package com.app.ChromaDress.wardrobe;

import com.app.ChromaDress.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ClothingItemMapper {

    @Value("${app.upload.base-url}")
    private String baseUrl;

    public ClothingItem RequestDtoToEntity(ClothingRequestDTO dto, User user) {

        ClothingItem item = new ClothingItem();
        item.setName(dto.name());
        item.setCategory(dto.category());
        item.setUser(user);

        return item;
    }

    public ClothingResponseDTO EntityToResponseDto(ClothingItem item) {

        return new ClothingResponseDTO(
                item.getId(),
                item.getName(),
                item.getCategory(),
                item.getHexColor(),
                baseUrl + item.getImageUrl()
        );
    }
}
