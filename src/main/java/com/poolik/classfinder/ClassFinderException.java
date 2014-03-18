package com.poolik.classfinder;

public class ClassFinderException extends RuntimeException {

  public ClassFinderException(String message) {
    super(message);
  }

  public ClassFinderException(String message, Throwable exception) {
    super(message, exception);
  }
}