package com.poolik.classfinder.otherTestClasses;

import com.poolik.classfinder.ClassFinderException;
import com.sun.istack.internal.NotNull;

@Deprecated
public class ConcreteClass extends AbstractClass {

  @NotNull
  private String test = "test";
  public int anotherValue = 0;

  private void test() throws ClassFinderException {}
  public static String another() throws ClassFinderException {
    return "";
  }
}
