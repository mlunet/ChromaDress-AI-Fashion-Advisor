package com.app.ChromaDress.wardrobe;

import com.app.ChromaDress.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.awt.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "clothing_items")
public class ClothingItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  private String category;

  private String hexColor;

  private int red;
  private int green;
  private int blue;

  private float hue;
  private float saturation;
  private float lightness;

  private String imageUrl;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  @JsonIgnore
  private User user;

  public void setHexAndRgb(String hex) {
    this.hexColor = hex;
    Color color = Color.decode(hex);
    this.red = color.getRed();
    this.green = color.getGreen();
    this.blue = color.getBlue();
  }

  public void setHslFromHex(String hex) {
    float[] hsl = new float[3];
    Color color = Color.decode(hex);
    Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsl);
    this.hue = hsl[0] * 360;
    this.saturation = hsl[1] * 100;
    this.lightness = hsl[2] * 100;
  }
}
