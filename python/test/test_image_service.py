import pytest
import numpy as np
import io
from unittest.mock import patch, MagicMock
from PIL import Image
from app.services.exceptions import AIModelError
from app.services.image_service import ImageService


@pytest.fixture
def image_service():
    with patch("app.services.image_service.pipeline") as mock_pipeline:
        mock_classifier = MagicMock()
        mock_pipeline.return_value = mock_classifier

        service = ImageService()
        service.mock_classifier = mock_classifier
        return service


@pytest.fixture
def mock_image():
    img = Image.new("RGBA", (100, 100), color=(255, 0, 0, 255))
    buffer = io.BytesIO()

    img.save(buffer, format="PNG")
    return buffer.getvalue()


def test_analyze_category_success(image_service, mock_image):
    image_service.mock_classifier.return_value = [{"label": "dress", "score": 0.98}]

    category = image_service.analyze_category(mock_image)

    assert category == "Dress"
    image_service.mock_classifier.assert_called_once()


def test_analyze_category_rases_AI_error(image_service):
    image_service.mock_classifier.side_effect = Exception("GPU OOM")

    with pytest.raises(AIModelError):
        image_service.analyze_category(b"invalid_bytes")


@patch("app.services.image_service.remove")
def test_extract_dominant_color_success(mock_remove, image_service, mock_image):
    mock_remove.return_value = mock_image

    color = image_service.extract_dominant_color(mock_image)

    assert color.startswith("#")
    assert len(color) == 7
    assert color.lower().startswith("#f")


def test_extract_dominant_color_transparent_image(image_service):
    img = Image.new("RGBA", (10, 10), color=(0, 0, 0, 0))
    buffer = io.BytesIO()
    img.save(buffer, format="PNG")

    with patch("app.services.image_service.remove", return_value=buffer.getvalue()):
        color = image_service.extract_dominant_color(buffer.getvalue())
        assert color == "#000000"
