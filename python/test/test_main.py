import pytest
from fastapi.testclient import TestClient
from unittest.mock import patch
from pathlib import Path
from app.main import app
from app.services.exceptions import AppError


@pytest.fixture
def client():
    return TestClient(app)


@pytest.fixture
def mock_color_service():
    with patch("app.main.color_service") as mock:
        yield mock


@pytest.fixture
def mock_image_service():
    with patch("app.main.image_service") as mock:
        yield mock


def test_get_wada_palette_api_successful(client, mock_color_service):
    mock_color_service.get_wada_palette.return_value = {
        "original_color": "#ff0000",
        "wada_name": "Pure Red",
        "wada_hex": "#ff0000",
        "combinations": [["#ffa500"]],
    }
    response = client.get("/palette?hex=%23ff0000")

    assert response.status_code == 200
    assert response.json()["wada_name"] == "Pure Red"


def test_get_classic_palette_api_successful(client, mock_color_service):
    mock_color_service.get_classic_suggestions.return_value = {
        "suggestions": ["#00ffff", "#ffffff"]
    }
    response = client.get("/classic-palette?hex=%23ff0000")

    assert response.status_code == 200
    assert "#00ffff" in response.json()["suggestions"]


def test_get_classic_palette_api_app_error_handler(client, mock_color_service):
    mock_color_service.get_classic_suggestions.side_effect = AppError(
        "Invalid HEX color format.", status_code=400
    )
    response = client.get("/classic-palette?hex=invalid")

    assert response.status_code == 400
    assert response.json()["status"] == "error"
    assert response.json()["type"] == "AppError"
    assert response.json()["message"] == "Invalid HEX color format."


def test_analyze_image_api_successful(client, mock_image_service):
    mock_image_service.analyze_category.return_value = "T-shirt"
    mock_image_service.extract_dominant_color.return_value = "#ff0000"

    file_content = b"fake image"
    file = {"file": ("test.png", file_content, "image/png")}
    response = client.post("/analyze", files=file)

    assert response.status_code == 200
    assert response.json() == {"color": "#ff0000", "category": "T-shirt"}


def test_analyze_image_api_raises_app_error(client):
    file = {"file": ("test.png", b"", "image/png")}
    response = client.post("/analyze", files=file)

    assert response.status_code == 400
    assert response.json()["message"] == "Empty file."
