class AppError(Exception):
    def __init__(self, message: str, status_code: int = 500):
        self.message = message
        self.status_code = status_code


class ImageProcessingError(AppError):
    def __init__(self, message="Error during image processing."):
        super().__init__(message, status_code=422)


class AIModelError(AppError):
    def __init__(self, message="Classification model failed."):
        super().__init__(message, status_code=503)


class ColorNotFoundError(AppError):
    def __init__(self, message="Color not found in the collection."):
        super().__init__(message, status_code=404)
