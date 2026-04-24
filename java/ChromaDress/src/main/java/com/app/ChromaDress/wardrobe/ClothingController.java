package com.app.ChromaDress.wardrobe;

import com.app.ChromaDress.analysis.PaletteType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/clothing")
@RequiredArgsConstructor
public class ClothingController {

    private final ClothingService clothingService;

    @PostMapping("/upload")
    public ResponseEntity<ClothingResponseDTO> uploadItem(@ModelAttribute ClothingRequestDTO dto) throws IOException {
        ClothingResponseDTO response = clothingService.saveItem(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-closet")
    public ResponseEntity<List<ClothingResponseDTO>> getMyCloset() {
        List<ClothingResponseDTO> closet = clothingService.getUserCloset();
        return ResponseEntity.ok(closet);
    }

    @GetMapping("/{clothingId}/recommendations")
    public ResponseEntity<List<OutfitSuggestionDTO>> outfitSuggestion(@PathVariable Long clothingId, @RequestParam(defaultValue = "WADA") PaletteType type) {
        List<OutfitSuggestionDTO> suggestions = clothingService.getOutfitSuggestions(clothingId, type);

        if (suggestions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(suggestions);
    }

    @DeleteMapping("/{clothingId}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long clothingId) {
        clothingService.deleteItem(clothingId);
        return ResponseEntity.noContent().build();
    }
}
