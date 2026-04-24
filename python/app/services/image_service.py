import cv2
import numpy as np
import io
from PIL import Image
from rembg import remove
from app.services.exceptions import ImageProcessingError, AIModelError
from sklearn.cluster import KMeans
from transformers import pipeline


class ImageService:
    def __init__(self):
        self.categories = [
            "t-shirt",
            "trousers",
            "jeans",
            "jacket",
            "dress",
            "skirt",
            "sneakers",
            "shirt",
            "sweater",
            "coat",
        ]
        self.classifier = pipeline(
            "zero-shot-image-classification", model="openai/clip-vit-base-patch32"
        )

    def analyze_category(self, contents):
        try:
            pil_img = Image.open(io.BytesIO(contents)).convert("RGB")
            predictions = self.classifier(pil_img, candidate_labels=self.categories)
            return predictions[0]["label"].capitalize()
        except Exception:
            raise AIModelError()

    def extract_dominant_color(self, contents):
        try:
            no_bg_bytes = remove(contents)
            nparr = np.frombuffer(no_bg_bytes, np.uint8)
            image_bgra = cv2.imdecode(nparr, cv2.IMREAD_UNCHANGED)

            if image_bgra is None:
                raise ValueError("Image cannot be decoded.")

            image_rgba = cv2.cvtColor(image_bgra, cv2.COLOR_BGRA2RGBA)
            resize_rgba = cv2.resize(
                image_rgba, (200, 200), interpolation=cv2.INTER_AREA
            )

            pixels = resize_rgba.reshape(-1, 4)
            valid_pixels = pixels[pixels[:, 3] > 200][:, :3]

            if len(valid_pixels) < 100:
                nparr_orig = np.frombuffer(contents, np.uint8)
                image_orig = cv2.imdecode(nparr_orig, cv2.IMREAD_COLOR)
                image_rgb = cv2.cvtColor(image_orig, cv2.COLOR_BGR2RGB)

                h, w, _ = image_rgb.shape
                start_h, start_w = h // 4, w // 4
                end_h, end_w = 3 * h // 4, 3 * w // 4
                center_crop = image_rgb[start_h:end_h, start_w:end_w]
                valid_pixels = center_crop.reshape(-1, 3)

            unique_colors = np.unique(valid_pixels, axis=0)
            possible_clusters = min(3, len(unique_colors))

            kmeans = KMeans(n_clusters=possible_clusters, random_state=42, n_init=10)
            kmeans.fit(valid_pixels)

            def get_saturation(rgb):
                r, g, b = rgb / 255.0
                mx, mn = max(r, g, b), min(r, g, b)
                return 0 if mx == mn else (mx - mn) / (1 - abs(2 * ((mx + mn) / 2) - 1))

            dominant = max(kmeans.cluster_centers_, key=get_saturation)
            print(
                f"DEBUG: Extracted Dominant: {"#{:02x}{:02x}{:02x}".format(
                int(dominant[0]), int(dominant[1]), int(dominant[2])
            )}"
            )

            return "#{:02x}{:02x}{:02x}".format(
                int(dominant[0]), int(dominant[1]), int(dominant[2])
            )

        except Exception as e:
            raise ImageProcessingError(f"{str(e)}")
