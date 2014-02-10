package com.poolik.classfinder;

public class ClassFinderException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public ClassFinderException() {
    super();
  }

  public ClassFinderException(Throwable exception) {
    super(exception);
  }

  public ClassFinderException(String message) {
    super(message);
  }

  public ClassFinderException(String message, Throwable exception) {
    super(message, exception);
  }
}