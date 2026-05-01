package com.app.ChromaDress.core.exception;

import lombok.Getter;

@Getter
public class PythonAnalysisException extends AppException {

  private final int statusCode;
  private final String type;

  public PythonAnalysisException(String message, String type, int statusCode) {
    super(message);
    this.type = type;
    this.statusCode = statusCode;
  }
}
