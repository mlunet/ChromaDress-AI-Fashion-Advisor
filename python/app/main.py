from fastapi import FastAPI, UploadFile, File, Request
from fastapi.responses import JSONResponse
from app.services.color_service import ColorService
from app.services.exceptions import AppError
from app.services.image_service import ImageService
import logging
import uvicorn

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI()

color_service = ColorService()
image_service = ImageService()


@app.exception_handler(AppError)
async def app_error_handler(request: Request, exc: AppError):
    return JSONResponse(
        status_code=exc.status_code,
        content={
            "status": "error",
            "type": exc.__class__.__name__,
            "message": exc.message,
        },
    )


@app.exception_handler(Exception)
async def general_exception_handler(request: Request, exc: Exception):
    logger.error(f"UNEXPECTED ERROR: {str(exc)}", exc_info=True)
    return JSONResponse(
        status_code=500,
        content={
            "status": "error",
            "type": "InternalServerError",
            "message": "An unexpected error occurred.",
        },
    )


@app.get("/palette")
async def get_wada_palette(hex: str):
    return color_service.get_wada_palette(hex)


@app.get("/classic-palette")
async def get_classic_palette(hex: str):
    return color_service.get_classic_suggestions(hex)


@app.post("/analyze")
async def analyze_image(file: UploadFile = File(...)):

    contents = await file.read()
    if not contents:
        raise AppError("Empty file.", 400)

    category = image_service.analyze_category(contents)
    color = image_service.extract_dominant_color(contents)

    return {"color": color, "category": category}


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=5000)
