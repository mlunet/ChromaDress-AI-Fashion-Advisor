package com.app.ChromaDress.wardrobe;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClothingRepository extends JpaRepository<ClothingItem, Long> {
    List<ClothingItem> findByUserId(Long userId);

    Optional<ClothingItem> findByIdAndUserId(Long id, Long userId);

    @Query(value = "SELECT *, LEAST(ABS(c.hue - :h), 360 - ABS(c.hue - :h)) * 2.5 + ABS(c.saturation - :s) * 1.0 + ABS(c.lightness - :l) * 1.2 AS weighted_distance FROM clothing_items c WHERE LEAST(ABS(c.hue - :h), 360 - ABS(c.hue - :h)) < 15.0 AND (ABS(c.lightness - :l) < 15.0 OR (:l < 30.0 AND c.lightness < 35.0)) AND ABS(c.saturation - :s) < 15.0 AND c.id != :currentId AND LOWER(c.category) != LOWER(:excludedCategory) ORDER BY weighted_distance", nativeQuery = true)
    List<ClothingItem> findSimilarHSL(@Param("h") float h, @Param("s") float s, @Param("l") float l, @Param("currentId") Long currentId, @Param("excludedCategory") String excludedCategory);
}
