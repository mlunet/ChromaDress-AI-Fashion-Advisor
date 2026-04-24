import pytest
import json
from unittest.mock import patch, mock_open
from app.services.color_service import ColorService
from app.services.exceptions import AppError, ColorNotFoundError


@pytest.fixture
def color_service(mock_wada_data):
    with patch("app.services.color_service.open", mock_open(read_data="[]")):
        with patch("app.services.color_service.json.load", return_value=mock_wada_data):
            return ColorService("fake_path.json")


@pytest.fixture
def mock_wada_data():
    return [
        {"name": "Pure Red", "hex": "#ff0000", "combinations": [1]},
        {"name": "Orange", "hex": "#ffa500", "combinations": [1]},
        {"name": "Pure Blue", "hex": "#0000ff", "combinations": [2]},
        {"name": "Cyan", "hex": "#00ffff", "combinations": [2]},
    ]


def test_init_populates_combination_map(color_service):

    assert "#ff0000" in color_service.combination_map[1]
    assert "#ffa500" in color_service.combination_map[1]
    assert len(color_service.combination_map[1]) == 2


@patch("app.services.color_service.open", new_callable=mock_open)
@patch("app.services.color_service.json.load")
def test_init_raises_app_error_on_corrupted_json(mock_json, mock_file):

    mock_json.side_effect = json.JSONDecodeError("Expecting value", "", 0)

    with pytest.raises(AppError) as exc:
        ColorService("corrupted_json")

    assert "File colors.json is corrupted" in str(exc.value)
    assert exc.value.status_code == 500


@patch("app.services.color_service.open", new_callable=mock_open)
@patch("app.services.color_service.json.load")
def test_init_raises_app_error_on_file_not_found(mock_json, mock_file):

    mock_file.side_effect = FileNotFoundError()

    with pytest.raises(AppError) as exc:
        ColorService("fake_path.json")

    assert "Collection file fake_path.json not found." in str(exc.value)
    assert exc.value.status_code == 500


@pytest.mark.parametrize(
    "hue, expected_name",
    [
        (0 % 360, "Red"),
        (20 % 360, "Orange"),
        (350 % 360, "Red"),
        (120 % 360, "Green"),
        (-20 % 360, "Pink"),
    ],
)
def test_get_macro_color_name(hue, expected_name):
    assert ColorService.get_macro_color_name(hue) == expected_name


def test_calculate_distance_raises_error_invalid_hex(color_service):
    with pytest.raises(AppError) as exc:
        color_service.calculate_distance("h1", "h2")
    assert "Error during distance calculation: invalid HEX colors." in str(exc.value)
    assert exc.value.status_code == 400


def test_get_classic_suggestions_populates_suggestions_list(color_service):
    hex_input = "#ff0000"
    result = color_service.get_classic_suggestions(hex_input)

    assert "suggestions" in result
    assert len(result["suggestions"]) > 0
    assert "#00ffff" in result["suggestions"]


@pytest.mark.parametrize("invalid_hex", ["not_hex", "#ggee22", "", "12345", "#1234567"])
def test_get_classic_suggestions_raises_exception(color_service, invalid_hex):
    with pytest.raises(AppError) as exc:
        color_service.get_classic_suggestions(invalid_hex)

    assert str(exc.value) == "Invalid HEX color format."
    assert exc.value.status_code == 400


@pytest.mark.parametrize("wada_input", [[], None])
def test_wada_palette_raises_error_when_empty(color_service, wada_input):
    color_service.wada_data = wada_input

    with pytest.raises(ColorNotFoundError):
        color_service.get_wada_palette("#ff0000")


def test_wada_palette_closest_match(color_service):
    result = color_service.get_wada_palette("#800000")

    assert result["wada_name"] == "Pure Red"
    assert result["wada_hex"] == "#ff0000"
    assert ["#ffa500"] in result["combinations"]


def test_wada_palette_filters_out_current_color(color_service):
    result = color_service.get_wada_palette("#0000ff")

    for palette in result["combinations"]:
        assert "#0000ff" not in [c.lower() for c in palette]
    assert ["#00ffff"] in result["combinations"]
