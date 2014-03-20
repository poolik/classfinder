package com.poolik.classfinder.io;

public class Predicates {
  private Predicates() {}

  public static <T> Predicate<T> alwaysTrue() {
    return new Predicate<T>() {
      @Override
      public boolean apply(Object o) {
        return true;
      }
    };
  }
}