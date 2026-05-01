package com.app.ChromaDress.core.exception;

import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<ErrorResponseDTO> handleInvalidCredentials(InvalidCredentialsException e) {
    ErrorResponseDTO error = new ErrorResponseDTO("error", "InvalidCredentialException",
        e.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorResponseDTO> handleBadCredentials(BadCredentialsException e) {
    ErrorResponseDTO error = new ErrorResponseDTO("error", "BadCredentialsException",
        e.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
  }

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<ErrorResponseDTO> handleUserAlreadyExists(UserAlreadyExistsException e) {
    ErrorResponseDTO error = new ErrorResponseDTO("error", "UserAlreadyExistsException",
        e.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponseDTO> handleResourceNotFound(ResourceNotFoundException e) {
    ErrorResponseDTO error = new ErrorResponseDTO("error", "ResourceNotFoundException",
        e.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponseDTO> handleMethodArgumentNotValid(
      MethodArgumentNotValidException e) {
    String details = e.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .collect(Collectors.joining(", "));
    ErrorResponseDTO error = new ErrorResponseDTO("error", "MethodArgumentNotValidException",
        details);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(HandlerMethodValidationException.class)
  public ResponseEntity<ErrorResponseDTO> handleHandlerMethodValidation(
      HandlerMethodValidationException e) {
    String details = e.getAllErrors().stream().map(MessageSourceResolvable::getDefaultMessage)
        .collect(Collectors.joining(", "));
    ErrorResponseDTO error = new ErrorResponseDTO("error", "HandlerMethodValidationException",
        details);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(PythonAnalysisException.class)
  public ResponseEntity<ErrorResponseDTO> handlePythonAnalysisException(PythonAnalysisException e) {
    ErrorResponseDTO error = new ErrorResponseDTO("error", e.getType(), e.getMessage());
    return ResponseEntity.status(e.getStatusCode()).body(error);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDTO> handleGeneralException(Exception e) {
    log.error("Unchecked error: ", e);
    ErrorResponseDTO error = new ErrorResponseDTO("error", "InternalServerError",
        "An unexpected error occurred");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }
}
