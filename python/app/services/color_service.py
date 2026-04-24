import colorsys
import json
import re
import numpy as np
from skimage import color
from collections import defaultdict
from app.services.exceptions import ColorNotFoundError, AppError


class ColorService:
    def __init__(self, json_path="./assets/colors.json"):
        try:
            with open(json_path, "r") as f:
                self.wada_data = json.load(f)
        except FileNotFoundError:
            raise AppError(f"Collection file {json_path} not found.", status_code=500)
        except json.JSONDecodeError:
            raise AppError("File colors.json is corrupted.", status_code=500)

        self.combination_map = defaultdict(list)
        for color in self.wada_data:
            for combo_id in color.get("combinations", []):
                self.combination_map[combo_id].append(color["hex"])

    @staticmethod
    def get_macro_color_name(hue: float) -> str:
        if hue >= 345 or hue < 15:
            return "Red"
        if 15 <= hue < 45:
            return "Orange"
        if 45 <= hue < 75:
            return "Yellow"
        if 75 <= hue < 165:
            return "Green"
        if 165 <= hue < 210:
            return "Cyan"
        if 210 <= hue < 255:
            return "Blue"
        if 255 <= hue < 285:
            return "Purple"
        if 285 <= hue < 345:
            return "Pink"
        return "Unknown"

    @staticmethod
    def calculate_distance(h1, h2):
        try:
            rgb1 = [int(h1.lstrip("#")[i : i + 2], 16) for i in (0, 2, 4)]
            rgb2 = [int(h2.lstrip("#")[i : i + 2], 16) for i in (0, 2, 4)]
            return sum((a - b) ** 2 for a, b in zip(rgb1, rgb2)) ** 0.5
        except:
            raise AppError(
                "Error during distance calculation: invalid HEX colors.",
                status_code=400,
            )

    @staticmethod
    def calculate_delta_e(h1, h2):
        try:
            rgb1 = (
                np.array([int(h1.lstrip("#")[i : i + 2], 16) for i in (0, 2, 4)])
                / 255.0
            )
            rgb2 = (
                np.array([int(h2.lstrip("#")[i : i + 2], 16) for i in (0, 2, 4)])
                / 255.0
            )

            lab1 = color.rgb2lab(rgb1.reshape(1, 1, 3))
            lab2 = color.rgb2lab(rgb2.reshape(1, 1, 3))

            return color.deltaE_ciede2000(lab1, lab2)[0][0]
        except:
            raise AppError(
                "Error during distance calculation: invalid HEX colors.",
                status_code=400,
            )

    @staticmethod
    def get_neutral(h, mode):
        s = 0.05
        v = 0.95 if mode == "light" else 0.20
        rgb = colorsys.hsv_to_rgb(h, s, v)
        return "#{:02x}{:02x}{:02x}".format(
            int(rgb[0] * 255), int(rgb[1] * 255), int(rgb[2] * 255)
        )

    def get_classic_suggestions(self, hex_color: str):
        try:
            if not re.fullmatch(r"#[0-9a-fA-F]{6}", hex_color):
                raise ValueError("Invalid HEX format.")

            hex_color = hex_color.lstrip("#").lower()
            r, g, b = [int(hex_color[i : i + 2], 16) / 255.0 for i in (0, 2, 4)]
            h, s, v = colorsys.rgb_to_hsv(r, g, b)

            angles = {
                "Complementary": (h + 0.5) % 1.0,
                "Similar_1": (h + 30 / 360) % 1.0,
                "Similar_2": (h - 30 / 360) % 1.0,
                "Triad_1": (h + 120 / 360) % 1.0,
                "Triad_2": (h + 240 / 360) % 1.0,
            }

            suggestions_dict = {}
            for label, new_h in angles.items():
                if v > 0.6:
                    new_v = max(0, v - 0.20)
                    new_s = min(1.0, s + 0.10)
                else:
                    new_v = min(1.0, v + 0.20)
                    new_s = max(0, s - 0.10)
                new_rgb = colorsys.hsv_to_rgb(new_h, new_s, new_v)
                new_hex = "#{:02x}{:02x}{:02x}".format(
                    int(new_rgb[0] * 255), int(new_rgb[1] * 255), int(new_rgb[2] * 255)
                )
                suggestions_dict[label] = new_hex

            suggestions_dict["Neutral_light"] = self.get_neutral(h, "light")
            suggestions_dict["Neutral_dark"] = self.get_neutral(h, "dark")

            suggestions = [[c] for c in suggestions_dict.values()]

            return {"suggestions": suggestions}

        except Exception:
            raise AppError("Invalid HEX color format.", status_code=400)

    def get_wada_palette(self, input_hex: str):
        if not self.wada_data:
            raise ColorNotFoundError("The collection is empty.")

        try:
            closest_wada = min(
                self.wada_data,
                key=lambda x: self.calculate_delta_e(input_hex, x["hex"]),
            )
        except ValueError:
            raise ColorNotFoundError()

        closest_hex = closest_wada["hex"].lower()

        palettes_hex = []
        for combo_id in closest_wada["combinations"]:
            all_colors = self.combination_map.get(combo_id, [])
            palette = [h for h in all_colors if h.lower() != closest_hex]
            if palette:
                palettes_hex.append(palette)

        return {
            "original_color": input_hex,
            "wada_name": closest_wada["name"],
            "wada_hex": closest_wada["hex"],
            "combinations": palettes_hex,
        }
